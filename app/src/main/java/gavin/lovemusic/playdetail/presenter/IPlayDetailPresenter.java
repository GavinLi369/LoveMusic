package gavin.lovemusic.playdetail.presenter;

import android.content.Context;

import gavin.lovemusic.service.ActivityCommand;

/**
 * Created by GavinLi on 16-9-10.
 * IPlayDetailPresenter
 */
public interface IPlayDetailPresenter {
    void setMusicProgress(int progress, Context context);

    void onPlayButtonClick(Context context);

    void changeMusic(Context context, ActivityCommand command);
}
