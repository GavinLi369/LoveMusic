package gavin.lovemusic.entity;

/**
 * Created by Gavin on 2015/8/26.
 * 每一句歌词
 */
public class LyricRow {
    /**
     * 歌词内容
      */
    private String lyricStr;

    /**
     * 歌词开始时间
     */
    private long lyricTime;

    /**
     * 构造方法
     */
    public LyricRow(long lyricTime, String lyricStr) {
        this.lyricStr = lyricStr;
        this.lyricTime = lyricTime;
    }

    public String getLyricStr() {
        return lyricStr;
    }

    public long getLyricTime() {
        return lyricTime;
    }
}
