package gavin.lovemusic.musicnews;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 4/5/17.
 */

public class MusicNewsPresenter implements MusicNewsContract.Presenter {
    private MusicNewsContract.View mView;
    private MusicNewsContract.Model mModel;

    public MusicNewsPresenter(MusicNewsContract.View view, MusicNewsContract.Model model) {
        mView = view;
        mModel = model;
        mView.setPresenter(this);
    }

    @Override
    public void loadNews() {
        Observable.create((Observable.OnSubscribe<List<NewsEntry>>) subscriber -> {
            try {
                subscriber.onNext(mModel.getNews());
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newsEntries -> {
                    mView.showNews(newsEntries);
                }, throwable -> {
                    if(throwable instanceof IOException)
                        mView.showNetworkError();
                });
    }
}
