package com.my.finger;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
//import android.view.Menu;
//import android.view.MenuItem;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.my.finger.adapter.DirectoryNodeBinder;
import com.my.finger.adapter.FileNodeBinder;
import com.my.finger.data.DeptItem;
import com.my.finger.data.DeptTopItem;
import com.my.finger.data.ImageItem;
import com.my.finger.data.ShareDataItem;
import com.my.finger.data.ShareMainItem;
import com.my.finger.utils.Constant;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewAdapter;

/**
 * 부서선택 팝업 Activity
 */
public class DeptPopupActivity extends Activity {
    private RecyclerView rv;
    private TreeViewAdapter adapter;
    private Button mbtnSelect;
    private TextView prevSelect = null;
    private final String TAG = "KDN_TAG";
    private String mSelectedDeptCd;
    private String mSelectedDeptNm;
    private List<TreeNode> mNodes = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

         setContentView(R.layout.activity_dept_popup);

        rv = findViewById(R.id.rv);

        mbtnSelect = findViewById(R.id.btnConfirm);
        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnConfirm:
                        deptCodeSelect();
                        break;
                }
            }
        };
        mbtnSelect.setOnClickListener(onClickListener);

        initData();
    }
    private void deptCodeSelect()
    {
        Intent intent = new Intent();
        intent.putExtra("deptCd", mSelectedDeptCd);
        intent.putExtra("deptNm", mSelectedDeptNm);
        setResult(RESULT_OK, intent);
        finish();
    }
    private void initData() {
        // 데이터를 조회한다.
        new Thread() {
            public void run() {
                // All your networking logic should be here
                try{
                    String url = Constant.DEPT_URL;
                    URL urlObj = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);

                    conn.connect();
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
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

                    if (code.equals("OK")) {
                        JSONArray rows = jsonObject.getJSONArray("rows");
                        for (int i = 0; i < rows.length(); i++) {
                            JSONObject object = rows.getJSONObject(i);
                            String deptCd = object.getString("deptCd");
                            String uppoDeptCd = object.getString("uppoDeptCd");
                            String deptNm = object.getString("deptNm");
                            String lev = object.getString("lev");
                            String groupYn = null;
                            try {
                                groupYn = object.getString("groupYn");  // null일 수 있다.
                            }catch(Exception egg)
                            {
                                groupYn = "N";
                            }
                            String childCnt = object.getString("childCnt");
                            Log.d(TAG, "lev : " + lev + ", groupYn : " + groupYn + ", deptCd : " + deptCd + ", uppoDeptCd : "+uppoDeptCd);
                            if (lev.equals("0"))
                            {
                                TreeNode<DeptTopItem> app = new TreeNode<>(new DeptTopItem(deptCd, deptNm));
                                mNodes.add(app);    // List<TreeNode>()
                            }else {
                                for (int t=0; t<mNodes.size(); t++)     // root node
                                {
                                    TreeNode<DeptTopItem> m = mNodes.get(t);
                                    if (lev.equals("1")) {  // level 1
                                        DeptTopItem dt = m.getContent();
                                        if (dt.deptCd.equals(uppoDeptCd)) {
                                            m.addChild(new TreeNode<>(new DeptTopItem(deptCd, deptNm)));
                                            mNodes.set(t, m);
                                            break;
                                        }
                                    }else {     // level 2
                                        if (m.getChildList() == null)
                                        {
                                            Log.d(TAG, "deptCd["+m.getContent().deptCd+"  childlist is null");
                                            continue;
                                        }
                                        List<TreeNode> lt = m.getChildList();
                                        int found = 0;
                                        for (int y=0; y<lt.size(); y++)
                                        {
                                            TreeNode<DeptTopItem> c = lt.get(y);
                                            DeptTopItem dt = c.getContent();
                                            if (dt.deptCd.equals(uppoDeptCd)) {
                                                c.addChild(new TreeNode<>(new DeptTopItem(deptCd, deptNm)));
                                                lt.set(y, c);
                                                m.setChildList(lt);
                                                mNodes.set(t, m);
                                                found = 1;
                                                break;
                                            }
                                        }
                                        if (found == 1) break;
                                    }
                                }
                            }
                        }
                    }else {
                        Log.d(TAG, "data not found...");
                        Toast.makeText(DeptPopupActivity.this, "데이터 조회 에러가 발생하였습니다. 관리자에게 문의바랍니다!!!", Toast.LENGTH_SHORT).show();
                    }
                    conn.disconnect();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            uiDeptTreeDraw();
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

    private void uiDeptTreeDraw()
    {
//        List<TreeNode> nodes = new ArrayList<>();
//        TreeNode<DeptTopItem> app = new TreeNode<>(new DeptTopItem("app", "app"));
//        nodes.add(app);
//        app.addChild(
//                new TreeNode<>(new DeptTopItem("manifests", "manifests"))
//                        .addChild(new TreeNode<>(new DeptItem("AndroidManifest.xml", "AndroidManifest.xml")))
//        );
//        app.addChild(
//                new TreeNode<>(new DeptTopItem("java", "java")).addChild(
//                        new TreeNode<>(new DeptTopItem("tellh", "tellh")).addChild(
//                                new TreeNode<>(new DeptTopItem("com", "com")).addChild(
//                                        new TreeNode<>(new DeptTopItem("recyclertreeview", "recyclertreeview"))
//                                                .addChild(new TreeNode<>(new DeptItem("Dir", "Dir")))
//                                                .addChild(new TreeNode<>(new DeptItem("DirectoryNodeBinder", "DirectoryNodeBinder")))
//                                                .addChild(new TreeNode<>(new DeptItem("File", "File")))
//                                                .addChild(new TreeNode<>(new DeptItem("FileNodeBinder", "FileNodeBinder")))
//                                                .addChild(new TreeNode<>(new DeptItem("TreeViewBinder", "TreeViewBinder")))
//                                )
//                        )
//                )
//        );
//        TreeNode<DeptTopItem> res = new TreeNode<>(new DeptTopItem("res", "res"));
//        nodes.add(res);
//        res.addChild(
//                new TreeNode<>(new DeptTopItem("layout", "layout")) // lock this TreeNode
//                        .addChild(new TreeNode<>(new DeptItem("activity_main.xml", "activity_main.xml")))
//                        .addChild(new TreeNode<>(new DeptItem("item_dir.xml", "item_dir.xml")))
//                        .addChild(new TreeNode<>(new DeptItem("item_file.xml", "item_file.xml")))
//        );
//        res.addChild(
//                new TreeNode<>(new DeptTopItem("mipmap", "mipmap"))
//                        .addChild(new TreeNode<>(new DeptItem("ic_launcher.png", "ic_laucher.png")))
//        );

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TreeViewAdapter(mNodes, Arrays.asList(new FileNodeBinder(), new DirectoryNodeBinder()));
        // whether collapse child nodes when their parent node was close.
//        adapter.ifCollapseChildWhileCollapseParent(true);
        adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
            @Override
            public boolean onClick(TreeNode node, RecyclerView.ViewHolder holder) {
                if (!node.isLeaf()) {
                    //Update and toggle the node.
                    onToggle(!node.isExpand(), holder);
//                    if (!node.isExpand()) {
//                    }else {
//                        Log.d(TAG, node.toString());
//                    }
////                        adapter.collapseBrotherNode(node);
                }else {
                    // select Item
                    DeptTopItem item = (DeptTopItem) node.getContent();
                    mSelectedDeptCd = item.deptCd;
                    mSelectedDeptNm = item.deptName;
                    if (prevSelect != null)
                    {
                        prevSelect.setBackgroundColor(Color.rgb(255,255,255));
                    }
                    DirectoryNodeBinder.ViewHolder viewHolder = (DirectoryNodeBinder.ViewHolder) holder;
                    TextView tv = viewHolder.getTvName();
                    tv.setBackgroundColor(Color.rgb(154,186, 206));
                    Log.d(TAG, "deptCd: " + item.deptCd + ", deptNm : " + item.deptName);
                    prevSelect = tv;
                }
                return false;
            }

            @Override
            public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
                DirectoryNodeBinder.ViewHolder dirViewHolder = (DirectoryNodeBinder.ViewHolder) holder;
                final ImageView ivArrow = dirViewHolder.getIvArrow();
                int rotateDegree = isExpand ? 90 : -90;
                ivArrow.animate().rotationBy(rotateDegree)
                        .start();
            }
        });
        rv.setAdapter(adapter);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.layout.menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.id_action_close_all:
//                adapter.collapseAll();
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
