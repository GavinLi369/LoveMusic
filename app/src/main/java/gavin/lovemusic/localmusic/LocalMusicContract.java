package gavin.lovemusic.localmusic;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class LocalMusicContract {
    interface Model {
        ArrayList<Music> getMusicList(Context context);

        void refreshMusicList(Context context) throws IOException;
    }

    interface View extends BaseView<Presenter> {
        void setMusicListView(ArrayList<Music> musicList);

        void hideRefreshing();
    }

    interface Presenter extends BasePresenter {
        void playNewMusic(int postion);

        void refreshMusicList(Context context);
    }
}
