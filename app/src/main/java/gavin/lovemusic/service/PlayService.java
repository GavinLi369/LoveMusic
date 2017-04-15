package gavin.lovemusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import gavin.lovemusic.entity.Music;

/**
 * Created by Gavin on 2015/11/3.
 * 歌曲播放服务
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MusicPlayer.OnStartedListener,
        MediaPlayer.OnErrorListener{
    public static int musicState;
    public static final int PLAYING = 0;        //歌曲正在播放
    public static final int PAUSE = 1;         //歌曲暂停

    private NotificationManager mNotificationManager;
    private MusicPlayer mMusicPlayer;

    private IBinder mBinder = new PlayServiceBinder();

    private List<MusicStatusChangedListener> mListeners = new CopyOnWriteArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicPlayer = new MusicPlayer(this,
                this, this, this);
        mNotificationManager = new NotificationManager(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMusicPlayer.release();
        mNotificationManager.release();
    }

    public synchronized void initMusic(List<Music> musics) {
        mMusicPlayer.resetMusicPlayer(musics);
        musicState = PAUSE;
        for(MusicStatusChangedListener listener : mListeners)
            listener.onPause();
    }

    public boolean containsMusic(Music music) {
        return mMusicPlayer.contains(music);
    }

    public synchronized void changeMusic(Music music) {
        isCompleted = false;
        try {
            mMusicPlayer.start(music);
            for(MusicStatusChangedListener listener : mListeners)
                listener.onPreparing(music);

            mNotificationManager.showNotification(music);
        } catch (IOException e) {
            e.printStackTrace();
            showMusicLoadError();
        }
    }

    public synchronized void pauseMusic() {
        musicState = PAUSE;
        mMusicPlayer.pause();
        mNotificationManager.showPause();

        for(MusicStatusChangedListener listener : mListeners)
            listener.onPause();
    }

    public synchronized void resumeMusic() {
        musicState = PLAYING;
        mMusicPlayer.resume();
        mNotificationManager.showResume();

        for(MusicStatusChangedListener listener : mListeners)
            listener.onResume();

    }

    public synchronized void setProgress(int progress) {
        mMusicPlayer.setProgress(progress);
    }

    public synchronized void previousMusic() {
        try {
            mMusicPlayer.previous();
            musicState = PLAYING;
            mNotificationManager.showNotification(mMusicPlayer.getCurrentMusic());
            for (MusicStatusChangedListener listener : mListeners)
                listener.onPreparing(mMusicPlayer.getCurrentMusic());
        } catch (IOException e) {
            e.printStackTrace();
            showMusicLoadError();
        }
    }

    public synchronized void nextMusic() {
        try {
            mMusicPlayer.next();
            musicState = PLAYING;
            mNotificationManager.showNotification(mMusicPlayer.getCurrentMusic());
            for (MusicStatusChangedListener listener : mListeners)
                listener.onPreparing(mMusicPlayer.getCurrentMusic());
        } catch (IOException e) {
            e.printStackTrace();
            showMusicLoadError();
        }
    }

    @Override
    public void onStarted() {
        musicState = PLAYING;
        for(MusicStatusChangedListener listener : mListeners)
            listener.onStarted(mMusicPlayer.getCurrentMusic());

        mNotificationManager.showResume();
    }

    //网络歌曲是否加载完成
    private boolean isCompleted = false;

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        if(i == 100) {
            isCompleted = true;
            //缓存进度为百分比
            for(MusicStatusChangedListener listener : mListeners)
                listener.onBufferingUpdate((int) mMusicPlayer.getCurrentMusic().getDuration());
        }
        if(!isCompleted) {
            //缓存进度为百分比
            for(MusicStatusChangedListener listener : mListeners)
                listener.onBufferingUpdate((int) (i / 100.0 * mMusicPlayer.getCurrentMusic().getDuration()));
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        try {
            mMusicPlayer.next();
            for (MusicStatusChangedListener listener : mListeners)
                listener.onPreparing(mMusicPlayer.getCurrentMusic());
        } catch (IOException e) {
            e.printStackTrace();
            showMusicLoadError();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        showMusicLoadError();
        return true;
    }

    void showMusicLoadError() {
        Toast.makeText(this, "资源加载出错", Toast.LENGTH_SHORT).show();
        mNotificationManager.showPause();
        for(MusicStatusChangedListener listener : mListeners)
            listener.onPause();
    }

    public class PlayServiceBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public interface MusicStatusChangedListener {
        /**
         * 网络歌曲正在准备
         * @param music 网络歌曲
         */
        void onPreparing(Music music);

        /**
         * 歌曲开始播放
         * @param music 当前歌曲
         */
        void onStarted(Music music);

        /**
         * 缓存加载
         * @param progress 缓存加载进度
         */
        void onBufferingUpdate(int progress);

        /**
         * 歌曲暂停
         */
        void onPause();

        /**
         * 歌曲继续
         */
        void onResume();
    }

    public void registerListener(MusicStatusChangedListener listener) {
        if(!mListeners.contains(listener))
            mListeners.add(listener);
        else
            throw new RuntimeException("This listener has been registered");
    }

    public void unregisterListener(MusicStatusChangedListener listener) {
        if(mListeners.contains(listener))
            mListeners.remove(listener);
        else
            throw new RuntimeException("Didn't find this listener");
    }
}
