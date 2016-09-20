package gavin.lovemusic.detailmusic.presenter;

import android.content.Context;
import android.content.Intent;

import gavin.lovemusic.detailmusic.view.IPlayDetailView;
import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.IServiceListener;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi on 16-9-10.
 * PlayDetailPresenter
 */
public class PlayDetailPresenter implements IPlayDetailPresenter, IServiceListener {
    private IPlayDetailView playDetailView;

    public PlayDetailPresenter(IPlayDetailView playDetailView) {
        this.playDetailView = playDetailView;
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
    public void changeMusic(Context context, ActivityCommand command) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", command);
        context.startService(intent);
    }

    @Override
    public void musicStatusChanged() {
        playDetailView.updateUI();
    }
}
