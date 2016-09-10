package gavin.lovemusic;

import android.app.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import gavin.lovemusic.data.Music;
import gavin.lovemusic.utils.FileUtils;

/**
 * Created by GavinLi on 16-9-10.
 *
 */
public class App extends Application {
    public static final String APP_DIR = File.separator + "sdcard" + File.separator + "LoveMusic";

    private ArrayList<Music> musicList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        checkAppDir();       //检查软件目录
    }

    /**
     * 检查软件目录是否存在,如果不存在则创建该目录
     */
    private void checkAppDir() {
        if (!FileUtils.isFileExist(APP_DIR + "/Music")) {
            try {
                FileUtils.createDirOnSDCard(APP_DIR + "/Music");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!FileUtils.isFileExist(APP_DIR + File.separator + "Album")) {
            try {
                FileUtils.createDirOnSDCard(APP_DIR + File.separator + "Album");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMusicList(ArrayList<Music> musicList) {
        this.musicList = musicList;
    }

    public ArrayList<Music> getMusicList(){
        return musicList;
    }
}
