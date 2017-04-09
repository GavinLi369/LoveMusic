package gavin.lovemusic.localmusic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
    private static final int TIMER_WHAT = 1;
    private static final int UPDATE_UI_WHAT = 2;

    private LocalMusicContract.View mView;
    private LocalMusicContract.Model mModel;
    private PlayService mPlayService;


    public LocalMusicPresenter(LocalMusicContract.View view,
                               LocalMusicContract.Model model, PlayService playService) {
        mView = view;
        mModel = model;
        mPlayService = playService;
        mHandler = new ScanningFileHandler(mView);
        mView.setPresenter(this);
    }

    @Override
    public void startNewMusic(List<Music> musics, int postion) {
        if(!mPlayService.containsMusic(musics.get(postion)))
            mPlayService.initMusic(musics);
        mPlayService.changeMusic(musics.get(postion));
    }

    @Override
    public void loadMusicList() {
        List<Music> musics = mModel.getMusicList();
        mPlayService.initMusic(musics);
        mView.setMusicListView(musics);
    }

    private static class ScanningFileHandler extends Handler {
        private WeakReference<LocalMusicContract.View> mViewWeakReference;

        private boolean canUpdateUI = true;

        public ScanningFileHandler(LocalMusicContract.View view) {
            mViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIMER_WHAT) {
                canUpdateUI = true;
            } else if (msg.what == UPDATE_UI_WHAT && canUpdateUI) {
                canUpdateUI = false;
                mViewWeakReference.get().updateScanningFile(msg.getData().getString("path"));
            }
        }
    }

    private final ScanningFileHandler mHandler;

    @Override
    public void refreshMusicList(Context context) {
        mView.showScanningFile();
        mModel.setFileScannerLinsenter(path -> {
            Message message = new Message();
            message.what = UPDATE_UI_WHAT;
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            message.setData(bundle);
            mHandler.sendMessage(message);
        });

        //防止TextView更新过快,导致的界面卡顿
        Runnable timer = new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = TIMER_WHAT;
                mHandler.sendMessage(message);
                mHandler.postDelayed(this, 100);
            }
        };
        mHandler.postDelayed(timer, 100);

        Observable<ArrayList<Music>> observable = Observable.create((Observable.OnSubscribe<ArrayList<Music>>) subscriber -> {
            try {
                mModel.refreshMusicList();
                subscriber.onNext(mModel.getMusicList());
                subscriber.onCompleted();
            } catch (IOException e) {
                e.printStackTrace();
                mHandler.removeMessages(TIMER_WHAT);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Subscriber<ArrayList<Music>>() {
            @Override
            public void onCompleted() {
                mHandler.removeMessages(TIMER_WHAT);
                mView.hideRefreshing();
                mView.removeScanningFile();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(ArrayList<Music> musics) {
                mView.setMusicListView(musics);
                mPlayService.initMusic(musics);
            }
        });
    }

    @Override
    public void cancalScanning() {
        mModel.cancalScanning();
        mView.removeScanningFile();
    }
}
