package gavin.lovemusic.mainview;

import android.support.v7.graphics.Palette;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.entity.Music;

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
        void onPlayButtonClicked();

        void pauseMusic();

        void resumeMusic();
    }
}
