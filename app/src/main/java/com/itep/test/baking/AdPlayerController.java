package com.itep.test.baking;

import com.itep.mt.common.app.AppController;
import com.itep.mt.common.constant.AppConstant;
import com.itep.mt.common.sys.SysConf;
import com.itep.mt.common.util.FileTypeUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 图片和视频播放列表控制
 */

public class AdPlayerController {
    //常量
    public static int MODE_RANDOM = 0;     //随机播放模式
    public static int MODE_ORDER = 1;      //指定播放列表模式
    public static int MODE_MIX = 2;      //混合播放模式
    public final static int MODE_ORDER_PICTURE_ALL = 1;      //指定播放列表模式,全部图片播放
    public final static int MODE_ORDER_PICTURE_SINGLE = 2;   //指定播放列表模式,单个图片播放
    public final static int MODE_ORDER_VIDEO_ALL = 3;       //指定播放列表模式，全部视频播放
    public final static int MODE_ORDER_VIDEO_SINGLE = 4;    //指定播放列表模式，单个视频播放
    public final static int MODE_ORDER_MIX = 5;             //指定播放列表模式，混合播放
    private static int DEF_DURATION = 5000; //默认图片切换时间，默认为5秒
    private static int INIT_POS = -1;       //播放序号


    //类成员变量
    private ArrayList<MediaResourceItem> playlist = new ArrayList<MediaResourceItem>();   //播放列表
    private int mode = MODE_RANDOM;                                 //播放模式，分为随机和指定两种
    private int order_mode = MODE_ORDER_PICTURE_ALL;               //指定播放模式，分为指定图片，全部图片，指定视频，全部视频，混合五种
    private int duration = DEF_DURATION;                            //图片切换时间
    private int pos = INIT_POS;                                     //当前正在播放的项

    //单一实例
    private static AdPlayerController _instance;

    private static String[] PICTURE_FOMAT_SUPPORT_LIST =
            { ".bmp", ".jpg", ".jpeg", ".gif", ".png" };//图片格式支持列表
    private static String[] VIDEO_FOMAT_SUPPORT_LIST =
            { ".avi", ".mp4",".mpg", ".mpeg", ".wmv", ".rm", ".rmvb", ".flv", ".vob" };  //视频图片格式支持列表

    private AdPlayerController() {
    }

    /**
     * 获得播放控制器单一实例
     *
     * @return 播放控制器单一实例
     */
    public static AdPlayerController getInstance() {
        if (_instance == null) {
            _instance = new AdPlayerController();
        }
        return _instance;
    }

    /**
     * 开始随机播放模式
     */
    public void enterRandomMode() {
        mode = MODE_RANDOM;
        refreshPlayList();
    }

    /**
     * 开始指定播放模式
     */
    public void enterOrderMode(String filePath, int order_mode) {
        mode = MODE_ORDER;
        this.order_mode = order_mode;
        refreshPlayList(filePath);
    }

    /**
     * 开始混合播放模式
     */
    public void enterMixtureMode() {
        mode = MODE_MIX;
        jsyh_setPlay();
    }

    /**
     * 马上播放下一个，用于更新指定播放操作
     */
    public void playNext() {
        //开始播放
        pos = INIT_POS;
        AppController.postExtraData(AdPlayerActivity.MSG_PLAY_NEXT_NOW, null);
    }

    /**
     * 重置播放下一个
     */
    public void resetPlayNext(){
        AppController.postExtraData(AdPlayerActivity.MSG_RESET_PLAY_NEXT, null);
    }

    /**
     * 是否处于随机播放模式
     *
     * @return true表示是处于随机播放模式
     */
    public boolean isRandomMode() {
        return mode == MODE_RANDOM;
    }

    /**
     * 得到播放列表大小
     *
     * @return 播放列表大小
     */
    public int getPlayListSize() {
        return playlist.size();
    }

    /**
     * 得到图片播放间隔时间，单位是毫秒
     *
     * @return 图片播放间隔时间
     */
    public int getDuration() {
        //Logger.i("getPicturePlaybackInterval:"+SysConf.getPicturePlaybackInterval());
        return SysConf.getPicturePlaybackInterval();
    }

