package com.itep.test.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.cameraview.CameraView;
import com.itep.test.R;
import com.itep.test.WeakRefHandler;

public class CameraActivity extends Activity {
    private static final String TAG = "camera test";
    private CameraView cameraView1;
    private CameraView cameraView2;
    private CameraView cameraView3;
    private CameraView cameraView4;
    private final int CAMERA_INIT = 0;
    private final int CAMERA_RESULT = 1;
    private boolean cameraExist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraView1 = findViewById(R.id.camera_view1);
        cameraView2 = findViewById(R.id.camera_view2);
        cameraView3 = findViewById(R.id.camera_view3);
        cameraView4 = findViewById(R.id.camera_view4);
        cameraView1.setVisibility(View.VISIBLE);
        cameraView2.setVisibility(View.VISIBLE);
        cameraView3.setVisibility(View.VISIBLE);
        cameraView4.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessage(CAMERA_INIT);
    }

    Handler mHandler = new WeakRefHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case CAMERA_INIT:
                    int count = Camera.getNumberOfCameras();
                    Log.i(TAG, "cameranum：" + count);
                    if (count > 0) {
                        cameraExist = true;
                        setCameraResult("有摄像头" );
                        cameraView1.addCallback(mCameraCallback1);
                        cameraView1.start();
                    } else {
                        cameraExist = false;
                        setCameraResult("没有摄像头");
                    }
                    break;
                case CAMERA_RESULT:
                    Log.e(TAG, message.obj.toString());
                    break;
            }
            return false;
        }
    });

    private void setCameraResult(Object result) {
        Message msg = mHandler.obtainMessage();
        msg.what = CAMERA_RESULT;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    private CameraView.Callback mCameraCallback1 = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
        }

        @Override
        public void onPictureTaken(CameraView cameraView, byte[] data) {
            super.onPictureTaken(cameraView, data);
        }
    };

}
