package com.itep.test;

import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import aria.apache.commons.net.ftp.FTP;
import aria.apache.commons.net.ftp.FTPClient;
import aria.apache.commons.net.ftp.FTPFile;
import aria.apache.commons.net.ftp.FTPReply;

/**
 * Created by cy on 2020/05/15.
 */
public class FtpUtil implements ICallback{
    private static final String TAG = FtpUtil.class.getSimpleName();
    private FTPClient ftpClient;

//    private static final String FTP_USER = "ClientUser";
//    private static final String FTP_PWD = "Start001";
//    private static final String FTP_USER = "StartIP";
//    private static final String FTP_PWD = "pCdfilih#418";
    private static final String FTP_USER = "test";
    private static final String FTP_PWD = "test";
    /**
     * 存储单位.
     */
    private static final int STOREUNIT = 1024;
    private long response;
    private FtpCallback ftpCallback;

    public FtpUtil() {
        if (ftpClient == null) {
            ftpClient = new FTPClient();
        }
    }

    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public boolean openConnect(String address, int port) {
        try {
            int reply; // 服务器响应值
            // 连接至服务器
            ftpClient.connect(address, port);
            // 获取响应值
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                // 断开连接
                ftpClient.disconnect();
                throw new IOException("connect fail: " + reply);
            }
            // 中文转码
            String ENCODING_CHARSET = "GBK";
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
                // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
                ENCODING_CHARSET = "UTF-8";
            }
            ftpClient.setControlEncoding(ENCODING_CHARSET);

