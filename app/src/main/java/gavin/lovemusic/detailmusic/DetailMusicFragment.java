package gavin.lovemusic.detailmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.LyricRow;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;

/**
 * Created by GavinLi
 * on 16-9-20.
 */
public class DetailMusicFragment extends Fragment implements DetailMusicContract.View,
        OnLyricViewSeekListener{
    @BindView(R.id.bgImageView) ImageView mBgImageView;
    @BindView(R.id.seekBarColumn) LinearLayout mSeekBarColumn;
    @BindView(R.id.playColumn) LinearLayout mPlayCoumn;
    @BindView(R.id.playButton) ImageButton mPlayButton;
    @BindView(R.id.currentTime) TextView mCurrentTime;
    @BindView(R.id.duration) TextView mDuration;
    @BindView(R.id.lyricView) LyricView mLyricView;
    @BindView(R.id.lyricSeekBar) SeekBar mLyricSeekBar;

    private DetailMusicContract.Presenter mDetailMusicPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_detail, container, false);
        ButterKnife.bind(this, view);
        mLyricSeekBar.setOnSeekBarChangeListener(new LyricSeekBarListener());
        mLyricView.setOnLyricViewSeekListener(this);
        mDetailMusicPresenter.subscribe();
        return view;
    }

    @Override
    public void onDestroyView() {
        mDetailMusicPresenter.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public void changeLyricView(ArrayList<LyricRow> lyricList) {
        mLyricView.setLyricList(lyricList);
    }

    @Override
    public void updateLyricView(int currentTime) {
        mLyricView.setTime(currentTime);
    }

    @Override
    public void updateSeekBar(int duration, int progress) {
        mLyricSeekBar.setMax(duration);
        mLyricSeekBar.setProgress(progress);
        mCurrentTime.setText(musicTimeFormat(progress));
        mDuration.setText(musicTimeFormat(duration));
    }

    @Override
    public void lyricViewSeek(LyricRow lyricRow) {
        mDetailMusicPresenter.setMusicProgress((int) lyricRow.getLyricTime(), getContext());
    }

    /**
     * 进度条监听
     */
    class LyricSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDetailMusicPresenter.setMusicProgress(seekBar.getProgress(), getActivity());
        }
    }

    @OnClick(R.id.playButton) void onPlayButtonClick() {
        mDetailMusicPresenter.onPlayButtonClick(getActivity());
    }

    @OnClick({R.id.previousButton, R.id.nextButton})
    void onButtonClick(android.view.View v) {
        switch (v.getId()) {
            case R.id.previousButton:
                mDetailMusicPresenter.changeMusic(getActivity(), ActivityCommand.PREVIOUS_MUSIC);
                break;
            case R.id.nextButton:
                mDetailMusicPresenter.changeMusic(getActivity(), ActivityCommand.NEXT_MUSIC);
                break;
        }
    }

    @Override
    public void changePlayToPause() {
        mPlayButton.setBackgroundResource(R.drawable.play_prey);
    }

    @Override
    public void changePauseToPlay() {
        mPlayButton.setBackgroundResource(R.drawable.pause);
    }

    @Override
    public void updateBgImage(String bgImageUrl) {
        Glide.with(this)
                .load(bgImageUrl)
                .into(mBgImageView);
    }

    @Override
    public void changeViewColor(Palette.Swatch swatch) {
        mPlayCoumn.setBackgroundColor(swatch.getRgb());
        mSeekBarColumn.setBackgroundColor(swatch.getRgb());
    }

    @Override
    public void changeViewColorDefault() {
        //noinspection deprecation
        mPlayCoumn.setBackgroundColor(getResources().getColor(R.color.playColumnDefault));
        //noinspection deprecation
        mSeekBarColumn.setBackgroundColor(getResources().getColor(R.color.playColumnDefault));
    }

    public void initMusic(Music music) {
        new DetailMusicPresenter(this).initMusic(music);
    }

    @Override
    public void setPresenter(DetailMusicContract.Presenter presenter) {
        mDetailMusicPresenter = presenter;
    }

    //以mm:ss形式获取歌曲已播放的时间
    private String musicTimeFormat(long time) {
        String currentTimeStr;
        long currentTimeMinute = time / 60000;
        long currentTimeSecond = (time / 1000) % 60;
        if (currentTimeMinute < 10) {
            currentTimeStr = "0" + currentTimeMinute;
        } else {
            currentTimeStr = "" + currentTimeMinute;
        }
        if (currentTimeSecond < 10) {
            currentTimeStr = currentTimeStr + ":0" + currentTimeSecond;
        } else {
            currentTimeStr = currentTimeStr + ":" + currentTimeSecond;
        }
        return currentTimeStr;
    }
}
