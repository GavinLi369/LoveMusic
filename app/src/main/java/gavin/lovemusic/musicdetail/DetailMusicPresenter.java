package gavin.lovemusic.musicdetail;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.Glide;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutionException;

import gavin.lovemusic.musicdetail.view.LyricRow;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.PlayService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by GavinLi on 16-9-10.
 * DetailMusicPresenter
 */
public class DetailMusicPresenter implements DetailMusicContract.Presenter {
    private DetailMusicContract.View mView;
    private DetailMusicContract.Model mModel;
    private PlayService mPlayService;
    private final UpdateSeekBarHandler mHandler;

    private Music mCurrentMusic;
    private int mCurrentTime = 0;
    private int mCurrentProgress = 0;
    private boolean mIsPlaying = false;
    private boolean mIsNetworkMusic = false;

    public DetailMusicPresenter(DetailMusicContract.View view,
                                DetailMusicContract.Model model,
                                Music currentMusic,
                                PlayService playService) {
        mView = view;
        mModel = model;
        mCurrentMusic = currentMusic;
        mPlayService = playService;
        mPlayService.registerListener(mListener);
        mHandler = new UpdateSeekBarHandler(mView);
        mView.setPresenter(this);
    }

    @Override
    public void initMusicDetail() {
        mView.changePauseToPlay();
        //每隔0.5秒更新一次视图
        new Thread(() -> {
            while(true) {
                if (mIsPlaying) {
                    if(mIsNetworkMusic &&
                            mCurrentProgress <= mCurrentTime) {
                        mIsPlaying = false;
                        continue;
                    }
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", (int) mCurrentMusic.getDuration());
                    bundle.putInt("progress", mCurrentTime);
                    bundle.putInt("buffer", mCurrentProgress);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                    mCurrentTime += 500;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();

        if(mCurrentMusic != null) {
            mView.modifySeekBar((int) mCurrentMusic.getDuration(), 0);
            changeViewColor(mCurrentMusic.getImage());
            mView.updateBgImage(mCurrentMusic.getImage());
            initLyricView();
        }
    }

    private PlayService.MusicStatusChangedListener mListener = new PlayService.MusicStatusChangedListener() {
        @Override
        public void onPreparing(Music music) {
            mIsPlaying = false;
            mCurrentMusic = music;
            mView.modifySeekBar((int) music.getDuration(), 0);
            mView.modifySeekBarBuffer(0);
            changeViewColor(music.getImage());
            mView.updateBgImage(music.getImage());
            mView.changePauseToPlay();
            initLyricView();
        }

        @Override
        public void onStarted(Music music) {
            mCurrentTime = 0;
            mIsPlaying = true;
        }

        @Override
        public void onBufferingUpdate(int progress) {
            mIsNetworkMusic = true;
            mCurrentProgress = progress;
            if(progress > mCurrentTime) mIsPlaying = true;
        }

        @Override
        public void onPause() {
            mIsPlaying = false;
            mView.changePlayToPause();
        }

        @Override
        public void onResume() {
            mIsPlaying = true;
            mView.changePauseToPlay();
        }
    };

    private static class UpdateSeekBarHandler extends Handler {
        private final WeakReference<DetailMusicContract.View> mViewWeakReference;

        public UpdateSeekBarHandler(DetailMusicContract.View view) {
            mViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            mViewWeakReference.get().modifySeekBar(
                    msg.getData().getInt("duration"),
                    msg.getData().getInt("progress"));
            mViewWeakReference.get().modifySeekBarBuffer(
                    msg.getData().getInt("buffer"));
        }
    }

    @Override
    public void setMusicProgress(int progress, Context context) {
        mCurrentTime = progress;
        mPlayService.setProgress(progress);
        mView.modifySeekBar((int) mCurrentMusic.getDuration(), progress);
    }

    @Override
    public void onPlayButtonClick() {
        switch (PlayService.musicState) {
            case PlayService.PLAYING:
                //暂停正在播放的歌曲
                mPlayService.pauseMusic();
                break;
            case PlayService.PAUSE:
                //暂停后开始播放歌曲
                mPlayService.resumeMusic();
                break;
        }
    }

    @Override
    public void previousMusic(Context context) {
        mPlayService.previousMusic();
    }

    @Override
    public void nextMusic(Context context) {
        mPlayService.nextMusic();
    }

    private void changeViewColor(String bgImageUrl) {
        Observable
                .create((Observable.OnSubscribe<Palette.Swatch>) subscriber -> {
                    try {
                        Bitmap bitmap = Glide.with(mPlayService)
                                .load(bgImageUrl)
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
                        mView.changeViewColor(swatch);
                    } else {
                        mView.changeViewColorDefault();
                    }
                }, throwable -> {
                    mView.changeViewColorDefault();
                });
    }

    private void initLyricView() {
        if(mCurrentMusic != null) {
            mView.showFindingLyric();
            Observable
                    .create((Observable.OnSubscribe<List<LyricRow>>) subscriber -> {
                        try {
                            List<LyricRow> lyricRows = mModel.getMusicLyric(mCurrentMusic);
                            subscriber.onNext(lyricRows);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            subscriber.onError(e);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(lyricRows -> {
                        mView.changeLyricView(lyricRows);
                    }, throwable -> {
                        if(throwable instanceof IOException) {
                            mView.showNotFoundLyric();
                        }
                    });
        }
    }

    @Override
    public void release() {
        mPlayService.unregisterListener(mListener);
    }
}
