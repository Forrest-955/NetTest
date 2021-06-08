package com.itep.test;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

/**
 * Created by wagaranai on 2019/09/10.
 */
public class Utils {
    /**
     * 使用root权限运行一个命令
     *
     * @param cmd 命令字符串
     * @return 是否成功运行
     */
    public static boolean runCmd(String cmd) {
        try {
            JSONObject params = new JSONObject();
            params.put("command", cmd);
            CmdResonse cr = SysAccessor.sendOneCmd("cmd_run", params, 400000);
            if (!cr.getResult()) {
                Log.e("runCmdResult",cr.getErr_msgs());
            }
            return cr.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 获取手机内部空间总大小
     *
     * @return 大小，字节为单位
     */
    static public long getTotalInternalMemorySize() {
        //获取内部存储根目录
        File path = Environment.getDataDirectory();
        //系统的空间描述类
        StatFs stat = new StatFs(path.getPath());
        //每个区块占字节数
        long blockSize = stat.getBlockSize();
        //区块总数
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 获取手机内部可用空间大小
     *
     * @return 大小，字节为单位
     */
    static public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        //获取可用区块数量
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }


    /**
     * 获取当前路径可用空间(字节)
     * @param path 指定路径
     * @return 可用空间的字节大小
     */
    public static long getAvailableSize (String path){
        try{
            File base = new File(path);
            StatFs stat = new StatFs(base.getPath());
            long nAvailableCount = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
            return nAvailableCount;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 记录结果
     * @param path
     * @param value
     */
    public static void  writeToFile(String path, Object value) {
        File file = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(value.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加结果
     * @param path
     * @param value
     */
    public static void addToFile(String path, String value) {
        RandomAccessFile raf = null;
        File file = new File(path);
        try {
            raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(value.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读结果
     * @param path
     * @return
     * @throws IOException
     */
    public static String readTXT(String path) throws IOException {
        File file = new File(path);
        InputStream inputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        return line;
    }

    public static void showToastMsg(Context context, String msg){
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
    }

    public static AlertDialog showRoundProcess(Context context, String title, String msg){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        return progressDialog;
    }

    public static AlertDialog showYesOrNo(Context context, String title, String msg
            , DialogInterface.OnClickListener listener){
        AlertDialog ad = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定", listener)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
        ad.setCanceledOnTouchOutside(true);
        return ad;
    }

    /**
     * HEX字符串转化byte数组
     *
     * @param hex 字符串
     * @return byte数组
     */
    public static byte[] hexToBytes(String hex) {
        int size = hex.length() / 2;
        String hexString = hex.toUpperCase();
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            int high = hexString.charAt(i * 2) & 0xFF;
            int low = hexString.charAt(i * 2 + 1) & 0xFF;
            if (high >= '0' && high <= '9') {
                high -= '0';
            } else {//A-F
                high -= 'A';
                high += 10;
            }
            if (low >= '0' && low <= '9') {
                low -= '0';
            } else {//A-F
                low -= 'A';
                low += 10;
            }
            data[i] = (byte) ((high << 4) | low);
        }
        return data;
    }
}
