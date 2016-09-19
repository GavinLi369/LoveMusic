package gavin.lovemusic.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;

import gavin.lovemusic.data.database.DBOperation;

/**
 * Created by Gavin on 2015/8/23.
 * MP3歌曲模型
 */
public class Music {
    private String musicName;   //歌曲名称
    private String artist;   //歌手名称
    private String albumName;    //歌曲专辑名称
    private long duration;        //歌曲时长

    /**
     * 为了在歌曲列表中定位，特意设定歌曲ID，用于在下一首或上一首时定位
     */
    private int id;                  //歌曲Id
    private File musicFile;                 //歌曲文件
    private Context mContext;                //上下文

    /**
     * SongInfo的构造方法
     * @param musicFile music文件
     */
    public Music(File musicFile, Context context) {
        this.musicFile = musicFile;
        mContext = context;
        init();
    }

    public void init() {
        DBOperation dbOperation = new DBOperation(mContext);
        dbOperation.openOrCreateDataBase();
        Cursor cursor = dbOperation.selectMusicInfo(new String[]{DBOperation.PATH, DBOperation.NAME,
                        DBOperation.ARTIST, DBOperation.ALBUM_NAME, DBOperation.DURATION, DBOperation.ALBUM},
                DBOperation.PATH + "=?", new String[]{getMusicPath()});
        if (cursor != null && cursor.moveToFirst()) {
            musicName = cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.NAME));
            artist = cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.ARTIST));
            albumName = cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.ALBUM_NAME));
            duration = cursor.getLong(cursor.getColumnIndexOrThrow(DBOperation.DURATION));
            cursor.close();
        } else {
            initByMusicFile();
            dbOperation.insertMusicInfo(this);
        }
        dbOperation.closeDataBase();
    }

    private void initByMusicFile() {
        try {
            Mp3File mp3File = new Mp3File(musicFile);
            if(mp3File.hasId3v2Tag()) {
                ID3v2 id3v2 = mp3File.getId3v2Tag();
                musicName = id3v2.getTitle();
                artist = id3v2.getArtist();
                albumName = id3v2.getAlbum();
                duration = id3v2.getLength();
            }
        } catch(IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getAlbum() {
        Bitmap bitmap = null;
        try {
            Mp3File mp3File = new Mp3File(musicFile);
            if(mp3File.hasId3v2Tag()) {
                byte[] albumArray = mp3File.getId3v2Tag().getAlbumImage();
                bitmap = BitmapFactory.decodeByteArray(albumArray, 0, albumArray.length);
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public String getMusicName() {
        return musicName;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumName() {
        return albumName;
    }

    public long getDuration() {
        return duration;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getMusicPath() {
        return musicFile.getPath();
    }
}
