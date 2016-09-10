package gavin.lovemusic.playdetail.presenter;

import android.content.Context;

/**
 * Created by GavinLi on 16-9-10.
 * IPlayDetailPresenter
 */
public interface IPlayDetailPresenter {
    void setMusicProgress(int progress, Context context);
    void onPlayButtonClick(Context context);
    void nextMusic(Context context);
    void previousMusic(Context context);
    void musicStatusChanged();
}
