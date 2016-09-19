package gavin.lovemusic.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import gavin.lovemusic.App;
import gavin.lovemusic.playdetail.view.PlayDetailActivity;
import gavin.lovemusic.constant.R;
import gavin.lovemusic.data.Music;

/**
 * Created by Gavin on 2015/11/3.
 * 歌曲播放服务
 */
public class PlayService extends Service {
    public static final String SERVICE_COMMAND = "gavin.music.service";

    public static int playMode;
    public static final int REPEAT = 0;        //歌曲播放模式：顺序播放
    public static final int REPEAT_ONE = 1;       //歌曲播放模式：单曲播放
    public static final int SHUFFLE = 2;        //歌曲播放模式：随机播放

    public static int musicState;
    public static final int STOP = 0;     //没有歌曲正在播放
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
    public static Music currentMusic;
    public static boolean prepared;

    private Notification notification;
    private RemoteViews contentView;

    private App app;

    private IServiceListener linstener;

    @Override
    public void onCreate() {
        super.onCreate();
        app = (App) getApplicationContext();
        initMusic();

        IntentFilter intentFilterPlay = new IntentFilter(NOTIFICATION_PLAY);
        registerReceiver(broadcastReceiver, intentFilterPlay);
        IntentFilter intentFilterNext = new IntentFilter(NOTIFICATION_NEXT);
        registerReceiver(broadcastReceiver, intentFilterNext);
        IntentFilter intentFilterStop = new IntentFilter(NOTIFICATION_STOP);
        registerReceiver(broadcastReceiver, intentFilterStop);
    }

    /**
     * 对从Activity发来的请求进行处理
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            ActivityCommand activityCommand = (ActivityCommand)intent.getSerializableExtra("musicCommand");
            switch (activityCommand) {
                case INIT_SERVICE:
                    initMusic();
                    break;
                case PLAY_MUSIC:
                    startMusic();
                    break;
                case PAUSE_MUSIC:
                    pauseMusic();
                    break;
                case RESUME_MUSIC:
                    resumeMusic();
                    break;
                case PREVIOUS_MUSIC:
                    previousMusic();
                    break;
                case NEXT_MUSIC:
                    nextMusic();
                    break;
                case CHANGE_PLAY_MODE:
                    playMode = intent.getExtras().getInt("playMode");
                    break;
                case CHANGE_PROGRESS:
                    setProgress(intent.getExtras().getInt("progress"));
                    break;
                default:
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void initMusic(){
        if (!app.getMusicList().isEmpty()) {
            int musicId = 0;
            SharedPreferences sharedPreferences =
                    app.getSharedPreferences("service_info", Service.MODE_PRIVATE);
            if (sharedPreferences != null) {
                musicId = sharedPreferences.getInt("musicId", 0);
                playMode = sharedPreferences.getInt("playMode", 0);
            }
            /**
             * 防止删除歌曲后数据越界
             */
            try {
                currentMusic = app.getMusicList().get(musicId);
            } catch (IndexOutOfBoundsException e) {
                currentMusic = app.getMusicList().get(0);
                e.printStackTrace();
            }
            mediaPlayer = MediaPlayer.create
                    (PlayService.this, Uri.parse("file://" + currentMusic.getMusicPath()));
            prepared = true;
        }
    }

    /**
     * 开始一首新的歌曲
     */
    private void startMusic() {
        if (mediaPlayer != null) {
            stopMusic();
        }
        mediaPlayer = MediaPlayer.create
                (PlayService.this, Uri.parse("file://" + currentMusic.getMusicPath()));
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(new MyCompletionListener());
        mediaPlayer.start();
        prepared = true;
        musicState = PLAYING;
        linstener.musicStatusChanged();

        showNotification();
    }

    /**
     * 暂停后开始播放歌曲
     */
    private void resumeMusic() {
        mediaPlayer.start();
        musicState = PLAYING;
        linstener.musicStatusChanged();

        contentView.setImageViewResource
                (R.id.playButton, R.drawable.img_button_notification_play_pause_grey);
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 暂停正在播放的歌曲
     */
    private void pauseMusic() {
        mediaPlayer.pause();
        musicState = PAUSE;
        linstener.musicStatusChanged();

        contentView.setImageViewResource
                (R.id.playButton, R.drawable.img_button_notification_play_play_grey);
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 停止正在播放的歌曲
     */
    private void stopMusic() {
        prepared = false;
        musicState = STOP;
        linstener.musicStatusChanged();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
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

        startMusic();
    }

    /**
     * 播放下一首歌曲
     */
    private void nextMusic() {
        switch (playMode) {
            case REPEAT:
                if (currentMusic.getId() != app.getMusicList().size() - 1) {
                    currentMusic = app.getMusicList().get(currentMusic.getId() + 1);
                } else {
                    currentMusic = app.getMusicList().get(0);
                }
                break;
            case REPEAT_ONE:
                break;
            case SHUFFLE:
                int index = (int) (Math.random() * app.getMusicList().size() - 1);
                currentMusic = app.getMusicList().get(index);
                break;
        }

        startMusic();
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
                    editor.putInt("playMode", playMode);
                    editor.apply();
                    stopMusic();
                    stopForeground(true);
                    mediaPlayer = MediaPlayer.create
                            (PlayService.this, Uri.parse("file://" + currentMusic.getMusicPath()));
                    prepared = true;
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

        Intent intentActivity = new Intent(this, PlayDetailActivity.class);
        intentActivity.addFlags
                (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        notification.contentIntent = PendingIntent.getActivity
                (getApplicationContext(), 0, intentActivity, 0);

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
            contentView.setImageViewResource
                    (R.id.playButton, R.drawable.img_button_notification_play_pause_grey);
        } else {
            contentView.setImageViewResource
                    (R.id.playButton, R.drawable.img_button_notification_play_play_grey);
        }

        contentView.setTextViewText(R.id.musicName, currentMusic.getMusicName());
        contentView.setTextViewText(R.id.artist, currentMusic.getArtist());
        contentView.setImageViewBitmap(R.id.musicAlbum, currentMusic.getAlbum());

        startForeground(NOTIFICATION_ID, notification);
    }

    public void setServiceLinstener(IServiceListener linstener) {
        this.linstener = linstener;
    }

    /**
     * 当PlayService被销毁时，解除广播接收器的注册
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    public class ServiceBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }
}
