package gavin.lovemusic.networkmusic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.service.Music;
import gavin.lovemusic.service.PlayService;
import rx.Observable;
import rx.Subscriber;
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
        Observable<ArrayList<Music>> observable = Observable.create((Observable.OnSubscribe<ArrayList<Music>>) subscriber -> {
            ArrayList<Music> musics = new ArrayList<>();
            mIndex = 0;
            try {
                musics.addAll(mModel.getBillboardHot(10, mIndex++));
                subscriber.onNext(musics);
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Subscriber<ArrayList<Music>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mView.showNetworkConnetionError();
                mView.hideRefreshView();
            }

            @Override
            public void onNext(ArrayList<Music> musics) {
                mView.showMoreMusics(musics);
                mView.hideRefreshView();
            }
        });
    }

    @Override
    public void loadMoreMusic() {
        Observable<ArrayList<Music>> observable = Observable.create((Observable.OnSubscribe<ArrayList<Music>>) subscriber -> {
            ArrayList<Music> musics = new ArrayList<>();
            try {
                musics.addAll(mModel.getBillboardHot(10, mIndex++));
                subscriber.onNext(musics);
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Subscriber<ArrayList<Music>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mView.showNetworkConnetionError();
            }

            @Override
            public void onNext(ArrayList<Music> musics) {
                mView.showMoreMusics(musics);
            }
        });
    }

    @Override
    public void startNewMusic(List<Music> musics, int postion) {
        if(!mPlayService.containsMusic(musics.get(postion)))
            mPlayService.initMusic(musics);
        mPlayService.changeMusic(musics.get(postion));
    }
}
