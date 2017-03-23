package gavin.lovemusic.localmusic;

import android.content.Context;

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
 * on 16-9-22.
 */
public class LocalMusicPresenter implements LocalMusicContract.Presenter {
    private LocalMusicContract.View mLocalMusicView;
    private LocalMusicContract.Model mLocalMusicModel;
    private PlayService mPlayService;

    public LocalMusicPresenter(LocalMusicContract.View localMusicView,
                        LocalMusicContract.Model localMusicModel, PlayService playService) {
        mLocalMusicView = localMusicView;
        mLocalMusicModel = localMusicModel;
        mPlayService = playService;
        mLocalMusicView.setPresenter(this);
    }

    @Override
    public void startNewMusic(List<Music> musics, int postion) {
        if(!mPlayService.containsMusic(musics.get(postion)))
            mPlayService.initMusic(musics);
        mPlayService.changeMusic(musics.get(postion));
    }

    @Override
    public void refreshMusicList(Context context) {
        Observable<ArrayList<Music>> observable = Observable.create((Observable.OnSubscribe<ArrayList<Music>>) subscriber -> {
            try {
                mLocalMusicModel.refreshMusicList();
                subscriber.onNext(mLocalMusicModel.getMusicList());
                subscriber.onCompleted();
            } catch (IOException e) {
                e.printStackTrace();
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Subscriber<ArrayList<Music>>() {
            @Override
            public void onCompleted() {
                mLocalMusicView.hideRefreshing();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(ArrayList<Music> musics) {
                mLocalMusicView.setMusicListView(musics);
                mPlayService.initMusic(musics);
            }
        });
    }

    @Override
    public void subscribe() {
        List<Music> musics = mLocalMusicModel.getMusicList();
        mPlayService.initMusic(musics);
        mLocalMusicView.setMusicListView(musics);
    }

    @Override
    public void unsubscribe() {
    }
}
