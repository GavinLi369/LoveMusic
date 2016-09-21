package gavin.lovemusic.detailmusic.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gavin.lovemusic.entity.Music;

/**
 * Created by Gavin on 2015/8/24.
 * 歌词实例
 */
public class Lyric {
    private String lyricStr = "";
    private ArrayList<LyricContent> lyricList = null;
    private Music music;

    public Lyric(Music music) {
        this.music = music;
        init();
    }

    /**
     * 获取歌词List
     */
    public ArrayList<LyricContent> getLyricList() {
        return lyricList;
    }

    /**
     * 根据歌曲文件名和歌曲路径获得Lrc文件
     */
    public File getLrcOfSong(String path) {
        String lrcFileName = path.replace(".mp3", ".lrc").replace("/Music", "/Musiclrc").replace(" - ", "-");
        return new File(lrcFileName);
    }

    /**
     * 初始化歌词
     */
    private void init() {
        File lrcFile = getLrcOfSong(music.getMusicPath());
        lyricStr = parseFile2String(lrcFile);
        parseLyric();
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
            LyricContent lyricContent = new LyricContent
                    (parseDateTime(matcher.group(1)), matcher.group(2));
            lyricList.add(lyricContent);
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
