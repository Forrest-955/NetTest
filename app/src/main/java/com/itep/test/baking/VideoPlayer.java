package com.itep.test.baking;

import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.itep.mt.common.util.FileTypeUtil;

import java.io.File;
import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 通用视频播放管理
 * Created by cy on 2019/7/15.
 */

public class VideoPlayer {

    private IMediaPlayer mMediaPlayer = null;

    private SurfaceView surfaceView;

    private CustomerVideoView videoView;

    private VideoPlayerListener listener;

    private boolean videoType;//是否为wmv,决定使用的播放器和播放视图

    private String lastPath;

    public VideoPlayer(SurfaceView surfaceView, CustomerVideoView videoView) {
        this.surfaceView = surfaceView;
        this.videoView = videoView;
    }

    /**
     * 设置视频地址。
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        stop();
        release();
        String ext = FileTypeUtil.getFileExtName(path).toUpperCase();
        //if (ext.equals(".WMV")||ext.equals(".MPG")||ext.equals(".MPEG")) {//使用ijkPlayer
        videoType = true;
        load(path);
        //        } else {//使用系统MediaPlayer
        //            videoType=false;
        //            Uri uri= Uri.parse(path);
        //            videoView.setVideoURI(uri);
        //        }
        setView(View.VISIBLE);
    }


    /**
     * 加载视频
     */
    private void load(String path) {
        lastPath = path;
        //每次都要重新创建IMediaPlayer
        createPlayer();
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setVolume(0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //给mediaPlayer设置视图
        mMediaPlayer.setDisplay(surfaceView.getHolder());

        mMediaPlayer.prepareAsync();

        surfaceView.getHolder().addCallback(surfaceCallback);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
    }

    /**
     * 创建一个新的player
     */
    private void createPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.setDisplay(null);
            mMediaPlayer.release();
        }
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        //IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);//日志显示等级

        //不开启硬解码
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
        //开启硬解码
        ///*
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        //开启跳帧，解决音画不同步问题
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzeduration",1);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);//0开启，画面质量高，解码开销大48关闭，画面质量差点，解码开销小
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_YV12);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "analyzeduration", "2000000");
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "probsize", "4096");
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);//0开启，画面质量高，解码开销大48关闭，画面质量差点，解码开销小
        //*/
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videotoolbox", 1);//0为软解,1为硬解

        mMediaPlayer = ijkMediaPlayer;

        if (listener != null) {
            mMediaPlayer.setOnPreparedListener(listener);
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.setOnInfoListener(listener);
            mMediaPlayer.setOnSeekCompleteListener(listener);
            mMediaPlayer.setOnBufferingUpdateListener(listener);
            mMediaPlayer.setOnErrorListener(listener);
        }
    }

    public void setListener(VideoPlayerListener listener) {
        this.listener = listener;
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnPreparedListener(listener);
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.setOnInfoListener(listener);
            mMediaPlayer.setOnSeekCompleteListener(listener);
            mMediaPlayer.setOnBufferingUpdateListener(listener);
            mMediaPlayer.setOnErrorListener(listener);
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //避免视频开头瞬间花屏
            holder.setFormat(PixelFormat.TRANSPARENT);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //避免切回视频播放黑屏
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(holder);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            //避免在后台播放视频
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }

        }
    };

    /**
     * -------======--------- 下面封装了一下控制视频的方法
     */

    public void start() {
        if (videoType) {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }
        } else {
            videoView.start();
        }
    }

    public void release() {
        if (videoType) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } else {
            videoView.stopPlayback();
        }
    }

    public boolean canPause() {
        if (videoType) {
            return mMediaPlayer != null;
        } else {
            return videoView.canPause();
        }
    }

    public void pause() {
        if (videoType) {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        } else {
            videoView.pause();
        }
    }

    public void stop() {
        if (videoType) {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
        } else {
            videoView.stopPlayback();
        }
    }


    public long getCurrentPosition() {
        if (videoType) {
            if (mMediaPlayer != null) {
                return mMediaPlayer.getCurrentPosition();
            } else {
                return 0;
            }
        } else {
            return videoView.getCurrentPosition();
        }
    }


    public void seekTo(long l) {
        if (videoType) {
            if (mMediaPlayer != null) {
                load(lastPath);
                mMediaPlayer.seekTo(l);
            }
        } else {
            videoView.seekTo((int) l);
        }
    }

    public boolean isPlaying() {
        if (videoType) {
            if (mMediaPlayer != null) {
                return mMediaPlayer.isPlaying();
            }
            return false;
        } else {
            return videoView.isPlaying();
        }
    }

    public void setView(int status) {
        if (status == View.VISIBLE) {
            surfaceView.setVisibility(videoType ? status : View.GONE);
            videoView.setVisibility(!videoType ? status : View.GONE);
        } else {
            surfaceView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
        }
    }

    /**
     * 是否指定播放位置有效
     *
     * @return
     */
    public boolean isSeekToValid() {
        File file = new File(lastPath);
        return file.exists();
    }


    public void setMediaVolume(float volume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    public String getLastPath() {
        return lastPath;
    }

    public void setLastPath(String lastPath) {
        this.lastPath = lastPath;
    }
}
