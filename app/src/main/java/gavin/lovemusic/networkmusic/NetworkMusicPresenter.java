package gavin.lovemusic.networkmusic;

import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.entity.Music;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi
 * on 16-9-24.
 */
public class NetworkMusicPresenter implements NetworkMusicContract.Presenter {
    private NetworkMusicContract.View mNetworkMusicView;
    private NetworkMusicContract.Model mNetworkMusicModel;

    private int mIndex = 0;

    public NetworkMusicPresenter(NetworkMusicContract.View networkMusicView) {
        this.mNetworkMusicView = networkMusicView;
        mNetworkMusicModel = new DongtingApi();
        mNetworkMusicView.setPresenter(this);
    }

    @Override
    public void refreshMusicList() {
        Observable<ArrayList<Music>> observable = Observable.create(new Observable.OnSubscribe<ArrayList<Music>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Music>> subscriber) {
                ArrayList<Music> musics = new ArrayList<>();
                mIndex = 0;
                try {
                    musics.addAll(mNetworkMusicModel.getBillboardHot(10, mIndex++));
                    subscriber.onNext(musics);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Subscriber<ArrayList<Music>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mNetworkMusicView.showNetworkConnetionError();
                mNetworkMusicView.hideRefreshView();
            }

            @Override
            public void onNext(ArrayList<Music> musics) {
                mNetworkMusicView.showMoreMusics(musics);
                mNetworkMusicView.hideRefreshView();
            }
        });
    }

    @Override
    public void loadMoreMusic() {
        Observable<ArrayList<Music>> observable = Observable.create(new Observable.OnSubscribe<ArrayList<Music>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Music>> subscriber) {
                ArrayList<Music> musics = new ArrayList<>();
                try {
                    musics.addAll(mNetworkMusicModel.getBillboardHot(10, mIndex++));
                    subscriber.onNext(musics);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Subscriber<ArrayList<Music>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mNetworkMusicView.showNetworkConnetionError();
            }

            @Override
            public void onNext(ArrayList<Music> musics) {
                mNetworkMusicView.showMoreMusics(musics);
            }
        });
    }

    @Override
    public void subscribe() {
        mNetworkMusicView.showRefreshView();
        refreshMusicList();
    }

    @Override
    public void unsubscribe() {
    }
}
