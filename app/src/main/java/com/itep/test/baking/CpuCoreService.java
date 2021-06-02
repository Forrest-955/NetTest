package com.itep.test.baking;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Created by Administrator on 2019/11/25.
 */

public class CpuCoreService extends android.app.Service{

    private static CPUThread1 thread1;
    private static CPUThread2 thread2;
    private static CPUThread3 thread3;
    private static CPUThread4 thread4;
    private static boolean isStop1 = false;
    private static boolean isStop2 = false;
    private static boolean isStop3 = false;
    private static boolean isStop4 = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initThread();
    }

    public static void run25(){
        stopThread();
        thread1 = new CPUThread1();
        thread1.setStop(false);
        thread1.start();
    }

    public static void run50(){
        stopThread();
        thread1 = new CPUThread1();
        thread1.setStop(false);
        thread1.start();
        thread2=new CPUThread2();
        thread2.setStop(false);
        thread2.start();
    }

    public static void run75(){
        stopThread();
        thread1 = new CPUThread1();
        thread1.setStop(false);
        thread1.start();
        thread2=new CPUThread2();
        thread2.setStop(false);
        thread2.start();
        thread3 = new CPUThread3();
        thread3.setStop(false);
        thread3.start();
    }

    public static void run100(){
        stopThread();
        thread1 = new CPUThread1();
        thread1.setStop(false);
        thread1.start();
        thread2=new CPUThread2();
        thread2.setStop(false);
        thread2.start();
        thread3 = new CPUThread3();
        thread3.setStop(false);
        thread3.start();
        thread4 = new CPUThread4();
        thread4.setStop(false);
        thread4.start();
    }

    public static void stopThread(){
        Log.e("CCC","stop thread");
        initThread();
    }

    private static void initThread(){
        Log.e("CCC","init thread");
        if(thread1 != null){
            Log.e("CCC","stop thread1");
            thread1.setStop(true);
            thread1 = null;
        }
        if(thread2 != null){
            Log.e("CCC","stop thread2");
            thread2.setStop(true);
            thread2 = null;
        }
        if(thread3 != null){
            Log.e("CCC","stop thread3");
            thread3.setStop(true);
            thread3 = null;
        }
        if(thread4 != null){
            Log.e("CCC","stop thread4");
            thread4.setStop(true);
            thread4 = null;
        }
    }

    /**
     * 提高cpu使用率的线程
     */
    private static class CPUThread1 extends Thread {

        @Override
        public void run() {
            while (!isStop1) {
                int n = 1;
            }
        }

        private void setStop(boolean b) {
            isStop1 = b;
        }
    }

    /**
     * 提高cpu使用率的线程
     */
    private static class CPUThread2 extends Thread {

        @Override
        public void run() {
            while (!isStop2) {
                int n = 1;
            }
        }

        private void setStop(boolean b) {
            isStop2 = b;
        }
    }

    /**
     * 提高cpu使用率的线程
     */
    private static class CPUThread3 extends Thread {

        @Override
        public void run() {
            while (!isStop3) {
                int n = 1;
            }
        }

        private void setStop(boolean b) {
            isStop3 = b;
        }
    }

    /**
     * 提高cpu使用率的线程
     */
    private static class CPUThread4 extends Thread {

        @Override
        public void run() {
            while (!isStop4) {
                int n = 1;
            }
        }

        private void setStop(boolean b) {
            isStop4 = b;
        }
    }
}
