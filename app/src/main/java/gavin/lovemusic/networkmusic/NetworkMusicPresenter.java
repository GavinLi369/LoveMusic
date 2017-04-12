package gavin.lovemusic.networkmusic;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.PlayService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-9-24.
 */
public class NetworkMusicPresenter implements NetworkMusicContract.Presenter {
    private NetworkMusicContract.View mView;
    private NetworkMusicContract.Model mModel;
    private PlayService mPlayService;

    private int mIndex = 0;

    public NetworkMusicPresenter(NetworkMusicContract.View view,
                                 NetworkMusicContract.Model model,
                                 PlayService playService) {
        mView = view;
        mModel = model;
        mPlayService = playService;
        mView.setPresenter(this);
    }

    @Override
    public void loadMusics() {
        mView.showRefreshView();
        refreshMusicList();
    }

    @Override
    public void refreshMusicList() {
        Observable
                .create((Observable.OnSubscribe<List<Music>>) subscriber -> {
                    List<Music> musics = new ArrayList<>();
                    try {
                        musics.addAll(mModel.getHotMusic(10, 0));
                        subscriber.onNext(musics);
                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(musics -> {
                    mView.showMoreMusics(musics);
                    mView.hideRefreshView();
                }, throwable -> {
                    if(throwable instanceof IOException) {
                        mView.showNetworkConnetionError();
                        mView.hideRefreshView();
                    }
                });
    }

    @Override
    public void loadMoreMusic() {
        Observable
                .create((Observable.OnSubscribe<List<Music>>) subscriber -> {
                    List<Music> musics = new ArrayList<>();
                    try {
                        musics.addAll(mModel.getHotMusic(10, mIndex));
                        subscriber.onNext(musics);
                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(musics -> {
                    mView.showMoreMusics(musics);
                }, throwable -> {
                    if(throwable instanceof IOException)
                        mView.showNetworkConnetionError();
                });
        mIndex += 10;
    }

    @Override
    public void startNewMusic(List<Music> musics, int postion) {
        if(!mPlayService.containsMusic(musics.get(postion)))
            mPlayService.initMusic(musics);
        mPlayService.changeMusic(musics.get(postion));
    }
}
