package com.itep.test;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.itep.mt.common.sys.SysCommand;
import com.itep.mt.common.sys.SysConf;
import com.itep.test.baking.AdPlayerActivity;
import com.itep.test.camera.Camera2Activity;
import com.itep.test.emmc.EmmcActivity;
import com.itep.test.hid.UartActivity;
import com.itep.test.hid.UsbActivity;
import com.itep.test.net.MainActivity;
import com.itep.test.serial.SerialPortActivity;
import com.itep.test.tf.TFCardActivity;

import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Button btnNet;
    private Button btnEmmc;
    private Button btnTF;
    private Button btnBaking;
    private Button btnCamera;
    private Button btnReboot;
    private Button btnUsb;
    private Button btnUart;
    private Timer timer = new Timer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        initView();
        if (SysConf.getReboot() == 1) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    SysCommand.reboot();
                }
            };
            timer.schedule(task, 6000);
            Utils.showYesOrNo(context, "提示", "5s后重启，是否停止自动重启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SysConf.setReboot(0);
                    timer.cancel();
                }
            });
        }
    }

    private void initView() {
        btnNet = findViewById(R.id.btn_net_test);
        btnEmmc = findViewById(R.id.btn_emmc_test);
        btnTF = findViewById(R.id.btn_tf_test);
        btnBaking = findViewById(R.id.btn_baking_test);
        btnCamera = findViewById(R.id.btn_camera_test);
        btnReboot = findViewById(R.id.btn_reboot_test);
        btnUsb = findViewById(R.id.btn_usb_test);
        btnUart = findViewById(R.id.btn_uart_test);
        btnNet.setOnClickListener(this);
        btnEmmc.setOnClickListener(this);
        btnTF.setOnClickListener(this);
        btnBaking.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnReboot.setOnClickListener(this);
        btnUsb.setOnClickListener(this);
        btnUart.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_net_test:
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_emmc_test:
                Intent intent2 = new Intent(context, EmmcActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn_tf_test:
                Intent intent4 = new Intent(context, TFCardActivity.class);
                startActivity(intent4);
                break;
            case R.id.btn_baking_test:
                Intent intent1 = new Intent(context, AdPlayerActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_camera_test:
                Intent intent3 = new Intent(context, Camera2Activity.class);
                startActivity(intent3);
                break;
            case R.id.btn_reboot_test:
                Utils.showYesOrNo(context, "提示", "是否开始自动重启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SysConf.setReboot(1);
                        SysCommand.reboot();
                    }
                });
                break;
            case R.id.btn_usb_test:
                Intent intent5 = new Intent(context, UsbActivity.class);
                startActivity(intent5);
                break;
            case R.id.btn_uart_test:
                Intent intent6 = new Intent(context, SerialPortActivity.class);
                startActivity(intent6);
                break;
        }
    }
}
