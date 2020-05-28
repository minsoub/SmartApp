package com.my.finger;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.my.finger.adapter.ListViewAdapter;
import com.my.finger.data.FileDataItem;
import com.my.finger.utils.Constant;
import com.my.finger.utils.DataBaseUtil;
import com.my.finger.utils.SessionUtil;
import com.my.finger.utils.UploadUtil;

import java.io.File;
import java.util.ArrayList;

public class SendActivity extends AppCompatActivity {
    private final String TAG = "KDN_TAG";
    private ArrayList<FileDataItem> arrayList;
    private DataBaseUtil mDB;
    private ListView view;
    private boolean isChecked;
    private ImageView btnCheckAll;
    private ImageView btnSend;
    private ImageView btnDelete;
    private TextView txtTitle;
    private ListViewAdapter adapter;
    public ArrayList<FileDataItem> checkList;
    private ProgressDialog dialog = null;
    public int serverResponseCode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        mDB = new DataBaseUtil(this);

        view =  findViewById(R.id.listImageView);
        arrayList = populateList();

        adapter = new ListViewAdapter(this, arrayList);
        view.setAdapter(adapter);
        isChecked = false;

        ImageView.OnClickListener onClickListener = new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.radioCheck:
                        if (isChecked)  // 체크 해제
                        {
                            btnCheckAll.setImageResource(R.mipmap.obj_chck_all);
                            isChecked = false;
                            checkAllProcess();
                        }else {
                            btnCheckAll.setImageResource(R.mipmap.obj_chck_on);
                            isChecked = true;
                            checkAllProcess();
                        }
                        break;
                    case R.id.btnSend:
                        if (SessionUtil.empid == null)
                        {
                            // 로그인 페이지 이동
                            if (mDB != null) mDB.close();
                            // your code.
                            Intent intent = new Intent(SendActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        sendProcess();
                        break;
                    case R.id.btnDelete:
                        deleteProcess();
                        break;
                }
            }
        };
        TextView.OnClickListener onTextClickListener = new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.send_title:
                        onBackPressed();
                        break;
                }
            }
        };

        btnCheckAll = findViewById(R.id.radioCheck);
        btnCheckAll.setOnClickListener(onClickListener);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(onClickListener);
        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(onClickListener);

        txtTitle = findViewById(R.id.send_title);
        txtTitle.setOnClickListener(onTextClickListener);
    }

    /**
     * 이미지 키를 받아서 상세 정보를 보여주는 페이지로 이동한다.
     *
     * @param imageKey
     */
    public void setMoveDetailImage(String imageKey)
    {
        Log.d(TAG, "setMoveDetailImage call : " + imageKey);
        Intent intent = new Intent(SendActivity.this, ImageDetailActivity.class);
        Bundle b = new Bundle();
        b.putString("imageKey", imageKey);
        intent.putExtras(b);
        startActivity(intent);
    }
    /**
     * 모든 체크박스 이미지를 찾아서 체크해제/체크선택을 수행한다.
     *
     */
    private void checkAllProcess()
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "checkAllProcess call ");
                adapter.setChecked(isChecked);

                adapter.notifyDataSetChanged();
                view.invalidateViews();
                view.refreshDrawableState();
            }
        });
    }

    /**
     * 체크된 이미지를 전송한다.
     *
     */
    private void sendProcess()
    {
        checkList = adapter.getList();
        Log.d(TAG, "sendProcess checkList : " + checkList.size());

        int count = 0;
        if (checkList.size() > 0)
        {
            for (int i=0; i<checkList.size(); i++) {
                FileDataItem item = checkList.get(i);
                if (item.isChecked1 == true) count++;
                if (item.isChecked2 == true) count++;
                if (item.isChecked3 == true) count++;
            }
            if (count == 0)
            {
                Toast.makeText(getBaseContext(), "전송을 위해서 사진을 체크하세요!!!", Toast.LENGTH_SHORT).show();
            }else {
                new AlertDialog.Builder(this)
                        .setTitle("파일전송")
                        .setMessage("선택한 파일을 전송하시겠습니까?")
                        .setIcon(R.mipmap.btn_del01)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int whichButton) {
                                dialog = ProgressDialog.show(SendActivity.this, "", "Uploading file...", true);
                                new Thread(new Runnable() {
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                             Log.d(TAG, "uploading started.....");
                                            }
                                        });
                                        uploadFile();
                                    }
                                }).start();
                            }})
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }})
                        .show();
            }
        }
    }

    /**
     * 체크된 이미지를 업로드를 한다.
     *
     * @return
     */
    public int uploadFile()
    {
//        runOnUiThread(new Runnable() {
//            public void run() {
                UploadUtil upload = new UploadUtil(SessionUtil.empid, SessionUtil.deptCd);
                Log.d(TAG, "uploadFile check list count [ " + checkList.size() +" ]");
                for (int i = 0; i < checkList.size(); i++) {
                    final FileDataItem item = checkList.get(i);
                    Log.d(TAG, "Current Check Item => " + item.toString());
                    if (item.isChecked1) {
                        Log.d(TAG, "파일 업로드 isChecked1 =>" + item.file1+ ", text : " + item.text1);
                        Constant.ReturnCode result = upload.fileUpload(item.file1);
                        if (result == Constant.ReturnCode.http201) {
                            Log.d(TAG, "파일 업로드 완료 =>" + item.file1);
                            mDB.delete(item.text1);
                            // 파일 삭제
                            File file = new File(item.file1);
                            if (file.exists()) {
                                file.delete();
                                Log.d(TAG, "파일 삭제 완료  =>" + item.file1);
                            }
                        } else {
                            dialog.dismiss();
                            Log.d(TAG, "Got Exception : see logcat ");
                            Toast.makeText(SendActivity.this, "업로드에 실패하였습니다. 관리자에게 문의하시기 바랍니다!!! ", Toast.LENGTH_SHORT).show();
                            serverResponseCode = -1;
                            break;
                        }
                    }
                    if (item.isChecked2) {
                        Log.d(TAG, "파일 업로드 isChecked2 =>" + item.file2+ ", text : " + item.text2);
                        Constant.ReturnCode result = upload.fileUpload(item.file2);
                        if (result == Constant.ReturnCode.http201) {
                            Log.d(TAG, "파일 업로드 완료  =>" + item.file2);
                            mDB.delete(item.text2);
                            // 파일 삭제
                            File file = new File(item.file2);
                            if (file.exists()) {
                                file.delete();
                                Log.d(TAG, "파일 삭제 완료  =>" + item.file2);
                            }
                        } else {
                            dialog.dismiss();
                            Log.d(TAG, "Got Exception : see logcat ");
                            Toast.makeText(SendActivity.this, "업로드에 실패하였습니다. 관리자에게 문의하시기 바랍니다!!! ", Toast.LENGTH_SHORT).show();
                            serverResponseCode = -1;
                            break;
                        }
                    }
                    if (item.isChecked3) {
                        Log.d(TAG, "파일 업로드 isChecked3 =>" + item.file3 + ", text : " + item.text3);
                        Constant.ReturnCode result = upload.fileUpload(item.file3);
                        if (result == Constant.ReturnCode.http201) {
                            Log.d(TAG, "파일 업로드 완료 =>" + item.file3);
                            mDB.delete(item.text3);
                            // 파일 삭제
                            File file = new File(item.file3);
                            if (file.exists()) {
                                file.delete();
                                Log.d(TAG, "파일 삭제 완료  =>" + item.file3);
                             }
                        } else {
                             dialog.dismiss();
                             Log.d(TAG, "Got Exception : see logcat ");
                             Toast.makeText(SendActivity.this, "업로드에 실패하였습니다. 관리자에게 문의하시기 바랍니다!!! ", Toast.LENGTH_SHORT).show();
                             serverResponseCode = -1;
                             break;
                        }
                    }
                }  // for end

                Log.d(TAG, "responseCode check : " + serverResponseCode);
                if (serverResponseCode == 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dialog.dismiss();
                            Log.d(TAG, "파일 업로드 완료");
                            Toast.makeText(SendActivity.this, "업로드를 완료하였습니다!!!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Log.d(TAG, "dialog dismiss...");
                    dialog.dismiss();
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "list clear and list view 초기화");
                        adapter.getList().clear();
                        arrayList.clear();
                        arrayList = populateList();
                        adapter.setList(arrayList);
                        adapter.notifyDataSetChanged();
                        view.invalidateViews();
                        view.refreshDrawableState();
                    }
                });
