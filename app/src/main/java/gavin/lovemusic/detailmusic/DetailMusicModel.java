package gavin.lovemusic.detailmusic;

import android.content.Context;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.App;
import gavin.lovemusic.service.Music;
import gavin.lovemusic.service.MusicDao;
import gavin.lovemusic.util.QqMusicUtil;

/**
 * Created by GavinLi
 * on 16-10-24.
 */

public class DetailMusicModel implements DetailMusicContract.Model {
    private Context mContext;
    private MusicDao mMusicDao;

    public DetailMusicModel(Context context) {
        this.mContext = context;
    }

    @Override
    public ArrayList<LyricRow> getMusicLyric(Music music) throws IOException, JSONException {
        String lyric = getLyricByLocal(music);
        if(lyric.isEmpty()) {
            lyric = getLyricByNetwork(music);
            if(lyric.isEmpty()) throw new IOException("this music doesn't have the lyric");
            String lyricPath = saveLyric(System.currentTimeMillis() + ".lrc", lyric);
            music.setLyricPath(lyricPath);
            mMusicDao.insertOrReplace(music);
        }
        return new LyricBuilder(lyric).build();
    }

    private String getLyricByLocal(Music music) {
        mMusicDao = ((App) mContext.getApplicationContext()).getLyricSession().getMusicDao();
        List<Music> musics = mMusicDao.queryBuilder()
                .where(MusicDao.Properties.Title.eq(music.getTitle()))
                .where(MusicDao.Properties.Album.eq(music.getAlbum()))
                .build()
                .list();
        if(!musics.isEmpty() && musics.get(0).getLyricPath() != null) {
            return parseFile2String(new File(musics.get(0).getLyricPath()));
        } else {
            return "";
        }
    }

    private String getLyricByNetwork(Music music) throws IOException, JSONException {
        return new QqMusicUtil().getLyric(music.getTitle(), music.getArtist());
    }

    private String saveLyric(String name, String lyric) {
        File file = new File(App.APP_DIR + File.separator + "MusicLrc" + File.separator + name);
        try {
            if(!file.exists()) {
                if(!file.createNewFile()) throw new IOException();
            }
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(lyric);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    /**
     * 将文本文件转换为String
     */
    private String parseFile2String(File file) {
        try {
            String line, buffer = "";
            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(new FileInputStream(file), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                buffer = buffer + line + "\n";
            }
            reader.close();
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
