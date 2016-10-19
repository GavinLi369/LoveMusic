package gavin.lovemusic.mainview;

import android.content.Context;
import android.support.v7.graphics.Palette;

import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class MainViewContract {
    interface View extends BaseView<Presenter> {
        void changeDragViewColor(Palette.Swatch swatch);

        void changeDragViewColorDefault();

        void changePlaying2Pause();

        void changePause2Playing();

        void changeMusicInfoes(Music currentMusic);

        void showMusicPlayView(Music music);
    }

    interface Presenter extends BasePresenter {
        void onPlayButtonClicked(Context context);

        void changeMusicStatus(Context context, ActivityCommand command);
    }
}
