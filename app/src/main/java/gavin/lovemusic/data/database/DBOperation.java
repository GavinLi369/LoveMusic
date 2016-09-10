package gavin.lovemusic.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import gavin.lovemusic.App;
import gavin.lovemusic.data.Music;
import gavin.lovemusic.utils.FileUtils;

/**
 * Created by Gavin on 2015/11/12.
 *
 */
public class DBOperation {
    public static final String DATABASE_NAME = "LoveMusicDataBase";
    public static final String TABLE_NAME = "MusicInfoList";
    public static final String ID = "_ID";
    public static final String PATH = "Path";
    public static final String NAME = "Name";
    public static final String ARTIST = "Artist";
    public static final String ALBUM_NAME = "Album_Name";
    public static final String ALBUM = "Album";
    public static final String DURATION = "Duration";

    private Context mContext;
    private SQLiteDatabase mSqLiteDatabase;

    public DBOperation(Context context){
        mContext = context;
    }

    public void openOrCreateDataBase(){
        DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext, DATABASE_NAME, null, 1);
        mSqLiteDatabase = dbOpenHelper.getWritableDatabase();
    }

    public Cursor selectAll(){
        if (mSqLiteDatabase != null) {
            return mSqLiteDatabase.query(TABLE_NAME,
                    new String[]{NAME, PATH},
                    null, null, null, null, null);
        }

        return null;
    }

    public Cursor selectMusicInfo(String[] columns, String selection, String[] selectionArgs){
        return selectMusicInfo(columns, selection, selectionArgs, null, null, null);
    }

    public Cursor selectMusicInfo(String[] columns, String selection, String[] selectionArgs,
                                  String groupBy, String having, String orderBy){
        if (mSqLiteDatabase != null){
            return mSqLiteDatabase.query(TABLE_NAME, columns,
                    selection, selectionArgs, groupBy, having, orderBy);
        }

        return null;
    }

    public void insertMusicInfo(Music music){
        if (mSqLiteDatabase != null && music != null) {
            ContentValues values = new ContentValues();
            values.put(PATH, music.getMusicPath());
            values.put(NAME, music.getMusicName());
            values.put(ARTIST, music.getArtist());
            values.put(ALBUM_NAME, music.getAlbumName());
            values.put(DURATION, music.getDuration());
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    values.put(ALBUM, writeAlbum2SDCard(music.getAlbumByID3V2()).getPath());
//
//                }
//            }).start();
            mSqLiteDatabase.insert(TABLE_NAME, "", values);
        }
    }

    private static File writeAlbum2SDCard(Bitmap album){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        album.compress(Bitmap.CompressFormat.JPEG, 100, out);
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        return FileUtils.write2SDCard
                (App.APP_DIR + "/Album/", "" + System.currentTimeMillis(), inputStream);

    }

    public void closeDataBase(){
        mSqLiteDatabase.close();
        mSqLiteDatabase = null;
    }
}
