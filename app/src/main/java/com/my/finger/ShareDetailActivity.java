package com.my.finger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
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

import com.my.finger.data.SerialImageItem;
import com.my.finger.data.ShareSerializeItem;
import com.my.finger.utils.Constant;
import com.my.finger.utils.SessionUtil;
import com.my.finger.utils.TouchImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Not use class
 */
public class ShareDetailActivity extends Activity implements View.OnTouchListener{
    private String mKey;
    private String mTitle;
    private String mOriFileName;
    private TouchImageView view;
    private ProgressDialog dialog = null;
    private final String TAG = "KDN_TAG";
    private ArrayList filename;
    private ArrayList filekey;
    private ArrayList<ShareSerializeItem> bundleList;
    private ViewFlipper viewFlipper;
    private float xAtDown, xAtUp;
    private TextView mtxtView;
    private String searchStDt;
    private String searchEndDt;
    private String searchText;
    private String searchType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_detail);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mKey = b.getString( "seqno");  // "imageKey");  // url
            mTitle = b.getString("seqno");   // seqno
            mOriFileName = b.getString("oriFileName");
            bundleList = (ArrayList<ShareSerializeItem>) b.get("prevData");
            searchStDt = b.getString("searchStDt");
            searchEndDt = b.getString("searchEndDt");
            searchText = b.getString("searchText");
            searchType = b.getString("searchType");

            viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
            viewFlipper.setOnTouchListener(this);

            mtxtView = findViewById(R.id.send_title);

            Log.d(TAG, "origin filename : " + mOriFileName);
            mtxtView.setText(mOriFileName);
            Log.d(TAG, "mKey : " + mKey);
            Log.d(TAG, "mTitle : " + mTitle);
        }

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnDelete:
                        imageDeleteProcess();
                        break;
                }
            }
        };

        // 버튼 이벤트 리스너
        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(onClickListener);


        imageFlipperLoad();
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
                    if (viewFlipper.getDisplayedChild() == (filekey.size()-1))
                        break;
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_left));
                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_left));
                    viewFlipper.showNext();
                    mKey = filekey.get(viewFlipper.getDisplayedChild()).toString();
                    mtxtView.setText(filename.get(viewFlipper.getDisplayedChild()).toString());

                } else {   // 오른쪽
                    if (viewFlipper.getDisplayedChild() == 0)
                        break;
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_right));
                    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.out_from_right));
                    viewFlipper.showPrevious();
                    mKey = filekey.get(viewFlipper.getDisplayedChild()).toString();
                    mtxtView.setText(filename.get(viewFlipper.getDisplayedChild()).toString());
                }
                break;
        }
        return true;
    }
    private void setFlipperImage(Bitmap bitmap, String filekey)
    {
        ImageView image = new ImageView(getApplicationContext());
        image.setImageBitmap(bitmap);
        image.setTag(filekey);
        viewFlipper.addView(image);
    }

    private int delFlipperImage(String key)
    {
        int found = -1;
        for (int i=0; i<filekey.size(); i++)
        {
            if (filekey.get(i).toString().equals(key))
            {
                found = i;
                break;
            }
        }
        viewFlipper.removeViewAt(found);
        filekey.remove(found);
        return found;
    }

    private void imageFlipperLoad()
    {
        Log.d(TAG, "imageFlipperLoad called...");

        dialog = ProgressDialog.show(ShareDetailActivity.this, "", "Loading Image file...", true);


                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                try {
                    int index = 0;
                    int found = 0;
                    filename = new ArrayList();
                    filekey = new ArrayList();
                    for (int i = 0; i < bundleList.size(); i++) {
                        ShareSerializeItem item = bundleList.get(i);
                        for (int j = 0; j < item.imageList.size(); j++) {
                            SerialImageItem data = item.imageList.get(j);

                            // bitmap image load
                            InputStream in = new java.net.URL(data.item1.logFileName).openStream();
                            Bitmap bmp = BitmapFactory.decodeStream(in);
                            Bitmap bitmap = Bitmap.createScaledBitmap(bmp, size.x, size.y, true);
                            filekey.add(data.item1.imageSeqno);
                            filename.add(data.item1.oriFileName);
                            setFlipperImage(bitmap, data.item1.imageSeqno);
                            Log.d(TAG, mTitle + "==" + data.item1.imageSeqno);
                            if (data.item1.imageSeqno.equals(mTitle)) {
                                found = index;
                                Log.d(TAG, "found 1 index : " + index);
                            }
                            if (data.item2.logFileName != null) {
                                index++;
                                in = new java.net.URL(data.item2.logFileName).openStream();
                                bmp = BitmapFactory.decodeStream(in);
                                bitmap = Bitmap.createScaledBitmap(bmp, size.x, size.y, true);
                                filekey.add(data.item2.imageSeqno);
                                filename.add(data.item2.oriFileName);
                                setFlipperImage(bitmap, data.item2.imageSeqno);
                                Log.d(TAG, mTitle + "==" + data.item2.imageSeqno);
                                if (data.item2.imageSeqno.equals(mTitle)) {
                                    found = index;
                                    Log.d(TAG, "found 2 index : " + index);
                                }
                            }
                            if (data.item3.logFileName != null) {
                                index++;
                                in = new java.net.URL(data.item3.logFileName).openStream();
                                bmp = BitmapFactory.decodeStream(in);
                                bitmap = Bitmap.createScaledBitmap(bmp, size.x, size.y, true);
                                filekey.add(data.item3.imageSeqno);
                                filename.add(data.item3.oriFileName);
                                setFlipperImage(bitmap, data.item3.imageSeqno);
                                Log.d(TAG, mTitle + "==" + data.item3.imageSeqno);
                                if (data.item3.imageSeqno.equals(mTitle)) {
                                    found = index;
                                    Log.d(TAG, "found 3 index : " + index);
                                }
                            }
                            index++;
                        }
                    }
                    Log.d(TAG, "found index : " + found);
                    viewFlipper.setDisplayedChild(found);
                } catch (Exception ex) {
                    //Toast.makeText(getBaseContext(), "이미지를 가져오는데 에러가 발생하였습니다[" + ex.toString() + "]", Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
                } finally {
                    dialog.dismiss();
                }
    }

