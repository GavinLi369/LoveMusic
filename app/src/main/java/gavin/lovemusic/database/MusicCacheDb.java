package gavin.lovemusic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by GavinLi
 * on 4/28/17.
 */

public class MusicCacheDb extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Entry.TABLE_NAME + " ("
            + Entry.ID + TEXT_TYPE + COMMA_SEP
            + Entry.TITLE + TEXT_TYPE + COMMA_SEP
            + Entry.ARTIST + TEXT_TYPE + COMMA_SEP
            + Entry.ALBUM + TEXT_TYPE + COMMA_SEP
            + Entry.IMAGE + TEXT_TYPE + COMMA_SEP
            + Entry.PATH + TEXT_TYPE + COMMA_SEP
            + Entry.ALIVE + " BIGINT )";
    private static final String SQL_DELET_ENTRIES =
            "DROP TABLE IF EXISTS " + Entry.TABLE_NAME;

    private static final int DATABASE_VERSION = 1;
    private static final String DATEBASE_NAME = "cache.db";

    public MusicCacheDb(Context context) {
        super(context, DATEBASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELET_ENTRIES);
        onCreate(db);
    }

    public static class Entry {
        public static final String TABLE_NAME = "MUSIC";
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String ARTIST = "artist";
        public static final String ALBUM = "album";
        public static final String IMAGE = "image";
        public static final String PATH = "path";
        public static final String ALIVE = "alive";
    }
}
