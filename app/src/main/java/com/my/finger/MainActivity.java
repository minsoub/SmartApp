package com.my.finger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.my.finger.data.FileDataItem;
import com.my.finger.utils.DataBaseUtil;
import com.my.finger.utils.SessionUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "KDN_TAG";
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = (ImageView) findViewById(R.id.logo_image);
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.mipmap.logo, options);

        // 이미지 사이즈 조절
        options.inSampleSize = 2; // setSimpleSize(options, 237, 44);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.logo, options);

        // 재설정된 이미지 출력
        imageView.setImageBitmap(bitmap);

        ImageView.OnClickListener onClickListener = new ImageView.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switch (view.getId())
                {
                    // 사진 촬영하기
                    case R.id.btnCamera:
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                        startActivity(intent);
                        break;
                    // 사진 전송하기
                    case R.id.btnSend:
                        Log.d(TAG, "btnSend click...");
                        sendProcess();
                        break;
                        // 사진 공유하기
                    case R.id.btnShare:
                        shareProcess();
                        break;
                    case R.id.bottom_title:
                        deleteProcess();
                        break;
                }
            }
        };

        // 각 버튼에 대한 이벤트 리스터를 지정한다.
        ImageView btnCamera = (ImageView)findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(onClickListener);

        ImageView btnSend = (ImageView)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(onClickListener);

        ImageView btnShare = (ImageView)findViewById(R.id.btnShare);
        btnShare.setOnClickListener(onClickListener);

        TextView btnClear = (TextView)findViewById(R.id.bottom_title);
        btnClear.setOnClickListener(onClickListener);

        mContext = this;
    }

    /**
     * 사진 전송 페이지 이동
     * 이동전에 네트워크 전송이 가능한지 먼저 체크하고 가능하지 않으면 알람 메시지 출력한다.
     */
    private void sendProcess()
    {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected)
        {
            // 로그인 여부
            if (SessionUtil.empid == null)
            {
                // 로그인 페이지 이동
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }else {
                Intent intent = new Intent(MainActivity.this, SendActivity.class);
                startActivity(intent);
            }
        }else {
            Toast.makeText(getBaseContext(), "네트워크가 연결되지 않았습니다!!!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 사진 공유 페이지 이동
     * 이동전에 네트워크 전송이 가능한지 먼저 체크하고 가능하지 않으면 알람 메시지 출력한다.
     */
    private void shareProcess()
    {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected)
        {
            // 로그인 여부
            if (SessionUtil.empid == null)
            {
                // 로그인 페이지 이동
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }else {
                Intent intent = new Intent(MainActivity.this, ShareActivity.class);
                startActivity(intent);
            }
        }else {
            Toast.makeText(getBaseContext(), "네트워크가 연결되지 않았습니다!!!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 삭제 여부를 확인한다.
     */
    private void deleteProcess()
    {
        new AlertDialog.Builder(this)
                .setTitle("파일 Clear")
                .setMessage("핸드폰에 저장된 임시파일을 삭제 하시겠습니까?")
                .setIcon(R.mipmap.btn_del01)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DataBaseUtil mDB;
                        mDB = new DataBaseUtil(mContext);
                        SQLiteDatabase db = mDB.getWritableDatabase();
                        String sql = "select id, filename, sts from tb_files where sts='N' order by id desc";
                        Cursor cursor = db.rawQuery(sql, null);

                        cursor.moveToFirst();
                        while(cursor.isAfterLast() == false) {

                            String filename = cursor.getString(1);
                            long id = cursor.getLong(0);

                            File file = new File(filename);
                            if (file.exists()) {
                                file.delete();
                            }
                            cursor.moveToNext();
                        }
                        cursor.close();
                        // 데이터베이스 내용 모두 삭제
                        db.delete("tb_files", null, null);
                        db.close();
                        mDB.close();
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 취소시 처리 로직
                    }})
                .show();
    }

    // 이미지 Resize 함수
    private int setSimpleSize(BitmapFactory.Options options, int requestWidth, int requestHeight){
        // 이미지 사이즈를 체크할 원본 이미지 가로/세로 사이즈를 임시 변수에 대입.
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        // 원본 이미지 비율인 1로 초기화
        int size = 1;

        // 해상도가 깨지지 않을만한 요구되는 사이즈까지 2의 배수의 값으로 원본 이미지를 나눈다.
        while(requestWidth < originalWidth || requestHeight < originalHeight){
            originalWidth = originalWidth / 2;
            originalHeight = originalHeight / 2;

            size = size * 2;
        }
        return size;
    }

}
