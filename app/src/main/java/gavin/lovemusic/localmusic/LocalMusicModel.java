package gavin.lovemusic.localmusic;

import android.content.Context;
import android.database.Cursor;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.App;
import gavin.lovemusic.database.DBOperation;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class LocalMusicModel implements LocalMusicContract.Model {
    public LocalMusicModel(Context context) {
        App app = (App) context.getApplicationContext();
        app.setMusicList(getMusicByDataBase(context));
        for (int i = 0; i < app.getMusicList().size(); i++) {
            app.getMusicList().get(i).setId(i);
        }
    }

    @Override
    public ArrayList<Music> getMusicList(Context context) {
        return getMusicByDataBase(context);
    }

    @Override
    public void refreshMusicList(Context context) throws IOException {
        File filePath = new File(App.APP_DIR + "/Album");
        File[] files = filePath.listFiles();
        for(File file : files) {
            if(!file.delete())
                throw new IOException("can't clean album bitmap");
        }

        ArrayList<Music> musicList = getMusicFromSDCard();
        DBOperation dbOperation = new DBOperation(context);
        dbOperation.openOrCreateDataBase();
        dbOperation.cleanDataBase();
        for (int i = 0; i < musicList.size(); i++) {
            musicList.get(i).setId(i);
            dbOperation.insertMusicInfo(musicList.get(i));
        }
        dbOperation.closeDataBase();

        App app = (App) context.getApplicationContext();
        app.setMusicList(musicList);
    }

    private ArrayList<Music> getMusicFromSDCard() {
        ArrayList<Music> musicList = new ArrayList<>();
        File filePath = new File(App.APP_DIR + "/Music");
        File[] files = filePath.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                try {
                    Mp3File mp3File = new Mp3File(file.getAbsoluteFile());
                    if (mp3File.hasId3v2Tag()) {
                        Music music = new Music(file);
                        ID3v2 id3v2 = mp3File.getId3v2Tag();
                        music.setTitle(id3v2.getTitle());
                        music.setArtist(id3v2.getArtist());
                        music.setAlbum(id3v2.getAlbum());
                        music.setDuration(mp3File.getLengthInMilliseconds());
                        music.setAlbumPath(writeAlbum2SDCard(id3v2.getAlbumImage()));
                        musicList.add(music);
                    }
                } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                    e.printStackTrace();
                }
            }
        }

        return musicList;
    }

    private String writeAlbum2SDCard(byte[] ablumByte){
        String filePath = App.APP_DIR + "/Album/" + System.currentTimeMillis();
        FileOutputStream out = null;
        try {
            File albumFile = new File(filePath);
            if(albumFile.createNewFile()) {
                out = new FileOutputStream(albumFile);
                out.write(ablumByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    private ArrayList<Music> getMusicByDataBase(Context context){
        ArrayList<Music> musicList = new ArrayList<>();
        DBOperation dbOperation = new DBOperation(context);
        dbOperation.openOrCreateDataBase();
        Cursor cursor = dbOperation.selectAll();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                File file = new File(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.PATH)));
                Music music = new Music(file);
                music.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.NAME)));
                music.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.ARTIST)));
                music.setAlbumPath(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.ALBUM)));
                music.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.ALBUM_NAME)));
                music.setDuration(cursor.getLong(cursor.getColumnIndexOrThrow(DBOperation.DURATION)));
                musicList.add(music);
            } while (cursor.moveToNext());
            cursor.close();
        }
        dbOperation.closeDataBase();
        return musicList;
    }
}
