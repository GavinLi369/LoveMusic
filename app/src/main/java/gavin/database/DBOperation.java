package gavin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.util.Log;

import gavin.model.MusicInfo;

/**
 * Created by Gavin on 2015/11/12.
 *
 */
public class DBOperation {
    public static final String DATABASE_NAME = "LoveMusicDataBase";
    public static final String TABLE_NAME = "MusicInfoList";
    public static final String ID = "_ID";
    public static final String NAME = "Name";
    public static final String ALBUM = "Album";

    private Context mContext;
    private SQLiteDatabase sqLiteDatabase;

    public DBOperation(Context context){
        mContext = context;
    }

    public void openOrCreateDataBase(){
        DBOpenHelper dbOpenHelper = new DBOpenHelper(mContext, DATABASE_NAME, null, 1);
        sqLiteDatabase = dbOpenHelper.getWritableDatabase();
    }

//    public Cursor selectAll(){
//        if (sqLiteDatabase != null) {
//            return sqLiteDatabase.query(TABLE_NAME,
//                    new String[]{NAME, ALBUM},
//                    null, null, null, null, null);
//        }
//
//        return null;
//    }

    public Cursor selectMusicInfo(String selection, String[] selectionArgs){
        return selectMusicInfo(selection, selectionArgs, null, null, null);
    }

    public Cursor selectMusicInfo(String selection, String[] selectionArgs,
                                  String groupBy, String having, String orderBy){
        if (sqLiteDatabase != null){
            return sqLiteDatabase.query(TABLE_NAME, new String[]{NAME, ALBUM},
                    selection, selectionArgs, groupBy, having, orderBy);
        }

        return null;
    }

    public long insertMusicInfo(MusicInfo musicInfo){
        if (sqLiteDatabase != null && musicInfo != null) {
            ContentValues values = new ContentValues();
            values.put(NAME, musicInfo.getMusicName());
            values.put(ALBUM, musicInfo.getAlbumByteArray());
            return sqLiteDatabase.insert(TABLE_NAME, "", values);
        }

        return -1;
    }

    public void closeDataBase(){
        sqLiteDatabase.close();
        sqLiteDatabase = null;
    }
}