            // 登录到服务器
            boolean login = ftpClient.login(FTP_USER, FTP_PWD);
            // 获取响应值
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                // 断开连接
                ftpClient.disconnect();
                ftpCallback.ftpFailed("失败");
                throw new IOException("connect fail: " + reply);
            } else {
                // 获取登录信息
                //FTPClientConfig config = new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
                //config.setServerLanguageCode("zh");
                //ftpClient.configure(config);
                // 使用被动模式设为默认
                ftpClient.enterLocalPassiveMode();
                // 二进制文件支持
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                Log.e(TAG, "login");
            }
            return login;
        } catch (Exception e) {
            e.printStackTrace();
            ftpCallback.ftpFailed("失败");
        }
        ftpCallback.ftpFailed("失败");
        return false;
    }

    /**
     * 本地字符编码
     */
    private static String LOCAL_CHARSET = "GBK";

    // FTP协议里面，规定文件名编码为iso-8859-1
    private static String SERVER_CHARSET = "ISO-8859-1";

    public void connectFtpServer(String address, int port) {
        if (ftpClient == null) {
            ftpClient = new FTPClient();
        }
        if (ftpClient.isConnected()) {
            return;
        }
        try {
            ftpClient.connect(address, port);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                if (ftpClient.login(FTP_USER, FTP_PWD)) {
                    if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
                        // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
                        LOCAL_CHARSET = "UTF-8";
                        Log.e(TAG, "UTF-8 support");
                    }
                    ftpClient.setControlEncoding(LOCAL_CHARSET);
                    ftpClient.enterLocalPassiveMode();// 设置被动模式
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);// 设置传输的模式
                    return;
                } else {
                    //throw new FileStorageException("Connet ftpServer error! Please check user or password");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            //closeConnect();
            //throw new FileStorageException("Connet ftpServer error! Please check the Configuration");
        }
    }
    //上传文件时，文件名称需要做编码转换
    //fileName = new String(fileName.getBytes(LOCAL_CHARSET),SERVER_CHARSET);


    /**
     * 关闭FTP服务.
     *
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            if (ftpClient.isConnected()) {
                // 登出FTP
                ftpClient.logout();
                // 断开连接
                ftpClient.disconnect();
                Log.e(TAG, "logout");
            }
        }
    }

    /**
     * 列出FTP下所有文件.
     *
     * @param remotePath 服务器目录
     * @return FTPFile集合
     * @throws IOException
     */
    public List<FTPFile> listFiles(String remotePath) throws IOException {
        List<FTPFile> list = new ArrayList<>();
        if (ftpClient != null) {
            // 获取文件
            try {
                FTPFile[] files = ftpClient.listFiles(remotePath);
                if (files != null && files.length > 0) {
                    // 遍历并且添加到集合
                    for (FTPFile file : files) {
                        list.add(file);
                    }
                }
            } catch (Exception e) {
                Log.e("TAG", "请稍等...");
            }
        }
        return list;
    }


    public void ftpDownLoad(String filePath, String fileName, String savePath) {
        try {
            savePath=savePath.endsWith(File.separator)?savePath:savePath+File.separator;
            //filePath=filePath.endsWith(File.separator)?filePath:filePath+File.separator;
            Log.e(TAG, "startDownload:" + filePath  + fileName + "to:" + savePath);
            boolean flag = true;
            // 初始化当前流量
            response = 0;
            // 更改FTP目录
            flag = ftpClient.changeWorkingDirectory(new String(filePath.getBytes(),ftpClient.getControlEncoding()));
            if (!flag) {
                Log.e(TAG,"change working dir error:"+filePath);
                return;
            }
            // 得到FTP当前目录下所有文件
            FTPFile[] ftpFiles = ftpClient.listFiles();
            // 循环遍历
            for (FTPFile ftpFile : ftpFiles) {
                // 找到需要下载的文件
                Log.e(TAG, "ftpFileName" + ftpFile.getName());
                if (ftpFile.getName().equals(fileName)) {
                    Log.e(TAG, "createLocalFile");
                    // 创建本地目录
                    File file = new File(savePath  + fileName);
                    // 下载前时间
                    Date startTime = new Date();
                    if (ftpFile.isFile()) {
                        flag = downloadSingle(ftpFile, file);
                    }
                    // 下载完时间
                    Date endTime = new Date();
                    // 返回值
                    float passTime = (endTime.getTime() - startTime.getTime())/1000;
                    String result = "下载完成\n" + "耗时:" + passTime + "s" + "\n" + "大小:" + getFormatSize(response) + "平均速度:" + response/1024/1024/passTime+"M/s";
                    ftpCallback.ftpSuccess(result, "平均速度:" + response/1024/1024/passTime+"M/s");
//                sendLog(result);
//                Log.e(TAG, "passtime:" + passTime+"s");
//                Log.e(TAG, "size:" + getFormatSize(response));
//                Log.e(TAG, "speed:" + response/1024/1024/passTime+"M/s");
                }
            }
        } catch (Exception e) {
            ftpCallback.ftpFailed("失败");
            e.printStackTrace();
        }
    }

    private boolean downloadSingle(FTPFile ftpFile, File localFile) throws IOException {
        boolean flag = true;
        // 创建输出流
        OutputStream outputStream = new FileOutputStream(localFile);
        // 统计流量
        response += ftpFile.getSize();
        // 下载单个文件
        flag = ftpClient.retrieveFile(localFile.getName(), outputStream);
        // 关闭文件流
        outputStream.close();
        return flag;
    }

    public void ftpUpload(File localFile, String remotePath, String remoteName) {
        try {
            boolean flag = false;
            // 初始化当前流量
            response = 0;
            // 二进制文件支持
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 设置模式
            ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
            // 改变FTP目录
            ftpClient.changeWorkingDirectory(remotePath);
            // 获取上传前时间
            Date startTime = new Date();
            if (localFile.isFile()) {
                // 上传单个文件
                Log.e(TAG,"start upload");
                flag = uploadingSingle(localFile, remoteName);
            }
            if (flag) {
                // 获取上传后时间
                Date endTime = new Date();
                float passTime = (endTime.getTime() - startTime.getTime())/1000;
                Log.e(TAG, "passtime:" + passTime+"s");
                Log.e(TAG, "size:" + getFormatSize(response));
                Log.e(TAG, "speed:" + response/1024/1024/passTime+"M/s");
                String result = "上传完成\n" + "耗时:" + passTime + "s" + "\n" + "大小:" + getFormatSize(response) + "平均速度:" + response/1024/1024/passTime+"M/s";
                ftpCallback.ftpSuccess(result, "平均速度:" + response/1024/1024/passTime+"M/s");
            } else {
                ftpCallback.ftpFailed("失败");
            }
        } catch (Exception e) {
            ftpCallback.ftpFailed("失败");
            e.printStackTrace();
        }
    }

    private void sendLog(String log) {
        Message.obtain(MainActivity._instance.handler, 0, log);
    }

    /**
     * 上传单个文件.
     *
     * @param localFile 本地文件
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    private boolean uploadingSingle(File localFile, String remoteName) throws IOException {
        boolean flag = true;
        // 创建输入流
        InputStream inputStream = new FileInputStream(localFile);
        // 统计流量
        response += (double) inputStream.available() / 1;
        // 上传单个文件
        flag = ftpClient.storeFile(remoteName, inputStream);
        // 关闭文件流
        inputStream.close();
        return flag;
    }

    /**
     * 转化文件单位.
     *
     * @param size 转化前大小(byte)
     * @return 转化后大小
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / STOREUNIT;
        if (kiloByte < 1) {
            return size + " Byte";
        }

        double megaByte = kiloByte / STOREUNIT;
        if (megaByte < 1) {
            BigDecimal result = new BigDecimal(Double.toString(kiloByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " KB";
        }

        double gigaByte = megaByte / STOREUNIT;
        if (gigaByte < 1) {
            BigDecimal result = new BigDecimal(Double.toString(megaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " MB";
        }

        double teraBytes = gigaByte / STOREUNIT;
        if (teraBytes < 1) {
            BigDecimal result = new BigDecimal(Double.toString(gigaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " GB";
        }
        BigDecimal result = new BigDecimal(teraBytes);
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " TB";
    }

    /**
     * 时间毫秒单位.
     */
    private static final int TIMEMSUNIT = 1000;

    /**
     * 时间单位.
     */
    private static final int TIMEUNIT = 60;

    /**
     * 转化时间单位.
     *
     * @param time 转化前大小(MS)
     * @return 转化后大小
     */
    public static String getFormatTime(long time) {
        double second = (double) time / TIMEMSUNIT;
        if (second < 1) {
            return time + " MS";
        }

        double minute = second / TIMEUNIT;
        if (minute < 1) {
            BigDecimal result = new BigDecimal(Double.toString(second));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " SEC";
        }

        double hour = minute / TIMEUNIT;
        if (hour < 1) {
            BigDecimal result = new BigDecimal(Double.toString(minute));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " MIN";
        }

        BigDecimal result = new BigDecimal(Double.toString(hour));
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " H";
    }


    /**
     * 转化字符串.
     *
     * @param source   转化前字符串
     * @param encoding 编码格式
     * @return 转化后字符串
     */
    public static String convertString(String source, String encoding) {
        try {
            byte[] data = source.getBytes("ISO8859-1");
            return new String(data, encoding);
        } catch (UnsupportedEncodingException ex) {
            return source;
        }
    }


    @Override
    public void setFtpCallback(FtpCallback callback) {
        this.ftpCallback = callback;
    }
}
