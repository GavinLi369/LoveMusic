package gavin.lovemusic.networkmusic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-24.
 */
public class NetworkMusicContract {
    interface Model {
        ArrayList<Music> getBillboardHot(int size, int offset) throws IOException;
    }

    interface View extends BaseView<Presenter> {
        void showMoreMusics(ArrayList<Music> musics);

        void showRefreshView();

        void hideRefreshView();

        void showNetworkConnetionError();
    }

    interface Presenter extends BasePresenter {
        void loadMusics();

        void refreshMusicList();

        void loadMoreMusic();

        void startNewMusic(List<Music> musics, int postion);
    }
}
