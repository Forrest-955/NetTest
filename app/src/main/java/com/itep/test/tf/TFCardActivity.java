package com.itep.test.tf;

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

import com.itep.mt.common.sys.SysCommand;
import com.itep.test.R;
import com.itep.test.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TFCardActivity extends Activity {
    private static final String TAG = "TF test";
    private int readCount;
    private int readErr;
    private int copyCount;
    private int copyErr;
    private String path;
    private String resPath;
    private Button btn_writefile;
    private Button btn_readfile;
    private Button btn_result;
    private Button btn_close;
    private TextView tv_msg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tf);
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
                    result = Utils.readTXT(resPath + "/wtf.txt") + "\n" + Utils.readTXT(resPath + "/rtf.txt");
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
                            if (copyCount < 2000) {
                                setText(String.format("第%d次写入文件", copyCount + 1));
                                Log.e(TAG, "copy" + copyCount);
                                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                    Log.e(TAG, "sdpath" + path);
                                }
                                File f;
                                if (copyCount % 2 == 0) {
                                    f = new File(path + "/tf2.zip");
                                } else {
                                    f = new File(path + "/tf1.zip");
                                }
                                long startTime = System.currentTimeMillis();
                                long size = Utils.getAvailableSize("/storage/emulated/0/");
                                Log.e(TAG, "size:" + size + "length:" + f.length());
                                if (copyCount % 2 == 0) {
                                    if (copyCount == 0) {
                                        if (!cmdCopyFile(path + "/tf.zip", path + "/tf1.zip")) {
                                            copyErr++;
                                        }
                                    } else {
                                        if (!cmdCopyFile(path + "/tf2.zip", path + "/tf1.zip")) {
                                            copyErr++;
                                        }
                                        f.delete();
                                    }
                                } else {
                                    if (!cmdCopyFile(path + "/tf1.zip", path + "/tf2.zip")) {
                                        copyErr++;
                                    }
                                    f.delete();
                                }
                                long costTime = System.currentTimeMillis() - startTime;

                                copyCount++;
                                setText("写入文件次数" + copyCount);
                                Log.e(TAG, "写文件" + copyCount);
                                double speed = 989.9 / costTime * 1000;
                                String result = copyCount + "次写入，失败次数" + copyErr + ",写入速度" + String.format("%.2f", speed) + "M/s";
                                Utils.writeToFile(resPath + "/wtf.txt", result);
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
                                setText(String.format("第%d次读取文件", readCount + 1));
                                long startTime = System.currentTimeMillis();
                                String read = readStringFromFile(path + "/tf.txt");
                                if (read.length() == 0) {
                                    readErr++;
                                }
                                Log.e(TAG, "file length:" + read.length());
                                long costTime = System.currentTimeMillis() - startTime;
                                readCount++;
                                setText("读取文件次数" + readCount);
                                double speed = 989.9 / costTime * 1000;
                                String result = readCount + "次读取，失败次数" + readErr + ",读取速度" + String.format("%.2f", speed) + "M/s";
                                Utils.writeToFile(resPath + "/rtf.txt", result);
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
            //打开文件输入流
            FileInputStream inputStream = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            //读取文件内容
            while(len > 0){
                sb.append(new String(buffer,0,len));
                //继续将数据放到buffer中
                len = inputStream.read(buffer);
            }
            //关闭输入流
            inputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return sb.toString();
    }
}
