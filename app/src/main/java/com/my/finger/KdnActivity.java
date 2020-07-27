package com.my.finger;

import android.app.Activity;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.my.finger.utils.CommonUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 사진 촬영 메인 페이지
 *
 */
public class KdnActivity extends AppCompatActivity implements SensorEventListener { // LifecycleObserver  {
    private static String TAG = "KDN_TAG";
    private static KdnPreview surfaceView;
    private SurfaceHolder holder;
    private static android.hardware.Camera mCamera;
    private int RESULT_PERMISSIONS = 100;
    public static KdnActivity getInstance;
    public static boolean layout_visible = false;
    public static boolean flash_on_off = false;
    public static byte[] mData;
    public static String tempFileName;
    private static Context mContext;
    private static int zoom = 0;
    private static String init = "0";
    private static int mOrientation;

    private SensorManager mSensorManager;
    private Sensor mRotationSensor;

    private static final int SENSOR_DELAY = 500 * 1000; // 500ms
    private static final int RADIAN_TO_DEGREE = -57;

    private static TextView seekText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            init = b.get("main").toString();
        }
        //setContentView(R.layout.activity_camera);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY);

        setInit();
        // 안드로이드 6.0 이상 버전에서는 CAMERA 권한 허가를 요청한다.
        // requestPermissionCamera();
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
                Intent intent = new Intent(KdnActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    //@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @Override
    public void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume initializeCamera called...[" + init + "]");
            mCamera = null;
            if (mCamera == null) {
                //requestPermissionCamera();
                if (init.equals("0")) {
                    Intent intent = new Intent(KdnActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    setInit();
                    init = "0";
                }
            }
        }catch(Exception ex) {
            ex.printStackTrace();
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

    /**
     * 이미지 갭쳐한 데이터를 파일로 저장한다.
     *
     * @param data
     */
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

    public static android.hardware.Camera getCamera(){
        return mCamera;
    }

    private void setInit(){
        Log.d(TAG, "setInit called...");
        getInstance = this;

        // 카메라 객체를 R.layout.activity_main의 레이아웃에 선언한 SurfaceView에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
        mCamera = android.hardware.Camera.open();

        setContentView(R.layout.activity_camera);

        // SurfaceView를 상속받은 레이아웃을 정의한다.
        surfaceView = (KdnPreview) findViewById(R.id.preview);

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

                android.hardware.Camera.Parameters params;
                switch (view.getId())
                {
//                    case R.id.btnZoom1:
//                        params=mCamera.getParameters();
//                        params.setZoom(0);
//                        mCamera.setParameters(params);
//
//                        break;
//                    case R.id.btnZoom2:
//                        params=mCamera.getParameters();
//                        params.setZoom(15);
//                        mCamera.setParameters(params);
//                        break;
//                    case R.id.btnZoom3:
//                        params=mCamera.getParameters();
//                        params.setZoom(20);
//                        mCamera.setParameters(params);
//                        break;
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
//        ImageView btnZoom1 = (ImageView)findViewById(R.id.btnZoom1);
//        btnZoom1.setOnClickListener(onClickListener);
//        ImageView btnZoom2 = (ImageView)findViewById(R.id.btnZoom2);
//        btnZoom2.setOnClickListener(onClickListener);
//        ImageView btnZoom3 = (ImageView)findViewById(R.id.btnZoom3);
//        btnZoom3.setOnClickListener(onClickListener);
        ImageView btnShut = (ImageView)findViewById(R.id.btnShut);
        btnShut.setOnClickListener(onClickListener);
        ImageView btnFlash = (ImageView)findViewById(R.id.btnFlash);
        btnFlash.setOnClickListener(onClickListener);
        ImageView btnSet = (ImageView)findViewById(R.id.btnSet);
        btnSet.setOnClickListener(onClickListener);

        seekText = (TextView)findViewById(R.id.seek_title);

        SeekBar seekBar = (SeekBar)findViewById(R.id.zoombar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            android.hardware.Camera.Parameters params;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double data = 1.0 + (double)(progress/10.0);
                setText(String.valueOf(data));
                params=mCamera.getParameters();
                params.setZoom(seekBar.getProgress());
                mCamera.setParameters(params);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double data = 1.0 + (double)(seekBar.getProgress()/10.0);
                setText(String.valueOf(data));
                params=mCamera.getParameters();
                params.setZoom(seekBar.getProgress());
                mCamera.setParameters(params);
            }
        });

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

    public void setText(String data)
    {
        seekText.setText("X "+data);
    }

    /**
     * Flash On
     */
    public void flashLightOn() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                //mCamera = Camera.open();
                android.hardware.Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
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
                android.hardware.Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
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
        Log.d(TAG, "requestPermissionCamera called...");
        if(sdkVersion >= Build.VERSION_CODES.M) {
            Log.d(TAG, "requestPermissionCamera called1...");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "requestPermissionCamera3 called...");
                ActivityCompat.requestPermissions(KdnActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_PERMISSIONS);
            }else {
                Log.d(TAG, "requestPermissionCamera4 called...");
                setInit();
            }
        }else{  // version 6 이하일때
            Log.d(TAG, "requestPermissionCamera5 called...");
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
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        Log.d(TAG , "onConfigurationChanged");
//        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){//세로 전환시
//            mOrientation = 90;
//            Log.d(TAG , "Configuration.ORIENTATION_PORTRAIT");
//        }else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){ //가로전환시
//            Log.d(TAG, "Configuration.ORIENTATION_LANDSCAPE");
//            mOrientation = 180;
//        }else{
//            mOrientation = 90;
//        }
//    }
//
    public int getOrientation() {
        return mOrientation;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mRotationSensor) {
            if (event.values.length > 4) {
                float[] truncatedRotationVector = new float[4];
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                checkOrientation(truncatedRotationVector);
            } else {
                checkOrientation(event.values);
            }
        }
    }

    private void checkOrientation(float[] vectors) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        int axisX = SensorManager.AXIS_X;
        int axisZ = SensorManager.AXIS_Z;
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisZ, adjustedRotationMatrix);
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);
        float azimuch = orientation[0] * RADIAN_TO_DEGREE;
        float pitch = orientation[1] * RADIAN_TO_DEGREE;
        float roll = orientation[2] * RADIAN_TO_DEGREE;

//        ((TextView)findViewById(R.id.azimuth)).setText("Azimuch: "+azimuch);
//        ((TextView)findViewById(R.id.pitch)).setText("Pitch: "+pitch);
//        ((TextView)findViewById(R.id.roll)).setText("Roll: "+roll);
        if(Math.abs(55) > pitch) {
            calculateOrientation((int) roll, (int) pitch);
            //((TextView) findViewById(R.id.orientation)).setText(calculateOrientation((int) roll, (int) pitch));


        }
    }

    private String calculateOrientation(int roll, int pitch) {
        if((-120 < roll && roll < -60) || (60 < roll && roll <120)){
            if (-120 < roll && roll < -60) mOrientation = 180;
            else mOrientation = 0;

            //surfaceView.setRotation(mOrientation);
            return "LANDSCAPE " + roll + "/" + pitch;
        }else if(( -30 < roll && roll < 30) || (150 < roll && roll < 180) || (-180 < roll && roll < -150)){
            mOrientation = 90;
            //surfaceView.setRotation(mOrientation);
            return "PORTRAIT " + roll + "/" + pitch;
        }else{
            return "UNKNOWN " + roll + "/" + pitch;
        }
    }
}
