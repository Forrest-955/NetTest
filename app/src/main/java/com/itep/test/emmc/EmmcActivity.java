package com.itep.test.emmc;

import android.app.Activity;
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

import com.itep.test.R;
import com.itep.test.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class EmmcActivity extends Activity {
    private static final String TAG = "Emmc test";
    private int readCount;
    private int readErr;
    private int copyCount;
    private int copyErr;
    private String path;
    private Button btn_writefile;
    private Button btn_readfile;
    private Button btn_result;
    private Button btn_close;
    private TextView tv_msg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emmc);
        btn_result = findViewById(R.id.btn_get_res);
        btn_writefile = findViewById(R.id.btn_write_file);
        btn_readfile = findViewById(R.id.btn_read_file);
        btn_close = findViewById(R.id.btn_close);
        tv_msg = findViewById(R.id.tv_msg);
        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = null;
                try {
                    result = Utils.readTXT(path + "/wemmc.txt") + "\n" + Utils.readTXT(path + "/remmc.txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tv_msg.setText(result);
            }
        });
        btn_writefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_writefile.setEnabled(false);
                btn_readfile.setEnabled(false);
                btn_result.setEnabled(false);
                copyCount = 0;
                copyErr = 0;
                handler.removeMessages(0);
                handler.sendEmptyMessage(0);
            }
        });
        btn_readfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_writefile.setEnabled(false);
                btn_readfile.setEnabled(false);
                readCount = 0;
                readErr = 0;
                handler.removeMessages(1);
                handler.sendEmptyMessage(1);
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        path = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 0:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (copyCount < 2000) {
                                Log.e(TAG, "copy" + copyCount);
                                setText(String.format("???%d???????????????", copyCount + 1));
                                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                    Log.e(TAG, "sdpath" + path);
                                }
                                File f;
                                if (copyCount % 2 == 0) {
                                    f = new File(path + "/emmc2.zip");
                                } else {
                                    f = new File(path + "/emmc1.zip");
                                }
                                long startTime = System.currentTimeMillis();
                                long size = Utils.getAvailableSize("/storage/emulated/0/");
                                Log.e(TAG, "size:" + size + "length:" + f.length());
                                if (copyCount % 2 == 0) {
                                    if (copyCount == 0) {
                                        if (!cmdCopyFile(path + "/emmc.zip", path + "/emmc1.zip")) {
                                            copyErr++;
                                        }
                                    } else {
                                        if (!cmdCopyFile(path + "/emmc2.zip", path + "/emmc1.zip")) {
                                            copyErr++;
                                        }
                                        f.delete();
                                    }
                                } else {
                                    if (!cmdCopyFile(path + "/emmc1.zip", path + "/emmc2.zip")) {
                                        copyErr++;
                                    }
                                    f.delete();
                                }
                                long costTime = System.currentTimeMillis() - startTime;
                                copyCount++;
                                setText("??????????????????" + copyCount);
                                Log.e(TAG, "?????????" + copyCount);
                                double speed = 989.9 / costTime * 1000;
                                String result = copyCount + "????????????????????????" + copyErr + ",????????????" + String.format("%.2f", speed) + "M/s";
                                Utils.writeToFile(path + "/wemmc.txt", result);
                                setText(result);
                                handler.sendEmptyMessageDelayed(0, 1000);
                            } else {
                                handler.sendEmptyMessageDelayed(1, 1000);
                            }
                        }
                    }).start();
                    break;
                case 1:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (readCount < 100000) {
                                setText(String.format("???%d???????????????", readCount + 1));
                                long startTime = System.currentTimeMillis();
                                String read = readStringFromFile(path + "/emmc.txt");
                                if (read.length() == 0) {
                                    readErr++;
                                }
                                Log.e(TAG, "file length:" + read.length());
                                long costTime = System.currentTimeMillis() - startTime;
                                readCount++;
                                setText("??????????????????" + readCount);
                                double speed = 989.9 / costTime * 1000;
                                String result = readCount + "????????????????????????" + readErr + ",????????????" + String.format("%.2f", speed) + "M/s";
                                Utils.writeToFile(path + "/remmc.txt", result);
                                setText(result);
                                handler.sendEmptyMessageDelayed(1, 1000);
                            } else {
                                handler.sendEmptyMessageDelayed(2, 1000);
                            }
                        }
                    }).start();
                    break;
                case 2:
                    btn_writefile.setEnabled(true);
                    btn_readfile.setEnabled(true);
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
