package gavin.lovemusic.mainview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;

import java.io.IOException;
import java.util.NoSuchElementException;

import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.Music;
import gavin.lovemusic.service.PlayService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.os.Build.VERSION_CODES.M;
import static gavin.lovemusic.service.ActivityCommand.PAUSE_MUSIC;
import static gavin.lovemusic.service.ActivityCommand.RESUME_MUSIC;

/**
 * Created by GavinLi on 16-9-10.
 * MainViewPresenter
 */
public class MainViewPresenter implements MainViewContract.Presenter, PlayService.OnMusicStatListener {
    private MainViewContract.View mView;
    private PlayService mPlayService;


    public MainViewPresenter(MainViewContract.View mMainView, PlayService playService) {
        mView = mMainView;
        mPlayService = playService;
        mMainView.setPresenter(this);
    }

    @Override
    public void onStarted(Music music) {
        mView.showMusicPlayView(music);
        mView.changeMusicInfoes(music);
        mView.changePause2Playing();
        Observable<Bitmap> observable = Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            if (music.getImage().startsWith("http")) {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(music.getImage())
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    subscriber.onNext(BitmapFactory.decodeStream(response.body().byteStream()));
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            } else {
                subscriber.onNext(BitmapFactory.decodeFile(music.getImage()));
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mView.changeDragViewColorDefault();
            }

            @Override
            public void onNext(Bitmap bitmap) {
                Palette.from(bitmap)
                        .maximumColorCount(24)
                        .generate(palette -> {
                            Palette.Swatch swatch = palette.getMutedSwatch();
                            if(swatch != null) {
                                mView.changeDragViewColor(swatch);
                            } else {
                                mView.changeDragViewColorDefault();
                            }
                        });
            }
        });
    }

    @Override
    public void onPause() {
        mView.changePlaying2Pause();
    }

    @Override
    public void onResume() {
        mView.changePause2Playing();
    }

    @Override
    public void onPlayButtonClicked(Context context) {
        switch (PlayService.musicState) {
            case PlayService.PLAYING:
                changeMusicStatus(context, PAUSE_MUSIC);
                break;
            case PlayService.PAUSE:
                changeMusicStatus(context, RESUME_MUSIC);
        }
    }

    @Override
    public void changeMusicStatus(Context context, ActivityCommand command) {
        switch (command) {
            case PAUSE_MUSIC: mPlayService.pauseMusic(); break;
            case RESUME_MUSIC: mPlayService.resumeMusic(); break;
            default: throw new NoSuchElementException("Not found this command");
        }
    }
}
