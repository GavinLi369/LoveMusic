package gavin.lovemusic.musicdetail;

import android.content.Context;
import android.support.v7.graphics.Palette;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.musicdetail.view.LyricRow;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public class DetailMusicContract {
    interface Model {
        ArrayList<LyricRow> getMusicLyric(Music music) throws IOException, JSONException;
    }

    interface View extends BaseView<Presenter> {
        void updateBgImage(String bgImageUrl);

        void changeViewColor(Palette.Swatch swatch);

        void changeViewColorDefault();

        void showFindingLyric();

        void showNotFoundLyric();

        void changeLyricView(List<LyricRow> lyricList);

        void changePlayToPause();

        void changePauseToPlay();

        void modifySeekBar(int duration, int progress);

        void modifySeekBarBuffer(int progress);
    }

    interface Presenter extends BasePresenter {
        void initMusicDetail();

        void setMusicProgress(int progress, Context context);

        void onPlayButtonClick();

        void previousMusic(Context context);

        void nextMusic(Context context);

        void release();
    }
}
