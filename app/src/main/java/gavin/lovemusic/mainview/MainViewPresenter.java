package gavin.lovemusic.mainview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.Glide;

import java.util.concurrent.ExecutionException;

import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.PlayService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi on 16-9-10.
 * MainViewPresenter
 */
public class MainViewPresenter implements MainViewContract.Presenter, PlayService.MusicStatusChangedListener {
    private MainViewContract.View mView;
    private PlayService mPlayService;


    public MainViewPresenter(MainViewContract.View mMainView, PlayService playService) {
        mView = mMainView;
        mPlayService = playService;
        mMainView.setPresenter(this);
    }

    @Override
    public void onPreparing(Music music) {
        mView.showMusicPlayView(music);
        mView.changeMusicInfoes(music);
        mView.changePause2Playing();
        Observable
                .create((Observable.OnSubscribe<Palette.Swatch>) subscriber -> {
                    try {
                        Bitmap bitmap = Glide.with(mPlayService)
                                .load(music.getImage())
                                .asBitmap()
                                .into(-1, -1)
                                .get();
                        Palette.from(bitmap)
                                .maximumColorCount(24)
                                .generate(palette -> {
                                    Palette.Swatch swatch = palette.getMutedSwatch();
                                    subscriber.onNext(swatch);
                                });
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(swatch -> {
                    if(swatch != null) {
                        mView.changeDragViewColor(swatch);
                    } else {
                        mView.changeDragViewColorDefault();
                    }
                }, throwable -> {
                    mView.changeDragViewColorDefault();
                });
    }

    @Override
    public void onStarted(Music music) {
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
    public void onBufferingUpdate(int progress) {
    }

    @Override
    public void onPlayButtonClicked() {
        switch (PlayService.musicState) {
            case PlayService.PLAYING: pauseMusic(); break;
            case PlayService.PAUSE: resumeMusic(); break;
        }
    }

    @Override
    public void pauseMusic() {
        mPlayService.pauseMusic();
    }

    @Override
    public void resumeMusic() {
        mPlayService.resumeMusic();
    }
}
