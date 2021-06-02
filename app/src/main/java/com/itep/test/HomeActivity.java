package com.itep.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.itep.mt.common.sys.SysConf;
import com.itep.test.baking.AdPlayerActivity;
import com.itep.test.emmc.EmmcActivity;
import com.itep.test.net.MainActivity;

public class HomeActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Button btnNet;
    private Button btnEmmc;
    private Button btnTF;
    private Button btnBaking;
    private Button btnCamera;
    private Button btnReboot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        initView();
    }

    private void initView() {
        btnNet = findViewById(R.id.btn_net_test);
        btnEmmc = findViewById(R.id.btn_emmc_test);
        btnTF = findViewById(R.id.btn_tf_test);
        btnBaking = findViewById(R.id.btn_baking_test);
        btnCamera = findViewById(R.id.btn_camera_test);
        btnReboot = findViewById(R.id.btn_reboot_test);
        btnNet.setOnClickListener(this);
        btnEmmc.setOnClickListener(this);
        btnTF.setOnClickListener(this);
        btnBaking.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnReboot.setOnClickListener(this);
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

                break;
            case R.id.btn_baking_test:
                Intent intent1 = new Intent(context, AdPlayerActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_camera_test:

                break;
            case R.id.btn_reboot_test:

                break;
        }
    }
}
