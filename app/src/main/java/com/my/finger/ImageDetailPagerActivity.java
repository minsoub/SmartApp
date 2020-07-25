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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.my.finger.adapter.SendDetailPagerAdapter;
import com.my.finger.adapter.SharePagerAdapter;
import com.my.finger.data.ShareDataItem;
import com.my.finger.utils.Constant;
import com.my.finger.utils.DataBaseUtil;
import com.my.finger.utils.SessionUtil;
import com.my.finger.utils.TouchImageView;
import com.my.finger.utils.UploadUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 사진 전송에서 상세 이미지 보기
 *
 */
public class ImageDetailPagerActivity extends AppCompatActivity {
    private DataBaseUtil mDB;
    private ProgressDialog dialog = null;
    private final String TAG = "KDN_TAG";
    private int serverResponseCode = 0;

    private EditText mtxtView;

    private SendDetailPagerAdapter adapter;
    private ViewPager viewPager;
    private ArrayList mFileName;
    private ArrayList mFilekey;
    private String mCurrentFileName;
    private String mKey;
    private int mIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail_pager);
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


        // 버튼 이벤트 리스너
        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(onClickListener);

        ImageView btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(onClickListener);
        mtxtView = findViewById(R.id.send_title);
        mtxtView.setInputType(EditorInfo.TYPE_NULL);
        mtxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText)view).setInputType(EditorInfo.TYPE_CLASS_TEXT); // setCursorVisible(true); 도 가능하다.
            }
        });
        mtxtView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( (event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) )
                {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mtxtView.getWindowToken(), 0);
                    mtxtView.setInputType(EditorInfo.TYPE_NULL);
                    Log.d(TAG, "Editable change complated....");

                    // 파일이름 변경 해야 한다.

                    return true;
                }else {
                    return false;
                }
            }
        });

        setImageLayout();

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                mIndex = position;
                mCurrentFileName = mFileName.get(position).toString();
                mKey = mFilekey.get(position).toString();

                int pos = mCurrentFileName.lastIndexOf(".");
                String _fileName = mCurrentFileName.substring(mCurrentFileName.lastIndexOf("/")+1, pos);
                mtxtView.setText(_fileName);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        adapter = new SendDetailPagerAdapter(this);
        adapter.setDataArray(mFileName, mFilekey);
        Display display = getWindowManager().getDefaultDisplay();
        adapter.setDisplay(display);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(mIndex);
    }

//    /**
//     * Pager Adapter에서 화면이 그려질 때 현재 Index와 key를 받아온다.
//     *
//     * @param fileName
//     * @param fileKey
//     * @param position
//     */
//    public void setCurrentImage(String fileName, String fileKey, int position)
//    {
//        mIndex = position;
//        mCurrentFileName = fileName;
//        mKey = fileKey;
//
//        int pos = mCurrentFileName.lastIndexOf(".");
//        String _fileName = mCurrentFileName.substring(mCurrentFileName.lastIndexOf("/")+1, pos);
//        mtxtView.setText(_fileName);
//    }

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
            mFileName = new ArrayList();
            mFilekey = new ArrayList();
            int i=0;
            while(cursor.isAfterLast() == false)
            {
                mFileName.add(cursor.getString(1));
                mFilekey.add(cursor.getString(0));
                if (cursor.getString(0).equals(mKey)) {
                    mCurrentFileName = cursor.getString(1);
                    mIndex = i;
                }
                cursor.moveToNext();
                i++;
            }
            cursor.close();
            db.close();
        }
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
                        File file = new File(mFileName.get(mIndex).toString());
                        if (file.exists())
                        {
                            file.delete();
                        }

                        Toast.makeText(getBaseContext(), "삭제를 완료하였습니다!!!",
                                Toast.LENGTH_SHORT).show();

                        result = viewPager.getCurrentItem();
                        adapter.deletePage(result);
                        result = result +1;
                        if (result <= (mFilekey.size()-1))
                        {
                            viewPager.setCurrentItem(result);
                        }else {
                            result = result - 1;
                            if (result >= 0)
                            {
                                viewPager.setCurrentItem(result);
                            }else {
                                onBackPressed();
                            }
                        }
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
                Intent intent = new Intent(ImageDetailPagerActivity.this, LoginActivity.class);
                startActivity(intent);
            }else {
                // 이미지를 전송한다.
                dialog = ProgressDialog.show(ImageDetailPagerActivity.this, "", "Uploading file...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(TAG, "uploading started.....");
                            }
                        });
                        uploadFile(mFileName.get(mIndex).toString());
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
                        Toast.makeText(ImageDetailPagerActivity.this, "업로드를 완료하였습니다!!!",
                                Toast.LENGTH_SHORT).show();
                        SQLiteDatabase db = mDB.getWritableDatabase();
                        int result = db.delete("tb_files", "id=?", new String[]{mKey});
                        db.close();

                        // 파일 삭제
                        File file = new File(mFileName.get(mIndex).toString());
                        if (file.exists())
                        {
                            file.delete();
                        }
                        result = viewPager.getCurrentItem();
                        adapter.deletePage(result);
                        result = result +1;
                        if (result <= (mFilekey.size()-1))
                        {
                            viewPager.setCurrentItem(result);
                        }else {
                            result = result - 1;
                            if (result >= 0)
                            {
                                viewPager.setCurrentItem(result);
                            }else {
                                onBackPressed();
                            }
                        }
                    }
                });
            }else {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "Got Exception : see logcat ");
                        Toast.makeText(ImageDetailPagerActivity.this, "업로드에 실패하였습니다. 관리자에게 문의하시기 바랍니다!!! ",
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
        Intent intent = new Intent(ImageDetailPagerActivity.this, SendActivity.class);
        startActivity(intent);
    }
}