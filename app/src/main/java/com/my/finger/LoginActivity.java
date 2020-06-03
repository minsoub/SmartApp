package com.my.finger;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.my.finger.utils.Constant;
import com.my.finger.utils.DataBaseUtil;
import com.my.finger.utils.SessionUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Login Activity Class
 */
public class LoginActivity extends AppCompatActivity {
    private final String TAG = "KDN_TAG";
    public boolean isLogin = false;
    private String id;
    private String pass;
    private DataBaseUtil mDB;
    private EditText idEdit;
    private EditText passEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        idEdit = findViewById(R.id.txtId);
        passEdit = findViewById(R.id.txtPass);

        Button.OnClickListener onClickListener = new ImageView.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switch (view.getId())
                {
                    // 사진 촬영하기
                    case R.id.btnLogin:
                        // 아이디/패스워드 입력 여부 확인
                        if (idEdit.getText() == null || "".equals(idEdit.getText().toString()))
                        {
                            Toast.makeText(getBaseContext(), "아이디를 입력하세요!!!",
                                    Toast.LENGTH_SHORT).show();
                            idEdit.forceLayout();
                            return;
                        }
                        if (passEdit.getText() == null || "".equals(passEdit.getText().toString()))
                        {
                            Toast.makeText(getBaseContext(), "비밀번호를 입력하세요!!!",
                                    Toast.LENGTH_SHORT).show();
                            passEdit.forceLayout();
                            return;
                        }
                        id = idEdit.getText().toString();
                        pass = passEdit.getText().toString();

                        onLoginProcess();
                        //new LoginProcess().execute(idEdit.getText().toString(), passEdit.getText().toString());
                        break;
                }
            }
        };
        // 각 버튼에 대한 이벤트 리스터를 지정한다.
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(onClickListener);

        mDB = new DataBaseUtil(this);
        SQLiteDatabase db = mDB.getWritableDatabase();
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS tb_users (id text primary key, login_dt text)"
        );

        String sql = "select id from tb_users";
        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();
        if (cursor.isAfterLast() == false)
        {
            String id = cursor.getString(0);
            if (id != null)
            {
                idEdit.setText(id);  // 마지막 로그인한 아이디
            }
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
    }

    /**
     * 아이디/패스워드 입력 체크 및 로그인 프로세스를 수행한다.
     */
    private void onLoginProcess()
    {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... Void) {
                // 전달된 URL 사용 작업 return total;
                try{
                    String url = Constant.LOGIN_URL;
                    URL urlObj = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);

                    conn.connect();
                    StringBuilder sbParams = new StringBuilder();
                    sbParams.append("empid").append("=").append(id);
                    sbParams.append("&").append("password").append("=").append(pass);
                    String paramsString = sbParams.toString();

                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(paramsString);
                    wr.flush();
                    wr.close();

                    // received...
                    InputStream responseBody = new BufferedInputStream(conn.getInputStream());
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");

                    JsonReader jsonReader = new JsonReader(responseBodyReader);

                    jsonReader.beginObject(); // Start processing the JSON object

                    while (jsonReader.hasNext()) { // Loop through all keys
                        String key = jsonReader.nextName(); // Fetch the next key
                        Log.d(TAG, "key : " + key);
                        if (key.equals("code")) { // Check if desired key
                            // Fetch the value as a String
                            String value = jsonReader.nextString();
                            if (value.equals("OK")) {
                                isLogin = true;
                            } else {
                                isLogin = false;
                                break;
                            }
                        }else if(key.equals("empid")) {
                            String value = jsonReader.nextString();
                            SessionUtil.empid = value;
                        }else if(key.equals("deptCd")) {
                            String value = jsonReader.nextString();
                            SessionUtil.deptCd = value;
                        }else if(key.equals("name")) {
                            String value = jsonReader.nextString();
                            SessionUtil.name = value;
                        }else if(key.equals("deptNm")) {
                            String value = jsonReader.nextString();
                            SessionUtil.deptNm = value;
                        } else {
                            jsonReader.skipValue(); // Skip values of other keys
                        }
                    }
                    jsonReader.close();
                    conn.disconnect();
                    Log.d(TAG, "result from server: " + jsonReader.toString());

                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (isLogin == true) {
                                // Storage save
                                SQLiteDatabase db = mDB.getWritableDatabase();
                                int found = 0;
                                String sql = "select id from tb_users";
                                Cursor cursor = db.rawQuery(sql, null);

                                cursor.moveToFirst();
                                if (cursor.isAfterLast() == false)
                                {
                                    String id = cursor.getString(0);
                                    if (id != null)
                                    {
                                        found = 1;
                                    }
                                    cursor.moveToNext();
                                }
                                cursor.close();

                                if (found == 1) {
                                    ContentValues values = new ContentValues();
                                    values.put("id", SessionUtil.empid);
                                    long id = db.update("tb_users", values, null,null);
                                }else {
                                    ContentValues values = new ContentValues();
                                    values.put("id", SessionUtil.empid);
                                    long id = db.insert("tb_users", null, values);
                                }
                                db.close();
                                mDB.close();
                                Toast.makeText(getBaseContext(), SessionUtil.name + "[" + SessionUtil.empid + "]님 로그인", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                // ?? nothing
                                Toast.makeText(getBaseContext(), "로그인하는데 에러가 발생하였습니다!!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "로그인하는데 에러가 발생하였습니다["+e.toString()+"]", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... progress) {
                // 파일 다운로드 퍼센티지 표시 작업
            }

            @Override
            protected void onPostExecute(Void result) {
                // doInBackground 에서 받아온 total 값 사용 장소
            }
        }.execute();
    }
}
