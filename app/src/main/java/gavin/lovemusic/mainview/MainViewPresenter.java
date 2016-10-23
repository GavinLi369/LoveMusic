package gavin.lovemusic.mainview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.PlayService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi on 16-9-10.
 * MainViewPresenter
 */
public class MainViewPresenter implements MainViewContract.Presenter {
    private MainViewContract.View mMainView;


    public MainViewPresenter(MainViewContract.View mMainView) {
        this.mMainView = mMainView;
        mMainView.setPresenter(this);
    }

    @Override
    public void onPlayButtonClicked(Context context) {
        switch (PlayService.musicState) {
            case PlayService.PLAYING:
                changeMusicStatus(context, ActivityCommand.PAUSE_MUSIC);
                break;
            case PlayService.PAUSE:
                changeMusicStatus(context, ActivityCommand.RESUME_MUSIC);
        }
    }

    @Override
    public void changeMusicStatus(Context context, ActivityCommand command) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", command);
        context.startService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicStarted(PlayService.MusicStartedEvent event) {
        mMainView.showMusicPlayView(event.currentMusic);
        mMainView.changeMusicInfoes(event.currentMusic);
        mMainView.changePause2Playing();
        Observable<Bitmap> observable = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                if (event.currentMusic.getImage().startsWith("http")) {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(event.currentMusic.getImage())
                            .build();
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        subscriber.onNext(BitmapFactory.decodeStream(response.body().byteStream()));
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                } else {
                    subscriber.onNext(BitmapFactory.decodeFile(event.currentMusic.getImage()));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mMainView.changeDragViewColorDefault();
            }

            @Override
            public void onNext(Bitmap bitmap) {
                Palette.from(bitmap)
                        .maximumColorCount(24)
                        .generate(palette -> {
                            Palette.Swatch swatch = palette.getMutedSwatch();
                            if(swatch != null) {
                                mMainView.changeDragViewColor(swatch);
                            } else {
                                mMainView.changeDragViewColorDefault();
                            }
                        });
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicPaused(PlayService.MusicPauseEvent event) {
        mMainView.changePlaying2Pause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicPlayed(PlayService.MusicPlayEvent event) {
        mMainView.changePause2Playing();
    }

    @Override
    public void subscribe() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unsubscribe() {
        EventBus.getDefault().unregister(this);
    }
}
