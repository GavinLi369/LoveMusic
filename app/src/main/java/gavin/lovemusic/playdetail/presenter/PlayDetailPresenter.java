package gavin.lovemusic.playdetail.presenter;

import android.content.Context;
import android.content.Intent;

import gavin.lovemusic.playdetail.view.IPlayDetailView;
import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi on 16-9-10.
 * PlayDetailPresenter
 */
public class PlayDetailPresenter implements IPlayDetailPresenter {
    private IPlayDetailView playDetailView;

    private static PlayDetailPresenter playDetailPresenter;

    public PlayDetailPresenter(IPlayDetailView playDetailView) {
        this.playDetailView = playDetailView;
        playDetailPresenter = this;
    }

    public static PlayDetailPresenter getPlayDetailPresenter() {
        return playDetailPresenter;
    }

    @Override
    public void setMusicProgress(int progress, Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.CHANGE_PROGRESS);
        intent.putExtra("progress", progress);
        context.startService(intent);
    }

    @Override
    public void onPlayButtonClick(Context context) {
        Intent intent;
        switch (PlayService.musicState) {
            case PlayService.STOP:
                //开始播放歌曲
                intent = new Intent(context, PlayService.class);
                intent.putExtra("musicCommand", ActivityCommand.PLAY_MUSIC);
                context.startService(intent);
                break;
            case PlayService.PLAYING:
                //暂停正在播放的歌曲
                intent = new Intent(context, PlayService.class);
                intent.putExtra("musicCommand", ActivityCommand.PAUSE_MUSIC);
                context.startService(intent);
                break;
            case PlayService.PAUSE:
                //暂停后开始播放歌曲
                intent = new Intent(context, PlayService.class);
                intent.putExtra("musicCommand", ActivityCommand.RESUME_MUSIC);
                context.startService(intent);
                break;
        }
    }

    @Override
    public void nextMusic(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.NEXT_MUSIC);
        context.startService(intent);
    }

    @Override
    public void previousMusic(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.PREVIOUS_MUSIC);
        context.startService(intent);
    }

    @Override
    public void musicStatusChanged() {
        playDetailView.updateUI();
    }
}
