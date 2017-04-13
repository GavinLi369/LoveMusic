package gavin.lovemusic.localmusic;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import gavin.lovemusic.App;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.entity.MusicDao;
import gavin.lovemusic.entity.MusicFactory;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class LocalMusicModel implements LocalMusicContract.Model {
    private Context mContext;
    private MusicDao mMusicDao;
    private FileScannerLinsenter mLinsenter;
    private boolean isCancalScanning = false;

    public LocalMusicModel(Context context) {
        this.mContext = context;
        mMusicDao = ((App) mContext.getApplicationContext()).getMusicSession().getMusicDao();
    }

    @Override
    public List<Music> getMusicList() {
        return getMusicByDataBase();
    }

    @Override
    public void refreshMusicList() {
        File filePath = new File(App.APP_DIR + "/Album");
        File[] files = filePath.listFiles();
        for(File file : files) {
            if(!file.delete()) throw new RuntimeException("file can not been deleted");
        }

        List<Music> musicList = getMusicFromSdcard();
        mMusicDao.deleteAll();
        mMusicDao.insertInTx(musicList);
        isCancalScanning = false;
    }

    @Override
    public void cancalScanning() {
        isCancalScanning = true;
    }

    private List<Music> getMusicFromSdcard() {
        List<Music> musics = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().getPath();
        File rootFile = new File(path);
        Queue<File> files = new LinkedList<>();
        files.add(rootFile);
        while(!files.isEmpty()) {
            if(isCancalScanning) return musics;
            File file = files.remove();
            if(mLinsenter != null) mLinsenter.onScanning(file.getPath());
            if(file.isDirectory()) {
                Collections.addAll(files, file.listFiles());
            } else if(file.getName().endsWith(".mp3")) {
                try {
                    Music music = MusicFactory.create(mContext, file.getPath());
                    if(music != null) musics.add(music);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return musics;
    }

    private ArrayList<Music> getMusicByDataBase() {
        return new ArrayList<>(mMusicDao.queryBuilder().build().list());
    }

    public void setFileScannerLinsenter(FileScannerLinsenter fileScannerLinsenter) {
        mLinsenter = fileScannerLinsenter;
    }

    public interface FileScannerLinsenter {
        void onScanning(String path);
    }
}
