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
import com.itep.mt.common.util.CommunicationUtil;
import com.itep.test.HomeActivity;
import com.itep.test.R;
import com.itep.test.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class UartActivity extends Activity {
    private static final String TAG = "uart test";
    private int copyCount;
    private int copyErr;
    private String path;
    private String resPath;
    private Button btn_uart;
    private Button btn_result;
    private Button btn_close;
    private TextView tv_msg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart);
        btn_result = findViewById(R.id.btn_get_res);
        btn_uart = findViewById(R.id.btn_uart_trans);
        btn_close = findViewById(R.id.btn_close);
        tv_msg = findViewById(R.id.tv_msg);
        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = null;
                try {
                    result = Utils.readTXT(resPath + "/wuart.txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tv_msg.setText(result);
            }
        });
        btn_uart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_uart.setEnabled(false);
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
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (copyCount < 1000000) {
                                long startTime = System.currentTimeMillis();
                                Log.e(TAG, "copy" + copyCount);
                                if (!uartIsLoopDevice()) {
                                    copyErr++;
                                }
                                long costTime = System.currentTimeMillis() - startTime;

                                copyCount++;
                                setText("写入文件次数" + copyCount);
                                Log.e(TAG, "写文件" + copyCount);
                                double speed = 989.9 / costTime * 1000;
                                String result = copyCount + "次写入，失败次数" + copyErr + ",写入速度" + String.format("%.2f", speed) + "M/s";
                                Utils.writeToFile(resPath + "/wuart.txt", result);
                                setText(result);
                                handler.sendEmptyMessageDelayed(0, 1000);
                            } else {
                                handler.sendEmptyMessageDelayed(2, 1000);
                            }
                        }
                    }).start();
                    break;
                case 2:
                    btn_uart.setEnabled(true);
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

    /**
     * 串口1是否回环
     *
     * @return true表示是，否则不是
     */
    private boolean uartIsLoopDevice() {
        try {
            CommunicationUtil cu = CommunicationUtil.getInstance();
            int fd = cu.uartopen("/dev/ttyS3", 9600);
            if (fd > 0) {
                String teststr = "test";
                byte[] buffer = new byte[4];

                //自发自收
                cu.uartwrite(fd, teststr.getBytes());
                Thread.sleep(100);//保证串口已经发送完毕，本机串口也接收完毕
                cu.uartread(fd, buffer, 1000);
                cu.uartclose(fd);

                if (new String(buffer).compareTo(teststr) == 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static byte[] File2byte(){
        byte[] buffer = null;
        try
        {
            FileInputStream fis = new FileInputStream(new File(Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath() + "/uart.txt"));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return buffer;
    }

    private void setText(String s) {
        Message msg = new Message();
        msg.what = 3;
        msg.obj = s;
        handler.sendMessage(msg);
    }

}
