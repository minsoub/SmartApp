package com.my.finger;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
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

import com.my.finger.adapter.SharePagerAdapter;
import com.my.finger.data.SerialImageItem;
import com.my.finger.data.SerialShareDataItem;
import com.my.finger.data.ShareDataItem;
import com.my.finger.data.ShareSerializeItem;
import com.my.finger.utils.Constant;
import com.my.finger.utils.SessionUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * 사진 공유의 상세 이미지 페이지 Class
 */
public class SharePagerActivity extends AppCompatActivity {
    private String mOriFileName;
    private final String TAG = "KDN_TAG";
    private ArrayList<ShareSerializeItem> bundleList;
    private TextView mtxtView;
    private String searchStDt;
    private String searchEndDt;
    private String searchText;
    private String searchType;

    private SharePagerAdapter adapter;
    private ArrayList<ShareDataItem> mList;
    private ViewPager viewPager;
    private String mTitle;
    private String mKey;
    private int mPosition;
    private int mSetIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_pager);

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

            mtxtView = findViewById(R.id.send_title);
            Log.d(TAG, "origin filename : " + mOriFileName);
            mtxtView.setText(mOriFileName);
            Log.d(TAG, "mKey : " + mKey);
            Log.d(TAG, "mTitle : " + mTitle);

            // bundleList 데이터를 ShareDataItem Array로 만들어서 PagerAdapter에 넘겨주어야 한다.
            setPagerDataCreate();
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

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                mTitle = mList.get(position).oriFileName;
                mKey = mList.get(position).imageSeqno;
                mtxtView.setText(mTitle);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });


        adapter = new SharePagerAdapter(this);
        adapter.setDataArray(mList);
        Display display = getWindowManager().getDefaultDisplay();
        adapter.setDisplay(display);
        Log.d(TAG , "adapter is set..");
        viewPager.setAdapter(adapter);
        Log.d(TAG , "adapter is set..end");
        Log.d(TAG , "setCurrentItem is set..");
        viewPager.setCurrentItem(mSetIndex);
        Log.d(TAG , "setCurrentItem is set..end");
        Log.d(TAG, "msetIndex : " + mSetIndex);
    }

//    /**
//     * 현재 페이지의 타이틀, 키, 포지션 위치를 받아서 저장한다.
//     * 삭제 시 포지션을 통해 제거해야 한다.
//     * @param title
//     * @param key
//     * @param position
//     */
//    public void setCurrentImage(String title, String key, int position)
//    {
//        mPosition = position;
//        mTitle = title;
//        mKey = key;
//        mtxtView.setText(title);
//    }
    /**
     * 페이져 뷰에서 사용할 데이터에 대해서 리스트화 한다.
     */
    private void setPagerDataCreate()
    {
        mList = new ArrayList<>();

        int index = 0;
        for (int i=0; i<bundleList.size(); i++)
        {
            ShareSerializeItem item = bundleList.get(i);
            ArrayList<SerialImageItem> sub_item = item.imageList;

            for (int j=0; j<sub_item.size(); j++)
            {
                SerialShareDataItem item1 = sub_item.get(j).item1;
                SerialShareDataItem item2 = sub_item.get(j).item2;
                SerialShareDataItem item3 = sub_item.get(j).item3;

                if (item1 != null && item1.oriFileName != null)
                {
                    ShareDataItem data = new ShareDataItem();
                    data.oriFileName = item1.oriFileName;
                    data.imageSeqno = item1.imageSeqno;
                    data.logFileName = item1.logFileName;
                    data.rgstEmpid = item1.rgstEmpid;
                    mList.add(data);
                    if (item1.imageSeqno.equals(mKey))
                    {
                        mSetIndex = index; // mList.size()-1;
                    }
                    index++;
                }
                if (item2 != null && item2.oriFileName != null)
                {
                    ShareDataItem data = new ShareDataItem();
                    data.oriFileName = item2.oriFileName;
                    data.imageSeqno = item2.imageSeqno;
                    data.logFileName = item2.logFileName;
                    data.rgstEmpid = item2.rgstEmpid;
                    mList.add(data);
                    if (item2.imageSeqno.equals(mKey))
                    {
                        mSetIndex =index; //  mList.size()-1;
                    }
                    index++;
                }
                if (item3 != null && item3.oriFileName != null)
                {
                    ShareDataItem data = new ShareDataItem();
                    data.oriFileName = item3.oriFileName;
                    data.imageSeqno = item3.imageSeqno;
                    data.logFileName = item3.logFileName;
                    data.rgstEmpid = item3.rgstEmpid;
                    mList.add(data);
                    if (item3.imageSeqno.equals(mKey))
                    {
                        mSetIndex = index; // mList.size()-1;
                    }
                    index++;
                }
            }
        }
    }

    /**
     * 이미지를 삭제한다. 파일에서 삭제하고 데이터베이스에서도 삭제한다.
     *
     */
    private void imageDeleteProcess()
    {
        if (SessionUtil.empid.equals(mList.get(mPosition).rgstEmpid)) {
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
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 취소시 처리 로직
                        }
                    })
                    .show();
        }else {
            Toast.makeText(getBaseContext(), "삭제 할 권한이 없습니다!!!",
                    Toast.LENGTH_SHORT).show();
        }
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

                                int result = viewPager.getCurrentItem();
                                adapter.deletePage(result);

                                result = result +1;
                                if (result <= (mList.size()-1))
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
        Intent intent = new Intent(SharePagerActivity.this, ShareActivity.class);
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