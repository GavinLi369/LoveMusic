package gavin.lovemusic.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.mainview.MainActivity;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by Gavin on 2015/11/3.
 * 歌曲播放服务
 */
public class PlayService extends Service implements MusicPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MusicPlayer.OnStartedListener{
    public static int musicState;
    public static final int PLAYING = 0;        //歌曲正在播放
    public static final int PAUSE = 1;         //歌曲暂停

    private Notification notification;
    private static final int NOTIFICATION_ID = 1;

    private static final String NOTIFICATION_PLAY = "gavin.notification.play";
    private static final String NOTIFICATION_NEXT = "gavin.notification.next";
    private static final String NOTIFICATION_STOP = "gavin.notification.stop";

    private RemoteViews contentView;
    private MusicPlayer mMusicPlayer;

    private IBinder mBinder = new PlayServiceBinder();

    private List<MusicStatusChangedListener> mListeners = new CopyOnWriteArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicPlayer = new MusicPlayer(this, this, this);

        IntentFilter intentFilterPlay = new IntentFilter(NOTIFICATION_PLAY);
        registerReceiver(broadcastReceiver, intentFilterPlay);
        IntentFilter intentFilterNext = new IntentFilter(NOTIFICATION_NEXT);
        registerReceiver(broadcastReceiver, intentFilterNext);
        IntentFilter intentFilterStop = new IntentFilter(NOTIFICATION_STOP);
        registerReceiver(broadcastReceiver, intentFilterStop);
    }

    //当PlayService被销毁时，解除广播接收器的注册
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMusicPlayer.release();
        unregisterReceiver(broadcastReceiver);
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
        for(MusicStatusChangedListener listener : mListeners)
            listener.onPreparing(music);
        mMusicPlayer.start(music);
    }

    @Override
    public void onStarted() {
        musicState = PLAYING;
        for(MusicStatusChangedListener listener : mListeners)
            listener.onStarted(mMusicPlayer.getCurrentMusic());

        showNotification();
    }

    public synchronized void pauseMusic() {
        mMusicPlayer.pause();
        musicState = PAUSE;
        for(MusicStatusChangedListener listener : mListeners)
            listener.onPause();

        contentView.setImageViewResource
                (R.id.playButton, R.drawable.play_prey);
        startForeground(NOTIFICATION_ID, notification);
    }

    public synchronized void resumeMusic() {
        mMusicPlayer.resume();
        musicState = PLAYING;
        for(MusicStatusChangedListener listener : mListeners)
            listener.onResume();

        contentView.setImageViewResource(R.id.playButton, R.drawable.pause);
        startForeground(NOTIFICATION_ID, notification);
    }

    public synchronized void setProgress(int progress) {
        mMusicPlayer.setProgress(progress);
    }

    public synchronized void previousMusic() {
        mMusicPlayer.previous();
        musicState = PLAYING;
        for (MusicStatusChangedListener listener : mListeners)
            listener.onPreparing(mMusicPlayer.getCurrentMusic());
    }

    public synchronized void nextMusic() {
        mMusicPlayer.next();
        musicState = PLAYING;
        for (MusicStatusChangedListener listener : mListeners)
            listener.onPreparing(mMusicPlayer.getCurrentMusic());
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        //缓存进度为百分比
        for(MusicStatusChangedListener listener : mListeners)
            listener.onBufferingUpdate((int) (i / 100.0 * mMusicPlayer.getCurrentMusic().getDuration()));
    }

    @Override
    public void onCompletion() {
        for (MusicStatusChangedListener listener : mListeners)
            listener.onPreparing(mMusicPlayer.getCurrentMusic());
    }

    /**
     * 对从Notification发来的广播进行处理
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NOTIFICATION_PLAY:
                    if (musicState == PLAYING) {
                        pauseMusic();
                    } else {
                        resumeMusic();
                    }
                    break;
                case NOTIFICATION_NEXT:
                    nextMusic();
                    break;
                case NOTIFICATION_STOP:
                    pauseMusic();
                    stopForeground(true);
            }
        }
    };

    /**
     * 显示Notification
     */
    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        if(SDK_INT >= 19) {
            builder.setSmallIcon(R.drawable.ic_launcher_alpha);
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
        }
        builder.setContentIntent(PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), 0));
        notification = builder.build();
        contentView = new RemoteViews
                (getPackageName(), R.layout.music_play_notification_small);
        notification.contentView = contentView;
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        Intent intentPlay = new Intent(NOTIFICATION_PLAY);
        PendingIntent pIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, 0);
        contentView.setOnClickPendingIntent(R.id.playButton, pIntentPlay);

        Intent intentNext = new Intent(NOTIFICATION_NEXT);
        PendingIntent pIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, 0);
        contentView.setOnClickPendingIntent(R.id.nextButton, pIntentNext);

        Intent intentClose = new Intent(NOTIFICATION_STOP);
        PendingIntent pIntentClose = PendingIntent.getBroadcast(this, 0, intentClose, 0);
        contentView.setOnClickPendingIntent(R.id.closeService, pIntentClose);

        if (musicState == PLAYING) {
            contentView.setImageViewResource(R.id.playButton, R.drawable.pause);
        } else {
            contentView.setImageViewResource(R.id.playButton, R.drawable.play_prey);
        }

        contentView.setTextViewText(R.id.musicName, mMusicPlayer.getCurrentMusic().getTitle());
        contentView.setTextViewText(R.id.artist, mMusicPlayer.getCurrentMusic().getArtist());
        Bitmap album = BitmapFactory.decodeFile(mMusicPlayer.getCurrentMusic().getImage());
        contentView.setImageViewBitmap(R.id.musicAlbum, album);

        startForeground(NOTIFICATION_ID, notification);
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
