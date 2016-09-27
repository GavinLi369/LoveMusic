package gavin.lovemusic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import gavin.lovemusic.entity.Music;

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
        return selectMusicInfo(
                new String[]{NAME, PATH, ARTIST, ALBUM_NAME, ALBUM, DURATION},
                null, null);
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
            values.put(PATH, music.getPath());
            values.put(NAME, music.getTitle());
            values.put(ARTIST, music.getArtist());
            values.put(ALBUM_NAME, music.getAlbum());
            values.put(DURATION, music.getDuration());
            values.put(ALBUM, music.getImage());
            mSqLiteDatabase.insert(TABLE_NAME, "", values);
        }
    }

    public void cleanDataBase() {
        if(mSqLiteDatabase != null) {
            mSqLiteDatabase.delete(TABLE_NAME, null, null);
        }
    }

    public void closeDataBase(){
        mSqLiteDatabase.close();
        mSqLiteDatabase = null;
    }
}
