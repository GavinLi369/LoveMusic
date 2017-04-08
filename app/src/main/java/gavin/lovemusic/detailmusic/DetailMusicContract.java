package gavin.lovemusic.detailmusic;

import android.content.Context;
import android.support.v7.graphics.Palette;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import gavin.lovemusic.BasePresenter;
import gavin.lovemusic.BaseView;
import gavin.lovemusic.detailmusic.view.LyricRow;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;

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

        void changeLyricView(ArrayList<LyricRow> lyricList);

        void changePlayToPause();

        void changePauseToPlay();

        void modifySeekBar(int duration, int progress);
    }

    interface Presenter extends BasePresenter {
        void initMusicDetail();

        void setMusicProgress(int progress, Context context);

        void onPlayButtonClick(Context context);

        void changeMusic(Context context, ActivityCommand command);

        long getMusicDuration();

        void release();
    }
}
