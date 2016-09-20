package gavin.lovemusic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Gavin on 2015/11/12.
 *
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DBOperation.TABLE_NAME + "(" +
                DBOperation.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBOperation.NAME + " VARCHAR NOT NULL," +
                DBOperation.PATH + " VARCHAR," +
                DBOperation.ARTIST + " VARCHAR," +
                DBOperation.ALBUM_NAME + " VARCHAR," +
                DBOperation.ALBUM + " VARCHAR," +
                DBOperation.DURATION + " INTEGER);");
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
