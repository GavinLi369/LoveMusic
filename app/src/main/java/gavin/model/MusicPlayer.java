package gavin.model;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import java.util.List;

import gavin.service.PlayService;

/**
 * Created by Gavin on 2015/8/24.
 * 歌曲播放器
 */
public class MusicPlayer{
    /**
     * 歌曲已经准备好
     */
    private boolean prepared = false;

    /**
     * 歌曲播放状态
     */
    private int musicState;

    /**
     * 没有歌曲正在播放
     */
    public static final int STOP = 0;

    /**
     * 歌曲正在播放
     */
    public static final int PLAYING = 1;

    /**
     * 歌曲暂停
     */
    public static final int PAUSE = 2;

    /**
     * 歌曲播放模式
     */
    private int playMode;

    /**
     * 歌曲播放模式：顺序播放
     */
    public static final int REPEAT = 0;

    /**
     * 歌曲播放模式：单曲播放
     */
    public static final int REPEAT_ONE = 1;

    /**
     * 歌曲播放模式：随机播放
     */
    public static final int SHUFFLE = 2;

    private Context context;

    /**
     * 歌曲播放列表
     */
    private List<MusicInfo> musicList;

    /**
     * 当前歌曲
     */
    private MusicInfo currentMusic;

    private MediaPlayer mediaPlayer;

    /**
     * 获取歌曲已播放时间
     */
    public long getCurrentTime() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 获取歌曲时长
     */
    public long getDuration() {
        return mediaPlayer.getDuration();
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    /**
     * 歌曲是否准备好
     */
    public boolean isPrepared() {
        return prepared;
    }

    /**
     * 设置歌曲播放模式
     */
    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    /**
     * 获得歌曲播放模式
     */
    public int getPlayMode() {
        return playMode;
    }

    /**
     * 设置歌曲播放器的状态
     */
    public void setMusicState(int musicState){
        this.musicState = musicState;
    }

    /**
     * 获得歌曲播放器的状态
     */
    public int getMusicState() {
        return musicState;
    }

    /**
     * 设定将要播放的歌曲
     */
    public void setCurrentMusic(MusicInfo currentMusic) {
        this.currentMusic = currentMusic;
    }

    /**
     * 获取正在播放的歌曲
     */
    public MusicInfo getCurrentMusic() {
        return currentMusic;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer){
        this.mediaPlayer = mediaPlayer;
    }

    /**
     * MusicPlayer的构造器，调用init()初始化方法
     */
    public MusicPlayer(Context context){
        this.context = context;
        init();
    }

    /**
     * 初始化MusicPlayer
     */
    public void init() {
        this.musicList = PlayService.musicList;
        currentMusic = musicList.get(0);

        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", PlayService.INIT_SERVICE);
        context.startService(intent);
    }

    /**
     * 设置播放进度
     */
    public void setProgress(int progress) {
        mediaPlayer.seekTo(progress);
    }

    /**
     * 以mm:ss形式获取歌曲已播放的时间
     */
    public String getCurrentTimeStr () {
        String currentTimeStr;
        int currentTimeMinute = mediaPlayer.getCurrentPosition() / 60000;
        int currentTimeSecond = (mediaPlayer.getCurrentPosition() / 1000) % 60;
        if (currentTimeMinute < 10) {
            currentTimeStr = "0" + currentTimeMinute;
        } else {
            currentTimeStr = "" + currentTimeMinute;
        }
        if (currentTimeSecond < 10) {
            currentTimeStr = currentTimeStr + ":0" + currentTimeSecond;
        } else {
            currentTimeStr = currentTimeStr + ":" + currentTimeSecond;
        }
        return currentTimeStr;
    }

    /**
     * 以mm:ss形式获取歌曲时长
     */
    public String getDurationStr () {
        String durationStr;
        int durationMinute = mediaPlayer.getDuration() / 60000;
        int durationSecond = (mediaPlayer.getDuration() / 1000) % 60;
        if (durationMinute < 10) {
            durationStr = "0" + durationMinute;
        } else {
            durationStr = "" + durationMinute;
        }
        if (durationSecond < 10) {
            durationStr = durationStr + ":0" + durationSecond;
        } else {
            durationStr = durationStr + ":" + durationSecond;
        }
        return durationStr;
    }

    /**
     * 开始一首新的歌曲
     */
    public void startMusic(MusicInfo musicInfo) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", PlayService.PLAY_MUSIC);
        intent.putExtra("musicId", musicInfo.getId());
        context.startService(intent);
        musicState = PLAYING;
    }

    /**
     * 暂停后开始播放歌曲
     */
    public void resumeMusic() {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", PlayService.RESUME_MUSIC);
        context.startService(intent);
        musicState = PLAYING;
    }

    /**
     * 暂停正在播放的歌曲
     */
    public void pauseMusic() {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", PlayService.PAUSE_MUSIC);
        context.startService(intent);
        musicState = PAUSE;
    }

    /**
     * 按照歌曲播放模式播放下一首歌曲
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
        startMusic(currentMusic);
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

        startMusic(currentMusic);
    }

}
