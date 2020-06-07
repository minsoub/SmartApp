package com.my.finger;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.my.finger.utils.CommonUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements LifecycleObserver  {
    private static String TAG = "KDN_TAG";
    private static CameraPreview surfaceView;
    private SurfaceHolder holder;
    private static Camera mCamera;
    private int RESULT_PERMISSIONS = 100;
    public static CameraActivity getInstance;
    public static boolean layout_visible = false;
    public static boolean flash_on_off = false;
    public static byte[] mData;
    public static String tempFileName;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_camera);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 안드로이드 6.0 이상 버전에서는 CAMERA 권한 허가를 요청한다.
        requestPermissionCamera();
    }
    /**
     * 핸드폰 Back 버튼 제어
     */
    @Override
    public void onBackPressed() {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "onBackPressed called..");
                mCamera.stopPreview();
                // your code.
                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    //@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "initializeCamera called...");
        mCamera = null;
        if (mCamera == null) {
            setInit();
            //mCamera = Camera.open();
        }
    }

    // @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "releaseCamera called...");
//        if (mCamera != null) {
//            mCamera.release();
//            mCamera = null;
//        }
    }

    public static void setByteImage(byte[] data)
    {
        FileOutputStream outStream = null;
        try {
            Log.d(TAG, "photo: " + mContext.getFilesDir().getAbsolutePath());
            File path = new File(mContext.getFilesDir().getAbsolutePath() + "/kdnapp");
            if (!path.exists()) {
                Log.d(TAG, "디렉토리 생성 : " + mContext.getFilesDir().getAbsolutePath() + "/kdnapp");
                path.mkdirs();
            }

            String saveName = CommonUtil.getCurrentDateFormat();;
            String fileName = String.format("%s.jpg", saveName);
            File outputFile = new File(path, fileName);
            Log.d(TAG, "파일 Write");
            outStream = new FileOutputStream(outputFile);
            outStream.write(data);
            outStream.flush();
            outStream.close();
            Log.d(TAG, "파일 Write End");
            tempFileName = outputFile.getAbsolutePath();  // path+"/"+fileName;
            Log.d(TAG, "파일 생성완료 : ["+data.length+"] =>" + tempFileName);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "파일 FileNotFoundException : " + tempFileName);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "파일 IOException : " + tempFileName);
            e.printStackTrace();
        }
    }
    public static void movePreivewImage()
    {
        Intent intent = new Intent(mContext, ImagePreivewActivity.class);
        Bundle b = new Bundle();
        b.putString("photo", tempFileName);
        intent.putExtras(b);
        mContext.startActivity(intent);
    }

    public static Camera getCamera(){
        return mCamera;
    }

    private void setInit(){
        Log.d(TAG, "setInit called...");
        getInstance = this;

        // 카메라 객체를 R.layout.activity_main의 레이아웃에 선언한 SurfaceView에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
        mCamera = Camera.open();

        setContentView(R.layout.activity_camera);

        // SurfaceView를 상속받은 레이아웃을 정의한다.
        surfaceView = (CameraPreview) findViewById(R.id.preview);

        // SurfaceView 정의 - holder와 Callback을 정의한다.
        holder = surfaceView.getHolder();
        holder.addCallback(surfaceView);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        surfaceView.setHolder(holder);
        Log.d(TAG, "setInit called...");


        //setContentView(R.layout.activity_camera);
        ImageView.OnClickListener onClickListener = new ImageView.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mCamera == null) {
                    Log.d(TAG, "Camera is null");
                    mCamera.open();
                }else {
                    Log.d(TAG, "Camera is not null");
                }

                Camera.Parameters params;
                switch (view.getId())
                {
                    case R.id.btnZoom1:
                        params=mCamera.getParameters();
                        params.setZoom(0);
                        mCamera.setParameters(params);
                        break;
                    case R.id.btnZoom2:
                        params=mCamera.getParameters();
                        params.setZoom(10);
                        mCamera.setParameters(params);
                        break;
                    case R.id.btnZoom3:
                        params=mCamera.getParameters();
                        params.setZoom(20);
                        mCamera.setParameters(params);
                        break;
                    case R.id.btnShut:
                        // get an image from the camera
                        CheckBox checkBox = (CheckBox) findViewById(R.id.set_check);
                        if (checkBox.isChecked())
                        {
                            surfaceView.setName(true);
                        }else {
                            surfaceView.setName(false);
                        }
                        surfaceView.takePicture();
                        break;
                    case R.id.btnFlash:
                        ImageView flash = (ImageView)findViewById(R.id.btnFlash);
                        if (flash_on_off == false) {
                            flashLightOn();
                            flash.setImageResource(R.mipmap.btn_flash_on);
                            flash_on_off = true;
                        }else {
                            flashLightOff();
                            flash.setImageResource(R.mipmap.btn_flash_off);
                            flash_on_off = false;
                        }
                        break;
                    case R.id.btnSet:
                        LinearLayout layout = (LinearLayout)findViewById(R.id.set);
                        if (layout_visible == false) {
                            layout.setVisibility(View.VISIBLE);
                            layout_visible = true;
                        }else {
                            layout.setVisibility(View.INVISIBLE);
                            layout_visible = false;
                        }
                        break;
                }
            }
        };
        // 각 버튼에 대한 이벤트 리스터를 지정한다.
        ImageView btnZoom1 = (ImageView)findViewById(R.id.btnZoom1);
        btnZoom1.setOnClickListener(onClickListener);
        ImageView btnZoom2 = (ImageView)findViewById(R.id.btnZoom2);
        btnZoom2.setOnClickListener(onClickListener);
        ImageView btnZoom3 = (ImageView)findViewById(R.id.btnZoom3);
        btnZoom3.setOnClickListener(onClickListener);
        ImageView btnShut = (ImageView)findViewById(R.id.btnShut);
        btnShut.setOnClickListener(onClickListener);
        ImageView btnFlash = (ImageView)findViewById(R.id.btnFlash);
        btnFlash.setOnClickListener(onClickListener);
        ImageView btnSet = (ImageView)findViewById(R.id.btnSet);
        btnSet.setOnClickListener(onClickListener);

        mContext = this;

        Bundle b = getIntent().getExtras();
        if(b != null) {
            if (b.getString("checked").equals("Y"))
            {
                // checkbox check
                CheckBox checkBox = (CheckBox) findViewById(R.id.set_check);
                checkBox.setChecked(true);
                surfaceView.setName(true);
            }
        }
    }

    /**
     * Flash On
     */
    public void flashLightOn() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                //mCamera = Camera.open();
                Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(p);
                //mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOn()",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Flash Off
     */
    public void flashLightOff() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception flashLightOff",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public boolean requestPermissionCamera(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(CameraActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_PERMISSIONS);
            }else {
                setInit();
            }
        }else{  // version 6 이하일때
            setInit();
            return true;
        }

        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (RESULT_PERMISSIONS == requestCode) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허가시
                setInit();
            } else {
                // 권한 거부시
            }
            return;
        }

    }
}
