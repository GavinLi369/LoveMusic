package gavin.lovemusic.detailmusic;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gavin.lovemusic.entity.LyricRow;

/**
 * Created by Gavin on 2015/8/24.
 * 歌词实例
 */
public class LyricBuilder {
    private String mLyric = "";
    private ArrayList<LyricRow> lyricList;

    public LyricBuilder(String lyric) {
        this.mLyric = lyric;
    }

    public ArrayList<LyricRow> build() {
        parseLyric();
        return lyricList;
    }

    /**
     * 将歌词文件解析，并存入map
     */
    private void parseLyric() {
        if (mLyric.equals("")) {
            return;
        }
        lyricList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\d{2}:\\d{2}\\.\\d{2,3})\\]([^\\n]+)");
        Matcher matcher = pattern.matcher(mLyric);
        while (matcher.find()) {
            LyricRow lyricRow = new LyricRow
                    (parseDateTime(matcher.group(1)), matcher.group(2));
            lyricList.add(lyricRow);
        }
    }

    /**
     * 将String类型的时间解析为long类型
     */
    private long parseDateTime(String dateTime) {
        if (dateTime == null) {
            return -1;
        }
        float minute = 0;
        float second = 0;
        Pattern pattern = Pattern.compile("(\\d{2}):(\\d{2}\\.\\d{2,3})");
        Matcher matcher = pattern.matcher(dateTime);
        while (matcher.find()) {
            minute = Float.parseFloat(matcher.group(1));
            second = Float.parseFloat(matcher.group(2));
        }

        return (long) ((minute * 60 + second) * 1000);
    }
}
