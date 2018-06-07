package com.andeqa.andeqa.camera;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateCollectionActivity;
import com.andeqa.andeqa.explore.ExploreFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity  implements
        SurfaceHolder.Callback, View.OnClickListener {
    private static final String TAG = ExploreFragment.class.getSimpleName();
    private static final String CAMERA_PATH = "camera image";
    private static final String CAMERA_VIDEO = "camera video";
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Handler customHandler = new Handler();
    int flag = 0;
    private File tempFile = null;
    private Camera.PictureCallback jpegCallback;
    private static  final int MAX_VIDEO_SIZE_UPLOAD = 25; //MB
    private MediaRecorder mediaRecorder;
    private RunTimePermissions runTimePermission;

    @Bind(R.id.changeCameraImageView)ImageView mChangeCameraImageView;
    @Bind(R.id.flashOnOffImageView)ImageView mFlashOnOffImageView;
    @Bind({R.id.imageSurfaceView})SurfaceView mSurfaceImageView;
    @Bind(R.id.imageCaptureImageView)ImageView mImageCaptureImageView;
    @Bind(R.id.countTextView) TextView mCountTextView;
    @Bind(R.id.galleryImageView)ImageView mGalleryImageView;
    @Bind(R.id.createCollectionImageView)ImageView mCreateCollectionImageView;
    @Bind(R.id.toolbar)Toolbar toolbar;
//    @Bind(R.id.recordVideoImageView)ImageView mRecordVideoImageView;

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (myOrientationEventListener != null)
                myOrientationEventListener.enable();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }


    private File folder = null;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (runTimePermission != null) {
            runTimePermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        runTimePermission = new RunTimePermissions(this);
        runTimePermission.requestPermission(new String[]{Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new RunTimePermissions.RunTimePermissionListener() {

            @Override
            public void permissionGranted() {
                // First we need to check availability of play services
                initControls();

                identifyOrientationEvents();

                //create a folder to get image
                folder = new File(Environment.getExternalStorageDirectory() + "/Andeqa");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                //capture image on callback
                captureImageCallback();
                //
                if (camera != null) {
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mFlashOnOffImageView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void permissionDenied() {
            }
        });


    }


    private void cancelSavePicTaskIfNeed() {
        if (savePicTask != null && savePicTask.getStatus() == AsyncTask.Status.RUNNING) {
            savePicTask.cancel(true);
        }
    }

    private void cancelSaveVideoTaskIfNeed() {
        if (saveVideoTask != null && saveVideoTask.getStatus() == AsyncTask.Status.RUNNING) {
            saveVideoTask.cancel(true);
        }
    }

    private SavePicTask savePicTask;

    private class SavePicTask extends AsyncTask<Void, Void, String> {
        private byte[] data;
        private int rotation = 0;

        public SavePicTask(byte[] data, int rotation) {
            this.data = data;
            this.rotation = rotation;
        }

        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                return saveToSDCard(data, rotation);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            activeCameraCapture();

            tempFile = new File(result);

            Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
            intent.putExtra(CameraActivity.CAMERA_PATH, tempFile.toString());
            startActivity(intent);

        }
    }

    public String saveToSDCard(byte[] data, int rotation) throws IOException {
        String imagePath = "";
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int reqHeight = metrics.heightPixels;
            int reqWidth = metrics.widthPixels;

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (rotation != 0) {
                Matrix mat = new Matrix();
                mat.postRotate(rotation);
                Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                if (bitmap != bitmap1) {
                    bitmap.recycle();
                }
                imagePath = getSavePhotoLocal(bitmap1);
                if (bitmap1 != null) {
                    bitmap1.recycle();
                }
            } else {
                imagePath = getSavePhotoLocal(bitmap);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    private String getSavePhotoLocal(Bitmap bitmap) {
        String path = "";
        try {
            OutputStream output;
            File file = new File(folder.getAbsolutePath(), "wc" + System.currentTimeMillis() + ".jpg");
            try {
                output = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.flush();
                output.close();
                path = file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    private void captureImageCallback() {

        surfaceHolder = mSurfaceImageView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {

                refreshCamera();

                cancelSavePicTaskIfNeed();
                savePicTask = new SavePicTask(data, getPhotoRotation());
                savePicTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

            }
        };
    }

    private class SaveVideoTask extends AsyncTask<Void, Void, Void> {

        File thumbFilename;

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(CameraActivity.this);
            progressDialog.setMessage("Processing a video...");
            progressDialog.show();
            super.onPreExecute();
            mImageCaptureImageView.setOnTouchListener(null);
            mCountTextView.setVisibility(View.GONE);
            mChangeCameraImageView.setVisibility(View.VISIBLE);
            mFlashOnOffImageView.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                try {
                    myOrientationEventListener.enable();

                    customHandler.removeCallbacksAndMessages(null);

                    mediaRecorder.stop();
                    releaseMediaRecorder();

                    tempFile = new File(folder.getAbsolutePath() + "/" + mediaFileName + ".mp4");
                    thumbFilename = new File(folder.getAbsolutePath(), "t_" + mediaFileName + ".jpeg");
                    generateVideoThmb(tempFile.getPath(), thumbFilename);


                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog != null) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            if (tempFile != null && thumbFilename != null)
                onVideoSendDialog(tempFile.getAbsolutePath(), thumbFilename.getAbsolutePath());
        }
    }

    private int mPhotoAngle = 90;

    private void identifyOrientationEvents() {

        myOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int iAngle) {

                final int iLookup[] = {0, 0, 0, 90, 90, 90, 90, 90, 90, 180, 180, 180, 180, 180, 180, 270, 270, 270, 270, 270, 270, 0, 0, 0}; // 15-degree increments
                if (iAngle != ORIENTATION_UNKNOWN) {

                    int iNewOrientation = iLookup[iAngle / 15];
                    if (iOrientation != iNewOrientation) {
                        iOrientation = iNewOrientation;
                        if (iOrientation == 0) {
                            mOrientation = 90;
                        } else if (iOrientation == 270) {
                            mOrientation = 0;
                        } else if (iOrientation == 90) {
                            mOrientation = 180;
                        }

                    }
                    mPhotoAngle = normalize(iAngle);
                }
            }
        };

        if (myOrientationEventListener.canDetectOrientation()) {
            myOrientationEventListener.enable();
        }

    }

    private void initControls() {

        mediaRecorder = new MediaRecorder();
        mCountTextView.setVisibility(View.GONE);
        mChangeCameraImageView.setOnClickListener(this);
        mFlashOnOffImageView.setOnClickListener(this);
        mCreateCollectionImageView.setOnClickListener(this);
//        mRecordVideoImageView.setOnClickListener(this);
        mGalleryImageView.setOnClickListener(this);

        activeCameraCapture();



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelSavePicTaskIfNeed();
    }

    @Override
    public void onClick(View v) {
        final Intent intent;
        switch (v.getId()) {
            case R.id.flashOnOffImageView:
                flashToggle();
                break;
            case R.id.galleryImageView:
                intent = new Intent(CameraActivity.this, GalleryActivity.class);
                startActivity(intent);
                break;
            case R.id.createCollectionImageView:
                intent = new Intent(CameraActivity.this, CreateCollectionActivity.class);
                startActivity(intent);
                break;
            case R.id.changeCameraImageView:
                camera.stopPreview();
                camera.release();

                if (flag == 0) {
                    mFlashOnOffImageView.setVisibility(View.GONE);
                    flag = 1;
                } else {
                    mFlashOnOffImageView.setVisibility(View.VISIBLE);
                    flag = 0;

                }
                surfaceCreated(surfaceHolder);
                refreshCamera();

                break;
            default:
                break;
        }
    }

    private void flashToggle() {

        if (flashType == 1) {

            flashType = 2;
        } else if (flashType == 2) {

            flashType = 3;
        } else if (flashType == 3) {

            flashType = 1;
        }
        refreshCamera();
    }

    private void captureImage() {
        camera.takePicture(null, null, jpegCallback);
        inActiveCameraCapture();
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = new MediaRecorder();
        }
    }


    public void refreshCamera() {

        if (surfaceHolder.getSurface() == null) {
            return;
        }else {
            try {
                camera.stopPreview();
                Camera.Parameters param = camera.getParameters();

                if (flag == 0) {
                    if (flashType == 1) {
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        mFlashOnOffImageView.setImageResource(R.drawable.ic_flash_auto);
                    } else if (flashType == 2) {
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        Camera.Parameters params = null;
                        if (camera != null) {
                            params = camera.getParameters();

                            if (params != null) {
                                List<String> supportedFlashModes = params.getSupportedFlashModes();

                                if (supportedFlashModes != null) {
                                    if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                        param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                    } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                                        param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                    }
                                }
                            }
                        }
                        mFlashOnOffImageView.setImageResource(R.drawable.ic_flash_on);
                    } else if (flashType == 3) {
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mFlashOnOffImageView.setImageResource(R.drawable.ic_flash_off);
                    }
                }

                refrechCameraPriview(param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void refrechCameraPriview(Camera.Parameters param) {
        try {
            camera.setParameters(param);
            setCameraDisplayOrientation(0);

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCameraDisplayOrientation(int cameraId) {

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        if (Build.MODEL.equalsIgnoreCase("Nexus 6") && flag == 1) {
            rotation = Surface.ROTATION_180;
        }
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
            result = (360 - result) % 360; // compensate the mirror

        } else {
            result = (info.orientation - degrees + 360) % 360;

        }

        camera.setDisplayOrientation(result);

    }

    //------------------SURFACE CREATED FIRST TIME--------------------//

    int flashType = 1;

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        try {
            if (flag == 0) {
                camera = Camera.open(0);
            } else {
                camera = Camera.open(1);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }

        try {
            Camera.Parameters param;
            param = camera.getParameters();
            List<Camera.Size> sizes = param.getSupportedPreviewSizes();
            //get diff to get perfact preview sizes
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;
            long diff = (height * 1000 / width);
            long cdistance = Integer.MAX_VALUE;
            int idx = 0;
            for (int i = 0; i < sizes.size(); i++) {
                long value = (long) (sizes.get(i).width * 1000) / sizes.get(i).height;
                if (value > diff && value < cdistance) {
                    idx = i;
                    cdistance = value;
                }
                Log.e("camera", "width=" + sizes.get(i).width + " height=" + sizes.get(i).height);
            }
            Log.e("camera", "INDEX:  " + idx);
            Camera.Size cs = sizes.get(idx);
            param.setPreviewSize(cs.width, cs.height);
            param.setPictureSize(cs.width, cs.height);
            camera.setParameters(param);
            setCameraDisplayOrientation(0);

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

            if (flashType == 1) {
                param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                mFlashOnOffImageView.setImageResource(R.drawable.ic_flash_auto);

            } else if (flashType == 2) {
                param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                Camera.Parameters params = null;
                if (camera != null) {
                    params = camera.getParameters();

                    if (params != null) {
                        List<String> supportedFlashModes = params.getSupportedFlashModes();

                        if (supportedFlashModes != null) {
                            if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                                param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                            }
                        }
                    }
                }
                mFlashOnOffImageView.setImageResource(R.drawable.ic_flash_on);

            } else if (flashType == 3) {
                param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlashOnOffImageView.setImageResource(R.drawable.ic_flash_off);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        try {
            camera.stopPreview();
            camera.release();
            camera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        refreshCamera();
    }

    //------------------SURFACE OVERRIDE METHIDS END--------------------//

    private long timeInMilliseconds = 0L, startTime = SystemClock.uptimeMillis(), updatedTime = 0L, timeSwapBuff = 0L;
    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hrs = mins / 60;

            secs = secs % 60;
            mCountTextView.setText(String.format("%02d", mins) + ":" + String.format("%02d", secs));
            customHandler.postDelayed(this, 0);

        }

    };

    private void scaleUpAnimation() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mImageCaptureImageView, "scaleX", 2f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mImageCaptureImageView, "scaleY", 2f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        scaleDownX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                View p = (View) mImageCaptureImageView.getParent();
                p.invalidate();
            }
        });
        scaleDown.start();
    }

    private void scaleDownAnimation() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mImageCaptureImageView, "scaleX", 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mImageCaptureImageView, "scaleY", 1f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        scaleDownX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                View p = (View) mImageCaptureImageView.getParent();
                p.invalidate();
            }
        });
        scaleDown.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {

            if (customHandler != null)
                customHandler.removeCallbacksAndMessages(null);

            releaseMediaRecorder();       // if you are using MediaRecorder, release it first

            if (myOrientationEventListener != null)
                myOrientationEventListener.enable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SaveVideoTask saveVideoTask = null;

    private void activeCameraCapture() {
        if (mImageCaptureImageView != null) {
            mImageCaptureImageView.setAlpha(1.0f);
//            mRecordVideoImageView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
////                    hintTextView.setVisibility(View.INVISIBLE);
//                    try {
//                        if (prepareMediaRecorder()) {
//                            myOrientationEventListener.disable();
//                            mediaRecorder.start();
//                            startTime = SystemClock.uptimeMillis();
//                            customHandler.postDelayed(updateTimerThread, 0);
//                        } else {
//                            return false;
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    mCountTextView.setVisibility(View.VISIBLE);
//                    mChangeCameraImageView.setVisibility(View.GONE);
//                    mFlashOnOffImageView.setVisibility(View.GONE);
//
//                    mRecordVideoImageView.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v, MotionEvent event) {
//                            if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
//                                return true;
//                            }
//                            if (event.getAction() == MotionEvent.ACTION_UP) {
//
//
//                                cancelSaveVideoTaskIfNeed();
//                                saveVideoTask = new SaveVideoTask();
//                                saveVideoTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//
//                                return true;
//                            }
//                            return true;
//
//                        }
//                    });
//                    return true;
//                }
//
//            });
            mImageCaptureImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isSpaceAvailable()) {
                        captureImage();
                    } else {
                        Toast.makeText(CameraActivity.this, "Memory is not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    public void onVideoSendDialog(final String videopath, final String thumbPath) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (videopath != null) {
                    File fileVideo = new File(videopath);
                    long fileSizeInBytes = fileVideo.length();
                    long fileSizeInKB = fileSizeInBytes / 1024;
                    long fileSizeInMB = fileSizeInKB / 1024;
                    if (fileSizeInMB > MAX_VIDEO_SIZE_UPLOAD) {
                        new android.support.v7.app.AlertDialog.Builder(CameraActivity.this)
                                .setMessage(getString(R.string.file_limit_size_upload_format, MAX_VIDEO_SIZE_UPLOAD))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {

                        Intent mIntent = new Intent(CameraActivity.this, PreviewActivity.class);
                        mIntent.putExtra("PATH", videopath.toString());
                        mIntent.putExtra("THUMB", thumbPath.toString());
                        mIntent.putExtra(CameraActivity.CAMERA_VIDEO, "video");
                        startActivity(mIntent);

                    }
                }
            }
        });
    }

    private void inActiveCameraCapture() {
        if (mImageCaptureImageView != null) {
            mImageCaptureImageView.setAlpha(0.5f);
            mImageCaptureImageView.setOnClickListener(null);
        }
    }

    //--------------------------CHECK FOR MEMORY -----------------------------//

    public int getFreeSpacePercantage() {
        int percantage = (int) (freeMemory() * 100 / totalMemory());
        int modValue = percantage % 5;
        return percantage - modValue;
    }

    public double totalMemory() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getBlockCount() * (double) stat.getBlockSize();
        return sdAvailSize / 1073741824;
    }

    public double freeMemory() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        return sdAvailSize / 1073741824;
    }

    public boolean isSpaceAvailable() {
        if (getFreeSpacePercantage() >= 1) {
            return true;
        } else {
            return false;
        }
    }
    //-------------------END METHODS OF CHECK MEMORY--------------------------//


    private String mediaFileName = null;

    @SuppressLint("SimpleDateFormat")
    protected boolean prepareMediaRecorder() throws IOException {

        mediaRecorder = new MediaRecorder(); // Works well
        camera.stopPreview();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        if (flag == 1) {
            mediaRecorder.setProfile(CamcorderProfile.get(1, CamcorderProfile.QUALITY_HIGH));
        } else {
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        }
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        mediaRecorder.setOrientationHint(mOrientation);

        if (Build.MODEL.equalsIgnoreCase("Nexus 6") && flag == 1) {

            if (mOrientation == 90) {
                mediaRecorder.setOrientationHint(mOrientation);
            } else if (mOrientation == 180) {
                mediaRecorder.setOrientationHint(0);
            } else {
                mediaRecorder.setOrientationHint(180);
            }

        } else if (mOrientation == 90 && flag == 1) {
            mediaRecorder.setOrientationHint(270);
        } else if (flag == 1) {
            mediaRecorder.setOrientationHint(mOrientation);
        }
        mediaFileName = "wc_vid_" + System.currentTimeMillis();
        mediaRecorder.setOutputFile(folder.getAbsolutePath() + "/" + mediaFileName + ".mp4"); // Environment.getExternalStorageDirectory()

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {

            public void onInfo(MediaRecorder mr, int what, int extra) {
                // TODO Auto-generated method stub

                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {

                    long downTime = 0;
                    long eventTime = 0;
                    float x = 0.0f;
                    float y = 0.0f;
                    int metaState = 0;
                    MotionEvent motionEvent = MotionEvent.obtain(
                            downTime,
                            eventTime,
                            MotionEvent.ACTION_UP,
                            0,
                            0,
                            metaState
                    );

                    mImageCaptureImageView.dispatchTouchEvent(motionEvent);

                    Toast.makeText(CameraActivity.this, "You reached to Maximum(25MB) video size.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        mediaRecorder.setMaxFileSize(1000 * 25 * 1000);

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            releaseMediaRecorder();
            e.printStackTrace();
            return false;
        }
        return true;

    }

    OrientationEventListener myOrientationEventListener;
    int iOrientation = 0;
    int mOrientation = 90;

    public void generateVideoThmb(String srcFilePath, File destFile) {
        try {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(srcFilePath, 120);
            FileOutputStream out = new FileOutputStream(destFile);
            ThumbnailUtils.extractThumbnail(bitmap, 200, 200).compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int normalize(int degrees) {
        if (degrees > 315 || degrees <= 45) {
            return 0;
        }

        if (degrees > 45 && degrees <= 135) {
            return 90;
        }

        if (degrees > 135 && degrees <= 225) {
            return 180;
        }

        if (degrees > 225 && degrees <= 315) {
            return 270;
        }

        throw new RuntimeException("Error....");
    }

    private int getPhotoRotation() {
        int rotation;
        int orientation = mPhotoAngle;

        Camera.CameraInfo info = new Camera.CameraInfo();
        if (flag == 0) {
            Camera.getCameraInfo(0, info);
        } else {
            Camera.getCameraInfo(1, info);
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }
        return rotation;
    }


}