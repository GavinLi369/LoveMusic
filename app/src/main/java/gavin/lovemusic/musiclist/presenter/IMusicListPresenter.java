package gavin.lovemusic.musiclist.presenter;

import android.content.Context;

import java.util.ArrayList;

import gavin.lovemusic.data.Music;

/**
 * Created by gavinli on 16-9-10.
 * MusicListPresenter接口
 */
public interface IMusicListPresenter {
    void startMusic(Context context);
    void onPlayButtonClick(Context context);
    void onNextButtonClick(Context context);
    void refreshMusicList(Context context);
    void resumeMusic(Context context);
    void pauseMusic(Context context);
    void musicStatusChanged();
    void serviceCreated();
    ArrayList<Music> getMusicList();
}