//    /**
//     * Flipper에 ImageView를 생성해서 추가한다.
//     *
//     * @param bitmap
//     * @param filekey
//     */
//    private void setFlipperImage(Bitmap bitmap, String filekey)
//    {
//        ImageView image = new ImageView(getApplicationContext());
//        image.setImageBitmap(bitmap);
//        image.setTag(filekey);
//        viewFlipper.addView(image);
//    }
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
                        new Thread(new Runnable() {
                            public void run() {
                                deleteFile();
                            }
                        }).start();
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 취소시 처리 로직
                    }})
                .show();
    }

    private void deleteFile()
    {
        new Thread() {
            public void run() {
                // All your networking logic should be here
                try {
                    StringBuilder sbParams = new StringBuilder();
                    sbParams.append("empid").append("=").append(SessionUtil.empid);
                    sbParams.append("&").append("imageSeqno").append("=").append(mKey);
                    String paramsString = sbParams.toString();
                    Log.d(TAG, "Parameter : " + sbParams.toString());


                    String url = Constant.DELETE_URL;
                    URL urlObj = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.connect();

                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(paramsString);
                    wr.flush();
                    wr.close();

                    // received...
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    String json = buffer.toString();
                    Log.d(TAG, "json : " + json);
                    // JSON Parsing
                    JSONObject jsonObject = new JSONObject(json);
                    String code = jsonObject.getString("code");
                    Log.d(TAG, "return code : " + code);
                    conn.disconnect();
                    if (code.equals("OK")) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "삭제를 완료하였습니다!!!",
                                        Toast.LENGTH_SHORT).show();

                                int result = delFlipperImage(mKey);
                                result = result +1;
                                if (result <= (filekey.size()-1))
                                {
                                    viewFlipper.setDisplayedChild(result);
                                }else {
                                    result = result - 1;
                                    if (result >= 0)
                                    {
                                        viewFlipper.setDisplayedChild(result);
                                    }else {
                                        onBackPressed();
                                    }
                                }
                                //onBackPressed();

                                //Intent intent = new Intent(ShareDetailActivity.this, ShareActivity.class);
                                //startActivity(intent);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "삭제하는데 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
                            }});
                    }
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getBaseContext(), "삭제하는데 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
                        }});
                    e.printStackTrace();
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getBaseContext(), "삭제하는데 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
                        }});
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }.start();
    }
    /**
     * 핸드폰 Back 버튼 제어
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed called..");

        // your code.
        Intent intent = new Intent(ShareDetailActivity.this, ShareActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("prevData", bundleList);
        b.putString("searchStDt", searchStDt);
        b.putString("searchEndDt", searchEndDt);
        b.putString("searchText", searchText);
        b.putString("searchType", searchType);
        intent.putExtras(b);

        startActivity(intent);
    }
}
