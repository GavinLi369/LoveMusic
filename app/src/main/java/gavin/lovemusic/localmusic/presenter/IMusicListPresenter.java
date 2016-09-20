package gavin.lovemusic.localmusic.presenter;

import android.content.Context;

import java.util.ArrayList;

import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;

/**
 * Created by gavinli on 16-9-10.
 * MusicListPresenter接口
 */
public interface IMusicListPresenter {
    ArrayList<Music> getMusicList();

    void onPlayButtonClick(Context context);

    void refreshMusicList(Context context);

    void changeMusicStatus(Context context, ActivityCommand command);

    void changeCurrentMusic(int postion);
}
