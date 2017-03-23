package gavin.lovemusic.localmusic;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.service.Music;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class LocalMusicContract {
    interface Model {
        ArrayList<Music> getMusicList();

        void refreshMusicList() throws IOException;
    }

    interface View extends BaseView<Presenter> {
        void setMusicListView(List<Music> musicList);

        void hideRefreshing();
    }

    interface Presenter extends BasePresenter {
        void startNewMusic(List<Music> musics, int postion);

        void refreshMusicList(Context context);
    }
}
