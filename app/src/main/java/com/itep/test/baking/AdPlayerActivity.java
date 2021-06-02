package com.itep.test.baking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.itep.mt.common.app.BaseActivity;
import com.itep.mt.common.sys.SysConf;
import com.itep.mt.common.sys.SysVoice;
import com.itep.mt.common.util.FileUtils;
import com.itep.mt.common.util.KeyCodeUtil;
import com.itep.mt.common.util.WeakRefHandler;
import com.itep.test.R;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


/**
 * 广告播放
 */
public class AdPlayerActivity extends BaseActivity implements View.OnKeyListener
        , MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    public static final int MSG_PLAY_NEXT = 1;     //播放下一个资源
    private static final int MSG_TRY_PLAY_NEXT = 2; //当无资源可播放时，间隔500ms就尝试做一下判断
    private static final int MSG_RESUME_PLAY = 3;   //恢复播放
    private static final int MSG_PLAY_LAST = 4;   //播放上一个资源
    private static final int MSG_PLAY_ERR = 5;   //播放视频错误
    private static final int INTERVAL_RETRY = 300;  //500ms尝试判断是否有新的资源可播放

    private ViewPager pager;                        //图片播放的切换器
    private ScreenSlidePagerAdapter pageAdapter;    //页的适配器
    private AdPlayerController controller;          //播放列表控制器
    private TextView tvTip;                         //背景提示字符

    private int adType = MediaResourceItem.TYPE_PICTURE;//广告类型
    private boolean isPaused = false;               //是否处于暂停播放的状态
    private boolean isCreating = true;              //是否是应用刚启动的状态
    private ImageView imageViewBuffer1;             //图像缓存视图1，形成双缓冲
    private ImageView imageViewBuffer2;             //图像缓存视图2
    private boolean isUsingBuffer1 = false;         //最后一次加载图像是否使用图像缓存1
    private SurfaceView surfaceView;                //ijk播放视图
    //    private TextureVideoView myTexture;
    private CustomerVideoView videoView;            //视频播放视图
    private VideoPlayer videoPlayer;
    //    private VideoPlayerNew videoPlayer;
    private long mTime;                             //按键间隔时间
    private String keyString;                       //存储按键值
    private TextView tv_text;                       //输入键盘框
    private long videoPos;                          //记录播放位置
    private boolean prepareLock = true;

    //手势滑动
    private static final int GESTURE_RIGHT = 0;
    private static final int GESTURE_LEFT = 1;
    private GestureDetector gestureDetector;

    private boolean isVideoPause = false;             //是否视频处于暂停，用于音频播放暂停视频

    public static final int MSG_PAUSE_VIDEO = 1;     //暂停视频播放
    public static final int MSG_RESUME_VIDEO = 2;     //恢复视频播放
    public static final int MSG_PLAY_NEXT_NOW = 3;     //立即播放下一个
    public static final int MSG_RESET_PLAY_NEXT = 4;     //立即播放下一个
    public static final int MSG_RESET_VIDEO_VOLUME = 5;//重置视频音量

    private static final String OPEN_SETTING_STR = "83703333";     //开启系统设置字符串
    private static final String OPEN_FACTORY_TESTS_STR = "83701111";//开启工厂测试字符串
    private static final String OPEN_ADB_STR = "1236547896321";     //开启adb字符串
    private static final String CLOSE_ADB_STR = "1236987456321";     //关闭adb字符串
    private static final String ADB_FILE_PATH = "/mnt/internal_sd/mt/system/adb_debug";     //adb文件路径
    private static final int KEY_CODE_CLEAR = 2000;     //密码键盘清除间隔时间
    private static final String SYSTEM_INPUT_STR = "83706666";     //进入系统页面密码
    private int touchTime;//判断touch次数

    private boolean isWaitMusic;//是否在等待音乐结束
    private MyTask mTask;
    private boolean isGif = false;
    protected GifTimeoutTimer timeoutTimer;
    private Context context;
    private SettingPopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adplayer);
        context = this;
        //加载so文件
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            this.finish();
        }
        //初始化
        controller = AdPlayerController.getInstance();
        controller.enterOrderMode("red.png,green.png,blue.png,white.png,black.png,1080P.mp4,01.png,02.png,03.png", AdPlayerController.MODE_ORDER_MIX);
        initView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                //有了权限，具体的动作
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 100);
            }
        } else {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, 100);
        }
        popupWindow = new SettingPopupWindow(AdPlayerActivity.this, 800,600);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //标记为暂停状态，如果是视频播放，则暂停
        isPaused = true;
        if (adType == MediaResourceItem.TYPE_VIDEO) {
            if (videoPlayer.isPlaying() && videoPlayer.canPause()) {
                videoPos = videoPlayer.getCurrentPosition();
                videoPlayer.pause();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCreating) {
            isCreating = false;
        } else {
            isPaused = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        waitMusicStop();
                        if (!isPaused) {
                            handler.removeMessages(MSG_RESUME_PLAY);//移除之前的jian
                            handler.removeMessages(MSG_PLAY_NEXT);
                            handler.sendEmptyMessageDelayed(MSG_RESUME_PLAY, INTERVAL_RETRY);//定时开始恢复播放
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    /**
     * 等待语音播放结束
     */
    private void waitMusicStop() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isWaitMusic = true;
        long startDate = System.currentTimeMillis();
        long endDate = System.currentTimeMillis();
        long waitTimeOut = 10 * 1000;//等待超时时间

        isWaitMusic = false;
    }

    @Override
    protected void onDestroy() {
        IjkMediaPlayer.native_profileEnd();
        super.onDestroy();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        int playListSize = controller.getPlayListSize();
        tvTip = (TextView) findViewById(R.id.tv_tip);
        noFinishOnPause();//当pause时，不要自动关闭activity
        //图片播放视图
        imageViewBuffer1 = new ImageView(this);
        imageViewBuffer2 = new ImageView(this);

        ImageView.ScaleType scaleTyp;
        if (SysConf.getAdPlayerImageMode() > 0) { // 根据配置文件获取显示模式
            scaleTyp = ImageView.ScaleType.FIT_CENTER;
        } else {
            scaleTyp = ImageView.ScaleType.FIT_XY;
        }
        imageViewBuffer1.setScaleType(scaleTyp);
        imageViewBuffer2.setScaleType(scaleTyp);

        //视频播放视图
        //        videoView = (CustomerVideoView) findViewById(R.id.view_video);

        // Create a new media player and set the listeners
        surfaceView = findViewById(R.id.video_ijk);
//        myTexture.setSurfaceTextureListener(this);
//        surfaceView.setRotation(90);
        videoView = (CustomerVideoView) findViewById(R.id.video_default);
        videoView.setOnCompletionListener(this);
        videoView.setOnErrorListener(this);
        videoView.setOnPreparedListener(this);
        surfaceView.setFocusable(true);
        videoView.setFocusable(true);

        //videoView.getHolder().addCallback(surfaceCallback);
        videoPlayer = new VideoPlayer(surfaceView, videoView);
        videoPlayer.setListener(videoPlayerListener);
        //        videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_STRETCH,0);
        //        videoView.setMediaController(new MediaController(this));
        //        videoView.requestFocus();
        //        //videoView.setOnBufferingUpdateListener(this);
        //        videoView.setOnCompletionListener(this);
        //        videoView.setOnErrorListener(this);
        //        videoView.setOnPreparedListener(this);


        //mMediaPlayer.prepareAsync();
        //mMediaPlayer = new io.vov.vitamio.MediaPlayer(this);
        //mMediaPlayer.setOnBufferingUpdateListener(this);
        //mMediaPlayer.setOnErrorListener(this);
        //mMediaPlayer.setOnCompletionListener(this);
        //mMediaPlayer.setOnPreparedListener(this);
        //mMediaPlayer.setOnVideoSizeChangedListener(this);
        //setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //图像播放多页切换
        pager = (ViewPager) findViewById(R.id.vp_imageview);
        pageAdapter = new ScreenSlidePagerAdapter();
        pager.setAdapter(pageAdapter);
        pager.setPageTransformer(true, new DefaultTransformer());
        ViewPagerScroller scroller = new ViewPagerScroller(this);
        scroller.initViewPagerScroll(pager);

        //没有资源，则显示提示文本
        if (playListSize == 0) {
            enterDetectStatus();
        } else {
            //开始播放
            handler.sendEmptyMessageDelayed(MSG_PLAY_NEXT, INTERVAL_RETRY);
        }

        tv_text = (TextView) findViewById(R.id.tv_text);
        tv_text.setFocusable(true);
        tv_text.setFocusableInTouchMode(true);
        tv_text.requestFocus();
        tv_text.setOnKeyListener(this);

        gestureDetector = new GestureDetector(AdPlayerActivity.this, onGestureListener);
    }

    /**
     * 播放下一个媒体资源
     */
    public void playNext() {
        if (!isFinishedImg) {
            return;
        }
        //如果视频正在播放则停止
        if (videoPlayer.isPlaying() && !isGif) {
            videoPlayer.stop();
            videoPlayer.release();
        }
        MediaResourceItem item = controller.getNextItem();
        if (item != null && item.isValid()) {
            boolean isPicture = item.isPicture();
            if (isPicture && item.getPath().contains(".gif")) {
                isPicture = false;
                isGif = true;
                if (timeoutTimer != null) {
                    timeoutTimer.cancel();
                }
                timeoutTimer = new GifTimeoutTimer(controller.getDuration());
                timeoutTimer.start();
            } else {
                isGif = false;
                if (timeoutTimer != null) {
                    timeoutTimer.cancel();
                }
            }
            enterPlayStatus(isPicture);
            if (isPicture) {//播放图片
                //videoPos=0;
                loadNextImage(item);
//                pager.setPageTransformer(true, Transformer.getRandomTransformer());
//                pager.setCurrentItem(isUsingBuffer1 ? 0 : 1, true);
                if (controller.getPlayListSize() <= 1) {//图片只有一张时不自动播放下一张
                    return;
                }
                //一段时间后尝试播放下一个
//                handler.sendEmptyMessageDelayed(MSG_PLAY_NEXT, controller.getDuration());
            } else {
                prepareLock = false;
                videoPos = 0;
                videoPlayer.setVideoPath(item.getPath());
            }

        } else {
            enterDetectStatus();
        }
    }

    /**
     * 播放上一个媒体资源
     */
    private void playLast() {
        //如果视频正在播放则停止
        if (!isFinishedImg) {
            return;
        }
        if (videoPlayer.isPlaying()) {
            videoPlayer.stop();
            videoPlayer.release();
        }
        MediaResourceItem item = controller.getLastItem();
        if (item != null && item.isValid()) {
            boolean isPicture = item.isPicture();
            if (isPicture && item.getPath().contains(".gif")) {
                isPicture = false;
                isGif = true;
                if (timeoutTimer != null) {
                    timeoutTimer.cancel();
                }
                timeoutTimer = new GifTimeoutTimer(controller.getDuration());
                timeoutTimer.start();
            } else {
                isGif = false;
                if (timeoutTimer != null) {
                    timeoutTimer.cancel();
                }
            }
            enterPlayStatus(isPicture);
            if (isPicture) {//播放图片
                if (controller.getPlayListSize() <= 1) {
                    return;
                }
                loadNextImage(item);
            } else {
                prepareLock = false;
                videoPos = 0;
                videoPlayer.setVideoPath(item.getPath());
            }

        } else {
            enterDetectStatus();
        }
    }

    //定时处理
    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (isPaused) {
                return false;
            }
            switch (msg.what) {
                case MSG_PLAY_NEXT://切换到下一个资源
                    if (!isWaitMusic) {
                        playNext();
                    }
                    break;
                case MSG_TRY_PLAY_NEXT://无资源时尝试重新加载
                    controller.enterRandomMode();
                    if (controller.getPlayListSize() == 0) {
                        handler.sendEmptyMessageDelayed(MSG_TRY_PLAY_NEXT, INTERVAL_RETRY);//定时再尝试一次
                    } else {
                        handler.removeMessages(MSG_TRY_PLAY_NEXT);
                        playNext();
                    }
                    break;
                case MSG_RESUME_PLAY://恢复播放
                    if (adType == MediaResourceItem.TYPE_VIDEO) {
                        enterPlayStatus(false);
                        videoPlayer.start();
                        if (videoPlayer.isSeekToValid()) {
                            videoPlayer.seekTo(videoPos);
                            videoPlayer.setMediaVolume(getMediaVolume());
                        } else {
                            playNext();
                        }
                        //设置播放结束回调，继续播放视频
//                        SysTTS.getInstance().setCallBack(new SysTTS.TTSEndCallBack() {
//                            @Override
//                            public void handle() {
//                                SysVoice.resetSysVolume();
//                            }
//                        });
                    } else {
                        handler.removeMessages(MSG_RESUME_PLAY);
                        playNext();
                    }
                    break;
                case MSG_PLAY_LAST:
                    playLast();
                    break;
                case MSG_PLAY_ERR:
                    tvTip.setText("该视频无法播放");
                    tvTip.setVisibility(View.VISIBLE);
                    videoPlayer.setView(View.GONE);
                    pager.setVisibility(View.GONE);
                    controller.enterRandomMode();
                    handler.removeMessages(MSG_PLAY_ERR);
                    handler.sendEmptyMessageDelayed(MSG_PLAY_NEXT, controller.getDuration());//定时再尝试一次
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    WeakRefHandler handler = new WeakRefHandler(callback);

    protected void handleDataMessage(Message msg) {
        //TODO: 增加从调度传递过来的start、stop等命令
        switch (msg.arg1) {
            case MSG_PAUSE_VIDEO:
                if (videoPlayer.isPlaying() && videoPlayer.canPause()) {
                    if (!isGif) {
                        videoPlayer.pause();
                        isVideoPause = true;
                    }
                }
                break;
            case MSG_RESUME_VIDEO:
                if (isVideoPause && !isPaused) {
                    SysVoice.resetSysVolume();
                    isVideoPause = false;
                    videoPlayer.start();
                    videoPlayer.setMediaVolume(getMediaVolume());
                }
                break;
            case MSG_PLAY_NEXT_NOW:
                handler.removeMessages(MSG_PLAY_NEXT);
                handler.sendEmptyMessageDelayed(AdPlayerActivity.MSG_PLAY_NEXT, INTERVAL_RETRY);
                break;
            case MSG_RESET_PLAY_NEXT:
                handler.removeMessages(MSG_RESUME_PLAY);
                handler.removeMessages(MSG_PLAY_NEXT);
                handler.sendEmptyMessageDelayed(AdPlayerActivity.MSG_PLAY_NEXT, INTERVAL_RETRY);
                break;
            case MSG_RESET_VIDEO_VOLUME:
                if (videoPlayer != null && videoPlayer.isPlaying()) {
                    SysVoice.resetSysVolume();
                    videoPlayer.setMediaVolume(getMediaVolume());
                }
                break;
        }
    }


    /**
     * 根据当前广告类型切换视图
     */
    private void switchView() {
        switch (adType) {
            case MediaResourceItem.TYPE_NONE://显示“无广告文件”的文本提示
                tvTip.setText("未找到广告资源文件");
                tvTip.setVisibility(View.VISIBLE);
                pager.setVisibility(View.GONE);
                videoPlayer.setView(View.GONE);
                break;
            case MediaResourceItem.TYPE_PICTURE://播放图片
                tvTip.setVisibility(View.GONE);
                pager.setVisibility(View.VISIBLE);
                videoPlayer.setView(View.GONE);
                break;
            case MediaResourceItem.TYPE_VIDEO://播放视频
                tvTip.setVisibility(View.GONE);
                pager.setVisibility(View.GONE);
                videoPlayer.setView(View.VISIBLE);
            default:
                break;
        }
    }


    /**
     * 当无广告资源时，定时检测尝试
     */
    private void enterDetectStatus() {
        adType = MediaResourceItem.TYPE_NONE;
        switchView();
        handler.sendEmptyMessageDelayed(MSG_TRY_PLAY_NEXT, INTERVAL_RETRY);//定时再尝试一次
    }

    /**
     * 进行播放状态
     */
    private void enterPlayStatus(boolean isPicture) {
        int newAdType = isPicture ? MediaResourceItem.TYPE_PICTURE : MediaResourceItem.TYPE_VIDEO;
        if (newAdType != adType) {
            adType = newAdType;
            switchView();
        }
    }

    /**
     * 加载下一张图片，返回缓存的视图
     *
     * @return 是否成功加载下一张
     */
    private void loadNextImage(final MediaResourceItem item) {
        try {
            isGif = false;
//            Bitmap bitmap = BitmapFactory.decodeFile(item.getPath());
            if (isFinishedImg) {
                isFinishedImg = false;
                mTask = new MyTask();
                mTask.execute(item);
            }
//            final ImageView view = isUsingBuffer1 ? imageViewBuffer2 : imageViewBuffer1;
//            Bitmap bitmap = decodeSampledBitmapFromResource(item, 1280, 960);
//            view.setImageBitmap(bitmap);
        } catch (OutOfMemoryError e) {//捕获oom错误
        }
    }


    VideoPlayerListener videoPlayerListener = new VideoPlayerListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        }

        @Override
        public void onCompletion(IMediaPlayer mp) {
            if (isGif) {
                videoPlayer.setVideoPath(videoPlayer.getLastPath());
            } else {
                playNext();
            }
        }

        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            //如果播放列表只有一个，表示指定视频播放，该视频无法播放，则直接进入随机播放
            if (AdPlayerController.getInstance().getPlayListSize() == 1) {
                handler.sendEmptyMessageDelayed(MSG_PLAY_ERR, INTERVAL_RETRY);
            } else {
                playNext();
            }
            return true;//返回true，以便不弹出“不能播放该视频”的提示
        }

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                //这里返回了视频旋转的角度，根据角度旋转视频到正确的画面
//                if (myTexture != null)
//                    myTexture.setRotation(extra);
            }
            return false;
        }

        @Override
        public void onPrepared(IMediaPlayer mp) {
            prepareLock = true;
            if (isPaused) {
                mp.pause();
            } else {//暂停时后台不播放
                float volume = getMediaVolume();
                mp.setVolume(volume, volume);
                mp.start();
                if (videoPos != 0) {
                    mp.seekTo(videoPos);
                }
            }
        }

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            mp.start();
        }

        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            //获取到视频的宽和高
        }
    };

    public float getMediaVolume() {
        float volume = SysConf.getMediaVolume();
        volume = volume / 15f;
        return volume;
    }


    /**
     * 准备好了再播放
     * <p>
     * /* @param mediaPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        prepareLock = true;
        if (!isPaused) {//暂停时后台不播放
            float volume = getMediaVolume();
            mediaPlayer.setVolume(volume, volume);
            mediaPlayer.start();
        }
    }

    /**
     * 当视频播放完成时，继续播放下一个
     * <p>
     * //* @param mediaPlayer
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //mediaPlayer.reset();
        playNext();
    }

    /**
     * 当视频播放发生错误时，也继续播放下一个
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        //如果播放列表只有一个，表示指定视频播放，该视频无法播放，则直接进入随机播放
        if (AdPlayerController.getInstance().getPlayListSize() == 1) {
            handler.sendEmptyMessageDelayed(MSG_PLAY_ERR, INTERVAL_RETRY);
        } else {
            playNext();
        }
        return true;//返回true，以便不弹出“不能播放该视频”的提示
    }

    /**
     * 键盘监听，用于界面操作
     *
     * @param v
     * @param keyCode
     * @param event
     * @return
     */
    private int lastKeyCode = -1;
    private int lastKeyAction = -1;

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == lastKeyCode && event.getAction() == lastKeyAction) {
            return true;
        }
        lastKeyCode = keyCode;
        lastKeyAction = event.getAction();
        //按钮间隔两秒则清空
        if ((System.currentTimeMillis() - mTime) > KEY_CODE_CLEAR) {
            keyString = "";
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTime = System.currentTimeMillis();
            keyString = keyString + KeyCodeUtil.keyCodeChangeStr(keyCode);
        }

        return false;
    }


    /*图片切换*/
    private class ScreenSlidePagerAdapter extends PagerAdapter {
        public final int COUNT_FIX = 2;      //容器大小


        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return COUNT_FIX;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            switch (position) {
                case 0:
                    container.removeView(imageViewBuffer1);
                    break;
                case 1:
                    container.removeView(imageViewBuffer2);
                    break;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0:
                    container.addView(imageViewBuffer1);
                    return imageViewBuffer1;
                case 1:
                    container.addView(imageViewBuffer2);
                    return imageViewBuffer2;
            }
            return null;
        }
    }

    private GestureDetector.OnGestureListener onGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                       float velocityY) {
                    float x = e2.getX() - e1.getX();
                    float y = e2.getY() - e1.getY();

                    if (x > 0) {
                        doResult(GESTURE_RIGHT);
                    } else if (x < 0) {
                        doResult(GESTURE_LEFT);
                    }
                    return true;
                }
            };

    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
        //        return true;
    }

    //传递到下一个View
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //按钮间隔两秒则清空
        if ((System.currentTimeMillis() - mTime) > KEY_CODE_CLEAR) {
            touchTime = 0;
        }
        //判断开启系统设置
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mTime = System.currentTimeMillis();
//            Logger.i("x:" + ev.getX());
//            Logger.i("y:" + ev.getY());
            if (touchTime == 0) {
                if (ev.getX() < 100 && ev.getY() < 100) {
                    touchTime = 1;
                }
            }
            if (touchTime == 1) {
                if (ev.getX() > 1000 && ev.getY() < 100) {
                    touchTime = 2;
                }
            }
            if (touchTime == 2) {
                if (ev.getX() > 1000 && ev.getY() > 600) {
                    touchTime = 3;
                }
            }

            if (touchTime == 3) {
                if (ev.getX() < 100 && ev.getY() > 600) {
                    popupWindow.showAtLocation(pager, Gravity.CENTER, 0, 0);
                }
            }

        }
        return gestureDetector.onTouchEvent(ev);
        //return true;
    }

    /**
     * 手势滑动结果操作
     *
     * @param action
     */
    public void doResult(int action) {
        //如果播放列表小于等于1则无法手势滑动
//        if (System.currentTimeMillis() - currentTime < 200) {
//            Logger.i("滑动太快");
//            return;
//        } else {
//            currentTime = System.currentTimeMillis();
//        }
        if (controller.getPlayListSize() <= 1) {
            return;
        }
        if (!prepareLock) {//上一个视频未准备好禁止切换，避免anr
            return;
        }
        switch (action) {
            //上一个
            case GESTURE_RIGHT:
                handler.removeMessages(MSG_PLAY_NEXT);
                playLast();
                break;
            //下一个
            case GESTURE_LEFT:
                handler.removeMessages(MSG_PLAY_NEXT);
                playNext();
                break;

        }
    }


    private boolean isFinishedImg = true;

    private class MyTask extends AsyncTask<MediaResourceItem, Integer, Bitmap> {


        // 方法1：onPreExecute（）
        // 作用：执行 线程任务前的操作
        // 注：根据需求复写
        @Override
        protected void onPreExecute() {
        }

        // 方法2：doInBackground（）
        // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
        // 注：必须复写，从而自定义线程任务
        @Override
        protected Bitmap doInBackground(MediaResourceItem... params) {
            return FileUtils.decodeSampledBitmapFromResource(params[0].getPath(), 1024, 600);
        }

        // 方法4：onPostExecute（）
        // 作用：接收线程任务执行结果、将执行结果显示到UI组件
        // 注：必须复写，从而自定义UI操作
        @Override
        protected void onPostExecute(Bitmap mBitmap) {
            try {
                ImageView view = isUsingBuffer1 ? imageViewBuffer2 : imageViewBuffer1;
                view.setImageBitmap(mBitmap);
                isUsingBuffer1 = !isUsingBuffer1;
//                pager.setPageTransformer(true, Transformer.getRandomTransformer());
                pager.setCurrentItem(isUsingBuffer1 ? 0 : 1, true);
                //一段时间后尝试播放下一个
                if (controller.getPlayListSize() > 1) {
                    handler.sendEmptyMessageDelayed(MSG_PLAY_NEXT, controller.getDuration());
                }
                isFinishedImg = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 实现倒计时内部类
     */
    public class GifTimeoutTimer extends CountDownTimer {

        public GifTimeoutTimer(int millisInFuture) {
            super((long) (millisInFuture), 1000);
        }

        public void onFinish() {
            if (isGif) {
                isGif = false;
                if (!isPaused) {
                    playNext();
                }

            }
        }

        public void onTick(long millisUntilFinished) {
            long time = millisUntilFinished / 1000;
        }
    }

}
