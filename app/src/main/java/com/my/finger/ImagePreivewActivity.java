package com.my.finger;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.my.finger.utils.CommonUtil;
import com.my.finger.utils.DataBaseUtil;
import com.my.finger.utils.TouchImageView;
import java.io.File;
import java.io.FileOutputStream;

public class ImagePreivewActivity extends AppCompatActivity  {
    private final String TAG = "KDN_TAG";
    private String mFileName;
    private DataBaseUtil mDB;


    private TouchImageView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preivew);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mFileName = b.getString("photo");
            Log.d(TAG, "photo: " + mFileName);

            File file = new File(mFileName);
            if (file.exists()) {
                Log.d(TAG, "File is exists....");
                Log.d(TAG, "file size : " + file.length());
            }else {
                Log.d(TAG, "File isn't exists....");
            }

            Bitmap bitmap = BitmapFactory.decodeFile(mFileName);

            if (bitmap == null)
            {
                Log.d(TAG, "bitmap is null");
            }else {
                Log.d(TAG, "bitmap is not null");
            }
            //Drawable drawable = new BitmapDrawable(getResources(), bitmap);

            view = (TouchImageView)findViewById(R.id.previewImage);
            view.setImageBitmap(bitmap);
            //matrix.setTranslate(1f, 1f);
            //view.setImageMatrix(matrix);
            //img.setBitmap(bitmap);
            //img.setImageDrawable(drawable);
            Log.d(TAG, "here is now...");
        }
        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnSave:
                        imageSaveProcess();
                        break;
                }
            }
        };

        // 버튼 이벤트 리스너
        Button btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(onClickListener);

        mDB = new DataBaseUtil(this);
    }

    /**
     * 이미지를 저장한다.
     * 사용자가 입력한 이미지명이 있으면 그 이름으로 저장한다.
     */
    private void imageSaveProcess()
    {
        FileOutputStream outStream = null;
        SQLiteDatabase db = mDB.getWritableDatabase();
        try {

            File path = new File(this.getFilesDir().getAbsolutePath() + "/kdnapp");
            if (!path.exists()) {
                path.mkdirs();
            }

            EditText editText = (EditText)findViewById(R.id.txtFileName);
            String fileName;
            String saveName;
            if (editText.getText().toString().length() != 0)
            {
                saveName = editText.getText().toString();
                fileName = String.format("%s.jpg", saveName);

                // file rename
                File file = new File(mFileName);
                File fileToMove = new File(path+"/"+fileName);
                file.renameTo(fileToMove);

                // 데이터베이스에 이미지 정보를 등록해야 된다.
                ContentValues values = new ContentValues();
                values.put("id", Long.parseLong(CommonUtil.getCurrentDateFormat()));
                values.put("filename", path + "/" + fileName);
                values.put("sts", "N");
                long id = db.insert("tb_files", null, values);
                Log.d(TAG, "insert id : " + id);

                Log.d(TAG, "imageSaveProcess - wrote to "
                        + fileToMove.getAbsolutePath());
            }
            Toast.makeText(getBaseContext(), "파일저장을 완료하였습니다",
                    Toast.LENGTH_SHORT).show();

            // Activity move
            Intent intent = new Intent(ImagePreivewActivity.this, CameraActivity.class);
            Bundle b = new Bundle();
            b.putString("checked", "Y");
            intent.putExtras(b);
            startActivity(intent);
        }finally {
            db.close();
        }
    }

    /**
     * 핸드폰 Back 버튼 제어
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        // your code.
        Intent intent = new Intent(ImagePreivewActivity.this, CameraActivity.class);
        Bundle b = new Bundle();
        b.putString("checked", "Y");
        intent.putExtras(b);
        startActivity(intent);
    }
}
