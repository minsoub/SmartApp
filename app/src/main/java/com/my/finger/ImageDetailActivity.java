package com.my.finger;

import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.view.View.OnTouchListener;

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
import java.util.ArrayList;

public class ImageDetailActivity extends Activity  implements View.OnTouchListener{
    private DataBaseUtil mDB;
    private String mFileName;
    private String mKey;
    private TouchImageView view;
    private ProgressDialog dialog = null;
    private final String TAG = "KDN_TAG";
    private int serverResponseCode = 0;
    private ArrayList filename;
    private ArrayList filekey;
    private TextView mtxtView;
    private ViewFlipper viewFlipper;
    private float xAtDown, xAtUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        mDB = new DataBaseUtil(this);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());

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
        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        viewFlipper.setOnTouchListener(this);

        // 버튼 이벤트 리스너
        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(onClickListener);

        ImageView btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(onClickListener);

        setImageLayout();
    }

    public boolean onTouch(View v, MotionEvent event)
    {
        Log.d(TAG, "viewFlipper event.getAction : " + event.getAction());
        if (v != viewFlipper) return false;

        Log.d(TAG, "onTouchEvent event.getAction : " + event.getAction());
        Log.d(TAG, "onTouchEvent viewFlipper.getDisplayedChild() : " + viewFlipper.getDisplayedChild() );
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xAtDown = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                float finalX = event.getX();
                if (xAtDown > finalX) {   // 왼쪽
                    //if (viewFlipper.getDisplayedChild() == 1)
                    //    break;
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_left));
                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_left));
                    viewFlipper.showNext();
                    mtxtView.setText(filekey.get(viewFlipper.getDisplayedChild()).toString());
                    mKey = filekey.get(viewFlipper.getDisplayedChild()).toString();
                    mFileName = filename.get(viewFlipper.getDisplayedChild()).toString();
                } else {   // 오른쪽
                    //if (viewFlipper.getDisplayedChild() == 0)
                    //    break;
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_right));
                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_right));
                    viewFlipper.showPrevious();
                    mtxtView.setText(filekey.get(viewFlipper.getDisplayedChild()).toString());
                    mKey = filekey.get(viewFlipper.getDisplayedChild()).toString();
                    mFileName = filename.get(viewFlipper.getDisplayedChild()).toString();
                }
                break;
        }
        return true;
    }

    /**
     * 데이터베이스에서 이미지를 읽어서 Flipper에 추가한다.
     */
    private void setImageLayout()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mKey = b.getString("imageKey");
            SQLiteDatabase db = mDB.getWritableDatabase();
            String sql = "select id, filename, sts from tb_files"; //   where id='"+mKey+"'";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            filename = new ArrayList();
            filekey = new ArrayList();
            int i=0;
            int found = 0;
            while(cursor.isAfterLast() == false)
            {
                filename.add(cursor.getString(1));
                filekey.add(cursor.getString(0));
                if (cursor.getString(0).equals(mKey)) {
                    mFileName = cursor.getString(1);
                    found = i;
                }
                // Image 출력
                Log.d(TAG, filename.get(i).toString());
                Bitmap bitmap = BitmapFactory.decodeFile(filename.get(i).toString());
                Bitmap newbitMap = Bitmap.createScaledBitmap(bitmap, size.x, size.y, true);
                setFlipperImage(newbitMap, cursor.getString(0));

                cursor.moveToNext();
                i++;
            }
            cursor.close();
            db.close();



//            view = findViewById(R.id.previewImage);
//            view.setImageBitmap(newbitMap);

            // Text 출력
            int pos = mFileName.lastIndexOf(".");
            String _fileName = mFileName.substring(mFileName.lastIndexOf("/")+1, pos);

            mtxtView = findViewById(R.id.send_title);
            mtxtView.setText(_fileName);
            viewFlipper.setDisplayedChild(found);
        }
    }

    /**
     * Flipper에 ImageView를 생성해서 추가한다.
     *
     * @param bitmap
     * @param filekey
     */
    private void setFlipperImage(Bitmap bitmap, String filekey)
    {
        ImageView image = new ImageView(getApplicationContext());
        image.setImageBitmap(bitmap);
        image.setTag(filekey);
        viewFlipper.addView(image);
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
