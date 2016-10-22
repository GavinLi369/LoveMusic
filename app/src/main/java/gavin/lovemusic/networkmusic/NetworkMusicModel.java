package gavin.lovemusic.networkmusic;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import gavin.lovemusic.App;
import gavin.lovemusic.entity.DaoSession;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.entity.MusicDao;

/**
 * Created by GavinLi
 * on 16-10-21.
 */
public class NetworkMusicModel implements NetworkMusicContract.Model {
    private DongtingApi dongtingApi = new DongtingApi();

    @Override
    public ArrayList<Music> getBillboardHot(Context context, int size, int offset) throws IOException {
        ArrayList<Music> musics = dongtingApi.getBillboardHot(size, offset);
        DaoSession daoSession = ((App) context.getApplicationContext()).getDaoSession();
        MusicDao musicDao = daoSession.getMusicDao();
        for(Music music : musics) {
            List<Music> cacheMusics = musicDao.queryBuilder()
                    .where(MusicDao.Properties.Id.eq(music.getId()))
                    .build()
                    .list();
            if(cacheMusics.isEmpty() || !new File(cacheMusics.get(0).getImage()).exists()) {
                try {
                    String imagePath = saveMusicImage(context, music);
                    music.setImage(imagePath);
                    musicDao.insert(music);
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                music.setImage(cacheMusics.get(0).getImage());
            }
        }
        return musics;
    }

    private String saveMusicImage(Context context, Music music) throws IOException, InterruptedException, ExecutionException {
        Bitmap bitmap = Glide.with(context).load(music.getImage()).asBitmap().into(-1, -1).get();
        File file = new File(App.APP_DIR + File.separator + "Cache" + File.separator + Long.toString(music.getId()));
        if(!file.exists()) {
            if(!file.createNewFile())
                throw new IOException("file can not create");
        }
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        out.close();
        return file.getPath();
    }
}
