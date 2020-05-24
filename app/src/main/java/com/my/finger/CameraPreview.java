package com.my.finger;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Surface;
import android.util.Log;

import com.my.finger.utils.CommonUtil;
import com.my.finger.utils.DataBaseUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 카메라 Preview 구현 클래스
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "CameraPreview";
    private Camera mCamera;
    public List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private Context context;
    private SurfaceHolder mHolder;
    private int mDisplayOrientation;
    private Camera.CameraInfo mCameraInfo;
    private DataBaseUtil mDB;
    private boolean isChecked = false;



    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; // Camera.CameraInfo.CAMERA_FACING_FRONT

    public CameraPreview(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.context = context;
        mCamera = CameraActivity.getCamera();
        if (mCamera == null)
        {
            mCamera = Camera.open();
        }
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

        mDB = new DataBaseUtil(context);
    }

    /**
     * 사진 이름 지정 체크 여부
     * ture인 경우 사진 이름을 지정할 수 있도록 다음 페이지로 연결해야 한다.
     * @param check
     */
    public void setName(boolean check)
    {
        isChecked = check;
    }
    public void setHolder(SurfaceHolder holder)
    {
        mHolder = holder;
    }

    /**
     * SurfaceView 생성시 호출
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            // 카메라 객체를 사용할 수 있게 연결한다.
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            // Camera info
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(CAMERA_FACING, cameraInfo);

            mCameraInfo = cameraInfo;
            mDisplayOrientation = CameraActivity.getInstance.getWindowManager().getDefaultDisplay().getRotation();

            int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
            mCamera.setDisplayOrientation(orientation);

            // 카메라 설정
            Camera.Parameters parameters = mCamera.getParameters();

            // 자동 포커스 설정
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // set the focus mode
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                Camera.Size optimalSize;
                optimalSize = getOptimalPreviewSize(sizes, mPreviewSize.width, mPreviewSize.height);
                parameters.setPictureSize(optimalSize.width, optimalSize.height);

                // set Camera parameters
                mCamera.setParameters(parameters);
            }
            mCamera.setPreviewDisplay(surfaceHolder);

            // 카메라 미리보기를 시작한다.
            mCamera.startPreview();
        }catch(Exception ex)
        {

        }
    }

    /**
     * SurfaceView의 크기가 바뀌면 호출
     * @param surfaceHolder
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 카메라 화면을 회전 할 때의 처리
        if (surfaceHolder.getSurface() == null){
            // 프리뷰가 존재하지 않을때
            return;
        }
        // 프리뷰를 다시 설정한다.
        try {
            mCamera .stopPreview();

            Camera.Parameters parameters = mCamera .getParameters();

            int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
            mCamera.setDisplayOrientation(orientation);

            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                Log.d(TAG, "Camera preview started.");
            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }

//            // 화면 회전시 사진 회전 속성을 맞추기 위해 설정한다.
//            int rotation = CameraActivity.getInstance.getWindowManager().getDefaultDisplay().getRotation();
//            if (rotation == Surface.ROTATION_0) {
//                mCamera .setDisplayOrientation(90);
//                parameters.setRotation(90);
//            }else if(rotation == Surface.ROTATION_90){
//                mCamera .setDisplayOrientation(0);
//                parameters.setRotation(0);
//            }else if(rotation == Surface.ROTATION_180){
//                mCamera .setDisplayOrientation(270);
//                parameters.setRotation(270);
//            }else{
//                mCamera .setDisplayOrientation(180);
//                parameters.setRotation(180);
//            }
//
//            // 변경된 화면 넓이를 설정한다.
//            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//            mCamera .setParameters(parameters);
//
//            // 새로 변경된 설정으로 프리뷰를 시작한다
//            mCamera .setPreviewDisplay(surfaceHolder);
//            mCamera .startPreview();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * SurfaceView 가 종료시 호출
     * @param surfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mCamera != null){
            // 카메라 미리보기를 종료한다.
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 화면이 회전할 때 화면 사이즈를 구한다.
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        if (changed && getChildCount() > 0) {
//            final View child = getChildAt(0);
//
//            final int width = r - l;
//            final int height = b - t;
//
//            int previewWidth = width;
//            int previewHeight = height;
//            if (mPreviewSize != null) {
//                previewWidth = mPreviewSize.width;
//                previewHeight = mPreviewSize.height;
//            }
//
//            // Center the child SurfaceView within the parent.
//            if (width * previewHeight > height * previewWidth) {
//                final int scaledChildWidth = previewWidth * height / previewHeight;
//                child.layout((width - scaledChildWidth) / 2, 0,
//                        (width + scaledChildWidth) / 2, height);
//            } else {
//                final int scaledChildHeight = previewHeight * width / previewWidth;
//                child.layout(0, (height - scaledChildHeight) / 2,
//                        width, (height + scaledChildHeight) / 2);
//            }
//        }
//    }

    public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
    /**
     * 안드로이드 디바이스 방향에 맞는 카메라 프리뷰를 화면에 보여주기 위해 계산합니다.
     */
    public static int calculatePreviewOrientation(Camera.CameraInfo info, int rotation) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    /**
     * 사직을 찍어서 저장한다.
     */
    public void takePicture() {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {

        }
    };
    Camera.PictureCallback rawCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera)
        {

        }
    };
    // 참고 : http://stackoverflow.com/q/37135675
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            //이미지의 너비와 높이 결정
            //int w = camera.getParameters().getPreviewSize().width; // getPictureSize().width;
            //int h = camera.getParameters().getPreviewSize().height;  // getPictureSize().height;

            int w = camera.getParameters().getPictureSize().width;
            int h = camera.getParameters().getPictureSize().height;

            //int orientation = CameraActivity.getInstance.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);


            //byte array를 bitmap으로 변환
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray( data, 0, data.length, options);


            //이미지를 디바이스 방향으로 회전
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap =  Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

            //bitmap을 byte array로 변환
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] currentData = stream.toByteArray();

            if (isChecked == true)
            {
                mCamera.stopPreview();
                mCamera = null;

                CameraActivity.setByteImage(currentData);
                CameraActivity.movePreivewImage();
            }else {
                //파일로 저장
                new SaveImageTask().execute(currentData);
            }
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;
            try {

                File path = new File(context.getFilesDir().getAbsolutePath() + "/kdnapp"); // Environment.getDataDirectory().getAbsolutePath() + "/kdnapp");
                if (!path.exists()) {
                    path.mkdirs();
                }

                String saveName = CommonUtil.getCurrentDateFormat();
                String fileName = String.format("%s.jpg", saveName);  // System.currentTimeMillis());
                File outputFile = new File(path, fileName);

                outStream = new FileOutputStream(outputFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                // 데이터베이스에 이미지 정보를 등록해야 된다.
                SQLiteDatabase db = mDB.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("id", Long.parseLong(saveName));
                values.put("filename", outputFile.getAbsolutePath());  // path+"/"+fileName);
                values.put("sts", "N");
                long id = db.insert("tb_files", null, values);
                Log.d(TAG, "insert id : " + id);
                        db.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to "
                        + outputFile.getAbsolutePath());
                try {
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();
                    Log.d(TAG, "Camera preview started.");
                } catch (Exception e) {
                    Log.d(TAG, "Error starting camera preview: " + e.getMessage());
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    }
}
