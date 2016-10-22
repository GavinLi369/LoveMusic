package gavin.lovemusic.localmusic;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;

import com.bumptech.glide.Glide;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import gavin.lovemusic.App;
import gavin.lovemusic.service.MusicListUpdateEvent;
import gavin.lovemusic.database.DBOperation;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class LocalMusicModel implements LocalMusicContract.Model {
    private Context mContext;

    public LocalMusicModel(Context context) {
        this.mContext = context;
    }

    @Override
    public ArrayList<Music> getMusicList() {
        return getMusicByDataBase(mContext);
    }

    @Override
    public void refreshMusicList() throws IOException {
        File filePath = new File(App.APP_DIR + "/Album");
        File[] files = filePath.listFiles();
        for(File file : files) {
            if(!file.delete())
                throw new IOException("can't clean album bitmap");
        }

        ArrayList<Music> musicList = getMusicFromContentResolver(Media.EXTERNAL_CONTENT_URI);
        musicList.addAll(getMusicFromContentResolver(Media.INTERNAL_CONTENT_URI));
        DBOperation dbOperation = new DBOperation(mContext);
        dbOperation.openOrCreateDataBase();
        dbOperation.cleanDataBase();
        for (int i = 0; i < musicList.size(); i++) {
            dbOperation.insertMusicInfo(musicList.get(i));
        }
        dbOperation.closeDataBase();
        EventBus.getDefault().post(new MusicListUpdateEvent(getMusicByDataBase(mContext)));
    }

    private ArrayList<Music> getMusicFromContentResolver(Uri uri) {
        ArrayList<Music> musics = new ArrayList<>();
        Cursor cursor = mContext.getContentResolver().query(uri,
                new String[] {Media.DATA}, Media.IS_MUSIC + " = 1", null, Media._ID);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    File file = new File(cursor.getString(cursor.getColumnIndex(Media.DATA)));
                    if (file.getName().endsWith(".mp3")) {
                        try {
                            Mp3File mp3File = new Mp3File(file.getAbsoluteFile());
                            if (mp3File.hasId3v2Tag()) {
                                Music music = new Music();
                                ID3v2 id3v2 = mp3File.getId3v2Tag();
                                music.setPath(file.getPath());
                                music.setTitle(id3v2.getTitle());
                                music.setArtist(id3v2.getArtist());
                                music.setAlbum(id3v2.getAlbum());
                                music.setDuration(mp3File.getLengthInMilliseconds());
                                music.setImage(writeAlbum2SDCard(id3v2.getAlbumImage()));
                                musics.add(music);
                            }
                        } catch (IOException | UnsupportedTagException | InvalidDataException
                                | InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return musics;
    }

    private String writeAlbum2SDCard(byte[] ablumByte) throws IOException, InterruptedException, ExecutionException{
        if(ablumByte != null) {
            String filePath = App.APP_DIR + "/Album/" + System.currentTimeMillis();
            Bitmap ablum = Glide.with(mContext).load(ablumByte).asBitmap().into(-1, -1).get();
            FileOutputStream out = new FileOutputStream(filePath);
            ablum.compress(Bitmap.CompressFormat.JPEG, 80, out);
            ablum.recycle();
            out.close();
            return filePath;
        } else {
            throw new InterruptedIOException();
        }
    }

    private ArrayList<Music> getMusicByDataBase(Context context){
        ArrayList<Music> musicList = new ArrayList<>();
        DBOperation dbOperation = new DBOperation(context);
        dbOperation.openOrCreateDataBase();
        Cursor cursor = dbOperation.selectAll();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Music music = new Music();
                music.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.PATH)));
                music.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.NAME)));
                music.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.ARTIST)));
                music.setImage(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.ALBUM)));
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
