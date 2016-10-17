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
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;

/**
 * Created by Gavin on 2015/11/3.
 * 歌曲播放服务
 */
public class PlayService extends Service {
    public static int musicState;
    public static final int PLAYING = 0;        //歌曲正在播放
    public static final int PAUSE = 1;         //歌曲暂停

    private Notification notification;
    public static final int NOTIFICATION_ID = 1;

    public static final String NOTIFICATION_PLAY = "gavin.notification.play";
    public static final String NOTIFICATION_NEXT = "gavin.notification.next";
    public static final String NOTIFICATION_STOP = "gavin.notification.stop";

    private RemoteViews contentView;
    private MusicPlayer mMusicPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicPlayer = new MusicPlayer();

        IntentFilter intentFilterPlay = new IntentFilter(NOTIFICATION_PLAY);
        registerReceiver(broadcastReceiver, intentFilterPlay);
        IntentFilter intentFilterNext = new IntentFilter(NOTIFICATION_NEXT);
        registerReceiver(broadcastReceiver, intentFilterNext);
        IntentFilter intentFilterStop = new IntentFilter(NOTIFICATION_STOP);
        registerReceiver(broadcastReceiver, intentFilterStop);

        EventBus.getDefault().register(this);
    }

    //当PlayService被销毁时，解除广播接收器的注册
    @Override
    public void onDestroy() {
        mMusicPlayer.release();
        unregisterReceiver(broadcastReceiver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * 对从Activity发来的请求进行处理
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            ActivityCommand activityCommand = (ActivityCommand)intent.getSerializableExtra("musicCommand");
            if(activityCommand != null) {
                switch (activityCommand) {
                    case PLAY_MUSIC: startMusic(mMusicPlayer.getCurrentMusic()); break;
                    case PAUSE_MUSIC: pauseMusic(); break;
                    case RESUME_MUSIC: resumeMusic(); break;
                    case PREVIOUS_MUSIC: previousMusic(); break;
                    case NEXT_MUSIC: nextMusic(); break;
                    case CHANGE_PROGRESS:
                        mMusicPlayer.setProgress(intent.getExtras().getInt("progress"));
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void initMusic(MusicListUpdateEvent event){
        mMusicPlayer.resetMusicPlayer(event.musicList);
        musicState = PAUSE;
        EventBus.getDefault().post(new MusicPauseEvent());
    }

    private void startMusic(Music music) {
        mMusicPlayer.start(music);
        musicState = PLAYING;
        EventBus.getDefault().post(new MusicStartedEvent(mMusicPlayer.getCurrentMusic()));
        EventBus.getDefault().post(new MusicPlayEvent());
        showNotification();
    }

    private void pauseMusic() {
        mMusicPlayer.pause();
        musicState = PAUSE;
        EventBus.getDefault().post(new MusicPauseEvent());

        contentView.setImageViewResource
                (R.id.playButton, R.drawable.play_prey);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void resumeMusic() {
        mMusicPlayer.resume();
        musicState = PLAYING;
        EventBus.getDefault().post(new MusicPlayEvent());

        contentView.setImageViewResource(R.id.playButton, R.drawable.pause);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void previousMusic() {
        mMusicPlayer.previous();
        EventBus.getDefault().post(
                new MusicStartedEvent(mMusicPlayer.getCurrentMusic()));
    }

    private void nextMusic() {
        mMusicPlayer.next();
        EventBus.getDefault().post(
                new MusicStartedEvent(mMusicPlayer.getCurrentMusic()));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void changeMusic(ChangeMusicEvent event) {
        startMusic(event.music);
        EventBus.getDefault().post(
                new MusicStartedEvent(mMusicPlayer.getCurrentMusic()));
    }

    public static class ChangeMusicEvent {
        public final Music music;

        public ChangeMusicEvent(Music music) {
            this.music = music;
        }
    }

    public static class MusicStartedEvent {
        public final Music currentMusic;

        public MusicStartedEvent(Music currentMusic) {
            this.currentMusic = currentMusic;
        }
    }

    public static class MusicPauseEvent {
    }

    public static class MusicPlayEvent {
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
                    mMusicPlayer.next();
                    break;
                case NOTIFICATION_STOP:
                    pauseMusic();
                    stopForeground(true);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 显示Notification
     */
    private void showNotification() {
        notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        contentView = new RemoteViews
                (getPackageName(), R.layout.music_play_notification_small);
        //noinspection deprecation
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
