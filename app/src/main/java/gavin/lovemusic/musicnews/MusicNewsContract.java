package gavin.lovemusic.musicnews;

import java.io.IOException;
import java.util.List;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;

/**
 * Created by GavinLi
 * on 4/5/17.
 */

public class MusicNewsContract {
    interface Model {
        List<NewsEntry> getNews() throws IOException;
    }

    interface View extends BaseView<Presenter> {
        void showNews(List<NewsEntry> newsEntries);

        void showNetworkError();
    }

    interface Presenter extends BasePresenter {
        void loadNews();
    }
}
