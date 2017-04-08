package gavin.lovemusic.service;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GavinLi
 * on 16-9-25.
 */
public class MusicPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private List<Music> mMusicPlayList = new ArrayList<>();
    private int mIndex;

    private OnCompletionListener mOnCompletionListener;

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    MusicPlayer(OnCompletionListener onCompletionListener) {
        this.mOnCompletionListener = onCompletionListener;
    }

    public void start(int index) {
        mIndex = index;
        mMediaPlayer.reset();
        try {
            Music music = mMusicPlayList.get(index);
            mMediaPlayer.setDataSource(music.getPath());
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepare();
            music.setDuration(mMediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
        mOnCompletionListener.onCompletion();
    }

    public void start(Music music) {
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

    public void next() {
        if(++mIndex != mMusicPlayList.size()) {
            start(mIndex);
        } else {
            start(0);
        }
    }

    public void previous() {
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
        if(mMusicPlayList.size() != 0) {
            try {
                mMediaPlayer.setDataSource(mMusicPlayList.get(0).getPath());
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnPreparedListener(null);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public interface OnCompletionListener {
        void onCompletion();
    }
}
