package gavin.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import java.util.ArrayList;

import gavin.activity.PlayerActivity;
import gavin.constant.R;
import gavin.model.MusicInfo;

/**
 * Created by Gavin on 2015/11/3.
 *
 * 歌曲播放服务
 */
public class PlayService extends Service {
    public static final String SERVICE_COMMAND = "gavin.music.service";

    public static final int INIT_SERVICE = 0;
    public static final int PLAY_MUSIC = 1;
    public static final int PAUSE_MUSIC = 2;
    public static final int RESUME_MUSIC = 3;
    public static final int PREVIOUS_MUSIC = 4;
    public static final int NEXT_MUSIC = 5;
    public static final int CHANGE_PLAY_MODE = 6;
    public static final int CHANGE_PROGRESS = 7;

    /**
     * 歌曲播放模式
     */
    public static int playMode;

    public static final int REPEAT = 0;        //歌曲播放模式：顺序播放
    public static final int REPEAT_ONE = 1;       //歌曲播放模式：单曲播放
    public static final int SHUFFLE = 2;        //歌曲播放模式：随机播放

    /**
     * 歌曲播放状态
     */
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
    public static MusicInfo currentMusic;
    public static boolean prepared;

    public static ArrayList<MusicInfo> musicList = null;

    private Notification notification;
    private RemoteViews contentView;

    @Override
    public void onCreate() {
        super.onCreate();
        musicList = getMusicList();
        for (int i = 0; i < musicList.size(); i++) {
            musicList.get(i).setId(i);
        }

        currentMusic = musicList.get(0);

        IntentFilter intentFilterPlay = new IntentFilter(NOTIFICATION_PLAY);
        registerReceiver(broadcastReceiver, intentFilterPlay);
        IntentFilter intentFilterNext = new IntentFilter(NOTIFICATION_NEXT);
        registerReceiver(broadcastReceiver, intentFilterNext);
        IntentFilter intentFilterStop = new IntentFilter(NOTIFICATION_STOP);
        registerReceiver(broadcastReceiver, intentFilterStop);
    }

    /**
     * 获取手机里的所有歌曲，并将其添加进List
     */
    private ArrayList<MusicInfo> getMusicList() {
        ArrayList<MusicInfo> musicInfos = new ArrayList<>();
        Cursor cursor = this.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[]{"audio/mpeg"}, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                MusicInfo musicInfo = new MusicInfo(cursor.getString(1), this);
                musicInfos.add(musicInfo);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return musicInfos;
    }

    /**
     * 对从Activity发来的请求进行处理
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int musicCommand = intent.getExtras().getInt("musicCommand");
            switch (musicCommand) {
                case INIT_SERVICE:
                    int musicId = intent.getExtras().getInt("musicId");
                    /**
                     * 防止删除歌曲后数据越界
                     */
                    try {
                        currentMusic = musicList.get(musicId);
                    } catch (IndexOutOfBoundsException e){
                        currentMusic = musicList.get(0);
                        e.printStackTrace();
                    }
                    mediaPlayer = MediaPlayer.create
                            (PlayService.this, Uri.parse("file://" + currentMusic.getMusicPath()));
                    prepared = true;
                    Intent onCreateIntent = new Intent(SERVICE_COMMAND);
                    onCreateIntent.putExtra("command", "onCreate");
                    sendBroadcast(onCreateIntent);
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

    /**
     * 当PlayService被销毁时，解除广播接收器的注册
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * 开始一首新的歌曲
     */
    public void startMusic() {
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

        Intent intent = new Intent(SERVICE_COMMAND);
        sendBroadcast(intent);

        showNotification();
    }

    /**
     * 暂停后开始播放歌曲
     */
    public void resumeMusic() {
        mediaPlayer.start();
        musicState = PLAYING;
        Intent intent = new Intent(SERVICE_COMMAND);
        sendBroadcast(intent);

        contentView.setImageViewResource
                (R.id.playButton, R.drawable.img_button_notification_play_pause_grey);
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 暂停正在播放的歌曲
     */
    public void pauseMusic() {
        mediaPlayer.pause();
        musicState = PAUSE;
        Intent intent = new Intent(SERVICE_COMMAND);
        sendBroadcast(intent);

        contentView.setImageViewResource
                (R.id.playButton, R.drawable.img_button_notification_play_play_grey);
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 停止正在播放的歌曲
     */
    public void stopMusic() {
        prepared = false;
        musicState = STOP;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    /**
     * 播放上一首歌曲
     */
    public void previousMusic() {
        if (currentMusic.getId() != 0) {
            currentMusic = musicList.get(currentMusic.getId() - 1);
        } else {
            currentMusic = musicList.get(musicList.size() - 1);
        }

        startMusic();
    }

    /**
     * 播放下一首歌曲
     */
    public void nextMusic() {
        switch (playMode) {
            case REPEAT:
                if (currentMusic.getId() != musicList.size() - 1) {
                    currentMusic = musicList.get(currentMusic.getId() + 1);
                } else {
                    currentMusic = musicList.get(0);
                }
                break;
            case REPEAT_ONE:
                break;
            case SHUFFLE:
                int index = (int) (Math.random() * musicList.size() - 1);
                currentMusic = musicList.get(index);
                break;
        }

        startMusic();
    }

    /**
     * 歌曲播放完毕，则开始下一首
     */
    class MyCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            nextMusic();
        }
    }

    /**
     * 设置播放进度
     */
    public void setProgress(int progress) {
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
                    Intent serviceIntent = new Intent(SERVICE_COMMAND);
                    sendBroadcast(serviceIntent);
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

        Intent intentActivity = new Intent(this, PlayerActivity.class);
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
