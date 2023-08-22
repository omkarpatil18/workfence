package com.workfence.activities;


import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.workfence.R;
import com.workfence.mrs.CameraConnectionFragment;
import com.workfence.mrs.ImageUtils;
import com.workfence.mrs.LegacyCameraConnectionFragment;
import com.workfence.mrs.MaskClassifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ClassifierActivity extends AppCompatActivity implements ImageReader.OnImageAvailableListener, Camera.PreviewCallback {
    /**
     * Classifier and camera elements
     **/
    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    protected int previewWidth = 0;
    private String predText;
    private TextView predView;
    protected int previewHeight = 0;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean useCamera2API;
    private Integer maxScan = 25;
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Rect boundingRect;
    private Runnable imageConverter;
    private FaceDetector faceDetector;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final float TEXT_SIZE_DIP = 10;
    private ProgressBar scanProgress;
    private Bitmap rgbFrameBitmap = null;
    private long lastProcessingTimeMs;
    private Integer sensorOrientation;
    private MaskClassifier classifier;
    private ArrayList<Integer> preds;
    /**
     * Input image size of the model along x axis.
     */
    private int imageSizeX;
    /**
     * Input image size of the model along y axis.
     */
    private int imageSizeY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_classifier);
        predView = findViewById(R.id.predText);
        scanProgress = findViewById(R.id.scanProgress);
        scanProgress.setMax(maxScan);
        boundingRect = new Rect(80,100,420,450);
        preds = new ArrayList<>();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Log.d("Display", String.valueOf(convertPixelsToDp(displayMetrics.widthPixels,this)));
        faceDetector = FaceDetection.getClient(new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build());
        Log.d("FaceDetector", "Face Detector Loaded!");
        if (hasPermission()) {
            Log.i("Test:", "Hello!");
            setFragment();
        } else {
            Log.i("Test:", "Permission!");
            requestPermission();
        }
        preds = new ArrayList<>();
    }

    protected int getLayoutId() {
        return R.layout.fragment_camera_connection;
    }

    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());

        recreateClassifier();
        if (classifier == null) {
            Log.e("Classifier", "No classifier on preview!");
            return;
        }

        sensorOrientation = rotation - getScreenOrientation();
        Log.i("Classifier", "Camera orientation relative to screen canvas:" + sensorOrientation.toString());

        Log.i("Classifier", "Initializing at size" + previewWidth + "x" + previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Runnable p = new Runnable() {
            @Override
            public void run() {
                if(faceDetector!=null){
                    InputImage image = InputImage.fromBitmap(rgbFrameBitmap,sensorOrientation);
                    faceDetector.process(image).addOnSuccessListener(
                            new OnSuccessListener<List<Face>>() {
                                @Override
                                public void onSuccess(List<Face> faces) {
                                    if(faces.size()==1){
                                        Rect r = faces.get(0).getBoundingBox();
                                        Log.d("Face:",r.toString());
                                        if(boundingRect.contains(r)){
                                            if (classifier != null) {
                                                final long startTime = SystemClock.uptimeMillis();
                                                final Integer results =
                                                        classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);
                                                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                                                Log.d("Classifier", "Detect: " + results.toString());
                                                preds.add(1 - results);
                                                predText = (results==0)?"Masked":"Not Masked";

                                                ClassifierActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        predView.setText(predText);
                                                        scanProgress.setProgressTintList((results==0)?ColorStateList.valueOf(Color.GREEN):ColorStateList.valueOf(Color.RED));
                                                        scanProgress.setProgress(preds.size());
                                                    }
                                                });
                                            }
                                        }
                                        else{
                                            ClassifierActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    predView.setText("Please place your face in scanner properly!");
                                                    scanProgress.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                                                }
                                            });
                                        }

                                    }
                                    else if (faces.size()==0){
                                        ClassifierActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                predView.setText("No Face!");
                                                scanProgress.setProgressTintList(ColorStateList.valueOf(Color.RED));
                                            }
                                        });
                                    }
                                    else{

                                        ClassifierActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                predView.setText("More Than One Face!");
                                                scanProgress.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                                            }
                                        });
                                    }
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Error!",e.toString());
                                        }
                                    });

                }

                readyForNextImage();
            }
        };
        runInBackground(p);
    }

    private void stopPrediction() {
        faceDetector.close();
        Integer sum = 0;
        for (Integer d : preds)
            sum += d;
        if (sum >= maxScan * 0.65) {
            Intent returnIntent = new Intent();
            setResult(1,returnIntent);
            finish();
        } else {
            Intent returnIntent = new Intent();
            setResult(0,returnIntent);
            finish();
        }
    }

    private void recreateClassifier() {
        if (classifier != null) {
            Log.d("Classifier", "Closing classifier.");
            classifier.close();
            classifier = null;
        }

        try {
            Log.d("Classifier",
                    "Creating classifier");
            classifier = new MaskClassifier(this);
        } catch (IOException e) {
            Log.e("Classifier Error", e.toString());
        }

        // Updates the input image size.
        imageSizeX = classifier.getImageSizeX();
        imageSizeY = classifier.getImageSizeY();
    }

    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            Log.w("Classifier", "Dropping frame!");
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
            Log.e("Classifier", "Exception!");
            return;
        }

        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
                    }
                };

        postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        camera.addCallbackBuffer(bytes);
                        isProcessingFrame = false;
                    }
                };
        processImage();
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (preds.size() < maxScan) {
            if (previewWidth == 0 || previewHeight == 0) {
                return;
            }
            if (rgbBytes == null) {
                rgbBytes = new int[previewWidth * previewHeight];
            }
            try {
                final Image image = reader.acquireLatestImage();

                if (image == null) {
                    return;
                }

                if (isProcessingFrame) {
                    image.close();
                    return;
                }
                isProcessingFrame = true;
                Trace.beginSection("imageAvailable");
                final Image.Plane[] planes = image.getPlanes();
                fillBytes(planes, yuvBytes);
                yRowStride = planes[0].getRowStride();
                final int uvRowStride = planes[1].getRowStride();
                final int uvPixelStride = planes[1].getPixelStride();

                imageConverter =
                        new Runnable() {
                            @Override
                            public void run() {
                                ImageUtils.convertYUV420ToARGB8888(
                                        yuvBytes[0],
                                        yuvBytes[1],
                                        yuvBytes[2],
                                        previewWidth,
                                        previewHeight,
                                        yRowStride,
                                        uvRowStride,
                                        uvPixelStride,
                                        rgbBytes);
                            }
                        };

                postInferenceCallback =
                        new Runnable() {
                            @Override
                            public void run() {
                                image.close();
                                isProcessingFrame = false;
                            }
                        };

                processImage();

            } catch (final Exception e) {
                Log.e("Classifier", "Exception!");
                Trace.endSection();
                return;
            }
            Trace.endSection();
        }
        else{
            reader.close();
            stopPrediction();
        }
    }

    @Override
    public synchronized void onStart() {
        Log.d("Classifier", "onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        Log.d("Classifier", "onResume " + this);
        super.onResume();

        preds = new ArrayList<>();
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        Log.d("Classifier", "onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            Log.e(e.toString(), "Exception!");
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        Log.d("Classifier", "onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        Log.d("Classifier", "onDestroy " + this);
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (allPermissionsGranted(grantResults)) {
                setFragment();
            } else {
                requestPermission();
            }
        }
    }

    private static boolean allPermissionsGranted(final int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(
                        ClassifierActivity.this,
                        "Camera permission is required for this demo",
                        Toast.LENGTH_LONG)
                        .show();
            }

            Log.i("Test:", "Permission Now!");
            requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
        }
    }

    // Returns true if the device supports the required hardware level, or better.
    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    private String chooseCamera() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We use a front facing camera in this sample.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
//                useCamera2API =
//                        (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
//                                || isHardwareLevelSupported(
//                                characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                useCamera2API = true;
                Log.i("Classifier", "Camera API lv2?:" + useCamera2API);
                return cameraId;
            }
        } catch (CameraAccessException e) {
            Log.e(e.toString(), "Not allowed to access camera");
        }

        return null;
    }

    protected void setFragment() {
        String cameraId = chooseCamera();

        Fragment fragment;
        if (useCamera2API) {
            CameraConnectionFragment camera2Fragment =
                    CameraConnectionFragment.newInstance(
                            new CameraConnectionFragment.ConnectionCallback() {
                                @Override
                                public void onPreviewSizeChosen(final Size size, final int rotation) {
                                    previewHeight = size.getHeight();
                                    previewWidth = size.getWidth();
                                    ClassifierActivity.this.onPreviewSizeChosen(size, rotation);
                                }
                            },
                            this,
                            getLayoutId(),
                            getDesiredPreviewFrameSize());

            camera2Fragment.setCamera(cameraId);
            fragment = camera2Fragment;
        } else {
            fragment =
                    new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
        }

        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                Log.d("Classifier", "Initializing buffer" + i + " at size" + buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }
    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}