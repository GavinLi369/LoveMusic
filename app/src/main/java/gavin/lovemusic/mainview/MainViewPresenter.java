package gavin.lovemusic.mainview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi on 16-9-10.
 * MainViewPresenter
 */
public class MainViewPresenter implements MainViewContract.Presenter {
    private MainViewContract.View mMusicListView;


    public MainViewPresenter(MainViewContract.View mMusicListView) {
        this.mMusicListView = mMusicListView;
        mMusicListView.setPresenter(this);
    }

    @Override
    public void onPlayButtonClick(Context context) {
        switch (PlayService.musicState) {
            case PlayService.PLAYING:
                changeMusicStatus(context, ActivityCommand.PAUSE_MUSIC);
                break;
            case PlayService.PAUSE:
                changeMusicStatus(context, ActivityCommand.RESUME_MUSIC);
        }
    }

    @Override
    public void changeMusicStatus(Context context, ActivityCommand command) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", command);
        context.startService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateUI(PlayService.MusicChangedEvent event) {
        Bitmap album = BitmapFactory.decodeFile(event.currentMusic.getAlbumPath());
        Palette.from(album)
                .maximumColorCount(32)
                .generate(palette -> {
                    Palette.Swatch swatch = palette.getDarkVibrantSwatch();
                    if(swatch != null) {
                        mMusicListView.changeDragViewColor(swatch);
                    } else {
                        mMusicListView.changeDragViewColorDefault();
                    }
                });

        switch (event.musicState) {
            case PlayService.PLAYING: mMusicListView.changePlayButton2Pause(); break;
            case PlayService.PAUSE: mMusicListView.changePauseButton2Play(); break;
        }

        mMusicListView.changeMusicInfo(event.currentMusic);
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
