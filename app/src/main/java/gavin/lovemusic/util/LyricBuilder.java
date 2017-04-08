package gavin.lovemusic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gavin.lovemusic.detailmusic.view.LyricRow;

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
        Pattern pattern = Pattern.compile("\\[(\\d{2}:\\d{2}\\.\\d{2,3})](\\[(\\d{2}:\\d{2}\\.\\d{2,3})])*([^\\n]*)");
        Matcher matcher = pattern.matcher(mLyric);
        while (matcher.find()) {
            //歌词不能为空
            if(matcher.group(4) == null || matcher.group(4).equals("")) continue;
            LyricRow lyricRow = new LyricRow(parseDateTime(matcher.group(1)), matcher.group(4));
            lyricList.add(lyricRow);
            //同一句歌词可能会有两个时间点
            if(matcher.group(2) != null && !matcher.group(2).equals("")) {
                lyricRow = new LyricRow(parseDateTime(matcher.group(3)), matcher.group(4));
                lyricList.add(lyricRow);
            }
        }
        Collections.sort(lyricList);
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
