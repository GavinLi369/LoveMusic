package gavin.lovemusic.detailmusic;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import gavin.lovemusic.entity.Lyric;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi on 16-9-10.
 * DetailMusicPresenter
 */
public class DetailMusicPresenter implements DetailMusicContract.Presenter {
    private DetailMusicContract.View mPlayDetailView;

    private Music currentMusic;

    private UpdateViewHandler handler = new UpdateViewHandler(this);

    public DetailMusicPresenter(DetailMusicContract.View playDetailView) {
        this.mPlayDetailView = playDetailView;
        mPlayDetailView.setPresenter(this);

        //每隔0.5秒更新一次视图
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
                handler.postDelayed(this, 500);
            }
        };

        handler.postDelayed(runnable, 500);
    }

    static class UpdateViewHandler extends Handler {
        private final WeakReference<DetailMusicPresenter> presenterWeakReference;

        public UpdateViewHandler(DetailMusicPresenter presenter) {
            this.presenterWeakReference = new WeakReference<>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            DetailMusicPresenter presenter = presenterWeakReference.get();
            if(presenter.currentMusic != null) {
                presenter.mPlayDetailView.updateSeekBar(presenter.getCurrentTime());
                presenter.mPlayDetailView.updateCurrentTimeTv(presenter.getCurrentTime());
                Lyric lyric = new Lyric(presenter.currentMusic);
                presenter.mPlayDetailView.updateLyricView(lyric.getLyricList(),
                        (int) presenter.currentMusic.getDuration(), presenter.getCurrentTime());
            }
        }
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
    public int getCurrentTime() {
        if(PlayService.mediaPlayer != null) {
            try {
                return PlayService.mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateUI(PlayService.MusicChangedEvent event) {
        currentMusic = event.currentMusic;
        mPlayDetailView.updateUI(event.currentMusic);
        mPlayDetailView.updatePlayButton(event.musicState);
    }

    @Override
    public void subscribe() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unsubscribe() {
        EventBus.getDefault().unregister(this);
    }
}
