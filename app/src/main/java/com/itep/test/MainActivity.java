package com.itep.test;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WIFI test";
    private int pingCount;
    private int ftpdownloadtestCount;
    private int ftpuploadtestCount;
    private int ftpDownloadSuccessCount;
    private int ftpDownloadFailCount;
    private int ftpUploadSuccessCount;
    private int ftpUploadFailCount;
    private int time;
    private TextView tvLog;
    private TextView tvResult;
    private EditText etTime;
    private TextView tvTXT;
    private String tmp = "/mnt/external_sd/";
    private String path = "/mnt/internal_sd/mt/tmp/";
    boolean block = false;
    private int pingErr;
    private int copyCount;
    private int copyErr;
    private int loadErr;
    private int loadCount;
    private String image1M = "1M.jpg";
    private String imageBig = "big_image.jpg";
    public static MainActivity _instance = null;
    private Button btnDownload;
    private Button btnUpload;
    private Button btnGetRes;
    private String resutlSpeed;
//    private String downloadresPath = "/mnt/internal_sd/tc/system/download.txt";
//    private String downloadresPath = "/mnt/internal_sd/mt/system/download.txt";
    private String downloadresPath;
//    private String uploadresPath = "/mnt/internal_sd/tc/system/upload.txt";
//    private String uploadresPath = "/mnt/internal_sd/mt/system/upload.txt";
    private String uploadresPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _instance = this;
        init();
        Log.e("available:", (Utils.getAvailableSize("/mnt/external_sd/") / 1024 + "M"));
    }

    private void init() {
//        downloadresPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/download.txt";
        downloadresPath = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath() + "/download.txt";
//        uploadresPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/upload.txt";
        uploadresPath = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath() + "/upload.txt";
        tvLog = findViewById(R.id.tv_log);
        tvResult = findViewById(R.id.tv_result);
        tvTXT = findViewById(R.id.tv_txt);
        etTime = findViewById(R.id.et_testtime);
        btnDownload = findViewById(R.id.btn_download);
        btnUpload = findViewById(R.id.btn_upload);
        btnGetRes = findViewById(R.id.btn_getRes);
        btnGetRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tvTXT.setText("下载测试：" + readTXT(downloadresPath) + "\n" +  "上传测试：" + readTXT(uploadresPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.btn_wifi_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiSetting();
            }
        });
        log = "";
        findViewById(R.id.btn_ping).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pingCount = 1;
                pingErr = 0;
                handler.removeMessages(1);
                handler.sendEmptyMessage(1);
                //log+=ping("www.baidu.com")+"\n";
                //tvLog.setText(log);
            }
        });
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ftpdownloadtestCount = 1;
                ftpuploadtestCount = 1;
                ftpDownloadSuccessCount = 0;
                ftpDownloadFailCount = 0;
                ftpUploadSuccessCount = 0;
                ftpUploadFailCount = 0;
                time = Integer.parseInt(etTime.getText().toString().trim());
                Log.e("time", "测试次数：" + time);
                writeToFile(downloadresPath, "");
                writeToFile(downloadresPath, "");
                handler.removeMessages(2);
                handler.sendEmptyMessage(2);
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftpUploadSuccessCount = 0;
                ftpUploadFailCount = 0;
                time = Integer.parseInt(etTime.getText().toString().trim());
                Log.e("time", "测试次数：" + time);
                handler.removeMessages(5);
                handler.sendEmptyMessage(5);
            }
        });
        findViewById(R.id.btn_web).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentWeb = new Intent(MainActivity.this, WebViewActivity.class);
                intentWeb.putExtra("extra_prefs_show_button_bar", true);
                intentWeb.putExtra("extra_prefs_set_back_text", "返回");
                intentWeb.putExtra("extra_prefs_set_next_text", "");
                startActivity(intentWeb);
            }
        });
        findViewById(R.id.btn_write_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setText("大文件写入开始");
                copyCount = 0;
                copyErr = 0;
                handler.removeMessages(3);
                handler.sendEmptyMessage(3);
            }
        });
        findViewById(R.id.btn_mac).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setText(getMacDefault());
                //startActivity(new Intent(MainActivity.this,VideoPlayActivity.class));
            }
        });
        findViewById(R.id.btn_load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCount = 0;
                loadErr = 0;
                handler.removeMessages(4);
                handler.sendEmptyMessage(4);
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //loadImage(tmp+"big_image.jpg");
        requestPermission();
    }

    private void requestPermission() {
        String[] permissions=new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions,0);
        }
    }

    private void ftpDownload() {

        try {
//            String imgPath = "/14 Linux终端镜像/0-通用镜像/V2.0.0.0";
//            String imgName = "CCLinuxV2.0.0.0_18020101.iso";
            String imgPath = "/test";
//            String imgName = "AB_Standard_V3.2.0.3.zip";
            String imgName = "test.zip";
//            String savePath= "/mnt/internal_sd/tc/system/tmp/";
//            String savePath= "/mnt/internal_sd/mt/tmp/";
//            String savePath = getApplicationContext().getFilesDir().getAbsolutePath();
            String savePath = Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath();
//            imgPath="/Image";
//            imgName="test.zip";
            FtpUtil ftpUtil=new FtpUtil();
            ftpUtil.setFtpCallback(new FtpCallback() {
                @Override
                public void ftpSuccess(String msg, String speed) {
                    setText(msg);
                    resutlSpeed = speed;
                    ftpDownloadSuccessCount++;
                }

                @Override
                public void ftpFailed(String msg) {
                    setText(msg);
                    ftpDownloadFailCount++;
                }
            });
//            ftpUtil.openConnect("172.16.64.17",21);
//            ftpUtil.openConnect("172.16.70.36",21);
            ftpUtil.openConnect("172.16.63.18",88);
            ftpUtil.ftpDownLoad(imgPath,imgName,savePath);
//            ftpUtil.ftpUpload(new File(savePath),imgPath,"test.zip");
            ftpUtil.closeConnect();
            //tvLog.setText("开始下载/tmptest/Launcher2.apk");

//            FileTransferUtil transferUtil = new FileTransferUtil(this);
//            String startPath="ftp://172.16.64.17:21//14 Linux终端镜像/0-通用镜像/V2.0.0.0/CCLinuxV2.0.0.0_18020101.iso";34
//            String oldPath="ftp://172.16.70.36:21"+imgPath + "/" + imgName;
//            String startPath = "ftp://172.16.63.18:88/test/AB_Standard_V3.2.0.3.zip";
//            transferUtil.ftpDownload(startPath, savePath);
            //ftp.downloadSingleFile(imgPath + "/" + imgName, "/mnt/internal_sd/mt/tmp/", "test.img",
            //        //            ftp.downloadSingleFile("/tmptest/Launcher2.apk", "/mnt/internal_sd/mt/tmp/", "test.apk",
            //        new FTPUtil.DownLoadProgressListener() {
            //            //ftp.downloadSingleFile("/tmptest/windows/CCWin10V1.0.3.2_17111601.wim",tmp,"test.win", new FTPUtil.DownLoadProgressListener() {
            //            @Override
            //            public void onDownLoadProgress(String currentStep, long downProcess, File file) {
            //                Log.e(TAG, currentStep + downProcess);
            //                log += currentStep + downProcess + "\n";
            //                setText(log);
            //                //tvLog.setText(currentStep + downProcess);
            //            }
            //        });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //ftp.downloadSingle("/50 共享软件/pcmark10/license.txt","/50 共享软件/pcmark10/license.txt");
        //        try {
        //            ftp.down("/tmptest/windows/CCWin10V1.0.3.2_17111601.wim",tmp,"test.win");
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        block = false;
    }

    private void ftpUpload() {

        try {
//            String imgPath = "/14 Linux终端镜像/0-通用镜像/V2.0.0.0";
            String imgPath = "/14 Linux终端镜像/0-通用镜像/V2.0.0.0";
//            String imgName = "CCLinuxV2.0.0.0_18020101.iso";
//            String imgName = "mt207c10-firmware_V1.0.5.0_20210209.img";
            String imgName = "upload.zip";
//            String savePath= "/mnt/internal_sd/tc/system/tmp/mt207c10-firmware_V1.0.5.0_20210209.img";
//            String savePath= "/mnt/internal_sd/mt/tmp/mt207c10-firmware_V1.0.5.0_20210209.img";
//            String savePath= getApplicationContext().getFilesDir().getAbsolutePath() + "/mt207c10-firmware_V1.0.5.0_20210209.img";
//            String savePath= Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath() + "/mt207c10-firmware_V1.0.5.0_20210209.img";
            String savePath= Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath() + "/test.zip";
            imgPath="/Image";
//            imgName="test.zip";
            FtpUtil ftpUtil=new FtpUtil();
            ftpUtil.setFtpCallback(new FtpCallback() {
                @Override
                public void ftpSuccess(String msg, String speed) {
                    setText(msg);
                    resutlSpeed = speed;
                    ftpUploadSuccessCount++;
                }

                @Override
                public void ftpFailed(String msg) {
                    setText(msg);
                    ftpUploadFailCount++;
                }
            });
//            ftpUtil.openConnect("172.16.70.36",21);
            ftpUtil.openConnect("172.16.63.18",88);
            //ftpUtil.ftpDownLoad(imgPath,imgName,savePath);
            ftpUtil.ftpUpload(new File(savePath),imgPath,imgName);
            //ftpUtil.closeConnect();
            ////tvLog.setText("开始下载/tmptest/Launcher2.apk");

            //FileTransferUtil transferUtil = new FileTransferUtil(this);
            //String startPath="ftp://172.16.64.17:21/01 外设项目/42 207C10/05 应用/01 通用版/V3.0.0.0/20200421/应用软件包/通用版_9600版#V3.0.0.0.zip";
            //String oldPath="ftp://172.16.70.36:21"+imgPath + "/" + imgName;
            //transferUtil.ftpDownload(oldPath, savePath);
            //ftp.downloadSingleFile(imgPath + "/" + imgName, "/mnt/internal_sd/mt/tmp/", "test.img",
            //        //            ftp.downloadSingleFile("/tmptest/Launcher2.apk", "/mnt/internal_sd/mt/tmp/", "test.apk",
            //        new FTPUtil.DownLoadProgressListener() {
            //            //ftp.downloadSingleFile("/tmptest/windows/CCWin10V1.0.3.2_17111601.wim",tmp,"test.win", new FTPUtil.DownLoadProgressListener() {
            //            @Override
            //            public void onDownLoadProgress(String currentStep, long downProcess, File file) {
            //                Log.e(TAG, currentStep + downProcess);
            //                log += currentStep + downProcess + "\n";
            //                setText(log);
            //                //tvLog.setText(currentStep + downProcess);
            //            }
            //        });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //ftp.downloadSingle("/50 共享软件/pcmark10/license.txt","/50 共享软件/pcmark10/license.txt");
        //        try {
        //            ftp.down("/tmptest/windows/CCWin10V1.0.3.2_17111601.wim",tmp,"test.win");
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        block = false;
    }

    private String ping(String ip) {
        try {
            if (ip != null) {
                //代表ping 3 次 超时时间为10秒
                //Process p = Runtime.getRuntime().exec("ping -c 3 -w 10 " + ip);//ping3次
                Process p = Runtime.getRuntime().exec("ping -w 3 " + ip);//ping,等待3秒
                int status = p.waitFor();
                Log.e(TAG, "status" + status);
                if (status == 0) {//代表成功
                    return "成功,status=0";
                } else {//代表失败
                    return "失败,status=" + status;
                }
            } else {
                return "失败,ip为空";
            }
        } catch (Exception e) {
            return "失败," + e.toString();
        }
    }

    private String httPing() {
        String ret = "";
        try {
            URL url = new URL("https://www.baidu.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "connection.getResponseCode():" + connection.getResponseCode());
            if (connection.getResponseCode() == 200) {
                ret = "成功,状态码" + connection.getResponseCode();
            } else if (connection.getResponseCode() >= 400) {
                ret = "失败，状态码" + connection.getResponseCode();
            } else {
                ret = "成功,状态码" + connection.getResponseCode();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ret = "失败," + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            ret = "失败," + e.toString();
        }
        return ret;
        //        httpClient = new DefaultHttpClient();
        //        HttpGet httpGet = new HttpGet("http://www.w3cschool.cc/python/python-tutorial.html");
        //        HttpResponse httpResponse = httpClient.execute(httpGet);
        //        if (httpResponse.getStatusLine().getStatusCode() == 200) {
    }

    String log;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {

            switch (message.what) {
                case 0://ping
                    //new SimpleDateFormat("yyyyMMdd hhmmss").format(new Date());
                    String msgStr = (String) message.obj;
                    tvLog.setText(msgStr);
                    //tvLog.setText(tvLog.getText()+"\n"+s);

                    //handler.sendEmptyMessageDelayed(1, 1000);
                    break;
                case 1:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (pingCount < 1001) {
                                Log.e(TAG, "ping" + pingCount);
                                String s = httPing();
                                //String s = ping("www.baidu.com");
                                if (s.contains("失败")) {
                                    pingErr++;
                                }
                                setText("第" + pingCount + "次" + s + ",丢包次数" + pingErr);
                                handler.sendEmptyMessageDelayed(1, 1000);
                                pingCount++;
                            } else {
                                block = false;
                            }
                        }
                    }).start();
                    //handler.sendEmptyMessageDelayed(1,100);
                    break;
                case 2:
                    log = "";
                    btnDownload.setEnabled(false);
                    btnUpload.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (ftpdownloadtestCount < time + 1) {
                                setText(String.format("正在进行第%s次下载测试" , ftpdownloadtestCount));
                                ftpDownload();
                                ftpdownloadtestCount++;
                                writeToFile(downloadresPath, "成功次数：" + ftpDownloadSuccessCount + "失败次数：" + ftpDownloadFailCount + " " + resutlSpeed);
                                handler.sendEmptyMessageDelayed(5, 1000);
                            } else {
                                handler.sendEmptyMessageDelayed(5, 1000);
                            }
                        }
                    }).start();
                    break;
                case 3:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (copyCount < 2001) {
                                Log.e(TAG, "copy" + copyCount);
                                File f = new File(path + "test1.iso");
                                long startTime = System.currentTimeMillis();
                                if (f.exists() && f.length() < Utils.getAvailableSize("/mnt/external_sd/")) {
                                    if (!cmdCopyFile(path + "test.iso", path + "test" + copyCount + ".iso")) {
                                        copyErr++;
                                    }
                                } else {
                                    f.delete();
                                    if (!cmdCopyFile(path + "test.iso", path + "test1.iso")) {
                                        copyErr++;
                                    }
                                }
                                long costTime = System.currentTimeMillis() - startTime;

                                copyCount++;
                                setText("写入文件次数" + copyCount);
                                Log.e(TAG, "写文件" + copyCount);
                                double speed = 989.9 / costTime * 1000;
                                setText(copyCount + "次写入，失败次数" + copyErr + ",写入速度" + String.format("%.2f", speed) + "M/s");
                                handler.sendEmptyMessageDelayed(3, 1000);
                            } else {
                                block = false;
                            }
                        }
                    }).start();
                    break;
                case 4:
                    if (loadCount < 100001) {
                        long length = loadTest();
                        if (length == 0) {
                            loadErr++;
                        }
                        loadCount++;
                        length = length / 1024 / 1024;
                        setText(loadCount + "次加载，文件大小" + length + "MB，失败次数" + loadErr);
                        handler.sendEmptyMessageDelayed(4, 200);
                    }
                    break;
                case 5:
                    log = "";
                    btnDownload.setEnabled(false);
                    btnUpload.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (ftpuploadtestCount < time + 1) {
                                setText(String.format("正在进行第%s次上传测试" , ftpuploadtestCount));
                                ftpUpload();
                                ftpuploadtestCount++;
                                writeToFile(uploadresPath, "成功次数：" + ftpUploadSuccessCount + "失败次数：" + ftpUploadFailCount + " " + resutlSpeed);
                                handler.sendEmptyMessageDelayed(2, 1000);
                            } else {
                                handler.sendEmptyMessageDelayed(6, 1000);
                            }
                        }
                    }).start();
                    break;
                case 6:
                    btnDownload.setEnabled(true);
                    btnUpload.setEnabled(true);
                    int successCount = ftpDownloadSuccessCount + ftpUploadSuccessCount;
                    int failCount = ftpUploadSuccessCount + ftpUploadFailCount;
                    tvResult.setText("下载成功次数：" + ftpDownloadSuccessCount + "上传成功次数：" + ftpUploadSuccessCount);
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

    private String readTXT(String path) throws IOException {
        File file = new File(path);
        InputStream inputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        return line;
    }

    private void setText(String s) {
        Message msg = new Message();
        msg.what = 0;
        msg.obj = s;
        handler.sendMessage(msg);
    }

    private void wifiSetting() {
        Intent intentWifi = new Intent(Settings.ACTION_WIFI_SETTINGS);//WIFI网络
        intentWifi.putExtra("extra_prefs_show_button_bar", true);
        intentWifi.putExtra("extra_prefs_set_back_text", "返回");
        intentWifi.putExtra("extra_prefs_set_next_text", "");
        startActivity(intentWifi);
    }

    private String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    public boolean checkWifiState(final WifiManager wifiManager, final String ssid) {
        String currSSID = wifiManager.getConnectionInfo().getSSID();
        if (currSSID != null)
            currSSID = currSSID.replace("\"", "");
        int currIp = wifiManager.getConnectionInfo().getIpAddress();
        Log.e(TAG, "currentSSID:" + currSSID + "currentIP" + currIp);
        return currSSID != null && currSSID.equals(ssid) && currIp != 0;
    }


    public boolean cmdCopyFile(String oldPath, String newPath) {
        return Utils.runCmd("cp " + oldPath + " " + newPath);
    }

    private long loadTest() {
        File f = new File(path + imageBig);
        if (f.exists()) {
            return f.length();
        } else {
            return 0;
        }
    }

    public String getMacDefault() {
        String mac = "";
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        List<ScanResult> list = wifiScan();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                mac += "\n" + list.get(i).SSID + list.get(i).level;
                Log.e(TAG, "ssid:" + list.get(i).SSID + "rssi:" + list.get(i).level);
            }
        }
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    public List<ScanResult> wifiScan() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.startScan();
        return wifi.getScanResults();
    }

    @Override
    protected void onDestroy() {
        handler.removeMessages(0);
        handler.removeMessages(1);
        handler.removeMessages(2);
        handler.removeMessages(3);
        handler.removeMessages(4);
        handler = null;
        super.onDestroy();
    }


}
