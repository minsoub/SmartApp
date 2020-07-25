package com.my.finger;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.my.finger.adapter.ShareRecycleAdapter;

import com.my.finger.data.ImageItem;
import com.my.finger.data.SerialImageItem;
import com.my.finger.data.SerialShareDataItem;
import com.my.finger.data.ShareDataItem;
import com.my.finger.data.ShareMainItem;
import com.my.finger.data.ShareSerializeItem;
import com.my.finger.utils.Constant;
import com.my.finger.utils.SessionUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 사진 공유 Activity Class
 */
public class ShareActivity extends AppCompatActivity {
    private final String TAG = "KDN_TAG";
    private Button btnDept;         // 부서선택 버튼
    private Button btnMyDept;       // 내부서 선택 버튼
    private Button btnMyPhoto;      // 내 사진 선택 버튼
    private ImageView btnCal1;
    private ImageView btnCal2;
    private TextView txtTitle;
    private EditText st_dt;
    private EditText et_dt;
    private EditText txtDept;
    private String txtDeptCd;
    private String searchStDt;
    private String searchEndDt;
    private String searchText;
    private String searchType;
    private String pageNo = "1";
    private ShareRecycleAdapter adapter;
    private Context mContext;

    private int selectedMode;   //  1 : 내사진, 2: 내 부서, 3: 타 부서
    private ArrayList<ShareMainItem> adapterList = new ArrayList<>();
    private ArrayList<ShareSerializeItem> bundleList;
    private RecyclerView mView;
    private DisplayMetrics metrics = new DisplayMetrics();
    private ProgressDialog dialog = null;

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.KOREA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_recycler);
        st_dt = findViewById(R.id.txtStDt);
        et_dt = findViewById(R.id.txtEtDt);

        Bundle b = getIntent().getExtras();     // ShareDatailActiviy에서 받아온다.

        if (b != null) {
            Log.d(TAG, "savedInstanceState not null.........");

            bundleList = (ArrayList<ShareSerializeItem>) b.get("prevData");
            searchStDt = b.getString("searchStDt");
            searchEndDt = b.getString("searchEndDt");
            searchText = b.getString("searchText");
            searchType = b.getString("searchType");
            Log.d(TAG, "savedInstanceState searchStDt........."+searchStDt);
            Log.d(TAG, "savedInstanceState searchEndDt........."+searchEndDt);
            Log.d(TAG, "savedInstanceState searchText........."+searchText);
            Log.d(TAG, "savedInstanceState searchType........."+searchType);
            if (searchType != null && !"".equals(searchType))
            {
                if (searchType.equals("id"))
                {
                    selectedMode = 1;        // 내 사진
                }else {
                    selectedMode = 2;        // 내 부서
                }
            }
            // 시작일자/종료일자 기본 세팅 (일주일 전 ~ 현재)
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
            try {
                st_dt.setText(sdf.format(df.parse(searchStDt)));
                et_dt.setText(sdf.format(df.parse(searchEndDt)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            selectedMode = 1;        // 내 사진
            // 시작일자/종료일자 기본 세팅 (일주일 전 ~ 현재)
            et_dt.setText(sdf.format(new Date()));

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -3);
            st_dt.setText(sdf.format(cal.getTime()));
        }


        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnDept:
                        // 부서 팝업
                        selectedMode = 3;
                        Intent intent = new Intent(ShareActivity.this, DeptPopupActivity.class);
                        startActivityForResult(intent, 1);
                        break;
                    case R.id.btnMyDept:
                        //if (selectedMode == true) {
                            selectedMode = 2;       // 부서
                            populateDataSearch();
                        //}
                        break;
                    case R.id.btnMyPhoto:
                        //if (selectedMode == false) {
                            selectedMode = 1;
                            populateDataSearch();
                        //}
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

        txtTitle = findViewById(R.id.send_title);
        txtTitle.setOnClickListener(onTextClickListener);

        txtDept = findViewById(R.id.txtDept);
        //txtDept.setText(SessionUtil.deptNm);
        txtDeptCd = SessionUtil.deptCd;

        mContext = this;

        mView =  findViewById(R.id.listShareView);
        adapter = new ShareRecycleAdapter(this, adapterList);
        mView.setHasFixedSize(true);
        mView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mView.setAdapter(adapter);

        populateDataSearch();
    }

     /**
     * 팝업 화면에서 부서 정보를 넘겨주었을 때 호출된다.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "popupclosed.. (requestCode) : " + requestCode + ", resultCode : " + resultCode);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                //데이터 받기
                selectedMode = 3;
                String cd = data.getStringExtra("deptCd");
                String nm = data.getStringExtra("deptNm");
                if (nm == null || "".equals(nm))
                {
                    Toast.makeText(getBaseContext(), "부서를 선택하지 않았습니다!!!", Toast.LENGTH_SHORT).show();
                }else {
                    txtDept.setText(nm);
                    txtDeptCd = cd;
                    populateDataSearch();
                }
            }
        }
    }
    /**
     * 데이터 조회
     */
    private void  populateDataSearch()
    {
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (txtDeptCd == null || "".equals(txtDeptCd))
        {
            txtDept.setText(SessionUtil.deptNm);
            txtDeptCd = SessionUtil.deptCd;
        }

        if (selectedMode == 1)       // 내 사진
        {
            btnMyDept.setBackgroundColor(Color.rgb(55,56,55));  // "#373837");
            btnMyPhoto.setBackgroundColor(Color.rgb(19, 142, 76));  // "#138e4c");
            btnDept.setBackgroundColor(Color.rgb(55,56,55));  // "#373837");
            searchText = SessionUtil.empid;
            searchType = "id";
        }else {         // 내 부서
            if (selectedMode == 2) {
                btnMyDept.setBackgroundColor(Color.rgb(19, 142, 76));  // "#138e4c");
                btnMyPhoto.setBackgroundColor(Color.rgb(55, 56, 55));  // "#373837");
                btnDept.setBackgroundColor(Color.rgb(55,56,55));  // "#373837");
                txtDeptCd = SessionUtil.deptCd;
                txtDept.setText(SessionUtil.deptNm);
            }else if(selectedMode == 3) {
                btnMyDept.setBackgroundColor(Color.rgb(55, 56, 55));  // "#138e4c");
                btnMyPhoto.setBackgroundColor(Color.rgb(55, 56, 55));  // "#373837");
                btnDept.setBackgroundColor(Color.rgb(19, 142, 76));  // "#373837");
            }

            // 부서정보 입력 여부 확인
            searchText = txtDeptCd;  // txtDept.getText().toString();
            searchType = "dept";
        }
        searchStDt = st_dt.getText().toString().replace("/", "").replace("/", "");
        searchEndDt = et_dt.getText().toString().replace("/", "").replace("/", "");

        bundleList = null;

        if (bundleList != null)
        {
            //TODO: 번들에 있는 데이터를 AdapterList에 넣고 이미지를 로드해야 한다.
            dialog = ProgressDialog.show(ShareActivity.this, "", "Loading Image file...", true);
            try {
                for (int i = 0; i < bundleList.size(); i++) {
                    ShareSerializeItem item = bundleList.get(i);
                    ShareMainItem st = new ShareMainItem();
                    st.rgstYmd = item.rgstYmd;
                    ArrayList<ImageItem> list = new ArrayList<>();

                    for (int j = 0; j < item.imageList.size(); j++) {
                        SerialImageItem a1 = item.imageList.get(j);
                        ImageItem s1 = new ImageItem();
                        ShareDataItem m1 = new ShareDataItem();
                        ShareDataItem m2 = new ShareDataItem();
                        ShareDataItem m3 = new ShareDataItem();
                        m1.imageSeqno = a1.item1.imageSeqno;
                        m1.logFileName = a1.item1.logFileName;
                        m1.oriFileName = a1.item1.oriFileName;
                        m1.rgstDeptCd = a1.item1.rgstDeptCd;
                        m1.thumbnailFileName = a1.item1.thumbnailFileName;
                        m1.rgstYmd = a1.item1.rgstYmd;
                        m1.rgstName = a1.item1.rgstName;
                        m1.rgstEmpid = a1.item1.rgstEmpid;
                        // bitmap image load
                        Log.d(TAG, "thumbnailFileName 1 : " + m1.thumbnailFileName);
                        InputStream in = new java.net.URL(m1.thumbnailFileName).openStream();
                        Bitmap bmp = BitmapFactory.decodeStream(in);
                        int newWidth = metrics.widthPixels / 3 - 15;
                        Bitmap newbitMap = Bitmap.createScaledBitmap(bmp, newWidth, newWidth, true);
                        m1.image = bmp; // newbitMap;

                        if (a1.item2.thumbnailFileName != null) {
                            m2.imageSeqno = a1.item2.imageSeqno;
                            m2.logFileName = a1.item2.logFileName;
                            m2.oriFileName = a1.item2.oriFileName;
                            m2.rgstDeptCd = a1.item2.rgstDeptCd;
                            m2.thumbnailFileName = a1.item2.thumbnailFileName;
                            m2.rgstYmd = a1.item2.rgstYmd;
                            m2.rgstName = a1.item2.rgstName;
                            m2.rgstEmpid = a1.item2.rgstEmpid;
                            // bitmap image load
                            Log.d(TAG, "thumbnailFileName 2 : " + m2.thumbnailFileName);
                            InputStream in2 = new java.net.URL(m2.thumbnailFileName).openStream();
                            Bitmap bmp2 = BitmapFactory.decodeStream(in2);
                            int newWidth2 = metrics.widthPixels / 3 - 15;
                            Bitmap newbitMap2 = Bitmap.createScaledBitmap(bmp2, newWidth2, newWidth2, true);
                            m2.image = bmp2; // newbitMap2;
                        }

                        if (a1.item3.thumbnailFileName != null) {
                            m3.imageSeqno = a1.item3.imageSeqno;
                            m3.logFileName = a1.item3.logFileName;
                            m3.oriFileName = a1.item3.oriFileName;
                            m3.rgstDeptCd = a1.item3.rgstDeptCd;
                            m3.thumbnailFileName = a1.item3.thumbnailFileName;
                            m3.rgstYmd = a1.item3.rgstYmd;
                            m3.rgstName = a1.item3.rgstName;
                            m3.rgstEmpid = a1.item3.rgstEmpid;
                            // bitmap image load
                            Log.d(TAG, "thumbnailFileName 3 : " + m3.thumbnailFileName);
                            InputStream in3 = new java.net.URL(m3.thumbnailFileName).openStream();
                            Bitmap bmp3 = BitmapFactory.decodeStream(in3);
                            int newWidth3 = metrics.widthPixels / 3 - 15;
                            Bitmap newbitMap3 = Bitmap.createScaledBitmap(bmp3, newWidth3, newWidth3, true);
                            m3.image = bmp3;  // newbitMap3;
                        }
                        s1.item1 = m1;
                        s1.item2 = m2;
                        s1.item3 = m3;

                        list.add(s1);
                    }
                    st.imageList = list;
                    adapterList.add(st);
                }
                bundleList.clear();
                bundleList = null;

                Log.d(TAG, "list clear and list view 초기화");
                //adapter.getList().clear();
                adapter.setList(adapterList);
                adapter.notifyDataSetChanged();
                //mView.invalidateItemDecorations();
                //mView.refreshDrawableState();
                Log.d(TAG, "Size => " + adapterList.size());

                if (adapterList.size() == 0) {
                    Toast.makeText(getBaseContext(), "조회데이터가 없습니다!!!", Toast.LENGTH_SHORT).show();
                }
            }catch(Exception ex) {
                Toast.makeText(getBaseContext(), "조회하는데 에러가 발생하였습니다["+ex.toString()+"]", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }finally {
                dialog.dismiss();
            }
        }else {
            // 데이터를 조회한다.
            dialog = ProgressDialog.show(ShareActivity.this, "", "Loading Image file...", true);
            new Thread() {
                public void run() {
                    // All your networking logic should be here
                    try {
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
                                int k = 0;
                                ImageItem item = null;
                                ShareMainItem mainItem = new ShareMainItem();
                                ArrayList<ImageItem> listItem = new ArrayList<>();
                                mainItem.rgstYmd = rgstYmd;
                                for (int j = 0; j < list.length(); j++) {
                                    JSONObject obj = list.getJSONObject(j);
                                    ShareDataItem data = new ShareDataItem();
                                    data.rgstYmd = rgstYmd;
                                    data.imageSeqno = obj.getString("imageSeqno");
                                    data.rgstEmpid = obj.getString("rgstEmpid");
                                    data.rgstName = obj.getString("rgstName");
                                    data.oriFileName = URLDecoder.decode(obj.getString("oriFileName"), "utf-8");
                                    data.logFileName = obj.getString("logFileName");
                                    data.thumbnailFileName = obj.getString("thumbnailFileName");
                                    // bitmap image load
                                    InputStream in = new java.net.URL(data.thumbnailFileName).openStream();
                                    Bitmap bmp = BitmapFactory.decodeStream(in);
                                    int newWidth = metrics.widthPixels / 3 - 15;

                                    Bitmap newbitMap = Bitmap.createScaledBitmap(bmp, newWidth, newWidth, true);

                                    data.image = bmp; // newbitMap;

                                    k++;

                                    if (k % 3 == 1) {
                                        item = new ImageItem();
                                        item.item1 = data;
                                    } else if (k % 3 == 2) {
                                        item.item2 = data;
                                    } else if (k % 3 == 0) {
                                        item.item3 = data;
                                        listItem.add(item);
                                        Log.d(TAG, "add => " + adapterList.size());
                                    }
                                }
                                if (k % 3 != 0) {
                                    if (item != null) listItem.add(item);
                                }
                                mainItem.imageList = listItem;
                                adapterList.add(mainItem);
                            }
                        } else {
                            Log.d(TAG, "data not found...");
                            Toast.makeText(ShareActivity.this, "데이터 조회 에러가 발생하였습니다. 관리자에게 문의바랍니다!!!", Toast.LENGTH_SHORT).show();
                        }
                        conn.disconnect();

                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(TAG, "list clear and list view 초기화");
                                //adapter.getList().clear();
                                adapter.setList(adapterList);
                                adapter.notifyDataSetChanged();
                                //mView.invalidateItemDecorations();
                                //mView.refreshDrawableState();
                                Log.d(TAG, "Size => " + adapterList.size());

                                if (adapterList.size() == 0) {
                                    Toast.makeText(getBaseContext(), "조회데이터가 없습니다!!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(new Runnable() {
                                          public void run() {
                                              Toast.makeText(getBaseContext(), "조회하는데 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
                                          }
                                      });
                        e.printStackTrace();
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "조회하는데 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d(TAG, e.toString());
                        e.printStackTrace();
                    }finally {
                        dialog.dismiss();
                    }
                }
            }.start();
        }
    }

    /**
     * 상세 이미지 조회
     *
     * @param url
     */
    public void setMoveDetailImage(String url, String seqno, String oriFileName)
    {
        Log.d(TAG, "setMoveDetailImage call : " + url);
        //Intent intent = new Intent(ShareActivity.this, ShareDetailActivity.class);
        Intent intent = new Intent(ShareActivity.this, SharePagerActivity.class);
        Bundle b = new Bundle();
        b.putString("imageKey", url);
        b.putString("seqno", seqno);
        b.putString("oriFileName", oriFileName);
        b.putSerializable("prevData", getSerializeData());
        b.putString("searchStDt", searchStDt);
        b.putString("searchEndDt", searchEndDt);
        b.putString("searchText", searchText);
        b.putString("searchType", searchType);
        intent.putExtras(b);
        startActivity(intent);
    }

    /**
     * 핸드폰 Back 버튼 제어
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed called..");
        // your code.
        Intent intent = new Intent(ShareActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private ArrayList<ShareSerializeItem> getSerializeData()
    {
        ArrayList<ShareSerializeItem> prevData = new ArrayList<>();

        for (int i=0; i<adapterList.size(); i++)
        {
            ShareMainItem item = adapterList.get(i);
            ShareSerializeItem st = new ShareSerializeItem();
            st.rgstYmd = item.rgstYmd;
            ArrayList<SerialImageItem> list = new ArrayList<>();

            for (int j=0; j<item.imageList.size(); j++)
            {
                ImageItem a1 = item.imageList.get(j);
                SerialImageItem s1 = new SerialImageItem();
                SerialShareDataItem m1 = new SerialShareDataItem();
                SerialShareDataItem m2 = new SerialShareDataItem();
                SerialShareDataItem m3 = new SerialShareDataItem();
                m1.imageSeqno = a1.item1.imageSeqno;
                m1.logFileName = a1.item1.logFileName;
                m1.oriFileName = a1.item1.oriFileName;
                m1.rgstDeptCd = a1.item1.rgstDeptCd;
                m1.thumbnailFileName = a1.item1.thumbnailFileName;
                m1.rgstYmd = a1.item1.rgstYmd;
                m1.rgstName = a1.item1.rgstName;
                m1.rgstEmpid = a1.item1.rgstEmpid;

                if (a1.item2 != null) {
                    m2.imageSeqno = a1.item2.imageSeqno;
                    m2.logFileName = a1.item2.logFileName;
                    m2.oriFileName = a1.item2.oriFileName;
                    m2.rgstDeptCd = a1.item2.rgstDeptCd;
                    m2.thumbnailFileName = a1.item2.thumbnailFileName;
                    m2.rgstYmd = a1.item2.rgstYmd;
                    m2.rgstName = a1.item2.rgstName;
                    m2.rgstEmpid = a1.item2.rgstEmpid;
                }
                if (a1.item3 != null) {
                    m3.imageSeqno = a1.item3.imageSeqno;
                    m3.logFileName = a1.item3.logFileName;
                    m3.oriFileName = a1.item3.oriFileName;
                    m3.rgstDeptCd = a1.item3.rgstDeptCd;
                    m3.thumbnailFileName = a1.item3.thumbnailFileName;
                    m3.rgstYmd = a1.item3.rgstYmd;
                    m3.rgstName = a1.item3.rgstName;
                    m3.rgstEmpid = a1.item3.rgstEmpid;
                }

                s1.item1 = m1;
                s1.item2 = m2;
                s1.item3 = m3;

                list.add(s1);
            }
            st.imageList = list;
            prevData.add(st);
        }

        return prevData;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        ArrayList<ShareSerializeItem> prevData = new ArrayList<>();

        for (int i=0; i<adapterList.size(); i++)
        {
            ShareMainItem item = adapterList.get(i);
            ShareSerializeItem st = new ShareSerializeItem();
            st.rgstYmd = item.rgstYmd;
            ArrayList<SerialImageItem> list = new ArrayList<>();

            for (int j=0; j<item.imageList.size(); j++)
            {
                ImageItem a1 = item.imageList.get(j);
                SerialImageItem s1 = new SerialImageItem();
                SerialShareDataItem m1 = new SerialShareDataItem();
                SerialShareDataItem m2 = new SerialShareDataItem();
                SerialShareDataItem m3 = new SerialShareDataItem();
                m1.imageSeqno = a1.item1.imageSeqno;
                m1.logFileName = a1.item1.logFileName;
                m1.oriFileName = a1.item1.oriFileName;
                m1.rgstDeptCd = a1.item1.rgstDeptCd;
                m1.thumbnailFileName = a1.item1.thumbnailFileName;
                m1.rgstYmd = a1.item1.rgstYmd;
                m1.rgstName = a1.item1.rgstName;
                m1.rgstEmpid = a1.item1.rgstEmpid;

                if (a1.item2 != null) {
                    m2.imageSeqno = a1.item2.imageSeqno;
                    m2.logFileName = a1.item2.logFileName;
                    m2.oriFileName = a1.item2.oriFileName;
                    m2.rgstDeptCd = a1.item2.rgstDeptCd;
                    m2.thumbnailFileName = a1.item2.thumbnailFileName;
                    m2.rgstYmd = a1.item2.rgstYmd;
                    m2.rgstName = a1.item2.rgstName;
                    m2.rgstEmpid = a1.item2.rgstEmpid;
                }
                if (a1.item3 != null) {
                    m3.imageSeqno = a1.item3.imageSeqno;
                    m3.logFileName = a1.item3.logFileName;
                    m3.oriFileName = a1.item3.oriFileName;
                    m3.rgstDeptCd = a1.item3.rgstDeptCd;
                    m3.thumbnailFileName = a1.item3.thumbnailFileName;
                    m3.rgstYmd = a1.item3.rgstYmd;
                    m3.rgstName = a1.item3.rgstName;
                    m3.rgstEmpid = a1.item3.rgstEmpid;
                }

                s1.item1 = m1;
                s1.item2 = m2;
                s1.item3 = m3;

                list.add(s1);
            }
            st.imageList = list;
            prevData.add(st);
        }
        // bitmap을 제거해야 한다.
        outState.putSerializable("prevData", prevData);
        outState.putString("searchStDt", searchStDt);
        outState.putString("searchEndDt", searchEndDt);
        outState.putString("searchText", searchText);
        outState.putString("searchType", searchType);
        //outState.put
        super.onSaveInstanceState(outState);
    }
}
