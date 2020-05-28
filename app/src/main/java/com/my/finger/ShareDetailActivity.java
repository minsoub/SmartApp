package com.my.finger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;

import com.my.finger.utils.DataBaseUtil;
import com.my.finger.utils.TouchImageView;

import java.io.IOException;
import java.io.InputStream;

public class ShareDetailActivity extends AppCompatActivity {
    private String mKey;
    private String mTitle;
    private TouchImageView view;
    private ProgressDialog dialog = null;
    private final String TAG = "KDN_TAG";

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
            mKey = b.getString("imageKey");
            mTitle = b.getString("title");

            // Image 출력
            InputStream in = null;
            try {
                in = new java.net.URL(mKey).openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            Bitmap bitmap = BitmapFactory.decodeStream(in);
            Bitmap newbitMap = Bitmap.createScaledBitmap(bitmap, size.x, size.y, true);

            view = findViewById(R.id.previewImage);
            view.setImageBitmap(newbitMap);
            TextView txt = findViewById(R.id.send_title);
            txt.setText(mTitle);
        }
    }

    /**
     * 핸드폰 Back 버튼 제어
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed called..");

        // your code.
        Intent intent = new Intent(ShareDetailActivity.this, ShareActivity.class);
        startActivity(intent);
    }
}
