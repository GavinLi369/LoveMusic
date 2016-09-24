package gavin.lovemusic.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import gavin.lovemusic.App;
import gavin.lovemusic.MusicListUpdateEvent;
import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;

/**
 * Created by Gavin on 2015/11/3.
 * 歌曲播放服务
 */
public class PlayService extends Service {
    public static int musicState;
    public static final int PLAYING = 1;        //歌曲正在播放
    public static final int PAUSE = 2;         //歌曲暂停

    /**
     * Notification的识别ID
     */
    public static final int NOTIFICATION_ID = 1;

    public static final String NOTIFICATION_PLAY = "gavin.notification.play";
    public static final String NOTIFICATION_NEXT = "gavin.notification.next";
    public static final String NOTIFICATION_STOP = "gavin.notification.stop";

    public static MediaPlayer mediaPlayer;
    private Music currentMusic;

    private Notification notification;
    private RemoteViews contentView;

    private App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = (App) getApplicationContext();

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
                    case PLAY_MUSIC: startMusic(currentMusic); break;
                    case PAUSE_MUSIC: pauseMusic(); break;
                    case RESUME_MUSIC: resumeMusic(); break;
                    case PREVIOUS_MUSIC: previousMusic(); break;
                    case NEXT_MUSIC: nextMusic(); break;
                    case CHANGE_PROGRESS:
                        setProgress(intent.getExtras().getInt("progress"));
                        break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    private void initMusic(MusicListUpdateEvent event){
        if (!event.musicList.isEmpty()) {
            int musicId = 0;
            SharedPreferences sharedPreferences =
                    app.getSharedPreferences("service_info", Service.MODE_PRIVATE);
            if (sharedPreferences != null) {
                musicId = sharedPreferences.getInt("musicId", 0);
            }
            /**
             * 防止删除歌曲后数据越界
             */
            try {
                currentMusic = event.musicList.get(musicId);
            } catch (IndexOutOfBoundsException e) {
                currentMusic = event.musicList.get(0);
                e.printStackTrace();
            }
            mediaPlayer = MediaPlayer.create
                    (PlayService.this, Uri.parse("file://" + currentMusic.getMusicPath()));
            EventBus.getDefault().post(new MusicChangedEvent(currentMusic, musicState));
        }
    }

    /**
     * 开始一首新的歌曲
     */
    private void startMusic(Music music) {
        currentMusic = music;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create
                (PlayService.this, Uri.parse("file://" + music.getMusicPath()));
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(new MyCompletionListener());
        mediaPlayer.start();
        musicState = PLAYING;
        EventBus.getDefault().post(new MusicChangedEvent(music, musicState));

        showNotification();
    }

    /**
     * 暂停后开始播放歌曲
     */
    private void resumeMusic() {
        mediaPlayer.start();
        musicState = PLAYING;
        EventBus.getDefault().post(new MusicChangedEvent(currentMusic, musicState));

        contentView.setImageViewResource(R.id.playButton, R.drawable.pause);
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 暂停正在播放的歌曲
     */
    private void pauseMusic() {
        mediaPlayer.pause();
        musicState = PAUSE;
        EventBus.getDefault().post(new MusicChangedEvent(currentMusic, musicState));

        contentView.setImageViewResource
                (R.id.playButton, R.drawable.play_prey);
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 播放上一首歌曲
     */
    private void previousMusic() {
        if (currentMusic.getId() != 0) {
            currentMusic = app.getMusicList().get(currentMusic.getId() - 1);
        } else {
            currentMusic = app.getMusicList().get(app.getMusicList().size() - 1);
        }

        startMusic(currentMusic);
    }

    /**
     * 播放下一首歌曲
     */
    private void nextMusic() {
        if (currentMusic.getId() != app.getMusicList().size() - 1) {
            currentMusic = app.getMusicList().get(currentMusic.getId() + 1);
        } else {
            currentMusic = app.getMusicList().get(0);
        }

        startMusic(currentMusic);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void changeMusic(ChangeMusicEvent event) {
        startMusic(event.music);
//        EventBus.getDefault().post(new MusicChangedEvent(event.music, musicState));
    }

    public static class ChangeMusicEvent {
        public final Music music;

        public ChangeMusicEvent(Music music) {
            this.music = music;
        }
    }

    public static class MusicChangedEvent {
        public final Music currentMusic;
        public final int musicState;

        public MusicChangedEvent(Music currentMusic, int musicState) {
            this.currentMusic = currentMusic;
            this.musicState = musicState;
        }
    }

    /**
     * 歌曲播放完毕，则开始下一首
     */
    private class MyCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            nextMusic();
        }
    }

    /**
     * 设置播放进度
     */
    private void setProgress(int progress) {
        mediaPlayer.seekTo(progress);
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
                    SharedPreferences sharedPreferences =
                            getSharedPreferences("service_info", Service.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("musicId", currentMusic.getId());
                    editor.apply();
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
        notification = new Notification();
        notification.icon = R.mipmap.ic_launcher;
        contentView = new RemoteViews
                (getPackageName(), R.layout.music_play_notification_small);
        notification.contentView = contentView;
        notification.flags = Notification.FLAG_ONGOING_EVENT;

//        Intent intentActivity = new Intent(this, MainActivity.class);
//        intentActivity.addFlags
//                (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        notification.contentIntent = PendingIntent.getActivity
//                (getApplicationContext(), 0, intentActivity, 0);

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

        contentView.setTextViewText(R.id.musicName, currentMusic.getTitle());
        contentView.setTextViewText(R.id.artist, currentMusic.getArtist());
        Bitmap album = BitmapFactory.decodeFile(currentMusic.getAlbumPath());
        contentView.setImageViewBitmap(R.id.musicAlbum, album);

        startForeground(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
