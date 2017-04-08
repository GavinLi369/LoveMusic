package gavin.lovemusic.detailmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.detailmusic.view.LyricRow;
import gavin.lovemusic.entity.Music;
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
 * DetailMusicPresenter
 */
public class DetailMusicPresenter implements DetailMusicContract.Presenter {
    private DetailMusicContract.View mDetailMusicView;
    private DetailMusicContract.Model mDetailMusicModel;
    private PlayService mPlayService;

    private Music mCurrentMusic;

    public DetailMusicPresenter(DetailMusicContract.View view,
                                DetailMusicContract.Model model,
                                Music currentMusic,
                                PlayService playService) {
        mDetailMusicView = view;
        mDetailMusicModel = model;
        mCurrentMusic = currentMusic;
        mPlayService = playService;
        mPlayService.registerListener(mListener);
        mDetailMusicView.setPresenter(this);
    }

    @Override
    public void initMusicDetail() {
        mDetailMusicView.changePauseToPlay();
        if(mCurrentMusic != null) {
            changeViewColor(mCurrentMusic.getImage());
            mDetailMusicView.updateBgImage(mCurrentMusic.getImage());
            initLyricView();
        }
    }

    private PlayService.OnMusicStatListener mListener = new PlayService.OnMusicStatListener() {
        @Override
        public void onStarted(Music music) {
            mCurrentMusic = music;
            changeViewColor(music.getImage());
            mDetailMusicView.updateBgImage(music.getImage());
            mDetailMusicView.modifySeekBar((int) music.getDuration(), 0);
            mDetailMusicView.changePauseToPlay();
            initLyricView();
        }

        @Override
        public void onPause() {
            mDetailMusicView.changePlayToPause();
        }

        @Override
        public void onResume() {
            mDetailMusicView.changePauseToPlay();
        }
    };

    @Override
    public void setMusicProgress(int progress, Context context) {
        mPlayService.setProgress(progress);
    }

    @Override
    public void onPlayButtonClick(Context context) {
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
    public void changeMusic(Context context, ActivityCommand command) {
        switch (command) {
            case PREVIOUS_MUSIC: mPlayService.previousMusic(); break;
            case NEXT_MUSIC: mPlayService.nextMusic(); break;
        }
    }

    @Override
    public long getMusicDuration() {
        if(mCurrentMusic != null)
            return mCurrentMusic.getDuration();
        else
            return 0;
    }

    private void changeViewColor(String bgImageUrl) {
        Observable<Bitmap> observable = Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            if (bgImageUrl.startsWith("http")) {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(bgImageUrl)
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    subscriber.onNext(BitmapFactory.decodeStream(response.body().byteStream()));
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            } else {
                subscriber.onNext(BitmapFactory.decodeFile(bgImageUrl));
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
                mDetailMusicView.changeViewColorDefault();
            }

            @Override
            public void onNext(Bitmap bitmap) {
                Palette.from(bitmap)
                        .maximumColorCount(24)
                        .generate(palette -> {
                            Palette.Swatch swatch = palette.getMutedSwatch();
                            if(swatch != null) {
                                mDetailMusicView.changeViewColor(swatch);
                            } else {
                                mDetailMusicView.changeViewColorDefault();
                            }
                        });
            }
        });
    }

    private void initLyricView() {
        if(mCurrentMusic != null) {
            mDetailMusicView.showFindingLyric();
            Observable<ArrayList<LyricRow>> observable = Observable.create((Observable.OnSubscribe<ArrayList<LyricRow>>) subscriber -> {
                try {
                    ArrayList<LyricRow> lyricRows = mDetailMusicModel.getMusicLyric(mCurrentMusic);
                    if(lyricRows.isEmpty())
                        subscriber.onError(new Exception("This music doesn't have the lyric"));
                    else
                        subscriber.onNext(lyricRows);
                    subscriber.onCompleted();
                } catch (IOException | JSONException e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            observable.subscribe(new Subscriber<ArrayList<LyricRow>>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    mDetailMusicView.showNotFoundLyric();
                }

                @Override
                public void onNext(ArrayList<LyricRow> lyricRows) {
                    mDetailMusicView.changeLyricView(lyricRows);
                }
            });
        }
    }

    @Override
    public void release() {
        mPlayService.unregisterListener(mListener);
    }
}
