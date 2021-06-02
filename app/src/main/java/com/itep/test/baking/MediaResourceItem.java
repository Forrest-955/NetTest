package com.itep.test.baking;

import java.io.File;

/**
 * 多媒体播放资源项
 */

public class MediaResourceItem {
    public final static int TYPE_NONE = 0;        //无效的类型
    public final static int TYPE_PICTURE = 1;     //图片类型
    public final static int TYPE_VIDEO = 2;       //视频类型

    private String path;        //文件路径
    private int type;           //文件类型， 可以是图片或视频
    private int duration = 0;   //播放后的休息时间，只对播放图片有效

    public MediaResourceItem(String path, int type) {
        this.path = path;
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }

    public int getType() {
        return type;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * 判断文件是否存在，决定是否是有效的资源
     *
     * @return true表示有效的资源
     */
    public boolean isValid() {
        File file = new File(path);
        return file.exists();
    }

    /**
     * 是否为图片资源
     *
     * @return true表示图片
     */
    public boolean isPicture() {
        return type == TYPE_PICTURE;
    }

}
