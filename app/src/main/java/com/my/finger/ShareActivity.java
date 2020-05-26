package com.my.finger;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.my.finger.adapter.ShareViewAdapter;
import com.my.finger.data.ImageItem;
import com.my.finger.data.ShareDataItem;
import com.my.finger.data.ShareMainItem;
import com.my.finger.utils.Constant;
import com.my.finger.utils.SessionUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShareActivity extends AppCompatActivity {
    private final String TAG = "KDN_TAG";
    private Button btnDept;         // 부서선택 버튼
    private Button btnMyDept;       // 내부서 선택 버튼
    private Button btnMyPhoto;      // 내 사진 선택 버튼
    private ImageView btnCal1;
    private ImageView btnCal2;
    private EditText st_dt;
    private EditText et_dt;
    private String searchStDt;
    private String searchEndDt;
    private String searchText;
    private String searchType;
    private String pageNo = "1";
    private boolean isData;
    private ShareViewAdapter adapter;
    private Context mContext;

    private boolean selectedMode;   //  true : 내사진, false: 내 부서
    private List<ShareMainItem> adapterList = new ArrayList<>();
    private ListView mView;

    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener myDatePickerStart = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelStart();
        }
    };
    DatePickerDialog.OnDateSetListener myDatePickerEnd = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelEnd();
        }
    };
    private void updateLabelStart ()
    {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);

        //EditText st = findViewById(R.id.txtStDt);
        st_dt.setText(sdf.format(myCalendar.getTime()));
    }
    private void updateLabelEnd ()
    {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);

        //EditText et = findViewById(R.id.txtEtDt);
        et_dt.setText(sdf.format(myCalendar.getTime()));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        mView =  findViewById(R.id.listShareImageView);
        selectedMode = true;        // 내 사진

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnDept:
                        break;
                    case R.id.btnMyDept:
                        if (selectedMode == true) {
                            selectedMode = false;       // 부서
                            populateDataSearch();
                        }
                        break;
                    case R.id.btnMyPhoto:
                        if (selectedMode == false) {
                            selectedMode = true;
                            populateDataSearch();
                        }
                        break;
                }
            }
        };

        ImageView.OnClickListener onImageClickListener = new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnCal1:
                        new DatePickerDialog(ShareActivity.this, myDatePickerStart, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                        break;
                    case R.id.btnCal2:
                        new DatePickerDialog(ShareActivity.this, myDatePickerEnd, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                        break;
                }
            }
        };

        // 시작일자/종료일자 기본 세팅 (일주일 전 ~ 현재)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.KOREA);
        et_dt = findViewById(R.id.txtEtDt);
        et_dt.setText(sdf.format(new Date()));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);

        st_dt = findViewById(R.id.txtStDt);
        st_dt.setText(sdf.format(cal.getTime()));

        // Button click event
        btnDept = findViewById(R.id.btnDept);
        btnDept.setOnClickListener(onClickListener);

        btnMyDept = findViewById(R.id.btnMyDept);
        btnMyDept.setOnClickListener(onClickListener);

        btnMyPhoto = findViewById(R.id.btnMyPhoto);
        btnMyPhoto.setOnClickListener(onClickListener);

        btnCal1 = findViewById(R.id.btnCal1);
        btnCal1.setOnClickListener(onImageClickListener);

        btnCal2 = findViewById(R.id.btnCal2);
        btnCal2.setOnClickListener(onImageClickListener);

        mContext = this;
        adapter = new ShareViewAdapter(mContext, adapterList);
        mView.setAdapter(adapter);

        mView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mView.setStackFromBottom(true);

        populateDataSearch();
    }

    /**
     * 데이터 조회
     */
    private void  populateDataSearch()
    {
        if (selectedMode == true)       // 내 사진
        {
            btnMyDept.setBackgroundColor(Color.rgb(55,56,55));  // "#373837");
            btnMyPhoto.setBackgroundColor(Color.rgb(19, 142, 76));  // "#138e4c");
            searchText = SessionUtil.empid;
            searchType = "id";
        }else {         // 내 부서
            btnMyDept.setBackgroundColor(Color.rgb(19, 142, 76));  // "#138e4c");
            btnMyPhoto.setBackgroundColor(Color.rgb(55,56,55));  // "#373837");
            searchText = "";        // 부서코드 구해야 됨
            searchType = "dept";
        }
        searchStDt = st_dt.getText().toString().replace("/", "").replace("/", "");
        searchEndDt = et_dt.getText().toString().replace("/", "").replace("/", "");

        // 데이터를 조회한다.
        new Thread() {
            public void run() {
                // All your networking logic should be here
                try{
                    String url = Constant.IMAGE_URL;
                    URL urlObj = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);

                    conn.connect();
                    StringBuilder sbParams = new StringBuilder();
                    sbParams.append("searchStDt").append("=").append(searchStDt);
                    sbParams.append("&").append("searchEndDt").append("=").append(searchEndDt);
                    sbParams.append("&").append("searchText").append("=").append(searchText);
                    sbParams.append("&").append("searchType").append("=").append(searchType);
                    sbParams.append("&").append("pageNo").append("=").append(pageNo);
                    String paramsString = sbParams.toString();

                    Log.d(TAG, "Parameter : " + sbParams.toString());

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

                    adapterList.clear();
                    if (code.equals("OK")) {
                        JSONArray rows = jsonObject.getJSONArray("rows");
                        for (int i = 0; i < rows.length(); i++) {
                            JSONObject object = rows.getJSONObject(i);
                            String rgstYmd = object.getString("rgstYmd");
                            JSONArray list = object.getJSONArray("imageShareList");
                            int k= 0;
                            ImageItem item = null;
                            ShareMainItem mainItem = new ShareMainItem();
                            List<ImageItem> listItem = new ArrayList<>();
                            mainItem.rgstYmd = rgstYmd;
                            for (int j = 0; j < list.length(); j++) {
                                JSONObject obj = list.getJSONObject(j);
                                ShareDataItem data = new ShareDataItem();
                                data.rgstYmd = rgstYmd;
                                data.imageSeqno = obj.getString("imageSeqno");
                                data.rgstEmpid = obj.getString("rgstEmpid");
                                data.rgstName = obj.getString("rgstName");
                                data.oriFileName = obj.getString("oriFileName");
                                data.logFileName = obj.getString("logFileName");
                                data.thumbnailFileName = obj.getString("thumbnailFileName");
                                // bitmap image load
                                InputStream in = new java.net.URL(data.thumbnailFileName).openStream();
                                Bitmap bmp = BitmapFactory.decodeStream(in);
                                data.image = bmp;

                                k++;

                                if (k % 3 ==1) {
                                    item = new ImageItem();
                                    item.item1 = data;
                                }else if(k % 3 == 2) {
                                    item.item2 = data;
                                }else if(k % 3 == 0) {
                                    item.item3 = data;
                                    listItem.add(item);
                                    Log.d(TAG, "add => "+ adapterList.size());
                                }
                            }
                            if (k % 3 != 0)
                            {
                                if (item != null) listItem.add(item);
                            }
                            mainItem.imageList = listItem;
                            adapterList.add(mainItem);
                        }
                    }else {
                        Log.d(TAG, "data not found...");
                        Toast.makeText(ShareActivity.this, "조회데이터가 업습니다!!!", Toast.LENGTH_SHORT).show();
                    }
                    conn.disconnect();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d(TAG, "list clear and list view 초기화");
                            //adapter.getList().clear();
                            adapter.setList(adapterList);
                            adapter.notifyDataSetChanged();
                            mView.invalidateViews();
                            mView.refreshDrawableState();
                            Log.d(TAG, "Size => " + adapterList.size());
                        }
                    });
                }catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
