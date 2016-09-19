package gavin.lovemusic.localmusic.model;

import android.content.Context;
import android.database.Cursor;

import java.io.File;
import java.util.ArrayList;

import gavin.lovemusic.App;
import gavin.lovemusic.data.Music;
import gavin.lovemusic.data.database.DBOperation;
import gavin.lovemusic.utils.FileUtils;

/**
 * Created by GavinLi on 16-9-10.
 * MusicListModel
 */
public class MusicListModel implements IMusicListModel {
    private ArrayList<Music> musicList;

    @Override
    public ArrayList<Music> getMusicList(Context context) {
        return ((App) context.getApplicationContext()).getMusicList();
    }

    @Override
    public void refreshMusicList(Context context) {
        App app = (App) context.getApplicationContext();
        app.setMusicList(getMusicFromSDCard(context));
        for (int i = 0; i < app.getMusicList().size(); i++) {
            app.getMusicList().get(i).setId(i);
        }
    }

    private ArrayList<Music> getMusicFromSDCard(Context context) {
        return FileUtils.getSongFiles(App.APP_DIR, context);
    }

    private ArrayList<Music> getMusicByDataBase(Context context){
        ArrayList<Music> musicList = new ArrayList<>();
        DBOperation dbOperation = new DBOperation(context);
        dbOperation.openOrCreateDataBase();
        Cursor cursor = dbOperation.selectAll();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                File file = new File(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.PATH)));
                Music music = new Music(file, context);
                musicList.add(music);
            } while (cursor.moveToNext());
            cursor.close();
        }
        dbOperation.closeDataBase();
        return musicList;
    }
}
