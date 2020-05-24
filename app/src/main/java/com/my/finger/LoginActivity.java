package com.my.finger;

import android.content.Intent;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                        loginProcess();
                        break;
                }
            }
        };
        // 각 버튼에 대한 이벤트 리스터를 지정한다.
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(onClickListener);

    }

    /**
     * 아이디/패스워드 입력 체크 및 로그인 프로세스를 수행한다.
     */
    private void loginProcess()
    {
        final EditText idEdit = findViewById(R.id.txtId);
        final EditText passEdit = findViewById(R.id.txtPass);

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

        // 아이디/패스워드 입력하였으므로 로그인 절차를 수행한다.
        new Thread() {
            public void run() {
                // All your networking logic should be here
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
                    sbParams.append("empid").append("=").append(idEdit.getText().toString());
                    sbParams.append("&").append("password").append("=").append(passEdit.getText().toString());
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
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }.start();
    }
}