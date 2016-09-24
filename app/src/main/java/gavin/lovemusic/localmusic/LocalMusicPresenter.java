package gavin.lovemusic.localmusic;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.MusicListUpdateEvent;
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
    private Context context;

    public LocalMusicPresenter(LocalMusicContract.View localMusicView, Context context) {
        this.mLocalMusicView = localMusicView;
        this.context = context;
        mLocalMusicModel = new LocalMusicModel(context);
        mLocalMusicView.setPresenter(this);
    }

    @Override
    public void playNewMusic(int postion) {
        Music music = mLocalMusicModel.getMusicList(context).get(postion);
        EventBus.getDefault().post(new PlayService.ChangeMusicEvent(music));
    }

    @Override
    public void refreshMusicList(Context context) {
        Observable<ArrayList<Music>> observable = Observable.create(new Observable.OnSubscribe<ArrayList<Music>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Music>> subscriber) {
                try {
                    mLocalMusicModel.refreshMusicList(context);
                    subscriber.onNext(mLocalMusicModel.getMusicList(context));
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
        mLocalMusicView.setMusicListView(mLocalMusicModel.getMusicList(context));
    }

    @Override
    public void unsubscribe() {
    }
}
