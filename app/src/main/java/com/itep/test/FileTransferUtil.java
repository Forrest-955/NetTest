package com.itep.test;

import android.content.Context;
import android.util.Log;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.FtpOption;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.CommonUtil;

import java.io.File;

/**
 * Created by cy on 2020/07/13.
 */

public class FileTransferUtil {
    private static final String TAG = FileTransferUtil.class.getSimpleName();
//    private static final String FTP_USER = "StartIP";
//    private static final String FTP_PWD = "pCdfilih#418";
//    private static final String FTP_USER = "ClientUser";
//    private static final String FTP_PWD = "Start001";
    private static final String FTP_USER = "test";
    private static final String FTP_PWD = "test";
    private Context context;
    private long mTaskId;

    public FileTransferUtil(Context context) {
        this.context = context;
        Aria.download(context).register();
        Aria.get(context).getDownloadConfig().setThreadNum(16);
    }

    public void httpDownload() {
    }

    public void httpUpload() {
    }

    public long ftpDownload(String url, String savePath) {
        mTaskId = Aria.download(context)
                .loadFtp(url)
                .setFilePath(savePath,true)
                .option(new FtpOption().login(FTP_USER, FTP_PWD))
                .resetState()
                .create();
        Log.e(TAG,"开始下载 ==> " + mTaskId);
        return mTaskId;
    }

    public void ftpUpload(String url, String path) {
        mTaskId=Aria.upload(context)
                .loadFtp(url)
                .option(new FtpOption().login(FTP_USER,FTP_PWD))
                .create();
    }

    public void sftpDownload() {
    }

    public void sftpUpload() {
    }


    @Download.onTaskRunning
    protected void running(DownloadTask task) {
        Log.e(TAG, "running ==> " + "下载进度：" + task.getPercent() + "%，速度:" + task.getConvertSpeed());
    }

    @Download.onTaskComplete
    void taskComplete(DownloadTask task) {
        Log.e(TAG, "下载完成 ==> " + task.getEntity().getId());
        Log.e(TAG, "speed ==> " + task.getConvertSpeed());
        Log.e(TAG, "path ==> " + task.getDownloadEntity().getFilePath());
        Log.e(TAG, "md5Code ==> " + CommonUtil.getFileMD5(new File(task.getFilePath())));
    }


}
