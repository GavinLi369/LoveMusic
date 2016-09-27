package gavin.lovemusic.service;

import java.util.ArrayList;

import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class MusicListUpdateEvent {
    public final ArrayList<Music> musicList;

    public MusicListUpdateEvent(ArrayList<Music> musicList) {
        this.musicList = musicList;
    }
}
