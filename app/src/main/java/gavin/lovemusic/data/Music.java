package gavin.lovemusic.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import gavin.lovemusic.data.database.DBOperation;
import gavin.lovemusic.model.Mp3ID3V2;

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
            Mp3ID3V2 mp3ID3V2 = new Mp3ID3V2(new FileInputStream(musicFile));
            musicName = mp3ID3V2.getTitle();
            artist = mp3ID3V2.getArtist();
            albumName = mp3ID3V2.getTitle();
            mp3ID3V2.close();

            /**
             * Mp3文件的ID3V2标签中没有歌曲长度，
             * 所以通过MediaMetadataRetriever解析mp3文件获取
             */
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getMusicPath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Long.parseLong(durationStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getAlbum() {
        Bitmap bitmap = null;
        try {
            Mp3ID3V2 mp3ID3V2 = new Mp3ID3V2(new FileInputStream(musicFile));
            byte[] albumArray = mp3ID3V2.getAlbumByteArray();
            bitmap = BitmapFactory.decodeByteArray(albumArray, 0, albumArray.length);
        } catch (IOException e) {
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
