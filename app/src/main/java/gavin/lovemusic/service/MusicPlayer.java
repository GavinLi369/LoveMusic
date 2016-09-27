package gavin.lovemusic.service;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-25.
 */
public class MusicPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private ArrayList<Music> mMusicPlayList = new ArrayList<>();
    private int mIndex;

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    MusicPlayer() {
//        mMediaPlayer.setLooping(false);
    }

    public void start(int index) {
        mIndex = index;
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(mMusicPlayList.get(index).getPath());
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepare();
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

    public void resetMusicPlayer(ArrayList<Music> musics) {
        mIndex = 0;
        mMediaPlayer.reset();
        mMusicPlayList.clear();
        mMusicPlayList.addAll(musics);
        try {
            mMediaPlayer.setDataSource(mMusicPlayList.get(0).getPath());
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if(mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}
