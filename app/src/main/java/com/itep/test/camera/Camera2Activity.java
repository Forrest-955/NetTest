package com.itep.test.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.CameraMetadata;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.itep.mt.common.sys.SysCommand;
import com.itep.mt.common.sys.SysVoice;
import com.itep.test.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Camera2Activity extends Activity {
    private static final String TAG = "cameratest";
    private TextureView mTextureView1;
    private TextureView mTextureView2;
    private TextureView mTextureView3;
    private TextureView mTextureView4;
    private static final int STATE_PREVIEW = 0;
    private ImageReader mImageReader1;
    private ImageReader mImageReader2;
    private ImageReader mImageReader3;
    private ImageReader mImageReader4;
    private CameraCaptureSession mCaptureSession1;
    private CameraCaptureSession mCaptureSession2;
    private CameraCaptureSession mCaptureSession3;
    private CameraCaptureSession mCaptureSession4;
    private CameraDevice mCameraDevice1;
    private CameraDevice mCameraDevice2;
    private CameraDevice mCameraDevice3;
    private CameraDevice mCameraDevice4;
    private Handler mBackgroundHandler1;
    private Handler mBackgroundHandler2;
    private Handler mBackgroundHandler3;
    private Handler mBackgroundHandler4;
    private HandlerThread mBackgroundThread1;
    private HandlerThread mBackgroundThread2;
    private HandlerThread mBackgroundThread3;
    private HandlerThread mBackgroundThread4;
    private int mState = STATE_PREVIEW;
    private static final int MAX_PREVIEW_WIDTH = 640;
    private static final int MAX_PREVIEW_HEIGHT = 480;
    private Context context;
    private File file;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        context = this;
        mTextureView1 = findViewById(R.id.textureView1);
        mTextureView2 = findViewById(R.id.textureView2);
        mTextureView3 = findViewById(R.id.textureView3);
        mTextureView4 = findViewById(R.id.textureView4);
//        startBackgroundThread();
        startBackgroundThread1();
        startBackgroundThread2();
        startBackgroundThread3();
        startBackgroundThread4();
        initPermission();

        initTextureView1();
        initTextureView2();
        initTextureView3();
        initTextureView4();
    }

    private void initTextureView1() {
        if (mTextureView1.isAvailable()) {
            initCameraV2(0);
        } else {
            mTextureView1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    initCameraV2(0);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }
            });
        }
    }

    private void initTextureView2() {
        if (mTextureView2.isAvailable()) {
            initCameraV2(1);
        } else {
            mTextureView2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    initCameraV2(1);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }
            });
        }
    }

    private void initTextureView3() {
        if (mTextureView3.isAvailable()) {
            initCameraV2(2);
        } else {
            mTextureView3.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    initCameraV2(2);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }
            });
        }
    }

    private void initTextureView4() {
        if (mTextureView4.isAvailable()) {
            initCameraV2(3);
        } else {
            mTextureView4.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    initCameraV2(3);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }
            });
        }
    }

    private final CameraDevice.StateCallback mStateCallback1 = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice1 = camera;
            createPreviewSession1(camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
        }
    };

    private final CameraDevice.StateCallback mStateCallback2 = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice2 = camera;
            createPreviewSession2(camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
        }
    };

    private final CameraDevice.StateCallback mStateCallback3 = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice3 = camera;
            createPreviewSession3(camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
        }
    };

    private final CameraDevice.StateCallback mStateCallback4 = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice4 = camera;
            createPreviewSession4(camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
        }
    };

    /**
     * 获取权限
     */
    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                SysCommand.runCmd("pm grant com.itep.mt.infoviewer android.permission.WRITE_EXTERNAL_STORAGE");
                SysCommand.runCmd("pm grant com.itep.mt.infoviewer android.permission.READ_EXTERNAL_STORAGE");
                SysCommand.runCmd("pm grant com.itep.mt.infoviewer android.permission.CAMERA");
            }
        }
    }

    /**
     * 初始化摄像头
     */
    private void initCameraV2(int cameraId) {
        //创建相机管理器
        CameraManager mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            //获取相机ID
            String[] cameraList = mCameraManager.getCameraIdList();
            if (cameraList.length == 0) {
                return;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //开启相机
            switch (cameraId) {
                case 0:
                    mCameraManager.openCamera(cameraList[cameraId], mStateCallback1, mBackgroundHandler1);
                    break;
                case 1:
                    mCameraManager.openCamera(cameraList[cameraId], mStateCallback2, mBackgroundHandler2);
                    break;
                case 2:
                    mCameraManager.openCamera(cameraList[cameraId], mStateCallback3, mBackgroundHandler3);
                    break;
                case 3:
                    mCameraManager.openCamera(cameraList[cameraId], mStateCallback4, mBackgroundHandler4);
                    break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback1 = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {

            switch (mState) {
                case STATE_PREVIEW: {

                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback2 = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {

            switch (mState) {
                case STATE_PREVIEW: {

                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback3 = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {

            switch (mState) {
                case STATE_PREVIEW: {

                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback4 = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {

            switch (mState) {
                case STATE_PREVIEW: {

                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    /**
     * 创建预览Session
     */
    private void createPreviewSession1(final CameraDevice device) {
        try {
            //设置预览参数
            SurfaceTexture texture = mTextureView1.getSurfaceTexture();
            texture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
            Surface surface = new Surface(texture);
            //创建预览请求构造器
            final CaptureRequest.Builder builder = mCameraDevice1.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            //创建Session
            mCameraDevice1.createCaptureSession(Arrays.asList(surface, mImageReader1.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession1 = session;
                    try {
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        CaptureRequest request = builder.build();
                        mState = STATE_PREVIEW;
                        mCaptureSession1.setRepeatingRequest(request, mCaptureCallback1, mBackgroundHandler1);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler1);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建预览Session
     */
    private void createPreviewSession2(final CameraDevice device) {
        try {
            //设置预览参数
            SurfaceTexture texture = mTextureView2.getSurfaceTexture();
            texture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
            Surface surface = new Surface(texture);
            //创建预览请求构造器
            final CaptureRequest.Builder builder = mCameraDevice2.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            //创建Session
            mCameraDevice2.createCaptureSession(Arrays.asList(surface, mImageReader2.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession2 = session;
                    try {
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        CaptureRequest request = builder.build();
                        mState = STATE_PREVIEW;
                        mCaptureSession2.setRepeatingRequest(request, mCaptureCallback2, mBackgroundHandler2);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler2);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建预览Session
     */
    private void createPreviewSession3(final CameraDevice device) {
        try {
            //设置预览参数
            SurfaceTexture texture = mTextureView3.getSurfaceTexture();
            texture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
            Surface surface = new Surface(texture);
            //创建预览请求构造器
            final CaptureRequest.Builder builder = mCameraDevice3.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            //创建Session
            mCameraDevice3.createCaptureSession(Arrays.asList(surface, mImageReader3.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession3 = session;
                    try {
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        CaptureRequest request = builder.build();
                        mState = STATE_PREVIEW;
                        mCaptureSession3.setRepeatingRequest(request, mCaptureCallback3, mBackgroundHandler3);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler3);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建预览Session
     */
    private void createPreviewSession4(final CameraDevice device) {
        try {
            //设置预览参数
            SurfaceTexture texture = mTextureView4.getSurfaceTexture();
            texture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
            Surface surface = new Surface(texture);
            //创建预览请求构造器
            final CaptureRequest.Builder builder = mCameraDevice4.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            //创建Session
            mCameraDevice4.createCaptureSession(Arrays.asList(surface, mImageReader4.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession4 = session;
                    try {
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        CaptureRequest request = builder.build();
                        mState = STATE_PREVIEW;
                        mCaptureSession4.setRepeatingRequest(request, mCaptureCallback4, mBackgroundHandler4);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler4);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片
     */
    private class ImageSaver implements Runnable {
        private final Image mImage;
        private final File mFile;

        ImageSaver(Image mImage, File mFile) {
            this.mImage = mImage;
            this.mFile = mFile;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
    }

    private void startBackgroundThread1() {
        mBackgroundThread1 = new HandlerThread("CameraBackground1");
        mBackgroundThread1.start();
        mBackgroundHandler1 = new Handler(mBackgroundThread1.getLooper());
        mImageReader1 = ImageReader.newInstance(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, ImageFormat.JPEG, 2);
        mImageReader1.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mBackgroundHandler1.post(new ImageSaver(reader.acquireNextImage(), file));
            }
        }, mBackgroundHandler1);
    }

    private void startBackgroundThread2() {
        mBackgroundThread2 = new HandlerThread("CameraBackground2");
        mBackgroundThread2.start();
        mBackgroundHandler2 = new Handler(mBackgroundThread2.getLooper());
        mImageReader2 = ImageReader.newInstance(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, ImageFormat.JPEG, 2);
        mImageReader2.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mBackgroundHandler1.post(new ImageSaver(reader.acquireNextImage(), file));
            }
        }, mBackgroundHandler1);
    }

    private void startBackgroundThread3() {
        mBackgroundThread3 = new HandlerThread("CameraBackground3");
        mBackgroundThread3.start();
        mBackgroundHandler3 = new Handler(mBackgroundThread3.getLooper());
        mImageReader3 = ImageReader.newInstance(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, ImageFormat.JPEG, 2);
        mImageReader3.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mBackgroundHandler1.post(new ImageSaver(reader.acquireNextImage(), file));
            }
        }, mBackgroundHandler1);
    }

    private void startBackgroundThread4() {
        mBackgroundThread4 = new HandlerThread("CameraBackground4");
        mBackgroundThread4.start();
        mBackgroundHandler4 = new Handler(mBackgroundThread4.getLooper());
        mImageReader4 = ImageReader.newInstance(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, ImageFormat.JPEG, 2);
        mImageReader4.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mBackgroundHandler1.post(new ImageSaver(reader.acquireNextImage(), file));
            }
        }, mBackgroundHandler1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBackgroundThread1();
        stopBackgroundThread2();
        stopBackgroundThread3();
        stopBackgroundThread4();
        releaseCamera();
    }

    /**
     * 关闭后台进程
     */
    private void stopBackgroundThread1() {
        mBackgroundThread1.quitSafely();
        try {
            mBackgroundThread1.join();
            mBackgroundThread1 = null;
            mBackgroundHandler1 = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭后台进程
     */
    private void stopBackgroundThread2() {
        mBackgroundThread2.quitSafely();
        try {
            mBackgroundThread2.join();
            mBackgroundThread2 = null;
            mBackgroundHandler2 = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 关闭后台进程
     */
    private void stopBackgroundThread3() {
        mBackgroundThread3.quitSafely();
        try {
            mBackgroundThread3.join();
            mBackgroundThread3 = null;
            mBackgroundHandler3 = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 关闭后台进程
     */
    private void stopBackgroundThread4() {
        mBackgroundThread4.quitSafely();
        try {
            mBackgroundThread4.join();
            mBackgroundThread4 = null;
            mBackgroundHandler4 = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        if (null != mCaptureSession1) {
            mCaptureSession1.close();
            mCaptureSession1 = null;
        }
        if (null != mCameraDevice1) {
            mCameraDevice1.close();
            mCameraDevice1 = null;
        }
        if (null != mCaptureSession2) {
            mCaptureSession2.close();
            mCaptureSession2 = null;
        }
        if (null != mCameraDevice2) {
            mCameraDevice2.close();
            mCameraDevice2 = null;
        }
        if (null != mCaptureSession3) {
            mCaptureSession3.close();
            mCaptureSession3 = null;
        }
        if (null != mCameraDevice3) {
            mCameraDevice3.close();
            mCameraDevice3 = null;
        }
        if (null != mCaptureSession4) {
            mCaptureSession4.close();
            mCaptureSession4 = null;
        }
        if (null != mCameraDevice4) {
            mCameraDevice4.close();
            mCameraDevice4 = null;
        }
        if (null != mImageReader1) {
            mImageReader1.close();
            mImageReader1 = null;
        }
        if (null != mImageReader2) {
            mImageReader2.close();
            mImageReader2 = null;
        }
        if (null != mImageReader3) {
            mImageReader3.close();
            mImageReader3 = null;
        }
        if (null != mImageReader4) {
            mImageReader4.close();
            mImageReader4 = null;
        }
    }
}
