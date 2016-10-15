package gavin.lovemusic.detailmusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gavin.lovemusic.entity.LyricRow;
import gavin.lovemusic.entity.Music;

/**
 * Created by Gavin on 2015/8/24.
 * 歌词实例
 */
public class LyricBuilder {
    private String lyricStr = "";
    private ArrayList<LyricRow> lyricList;
    private Music music;

    public LyricBuilder(Music music) {
        this.music = music;
    }

    public ArrayList<LyricRow> build() {
        File lrcFile = getLrcOfSong(music.getPath());
        lyricStr = parseFile2String(lrcFile);
        parseLyric();
        return lyricList;
    }

    /**
     * 根据歌曲文件名和歌曲路径获得Lrc文件
     */
    private File getLrcOfSong(String path) {
        String lrcFileName = path.replace(".mp3", ".lrc").replace("/Music", "/MusicLrc").replace(" - ", " - ");
        return new File(lrcFileName);
    }

    /**
     * 将文本文件转换为String
     */
    private String parseFile2String(File file) {
        if (file == null) {
            return "";
        }

        BufferedReader reader;
        String line;
        String buffer = "";
        try {
            reader = new BufferedReader
                    (new InputStreamReader(new FileInputStream(file), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                buffer = buffer + line + "\n";
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    /**
     * 将歌词文件解析，并存入map
     */
    private void parseLyric() {
        if (lyricStr.equals("")) {
            return;
        }
        lyricList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\d{2}:\\d{2}\\.\\d{2,3})\\]([^\\n]+)");
        Matcher matcher = pattern.matcher(lyricStr);
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
