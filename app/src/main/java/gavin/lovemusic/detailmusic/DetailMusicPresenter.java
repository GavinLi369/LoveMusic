package gavin.lovemusic.detailmusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.graphics.Palette;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import gavin.lovemusic.entity.LyricRow;
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

    private Music mCurrentMusic;

    private int mCurrentTime = 0;
    private boolean isPlaying = true;

    private UpdateViewHandler handler = new UpdateViewHandler(this);

    public DetailMusicPresenter(DetailMusicContract.View view,
                                DetailMusicContract.Model model) {
        this.mDetailMusicView = view;
        this.mDetailMusicModel = model;
        mDetailMusicView.setPresenter(this);
    }

    static class UpdateViewHandler extends Handler {
        private final WeakReference<DetailMusicPresenter> presenterWeakReference;

        public UpdateViewHandler(DetailMusicPresenter presenter) {
            this.presenterWeakReference = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            DetailMusicPresenter presenter = presenterWeakReference.get();
            presenter.mCurrentTime += 500;
            if(presenter.mCurrentMusic != null) {
                presenter.mDetailMusicView.updateSeekBar((int) presenter.mCurrentMusic.getDuration(), presenter.mCurrentTime);
                presenter.mDetailMusicView.updateLyricView(presenter.mCurrentTime);
            }
        }
    }

    @Override
    public void setMusicProgress(int progress, Context context) {
        mCurrentTime = progress;
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.CHANGE_PROGRESS);
        intent.putExtra("progress", progress);
        context.startService(intent);
    }

    @Override
    public void onPlayButtonClick(Context context) {
        Intent intent;
        switch (PlayService.musicState) {
            case PlayService.PLAYING:
                //暂停正在播放的歌曲
                intent = new Intent(context, PlayService.class);
                intent.putExtra("musicCommand", ActivityCommand.PAUSE_MUSIC);
                context.startService(intent);
                break;
            case PlayService.PAUSE:
                //暂停后开始播放歌曲
                intent = new Intent(context, PlayService.class);
                intent.putExtra("musicCommand", ActivityCommand.RESUME_MUSIC);
                context.startService(intent);
                break;
        }
    }

    @Override
    public void changeMusic(Context context, ActivityCommand command) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", command);
        context.startService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startMusic(PlayService.MusicStartedEvent event) {
        mCurrentMusic = event.currentMusic;
        mCurrentTime = 0;
        changeViewColor(event.currentMusic.getImage());
        mDetailMusicView.updateBgImage(event.currentMusic.getImage());
        mDetailMusicView.updateSeekBar((int) event.currentMusic.getDuration(), mCurrentTime);
        mDetailMusicView.changePauseToPlay();
        initLyricView();
        isPlaying = true;
    }

    private void changeViewColor(String bgImageUrl) {
        Observable<Bitmap> observable = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
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
            }
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
            Observable<ArrayList<LyricRow>> observable = Observable.create(new Observable.OnSubscribe<ArrayList<LyricRow>>() {
                @Override
                public void call(Subscriber<? super ArrayList<LyricRow>> subscriber) {
                    try {
                        subscriber.onNext(mDetailMusicModel.getMusicLyric(mCurrentMusic));
                        subscriber.onCompleted();
                    } catch (IOException | JSONException e) {
                        subscriber.onError(e);
                    }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void musicPause(PlayService.MusicPauseEvent event) {
        isPlaying = false;
        mDetailMusicView.changePlayToPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void musicPlay(PlayService.MusicPlayEvent event) {
        isPlaying = true;
        mDetailMusicView.changePauseToPlay();
    }

    @Override
    public void subscribe() {
        EventBus.getDefault().register(this);
        //每隔0.5秒更新一次视图
        new Thread(() -> {
            while(true) {
                if(isPlaying)
                    handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
        mDetailMusicView.changePauseToPlay();
        if(mCurrentMusic != null) {
            changeViewColor(mCurrentMusic.getImage());
            mDetailMusicView.updateBgImage(mCurrentMusic.getImage());
            initLyricView();
        }
    }

    //TODO 用于初始化View刚显示时所播放的歌曲，代码逻辑不清晰，以后优化
    public void initMusic(Music music) {
        mCurrentMusic = music;
    }

    @Override
    public void unsubscribe() {
        EventBus.getDefault().unregister(this);
    }
}
