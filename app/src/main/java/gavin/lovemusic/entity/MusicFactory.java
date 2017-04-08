package gavin.lovemusic.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import gavin.lovemusic.App;

/**
 * Created by GavinLi
 * on 4/8/17.
 */

public class MusicFactory {
    public static @Nullable Music create(Context context, @NonNull String path)
            throws IOException {
        try {
            Mp3File mp3File = new Mp3File(path);
            if (!mp3File.hasId3v2Tag()) return null;
            Music music = new Music();
            ID3v2 id3v2 = mp3File.getId3v2Tag();
            music.setPath(path);
            music.setTitle(id3v2.getTitle());
            music.setArtist(id3v2.getArtist());
            music.setAlbum(id3v2.getAlbum());
            music.setDuration(mp3File.getLengthInMilliseconds());
            music.setImage(writeAlbum2SDCard(context, id3v2.getAlbumImage()));
            return music;
        } catch (InvalidDataException |
                InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
            throw new RuntimeException("该文件不是合法的mp3文件");
        }
    }

    private static String writeAlbum2SDCard(Context context, @NonNull byte[] ablumByte)
            throws IOException, InterruptedException, ExecutionException {
        String filePath = App.APP_DIR + "/Album/" + System.currentTimeMillis();
        Bitmap ablum = Glide.with(context).load(ablumByte).asBitmap().into(-1, -1).get();
        FileOutputStream out = new FileOutputStream(filePath);
        ablum.compress(Bitmap.CompressFormat.JPEG, 80, out);
        out.close();
        return filePath;
    }
}
