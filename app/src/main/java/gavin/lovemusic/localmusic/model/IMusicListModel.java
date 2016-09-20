package gavin.lovemusic.localmusic.model;

import android.content.Context;

import java.util.ArrayList;

import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi on 16-9-10.
 * IMusicListModel
 */
public interface IMusicListModel {
    ArrayList<Music> getMusicList(Context context);

    void refreshMusicList(Context context);
}
