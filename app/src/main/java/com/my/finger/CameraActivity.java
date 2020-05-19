package com.my.finger;

import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class CameraActivity extends AppCompatActivity {

    private static CameraPreview surfaceView;
    private SurfaceHolder holder;
    private static Camera mCamera;
    private int RESULT_PERMISSIONS = 100;
    public static CameraActivity getInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_camera);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // 안드로이드 6.0 이상 버전에서는 CAMERA 권한 허가를 요청한다.
        requestPermissionCamera();

        //setContentView(R.layout.activity_camera);

        ImageView.OnClickListener onClickListener = new ImageView.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
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
                        params.setZoom(5);
                        mCamera.setParameters(params);
                        break;
                    case R.id.btnZoom3:
                        params=mCamera.getParameters();
                        params.setZoom(20);
                        mCamera.setParameters(params);
                        break;
                    case R.id.btnShut:
                        // get an image from the camera
                        surfaceView.takePicture();
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
    }
    public static Camera getCamera(){
        return mCamera;
    }

    private void setInit(){
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
