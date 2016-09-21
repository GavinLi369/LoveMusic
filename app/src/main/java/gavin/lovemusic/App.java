package gavin.lovemusic;

import android.app.Application;
import android.os.Environment;
import android.support.annotation.ColorInt;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi on 16-9-10.
 *
 */
public class App extends Application {
    private static String mExternalStorage = Environment.getExternalStorageDirectory().getPath();
    public static final String APP_DIR = mExternalStorage + File.separator + "LoveMusic";

    private ArrayList<Music> musicList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            checkAppDir();       //检查软件目录
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.init();
    }

    /**
     * 检查软件目录是否存在,如果不存在则创建该目录
     */
    private void checkAppDir() throws IOException {
        File dir = new File(APP_DIR + File.separator + "Music");
        if (!dir.exists()) {
            if(!dir.mkdirs())
                throw new IOException("App dir can't make");
        }
        dir = new File(APP_DIR + File.separator + "Album");
        if (!dir.exists()) {
            if(!dir.mkdirs())
                throw new IOException("App dir can't make");
        }
    }

    public void setMusicList(ArrayList<Music> musicList) {
        this.musicList = musicList;
    }

    public ArrayList<Music> getMusicList(){
        return musicList;
    }
}
