package gavin.lovemusic.entity;

import java.io.File;

/**
 * Created by Gavin on 2015/8/23.
 * MP3歌曲模型
 */
public class Music {
    private String title;   //歌曲名称
    private String artist;   //歌手名称
    private String album;    //歌曲专辑名称
    private long duration;        //歌曲时长
    private String albumPath;  //专辑封面路径

    //为了在歌曲列表中定位，特意设定歌曲ID，用于在下一首或上一首时定位
    private int id;                         //歌曲Id
    private File musicFile;                 //歌曲文件

    public Music(File musicFile) {
        this.musicFile = musicFile;
    }

    public String getAlbumPath() {
        return albumPath;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setAlbumPath(String albumPath) {
        this.albumPath = albumPath;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
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