    /**
     * 设置图片播放间隔时间，单位是毫秒
     *
     * @param duration 图片播放间隔时间
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * 从播放列表中取出一个有效的播放资源，并返回
     *
     * @return 媒体播放资源
     */
    public MediaResourceItem getNextItem() {
        int count = playlist.size();

        //若之前不存在播放列表，则自动更新
        if (count == 0) {
            enterRandomMode();//播放列表为空，则自动进入随机模式
            pos = INIT_POS;
        } else if (!hasMoreValidItem()) {//若整个列表已播放完毕,则也更新
            //若是随机播放模式，则每次播放完成都重新更新列表；若是指定文件播放，则重新播放
            if (isRandomMode()) {
                refreshPlayList();
                pos = INIT_POS;
            } else {
                if (mode == MODE_ORDER) {
                    switch (order_mode) {
                        //全部图片播放则更新图片
                        case MODE_ORDER_PICTURE_ALL:
                            playlist.clear();
                            playlist.addAll(getPictureList());
                            break;
                        //全部视频播放则更新视频
                        case MODE_ORDER_VIDEO_ALL:
                            playlist.clear();
                            playlist.addAll(getVideoList());
                            break;
                        default:
                            break;
                    }
                }
                pos = INIT_POS;
                //若处于特定文件播放模式，而无有效播放文件，则自动进入随机模式
                if (!hasMoreValidItem()) {
                    enterRandomMode();
                }//else 无需处理
            }
        }//else 继续

        count = playlist.size();
        if (count > 0) {
            //寻找到一条有效的资源并返回
            for (pos = pos + 1; pos < count; pos++) {
                MediaResourceItem item = playlist.get(pos);
                if (item.isValid()) {
                    item.setDuration(duration);//更新播放间隔设置
                    return item;
                }
            }
        }//else 无资源可播
        return null;
    }

    /**
     * 从播放列表中取出上一个有效的播放资源，并返回
     *
     * @return 媒体播放资源
     */
    public MediaResourceItem getLastItem() {
        int count = playlist.size();

        //若之前不存在播放列表，则自动更新
        if (count == 0) {
            enterRandomMode();//播放列表为空，则自动进入随机模式
            pos = INIT_POS;
        } else if (!hasMoreValidItem()) {//若整个列表已播放完毕,则也更新
            //若是随机播放模式，则每次播放完成都重新更新列表；若是指定文件播放，则重新播放
            if (isRandomMode()) {
                refreshPlayList();
                pos = INIT_POS;
            } else {
                pos = INIT_POS;

                //若处于特定文件播放模式，而无有效播放文件，则自动进入随机模式
                if (!hasMoreValidItem()) {
                    enterRandomMode();
                }//else 无需处理
            }
        }//else 继续

        count = playlist.size();
        if (count > 0) {
            //寻找到一条有效的资源并返回
            for (pos = pos - 1; pos < count; pos--) {
                //小于0则倒过来计算
                if (pos < 0) {
                    pos = count + pos;
                }
                MediaResourceItem item = playlist.get(pos);
                if (item.isValid()) {
                    item.setDuration(duration);//更新播放间隔设置
                    return item;
                }
            }
        }//else 无资源可播
        return null;
    }

