package gavin.lovemusic.entity;

/**
 * Created by Gavin on 2015/8/23.
 * MP3歌曲模型
 */
public class Music {
    private String title;   //歌曲名称
    private String artist;   //歌手名称
    private String album;    //歌曲专辑名称
    private long duration;        //歌曲时长
    private String image;  //专辑封面路径
    private String path;

    public String getImage() {
        return image;
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

    public void setImage(String image) {
        this.image = image;
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

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Music)) return false;

        Music music = (Music) o;

        if (title != null ? !title.equals(music.title) : music.title != null) return false;
        if (artist != null ? !artist.equals(music.artist) : music.artist != null) return false;
        return album != null ? album.equals(music.album) : music.album == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        return result;
    }
}
