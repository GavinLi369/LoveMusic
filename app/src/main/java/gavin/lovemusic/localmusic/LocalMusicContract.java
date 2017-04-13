package gavin.lovemusic.localmusic;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class LocalMusicContract {
    interface Model {
        List<Music> getMusicList();

        void refreshMusicList();

        void cancalScanning();

        void setFileScannerLinsenter(LocalMusicModel.FileScannerLinsenter fileScannerLinsenter);
    }

    interface View extends BaseView<Presenter> {
        void setMusicListView(List<Music> musicList);

        void hideRefreshing();

        void showScanningFile();

        void updateScanningFile(String path);

        void removeScanningFile();
    }

    interface Presenter extends BasePresenter {
        void startNewMusic(List<Music> musics, int postion);

        void loadMusicList();

        void refreshMusicList(Context context);

        void cancalScanning();
    }
}
