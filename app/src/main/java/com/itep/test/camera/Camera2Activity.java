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
    private ImageReader mImageReader;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private int mState = STATE_PREVIEW;
    private static final int MAX_PREVIEW_WIDTH = 640;
    private static final int MAX_PREVIEW_HEIGHT = 480;
    private Context context;
    private File file;
    private DualCamera dualCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        context = this;
        mTextureView1 = findViewById(R.id.textureView1);
        mTextureView2 = findViewById(R.id.textureView2);
        mTextureView3 = findViewById(R.id.textureView3);
        mTextureView4 = findViewById(R.id.textureView4);
        dualCamera = getDualCamera(context);
        startBackgroundThread();
        initPermission();

        initTextureView1();
//        initTextureView2();
//        initTextureView3();
//        initTextureView4();
    }

    private void initTextureView1() {
        if (mTextureView1.isAvailable()) {
            initCameraV2(3);
        } else {
            mTextureView1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
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


    /**
     * 创建相机设备状态回调
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            createCameraPreviewSession();
//            config(camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private static DualCamera getDualCamera(Context context) {
        DualCamera dualCamera = new DualCamera();
        //获取管理类
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        assert manager != null;
        try {
            //获取所有逻辑ID
            String[] cameraIdList = manager.getCameraIdList();

            //获取逻辑摄像头下拥有多个物理摄像头的类 作为双镜类
            for (String id : cameraIdList) {
                try {
                    CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(id);
                    Set<String> physicalCameraIds = cameraCharacteristics.getPhysicalCameraIds();
                    Log.d(TAG, "逻辑ID：" + id + " 下的物理ID: " + Arrays.toString(physicalCameraIds.toArray()));
                    if (physicalCameraIds.size() >= 2) {
                        dualCamera.setLoginCameraID(id);
                        Object[] objects = physicalCameraIds.toArray();
                        //获取前两个物理摄像头作为双镜头
                        dualCamera.setPhysicsCameraID1(String.valueOf(objects[0]));
                        dualCamera.setPhysicsCameraID2(String.valueOf(objects[1]));
                        return dualCamera;
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 配置摄像头参数
     * @param cameraDevice
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void config(CameraDevice cameraDevice){
        try {
            //构建输出参数  在参数中设置物理摄像头
            List<OutputConfiguration> configurations = new ArrayList<>();
            final CaptureRequest.Builder mPreViewBuidler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //配置第一个物理摄像头
            SurfaceTexture texture = mTextureView1.getSurfaceTexture();
            OutputConfiguration outputConfiguration = new OutputConfiguration(new Surface(texture));
            outputConfiguration.setPhysicalCameraId(dualCamera.getPhysicsCameraID1());
            configurations.add(outputConfiguration);
            mPreViewBuidler.addTarget(Objects.requireNonNull(outputConfiguration.getSurface()));

            //配置第2个物理摄像头
            SurfaceTexture texture2 = mTextureView2.getSurfaceTexture();
            OutputConfiguration outputConfiguration2 = new OutputConfiguration(new Surface(texture2));
            outputConfiguration2.setPhysicalCameraId(dualCamera.getPhysicsCameraID2());
            configurations.add(outputConfiguration2);
            mPreViewBuidler.addTarget(Objects.requireNonNull(outputConfiguration2.getSurface()));

            //注册摄像头
            SessionConfiguration sessionConfiguration = new SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    configurations,
                    AsyncTask.SERIAL_EXECUTOR,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                mCaptureSession = cameraCaptureSession;
                                cameraCaptureSession.setRepeatingRequest(mPreViewBuidler.build(), null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    }
            );
            cameraDevice.createCaptureSession(sessionConfiguration);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

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
            Log.e(TAG, cameraList[cameraId] + "");
            if (cameraList.length == 0) {
                return;
            }
            //创建图像数据流
            mImageReader = ImageReader.newInstance(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), file));
                }
            }, mBackgroundHandler);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //开启相机
            mCameraManager.openCamera(cameraList[cameraId], mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建相机会话Capture回调，用于拍照预览
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
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
     * 创建预览通道
     */
    private void createCameraPreviewSession() {
        try {
            //设置预览参数
            SurfaceTexture texture = mTextureView1.getSurfaceTexture();
            texture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
            Surface surface = new Surface(texture);
            //创建预览请求构造器
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            //创建Session，新建会话状态回调StateCallback
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (null == mCameraDevice) {
                        return;
                    }
                    mCaptureSession = session;
                    try {
                        //构造器设置自动对焦参数
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        //创建预览请求
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mState = STATE_PREVIEW;
                        //发起预览请求
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler);

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

    /**
     * 开启后台进程
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBackgroundThread();
        releaseCamera();
    }

    /**
     * 关闭后台进程
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }
}