//            }
//        });
        Log.d(TAG, "return job end...");
        return serverResponseCode;
    }
    /**
     * 체크된 이미지를 삭제한다.
     */
    private void deleteProcess()
    {
        checkList = adapter.getList();
        int count = 0;
        if (checkList.size() > 0)
        {
            for (int i=0; i<checkList.size(); i++) {
                FileDataItem item = checkList.get(i);
                if (item.isChecked1 == true) count++;
                if (item.isChecked2 == true) count++;
                if (item.isChecked3 == true) count++;
            }
            if (count == 0)
            {
                Toast.makeText(getBaseContext(), "삭제를 위해서 사진을 체크하세요!!!", Toast.LENGTH_SHORT).show();
            }else {
                new AlertDialog.Builder(this)
                        .setTitle("파일삭제")
                        .setMessage("선택한 파일을 하시겠습니까?")
                        .setIcon(R.mipmap.btn_del01)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                for (int i=0; i<checkList.size(); i++) {
                                    // 확인시 처리 로직
                                    FileDataItem item = checkList.get(i);
                                    if (item.isChecked1) {
                                        int result = mDB.delete(item.text1);
                                        // 파일 삭제
                                        File file = new File(item.file1);
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                    }else if(item.isChecked2)
                                    {
                                        int result = mDB.delete(item.text2);
                                        // 파일 삭제
                                        File file = new File(item.file2);
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                    }else if(item.isChecked3)
                                    {
                                        int result = mDB.delete(item.text3);
                                        // 파일 삭제
                                        File file = new File(item.file3);
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                    }
                                }
                                // 작업이 완료되었으면..
                                // 리스트를 다시 읽어 들어야 한다.
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        adapter.getList().clear();
                                        arrayList.clear();
                                        arrayList = populateList();
                                        adapter.setList(arrayList);
                                        adapter.notifyDataSetChanged();
                                        view.invalidateViews();
                                        view.refreshDrawableState();
                                    }
                                });
                            }})
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // 취소시 처리 로직
                            }})
                        .show();
            }
        }
    }

    /**
     * 데이터베이스에서 등록된 이미지 리스트를 읽어서 Map에 구성한다.
     * 3개의 컬럼에 데이터가 들어갈 수 있도록 구성해야 한다.
     */
    private ArrayList<FileDataItem> populateList()
    {
        Log.d(TAG, "populateList called....");
        ArrayList<FileDataItem> list = new ArrayList<>();
        SQLiteDatabase db = mDB.getWritableDatabase();
        String sql = "select id, filename, sts from tb_files where sts='N' order by id desc";
        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();

        int i=0;
        FileDataItem map = null;
        while(cursor.isAfterLast() == false)
        {
            long id = cursor.getLong(0);
            String filename = cursor.getString(1);
            String sts = cursor.getString(2);
            // Log.d(TAG, "fileName : " + filename);
            i++;
            if (i % 3 ==1) {
                map = new FileDataItem();
                map.text1 = String.valueOf(id);
                map.image1 = getAdjustSize(filename);
                map.file1 = filename;
                map.isChecked1 = false;
            }else if(i % 3 == 2) {
                map.text2 = String.valueOf(id);
                map.image2 = getAdjustSize(filename);
                map.file2 = filename;
                map.isChecked2 = false;
            }else if(i % 3 == 0)
            {
                map.text3 = String.valueOf(id);
                map.image3 = getAdjustSize(filename);
                map.file3 = filename;
                map.isChecked3 = false;
                list.add(map);
            }
            cursor.moveToNext();
        }
        if (i % 3 != 0)
        {
            if (map != null) list.add(map);
        }
        cursor.close();
        db.close();

        return list;
    }

    /**
     * 비트맵 출력시 화면의 크기를 고려해서 적당한 크기로 resize
     * @param fileName
     * @return
     */
    private Bitmap getAdjustSize(String fileName)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        File file = new File(fileName);
        if (file.exists()) {
            Bitmap bitmap1 = BitmapFactory.decodeFile(fileName);

            if (bitmap1 == null) return bitmap1;

            int newWidth = metrics.widthPixels / 3 - 20;

            Bitmap newbitMap = Bitmap.createScaledBitmap(bitmap1, newWidth, newWidth, true);

            return newbitMap;
        }
        return null;
    }

    /**
     * 핸드폰 Back 버튼 제어
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed called..");

        if (mDB != null) mDB.close();
        // your code.
        Intent intent = new Intent(SendActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
