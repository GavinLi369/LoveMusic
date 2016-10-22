package gavin.lovemusic;

import android.app.Application;
import android.os.Environment;

import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.IOException;

import gavin.lovemusic.entity.DaoMaster;
import gavin.lovemusic.entity.DaoMaster.DevOpenHelper;
import gavin.lovemusic.entity.DaoSession;

/**
 * Created by GavinLi on 16-9-10.
 *
 */
public class App extends Application {
    private static String sExternalStorage = Environment.getExternalStorageDirectory().getPath();
    public static final String APP_DIR = sExternalStorage + File.separator + "LoveMusic";

    private DaoSession mDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            checkAppDir();       //检查软件目录
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.init();

        DevOpenHelper helper = new DevOpenHelper(this, "music-db");
        Database db = helper.getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
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
        dir = new File(APP_DIR + File.separator + "MusicLrc");
        if (!dir.exists()) {
            if(!dir.mkdirs())
                throw new IOException("App dir can't make");
        }
    }
}
