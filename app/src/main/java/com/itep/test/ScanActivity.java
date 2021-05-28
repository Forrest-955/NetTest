package com.itep.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortFinder;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ScanActivity extends Activity {
    int time;
    int count;
    private TextView tvHint;
    private TextView tvTime;
    private TextView tvSuccess;
    private EditText etSN;
    private TextView tvResult;
    private Button btnGetResult;
    private SerialPortManager mSerialPortManager;
    private String path = Environment.getExternalStoragePublicDirectory("Download") + "/scan.txt";

    String TAG = "scanner";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        tvHint = findViewById(R.id.tvHint);
        tvTime = findViewById(R.id.tvTime);
        tvSuccess = findViewById(R.id.tvSuccess);
        etSN = findViewById(R.id.etSN);
        tvResult = findViewById(R.id.tvResult);
        btnGetResult = findViewById(R.id.btnGetResult);
        btnGetResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = null;
                try {
                    result = readTXT(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tvResult.setText(result);
            }
        });
        initSerialScanner();
    }

    private void initSerialScanner() {
        Device mScanner = null;
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        ArrayList<Device> devices = serialPortFinder.getDevices();
        for (Device device : devices) {
            if (device.getName().contains("ttyACM0")) {
                mScanner = device;
            }
        }
        if (mScanner == null) {
            //没有串口设备
            tvHint.setText("没有串口设备");
            return;
        }
        if (mScanner.getFile().canRead() && mScanner.getFile().canWrite()) {
            Log.e(TAG, "ACM0 exist");
            mSerialPortManager = new SerialPortManager();
            boolean isOpen = mSerialPortManager.openSerialPort(mScanner.getFile(), 9600);
            mSerialPortManager.setOnSerialPortDataListener(new OnSerialPortDataListener() {
                @Override
                public void onDataReceived(final byte[] bytes) {
                    time++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTime.setText(time + "");
                            if (bytes != null) {
                                count++;
                                tvHint.setText("读取成功");
                                tvSuccess.setText(count + "");
                                etSN.setText(new String(bytes));
                                writeToFile(path, "测试次数：" + time + "成功次数：" + count);
//                                close();
                            }
                        }
                    });

                }

                @Override
                public void onDataSent(byte[] bytes) {

                }
            });
        } else {
            tvHint.setText("串口设备没有权限");
        }
    }

    private String readTXT(String path) throws IOException {
        File file = new File(path);
        InputStream inputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        return line;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0:
                    initSerialScanner();
                    break;
            }
            return false;
        }
    });

    private void writeToFile(String path, Object value) {
        File file = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(value.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() {
        mSerialPortManager.closeSerialPort();
        handler.sendEmptyMessageDelayed(0, 2000);
    }
}
