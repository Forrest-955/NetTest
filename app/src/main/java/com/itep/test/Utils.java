package com.itep.test;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
}
