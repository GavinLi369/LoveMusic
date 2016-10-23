package gavin.lovemusic.localmusic;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

import gavin.lovemusic.service.MusicListUpdateEvent;
import gavin.lovemusic.entity.Music;
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

    @Inject
    public LocalMusicPresenter(LocalMusicContract.View localMusicView,
                        LocalMusicContract.Model localMusicModel) {
        this.mLocalMusicView = localMusicView;
        this.mLocalMusicModel = localMusicModel;
        mLocalMusicView.setPresenter(this);
    }

    @Override
    public void playNewMusic(int postion) {
        Music music = mLocalMusicModel.getMusicList().get(postion);
        EventBus.getDefault().post(new PlayService.ChangeMusicEvent(music));
    }

    @Override
    public void refreshMusicList(Context context) {
        Observable<ArrayList<Music>> observable = Observable.create(new Observable.OnSubscribe<ArrayList<Music>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Music>> subscriber) {
                try {
                    mLocalMusicModel.refreshMusicList();
                    subscriber.onNext(mLocalMusicModel.getMusicList());
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                subscriber.onCompleted();
            }
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
            }
        });
    }

    @Override
    public void subscribe() {
        EventBus.getDefault().post(new MusicListUpdateEvent(mLocalMusicModel.getMusicList()));
        mLocalMusicView.setMusicListView(mLocalMusicModel.getMusicList());
    }

    @Override
    public void unsubscribe() {
    }
}
