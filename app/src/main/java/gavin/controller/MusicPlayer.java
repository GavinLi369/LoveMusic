package gavin.controller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import gavin.activity.BaseActivity;
import gavin.activity.MainActivity;
import gavin.constant.AppConstant;
import gavin.database.DBOperation;
import gavin.model.MusicInfo;
import gavin.service.ActivityCommand;
import gavin.service.PlayService;
import gavin.utils.FileUtils;

/**
 * Created by Gavin Li on 2016/1/20.
 *
 */
public class MusicPlayer {
    private static MusicPlayer musicPlayer = new MusicPlayer();

    private static Context mContext;
    private static BaseActivity mBaseActivity;
    /**
     * 为了调用MainActivity的setSongListView方法
     * 目前还没有想到更好的办法
     */
    private static MainActivity mainActivity;

    private static ArrayList<MusicInfo> musicList = new ArrayList<>();

    private Handler mHandler= new UIHandler();

    private MusicPlayer(){
        checkAppDir();       //检查软件目录
    }

    public static MusicPlayer getInstance(){
        return musicPlayer;
    }

    public static MusicPlayer getInstance(Context context){
        if (context instanceof BaseActivity) {
            mBaseActivity = (BaseActivity)context;
            if (context instanceof MainActivity) {
                mainActivity = (MainActivity)context;
            }
        }
        mContext = context;
        return musicPlayer;
    }

    private static class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mainActivity.setSongListView();
                    mainActivity.updateUI();
                    break;
                default:
            }
        }
    }

    /**
     * 初始化歌曲List
     */
    public void initMusicList(){
        musicList = getMusicByDataBase();
        for (int i = 0; i < musicList.size(); i++) {
            musicList.get(i).setId(i);
        }
        initService();
    }

    public ArrayList<MusicInfo> getMusicList(){
        return musicList;
    }

    /**
     * 创建歌曲播放服务，等待下一步指令
     */
    private void initService() {
        int musicId = 0;
        int playMode = 0;
        SharedPreferences sharedPreferences =
                mContext.getSharedPreferences("service_info", Service.MODE_PRIVATE);
        if (sharedPreferences != null) {
            musicId = sharedPreferences.getInt("musicId", 0);
            playMode = sharedPreferences.getInt("playMode", 0);
        }

        Intent intent = new Intent(mContext, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.INIT_SERVICE);
        intent.putExtra("musicId", musicId);
        intent.putExtra("playMode", playMode);
        mContext.startService(intent);
    }

    /**
     * 开始一首新的歌曲
     */
    public void startMusic() {
        Intent intent = new Intent(mContext, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.PLAY_MUSIC);
        mContext.startService(intent);
    }

    /**
     * 暂停后开始播放歌曲
     */
    public void resumeMusic() {
        Intent intent = new Intent(mContext, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.RESUME_MUSIC);
        mContext.startService(intent);
    }

    /**
     * 暂停正在播放的歌曲
     */
    public void pauseMusic() {
        Intent intent = new Intent(mContext, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.PAUSE_MUSIC);
        mContext.startService(intent);
    }

    /**
     * 播放上一首歌曲
     */
    public void previousMusic() {
        Intent intent = new Intent(mContext, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.PREVIOUS_MUSIC);
        mContext.startService(intent);
    }

    /**
     * 播放下一首歌曲
     */
    public void nextMusic() {
        Intent intent = new Intent(mContext, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.NEXT_MUSIC);
        mContext.startService(intent);
    }

    /**
     * 设置播放进度
     */
    public void setMusicProgress(int progress) {
        Intent intent = new Intent(mContext, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.CHANGE_PROGRESS);
        intent.putExtra("progress", progress);
        mContext.startService(intent);
    }

    public void refreshMusicList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                musicList = getMusicFromSDCard();
                for (int i = 0; i < musicList.size(); i++) {
                    musicList.get(i).setId(i);
                }
                initService();
                Message message = new Message();
                message.what = 0;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 当歌曲状态改变
     */
    public void musicStatusChanged(){
        mBaseActivity.updateUI();
    }

    public void serviceCreated(){
        mainActivity.setSongListView();
        mainActivity.updateUI();
    }

    /**
     * 获取手机里的所有歌曲，并将其添加进List
     */
    private ArrayList<MusicInfo> getMusicFromSDCard() {
        return FileUtils.getSongFiles("/storage/", mContext);
    }

    private ArrayList<MusicInfo> getMusicByDataBase(){
        ArrayList<MusicInfo> musicList = new ArrayList<>();
        DBOperation dbOperation = new DBOperation(mContext);
        dbOperation.openOrCreateDataBase();
        Cursor cursor = dbOperation.selectAll();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                File file = new File(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.PATH)));
                MusicInfo musicInfo = new MusicInfo(file, mContext);
                musicList.add(musicInfo);
            } while (cursor.moveToNext());
            cursor.close();
        }
        dbOperation.closeDataBase();
        return musicList;
    }

    /**
     * 检查软件目录是否存在,如果不存在则创建该目录
     */
    private void checkAppDir() {
        if (!FileUtils.isFileExist(AppConstant.APP_DIR + "/Music")) {
            try {
                FileUtils.createDirOnSDCard(AppConstant.APP_DIR + "/Music");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!FileUtils.isFileExist(AppConstant.APP_DIR + File.separator + "Album")) {
            try {
                FileUtils.createDirOnSDCard(AppConstant.APP_DIR + File.separator + "Album");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