    /**
     * 从当前POS开始查找，看是否能找到有效的资源
     *
     * @return true表示能够找到
     */
    private boolean hasMoreValidItem() {
        int count = playlist.size();
        for (int i = pos + 1; i < count; i++) {
            MediaResourceItem item = playlist.get(i);
            if (item.isValid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 刷新生成新的播放列表
     */
    public void refreshPlayList() {
        playlist.clear();
        playlist.addAll(getPictureList());
        playlist.addAll(getVideoList());
    }

    /**
     * 更新播放
     */
    public void updatePlay(){
        if(pos >= playlist.size() || pos <0){//越界判断
            return ;
        }
        MediaResourceItem item=playlist.get(pos);
        if(item!=null){
            if(!item.isValid()){
                playNext();
            }
        }
    }


    /**
     * 刷新生成指定播放列表
     */
    private void refreshPlayList(String filePath) {
        playlist.clear();
        switch (order_mode) {
            case MODE_ORDER_PICTURE_SINGLE:
                playlist.add(getPicture(filePath));
                break;
            case MODE_ORDER_PICTURE_ALL:
                playlist.addAll(getPictureList());
                break;
            case MODE_ORDER_VIDEO_SINGLE:
                playlist.add(getVideo(filePath));
                break;
            case MODE_ORDER_VIDEO_ALL:
                playlist.addAll(getVideoList());
                break;
            case MODE_ORDER_MIX:
                playlist.addAll(getMixList(filePath));
                break;
        }
    }

    private void jsyh_setPlay() {
        String filePath = AppConstant.PATH_MEDIA_ROOT + "jsyhfile/" + SysConf.get_JSYH_SET_DIS_SUBJ();
        playlist.clear();
        playlist.addAll(getJSYHMixList(filePath));


    }

    /**
     * 从图片文件夹遍历图片，生成列表返回
     *
     * @return 图片列表
     */
    public ArrayList<MediaResourceItem> getPictureList() {
        ArrayList<MediaResourceItem> list = new ArrayList<MediaResourceItem>();

        File dir = new File(AppConstant.PATH_PICTURE_ROOT);
        if (dir.exists() && dir.isDirectory()) {

            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (!f.isFile() || !FileTypeUtil.isPictureFile(f.getName())) {
                    continue;
                }//else 是普通文件，则继续

                list.add(new MediaResourceItem(f.getAbsolutePath(), MediaResourceItem.TYPE_PICTURE));
            }
        }
        return list;
    }

    /**
     * 根据路径返回单个图片
     *
     * @return 图片
     */
    private MediaResourceItem getPicture(String filePath) {

        File f = new File(filePath);
        if (!f.isFile() || !FileTypeUtil.isPictureFile(f.getName())) {
            return null;
        }//else 是普通文件，则继续

        return new MediaResourceItem(f.getAbsolutePath(), MediaResourceItem.TYPE_PICTURE);
    }


    /**
     * 从视频文件夹遍历视频文件，生成列表返回
     *
     * @return 视频列表
     */
    private ArrayList<MediaResourceItem> getVideoList() {
        ArrayList<MediaResourceItem> list = new ArrayList<MediaResourceItem>();

        File dir = new File(AppConstant.PATH_VIDEO_ROOT);
        if (dir.exists() && dir.isDirectory()) {

            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (!f.isFile() || !FileTypeUtil.isVideoFile(f.getName())) {
                    continue;
                }//else 是普通文件，则继续

                list.add(new MediaResourceItem(f.getAbsolutePath(), MediaResourceItem.TYPE_VIDEO));
            }
        }
        return list;
    }

    /**
     * 根据路径返回单个视频
     *
     * @return 图片
     */
    private MediaResourceItem getVideo(String filePath) {

        File f = new File(filePath);
        if (!f.isFile() || !FileTypeUtil.isVideoFile(f.getName())) {
            return null;
        }//else 是普通文件，则继续

        return new MediaResourceItem(f.getAbsolutePath(), MediaResourceItem.TYPE_VIDEO);
    }

    /**
     * 根据文件名判断文件是否是视频文件
     * @param name 视频文件名称
     * @return true表示是视频
     */
    public boolean isVideoFile(String name){
        int n = name.lastIndexOf('.');
        if( n != -1 && n < name.length() ){
            String extName = name.substring(n).toLowerCase();
            for( int i = 0; i < VIDEO_FOMAT_SUPPORT_LIST.length; i++ ){
                if( extName.compareTo(VIDEO_FOMAT_SUPPORT_LIST[i]) == 0){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 根据指定列表，用逗号隔开，生成混合列表
     *
     * @return 混合列表
     */
    private ArrayList<MediaResourceItem> getMixList(String fileNameList) {
        ArrayList<MediaResourceItem> list = new ArrayList<MediaResourceItem>();

        String[] splitString = fileNameList.split(",", -1);
        for (int i = 0; i < splitString.length; i++) {
            //判断文件名是否是视频
            if (FileTypeUtil.isVideoFile(splitString[i])) {
                File f = new File(AppConstant.PATH_VIDEO_ROOT + splitString[i]);
                if (!f.isFile()) {
                    continue;
                }//else 是普通文件，则继续
                list.add(new MediaResourceItem(f.getAbsolutePath(), MediaResourceItem.TYPE_VIDEO));
            } else
                //判断文件名是否是视频
                if (FileTypeUtil.isPictureFile(splitString[i])) {
                    File f = new File(AppConstant.PATH_PICTURE_ROOT + splitString[i]);
                    if (!f.isFile()) {
                        continue;
                    }//else 是普通文件，则继续
                    list.add(new MediaResourceItem(f.getAbsolutePath(), MediaResourceItem.TYPE_PICTURE));
                } else {
                    continue;
                }
        }
        return list;
    }

    private ArrayList<MediaResourceItem> getJSYHMixList(String fileAbsolutePath) {
        ArrayList<MediaResourceItem> list = new ArrayList<MediaResourceItem>();
        File file = new File(fileAbsolutePath);
        ;
        if (!file.exists()) {
            file = new File(fileAbsolutePath.substring(0, fileAbsolutePath.lastIndexOf("/")) + "/default");//建设默认目录
            if (!file.exists()) {
                file = new File(AppConstant.PATH_PICTURE_ROOT);
            } else {

            }
        }
        File[] subFile = file.listFiles();
        String allFilePath = "";
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                //判断文件名是否是视频
                if (FileTypeUtil.isVideoFile(subFile[iFileLength].getName())) {
                    list.add(new MediaResourceItem(subFile[iFileLength].getAbsolutePath(), MediaResourceItem.TYPE_VIDEO));
                } else
                    //判断文件名是否是视频
                    if (FileTypeUtil.isPictureFile(subFile[iFileLength].getName())) {
                        list.add(new MediaResourceItem(subFile[iFileLength].getAbsolutePath(), MediaResourceItem.TYPE_PICTURE));
                    }
            }
        }
        return list;
    }

    /**
     * 根据文件名判断文件是否是图片文件
     * @param name 图片文件名称
     * @return true表示是图片
     */
    public boolean isPictureFile(String name){
        int n = name.lastIndexOf('.');
        if( n != -1 && n < name.length() ){
            String extName = name.substring(n).toLowerCase();
            for( int i = 0; i < PICTURE_FOMAT_SUPPORT_LIST.length; i++ ){
                if( extName.compareTo(PICTURE_FOMAT_SUPPORT_LIST[i]) == 0){
                    return true;
                }
            }
        }
        return false;
    }
}
