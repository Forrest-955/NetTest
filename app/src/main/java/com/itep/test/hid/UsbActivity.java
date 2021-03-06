package com.itep.test.hid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.itep.mt.common.sys.SysCommand;
import com.itep.test.HomeActivity;
import com.itep.test.R;
import com.itep.test.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class UsbActivity extends Activity {
    private static final String TAG = "usb test";
    private int readCount;
    private int readErr;
    private int copyCount;
    private int copyErr;
    private String path;
    private String resPath;
    private Button btn_usb;
    private Button btn_result;
    private Button btn_close;
    private TextView tv_msg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        btn_result = findViewById(R.id.btn_get_res);
        btn_usb = findViewById(R.id.btn_usb_trans);
        btn_close = findViewById(R.id.btn_close);
        tv_msg = findViewById(R.id.tv_msg);
        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = null;
                try {
                    result = Utils.readTXT(resPath + "/wusb.txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tv_msg.setText(result);
            }
        });
        btn_usb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_usb.setEnabled(false);
                btn_result.setEnabled(false);
                copyCount = 0;
                copyErr = 0;
                handler.removeMessages(0);
                handler.sendEmptyMessage(0);
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        resPath = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath();
        path = SysCommand.runCmdForResult("ls /storage/|grep -vE \"[emulated|self]\" | busybox sed \"s:^:/storage/: \" | head -1");
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (copyCount < 100000) {
                                long startTime = System.currentTimeMillis();
                                Log.e(TAG, "copy" + copyCount);
                                if (!cmdCopyFile(resPath + "/usb.zip", path + "/usb1.zip")) {
                                    copyErr++;
                                }
                                long costTime = System.currentTimeMillis() - startTime;

                                copyCount++;
                                setText("??????????????????" + copyCount);
                                Log.e(TAG, "?????????" + copyCount);
                                double speed = 989.9 / costTime * 1000;
                                String result = copyCount + "????????????????????????" + copyErr + ",????????????" + String.format("%.2f", speed) + "M/s";
                                Utils.writeToFile(resPath + "/wusb.txt", result);
                                setText(result);
                                handler.sendEmptyMessageDelayed(0, 1000);
                            } else {
                                handler.sendEmptyMessageDelayed(2, 1000);
                            }
                        }
                    }).start();
                    break;
                case 2:
                    btn_usb.setEnabled(true);
                    btn_result.setEnabled(true);
                    break;
                case 3:
                    String msgStr = (String) message.obj;
                    tv_msg.setText(msgStr);
                    break;
            }
            return false;
        }
    });

    private void setText(String s) {
        Message msg = new Message();
        msg.what = 3;
        msg.obj = s;
        handler.sendMessage(msg);
    }

    public boolean cmdCopyFile(String oldPath, String newPath) {
        return Utils.runCmd("cp " + oldPath + " " + newPath);
    }

    private String readStringFromFile(String fileName){
        StringBuilder sb = new StringBuilder("");
        try {
            //?????????????????????
            FileInputStream inputStream = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            //??????????????????
            while(len > 0){
                sb.append(new String(buffer,0,len));
                //?????????????????????buffer???
                len = inputStream.read(buffer);
            }
            //???????????????
            inputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return sb.toString();
    }
}
