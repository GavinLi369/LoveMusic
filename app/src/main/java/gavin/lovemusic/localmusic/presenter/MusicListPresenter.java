package gavin.lovemusic.localmusic.presenter;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import gavin.lovemusic.entity.Music;
import gavin.lovemusic.localmusic.model.IMusicListModel;
import gavin.lovemusic.localmusic.model.MusicListModel;
import gavin.lovemusic.localmusic.view.IMusicListView;
import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.IServiceListener;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi on 16-9-10.
 * MusicListPresenter
 */
public class MusicListPresenter implements IMusicListPresenter, IServiceListener {
    private IMusicListModel musicListModel = new MusicListModel();
    private IMusicListView musicListView;

    private Context context;

    public MusicListPresenter(IMusicListView musicListView) {
        this.musicListView = musicListView;
        if(musicListView instanceof Context)
            this.context = (Context) musicListView;
        refreshMusicList(context);
    }

    @Override
    public void onPlayButtonClick(Context context) {
        switch (PlayService.musicState) {
            case PlayService.STOP:
                changeMusicStatus(context, ActivityCommand.PLAY_MUSIC);
                break;
            case PlayService.PLAYING:
                changeMusicStatus(context, ActivityCommand.PAUSE_MUSIC);
                break;
            case PlayService.PAUSE:
                changeMusicStatus(context, ActivityCommand.RESUME_MUSIC);
        }
    }

    @Override
    public void musicStatusChanged() {
        musicListView.updateUI();
    }

    @Override
    public void changeCurrentMusic(int postion) {
        PlayService.currentMusic = musicListModel.getMusicList(context).get(postion);
    }

    @Override
    public ArrayList<Music> getMusicList() {
        return musicListModel.getMusicList(context);
    }

    @Override
    public void refreshMusicList(Context context) {
        musicListModel.refreshMusicList(context);
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.INIT_SERVICE);
        context.startService(intent);
    }

    @Override
    public void changeMusicStatus(Context context, ActivityCommand command) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", command);
        context.startService(intent);
    }
}
