package com.my.finger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.my.finger.data.FileDataItem;
import com.my.finger.utils.CommonUtil;
import com.my.finger.utils.Constant;
import com.my.finger.utils.DataBaseUtil;
import com.my.finger.utils.SessionUtil;
import com.my.finger.utils.TouchImageView;
import com.my.finger.utils.UploadUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageDetailActivity extends AppCompatActivity {
    private DataBaseUtil mDB;
    private String mFileName;
    private String mKey;
    private TouchImageView view;
    private ProgressDialog dialog = null;
    private final String TAG = "KDN_TAG";
    private int serverResponseCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        mDB = new DataBaseUtil(this);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mKey = b.getString("imageKey");
            SQLiteDatabase db = mDB.getWritableDatabase();
            String sql = "select id, filename, sts from tb_files where id='"+mKey+"'";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();

            while(cursor.isAfterLast() == false)
            {
                mFileName = cursor.getString(1);
                cursor.moveToNext();
            }
            cursor.close();
            db.close();

            // Image 출력
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Bitmap bitmap = BitmapFactory.decodeFile(mFileName);
            Bitmap newbitMap = Bitmap.createScaledBitmap(bitmap, size.x, size.y, true);

            view = findViewById(R.id.previewImage);
            view.setImageBitmap(newbitMap);

            // Text 출력
            int pos = mFileName.lastIndexOf(".");
            String _fileName = mFileName.substring(mFileName.lastIndexOf("/")+1, pos);

            TextView txt = findViewById(R.id.send_title);
            txt.setText(_fileName);
        }

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnDelete:
                        imageDeleteProcess();
                        break;
                    case R.id.btnSend:
                        imageSendProcess();
                        break;
                }
            }
        };

        // 버튼 이벤트 리스너
        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(onClickListener);

        ImageView btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(onClickListener);
    }

    /**
     * 이미지를 삭제한다. 파일에서 삭제하고 데이터베이스에서도 삭제한다.
     *
     */
    private void imageDeleteProcess()
    {
        new AlertDialog.Builder(this)
                .setTitle("파일삭제")
                .setMessage("파일삭제 하시겠습니까?")
                .setIcon(R.mipmap.btn_del01)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 확인시 처리 로직
                        SQLiteDatabase db = mDB.getWritableDatabase();
                        int result = db.delete("tb_files", "id=?", new String[]{mKey});
                        db.close();

                        // 파일 삭제
                        File file = new File(mFileName);
                        if (file.exists())
                        {
                            file.delete();
                        }

                        Toast.makeText(getBaseContext(), "삭제를 완료하였습니다!!!",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ImageDetailActivity.this, SendActivity.class);
                        startActivity(intent);
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 취소시 처리 로직
                    }})
                .show();
    }
    /**
     * 이미지를 전송한다.
     *
     */
    private void imageSendProcess()
    {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected)
        {
            Log.d(TAG, "session empid : " + SessionUtil.empid);
            // 로그인 체크
            if (SessionUtil.empid == null)
            {
                // 로그인 페이지 이동
                if (mDB != null) mDB.close();
                // your code.
                Intent intent = new Intent(ImageDetailActivity.this, LoginActivity.class);
                startActivity(intent);
            }else {
                // 이미지를 전송한다.
                dialog = ProgressDialog.show(ImageDetailActivity.this, "", "Uploading file...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(TAG, "uploading started.....");
                            }
                        });
                        uploadFile(mFileName);
                    }
                }).start();
            }
        }else {
            Toast.makeText(getBaseContext(), "네트워크가 연결되지 않았습니다!!!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public int uploadFile(String sourceFileUri)
    {
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            dialog.dismiss();
            Log.e(TAG, "Source File not exist :"+sourceFileUri);
            runOnUiThread(new Runnable() {
                public void run() {
                    Log.e(TAG, "Source File not exist :");
                }
            });
            return 0;
        }
        else {
            UploadUtil upload = new UploadUtil(SessionUtil.empid, SessionUtil.deptCd);
            Constant.ReturnCode result = upload.fileUpload(sourceFileUri);
            if (result == Constant.ReturnCode.http201)
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "파일 업로드 완료");
                        Toast.makeText(ImageDetailActivity.this, "업로드를 완료하였습니다!!!",
                                Toast.LENGTH_SHORT).show();
                        SQLiteDatabase db = mDB.getWritableDatabase();
                        int result = db.delete("tb_files", "id=?", new String[]{mKey});
                        db.close();

                        // 파일 삭제
                        File file = new File(mFileName);
                        if (file.exists())
                        {
                            file.delete();
                        }
                        // Activity Move
                        onBackPressed();

                    }
                });
            }else {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "Got Exception : see logcat ");
                        Toast.makeText(ImageDetailActivity.this, "업로드에 실패하였습니다. 관리자에게 문의하시기 바랍니다!!! ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        dialog.dismiss();
        return serverResponseCode;
    }

    /**
     * 핸드폰 Back 버튼 제어
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed called..");

        if (mDB != null) mDB.close();
        // your code.
        Intent intent = new Intent(ImageDetailActivity.this, SendActivity.class);
        startActivity(intent);
    }
}
