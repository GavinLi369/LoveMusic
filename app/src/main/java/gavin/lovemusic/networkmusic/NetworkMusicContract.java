package gavin.lovemusic.networkmusic;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-24.
 */
public class NetworkMusicContract {
    interface Model {
        ArrayList<Music> getBillboardHot(Context context, int size, int offset) throws IOException;
    }

    interface View extends BaseView<Presenter> {
        void showMoreMusics(ArrayList<Music> musics);

        void showRefreshView();

        void hideRefreshView();

        void showNetworkConnetionError();
    }

    interface Presenter extends BasePresenter {
        void refreshMusicList();

        void loadMoreMusic();
    }
}
