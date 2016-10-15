package gavin.lovemusic.detailmusic;

import android.content.Context;
import android.support.v7.graphics.Palette;

import java.util.ArrayList;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.entity.LyricRow;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class DetailMusicContract {
    interface View extends BaseView<Presenter> {
        void updateBgImage(Music currentMusic);

        void changeDragViewColor(Palette.Swatch swatch);

        void changeDragViewColorDefault();

        void changeLyricView(ArrayList<LyricRow> lyricList);

        void updateLyricView(int currentTime);

        void changePlayToPause();

        void changePauseToPlay();

        void updateSeekBar(int duration, int progress);
    }

    interface Presenter extends BasePresenter {
        void setMusicProgress(int progress, Context context);

        void onPlayButtonClick(Context context);

        void changeMusic(Context context, ActivityCommand command);
    }
}
