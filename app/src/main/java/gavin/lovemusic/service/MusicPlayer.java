package gavin.lovemusic.service;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-25.
 */
//TODO 可能有内存泄漏
public class MusicPlayer implements MediaPlayer.OnPreparedListener{
    private List<Music> mMusicPlayList = new ArrayList<>();
    private int mIndex;

    private MediaPlayer.OnCompletionListener mCompletionListener;
    private MediaPlayer.OnBufferingUpdateListener mUpdateListener;
    private MediaPlayer.OnErrorListener mErrorListener;
    private OnStartedListener mStartedListener;

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    MusicPlayer(MediaPlayer.OnCompletionListener completionListener,
                MediaPlayer.OnBufferingUpdateListener updateListener,
                MediaPlayer.OnErrorListener errorListener,
                OnStartedListener startedListener) {
        mCompletionListener = completionListener;
        mStartedListener = startedListener;
        mErrorListener = errorListener;
        mUpdateListener = updateListener;
    }

    public void start(int index) throws IOException {
        mIndex = index;
        mMediaPlayer.reset();
        Music music = mMusicPlayList.get(index);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
        //网络歌曲异步加载
        mMediaPlayer.setDataSource(music.getPath());
        if(music.getPath().startsWith("http")) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnBufferingUpdateListener(mUpdateListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.prepareAsync();
        } else {
            mMediaPlayer.prepare();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMusicPlayList.get(mIndex).setDuration(mMediaPlayer.getDuration());
        mMediaPlayer.start();
        mStartedListener.onStarted();
    }

    public void start(Music music) throws IOException {
        if(mMusicPlayList.contains(music)) {
            start(mMusicPlayList.indexOf(music));
        } else {
            addMusic(music);
            start(mMusicPlayList.size() - 1);
        }
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void resume() {
        mMediaPlayer.start();
    }

    public void next() throws IOException {
        if(++mIndex != mMusicPlayList.size()) {
            start(mIndex);
        } else {
            start(0);
        }
    }

    public void previous() throws IOException {
        if(--mIndex != -1) {
            start(mIndex);
        } else {
            start(mMusicPlayList.size() - 1);
        }
    }

    public void setProgress(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    public void addMusic(Music music) {
        mMusicPlayList.add(music);
    }

    public Music getCurrentMusic() {
        return mMusicPlayList.get(mIndex);
    }

    public void resetMusicPlayer(List<Music> musics) {
        mIndex = 0;
        mMediaPlayer.reset();
        mMusicPlayList.clear();
        mMusicPlayList.addAll(musics);
    }

    public boolean contains(Music music) {
        return mMusicPlayList.contains(music);
    }

    public void release() {
        if(mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public interface OnStartedListener {
        void onStarted();
    }
}
