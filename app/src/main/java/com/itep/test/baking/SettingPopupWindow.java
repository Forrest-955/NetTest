package com.itep.test.baking;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itep.mt.common.sys.SysConf;
import com.itep.mt.common.util.Logger;
import com.itep.mt.common.util.MathUtils;
import com.itep.test.R;
import com.itep.test.baking.CpuCoreService;

import java.io.BufferedReader;
import java.io.FileReader;

import static android.content.Context.AUDIO_SERVICE;

public class SettingPopupWindow extends PopupWindow implements View.OnClickListener {
    // 最大音量
    private int maxVolume;
    private int mediaVolume = SysConf.getMediaVolume();//多媒体音量
    private SeekBar mediaSeekBar;//多媒体音量
    private TextView tvVoiceMedia;//业务音量值显示
    private SeekBar light_seekbar;//亮度
    private TextView tv_light;//亮度显示
    private TextView tv_cpu;
    private AudioManager mAudioManager;
    private SoundPool soundPool;
    private int soundId;
    private RadioButton rb_25;
    private RadioButton rb_50;
    private RadioButton rb_75;
    private RadioButton rb_100;
    private Button btn_close;

    public SettingPopupWindow(final Activity context, int w, int h) {
        View view = View.inflate(context, R.layout.activity_setting, null);
        mediaSeekBar = view.findViewById(R.id.seek_bar_media);
        tvVoiceMedia = view.findViewById(R.id.tv_voice_media);
        tv_cpu = view.findViewById(R.id.tv_cpu);
        light_seekbar = view.findViewById(R.id.light_seekbar);
        tv_light = view.findViewById(R.id.tv_light);
        btn_close = view.findViewById(R.id.btn_close);
        rb_25 = view.findViewById(R.id.rb_25);
        rb_50 = view.findViewById(R.id.rb_50);
        rb_75 = view.findViewById(R.id.rb_75);
        rb_100 = view.findViewById(R.id.rb_100);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        //设置多媒体音量
        mediaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playSound(mediaVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mediaVolume = progress;
                tvVoiceMedia.setText("" + progress);
                // 设置音量
                SysConf.setMediaVolume(progress);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
        });
        // 调节亮度
        light_seekbar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        tv_light.setText("" + progress);
                        setScreenBrightness(progress, context);
                    }
                });
        rb_25.setOnClickListener(this);
        rb_50.setOnClickListener(this);
        rb_75.setOnClickListener(this);
        rb_100.setOnClickListener(this);
        this.setContentView(view);
        this.setHeight(h);
        this.setWidth(w);
        initVolume();
        initLight(context);
        initVoice(context);
        getCPURate.start();
    }

    /**
     * 每隔1秒获取一次cpu占用率
     */
    Thread getCPURate = new Thread() {
        @Override
        public void run() {
            while (true) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 更新UI
     */
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    tv_cpu.setText("当前CPU使用率为" + getProcessCpuRate() + "%");
                    break;
                case 2:

                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rb_25:
                CpuCoreService.stopThread();
                CpuCoreService.run25();
                break;
            case R.id.rb_50:
                CpuCoreService.stopThread();
                CpuCoreService.run50();
                break;
            case R.id.rb_75:
                CpuCoreService.stopThread();
                CpuCoreService.run75();
                break;
            case R.id.rb_100:
                CpuCoreService.stopThread();
                CpuCoreService.run100();
                break;
        }
    }

    private class CpuStat {
        public long user = 0;
        public long system = 0;
        public long nice = 0;
        public long idle = 0;
    }

    private CpuStat cs = new CpuStat();//记录原始的CPU状态

    /**
     * 获取cpu使用率
     *
     * @return rate
     */
    private int getProcessCpuRate() {
        int rate = 0;
        try {
            //读CPU状态
            BufferedReader br = new BufferedReader(new FileReader("/proc/stat"));
            String line = br.readLine();
            br.close();

            //分析使用率
            String[] ls = line.split(" ");
            long user = Long.parseLong(ls[2]);
            long system = Long.parseLong(ls[3]);
            long nice = Long.parseLong(ls[4]);
            long idle = Long.parseLong(ls[5]);

            long od = cs.user + cs.system + cs.nice + cs.idle;
            long nd = user + system + nice + idle;
            long id = user - cs.user;
            long sd = system - cs.system;
            if (cs.user != 0) {//初次不计算，从第2次开始计算
                rate = (int) ((((double) (sd + id)) * 100.0) / ((double) (nd - od)));
            }

            cs.user = user;
            cs.system = system;
            cs.nice = nice;
            cs.idle = idle;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rate;
    }

    /**
     * 提示声
     */
    public void playSound(int mVolume) {
        try {
            //            currentVolume = mAudioManager
            //                    .getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);
            float streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = 0;
            if (SysConf.getIsAndroid9()) {
                int progress = 15;   //获取音量进度条最大值
                volume = mVolume / (float)progress;   //根据选择值计算音量比例
            } else {
                volume = mVolume / streamVolumeMax;
            }
            soundPool.play(soundId, volume, volume, 1, 0, 1f);
        } catch (Exception e) {
            Logger.i("播放按键声异常!");
        }
    }

    /**
     * 播放声音初始化
     */
    public void initVoice(Activity context) {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(context, R.raw.b, 1);
    }

    /**
     * 初始化音量数据
     */
    private void initVolume() {
        // 获取系统最大音量
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 设置voice_seekbar的最大值
        mediaSeekBar.setMax(maxVolume);
        mediaVolume = SysConf.getMediaVolume();
        if (mediaVolume == 0) {//音量为0时则设置为1，否则按音键无声音
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaVolume, 0);
        }
//        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

        tvVoiceMedia.setText("" + mediaVolume);
        mediaSeekBar.setProgress(mediaVolume);
    }

    /**
     * 保存当前的屏幕亮度值，并使之生效
     */
    private void setScreenBrightness(int paramInt, Activity context) {
        int brightness= (int) MathUtils.mul(2.55,paramInt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                //有了权限，具体的动作
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, brightness);
            }
        } else {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, brightness);
        }

    }


    /**
     * 获取当前屏幕亮度
     */
    private void initLight(Activity context) {
        int currentBright = 255;
        try {
            // 系统亮度值范围：0～255
            currentBright = android.provider.Settings.System.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int curLight=(int) Math.round(currentBright/2.55);
        light_seekbar.setMax(100);
        light_seekbar.setProgress(curLight);
        tv_light.setText("" + curLight);
    }
}
