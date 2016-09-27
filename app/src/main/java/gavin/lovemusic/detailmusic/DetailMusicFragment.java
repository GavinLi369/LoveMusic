package gavin.lovemusic.detailmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
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
import gavin.lovemusic.entity.LyricContent;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;

/**
 * Created by GavinLi
 * on 16-9-20.
 */
public class DetailMusicFragment extends Fragment implements DetailMusicContract.View {
    @BindView(R.id.bgImageView) ImageView mBgImageView;
    @BindView(R.id.seekBarColumn) LinearLayout mSeekBarColumn;
    @BindView(R.id.playColumn) LinearLayout mPlayCoumn;
    @BindView(R.id.playButton) ImageButton mPlayButton;
    @BindView(R.id.currentTime) TextView mCurrentTime;
    @BindView(R.id.duration) TextView mDuration;
    @BindView(R.id.lyricView) LyricView lyricView;
    @BindView(R.id.lyricSeekBar) SeekBar mLyricSeekBar;

    private DetailMusicContract.Presenter mDetailMusicPresenter;

    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                          @Nullable Bundle savedInstanceState) {
        android.view.View rootView = inflater.inflate(R.layout.fragment_music_detail, container, false);
        ButterKnife.bind(this, rootView);

        mLyricSeekBar.setOnSeekBarChangeListener(new LyricSeekBarListener());

        new DetailMusicPresenter(this);
        mDetailMusicPresenter.subscribe();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDetailMusicPresenter.unsubscribe();
    }

    @Override
    public void updateLyricView(ArrayList<LyricContent> lyricList, int duration, int currentTime) {
        lyricView.setLyricList(lyricList);
        lyricView.setIndex(getLyricIndex(lyricList, currentTime, duration));
        lyricView.invalidate();
    }

    @Override
    public void updateSeekBar(int duration, int progress) {
        mLyricSeekBar.setMax(duration);
        mLyricSeekBar.setProgress(progress);
        mCurrentTime.setText(musicTimeFormat(progress));
        mDuration.setText(musicTimeFormat(duration));
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
    public void updateBgImage(Music currentMusic) {
        Glide.with(this)
                .load(currentMusic.getImage())
                .into(mBgImageView);
    }

    @Override
    public void changeDragViewColor(Palette.Swatch swatch) {
        mPlayCoumn.setBackgroundColor(swatch.getRgb());
        mSeekBarColumn.setBackgroundColor(swatch.getRgb());
    }

    @Override
    public void changeDragViewColorDefault() {
        //noinspection deprecation
        mPlayCoumn.setBackgroundColor(getResources().getColor(R.color.playColumnDefault));
        //noinspection deprecation
        mSeekBarColumn.setBackgroundColor(getResources().getColor(R.color.playColumnDefault));
    }

    @Override
    public void setPresenter(DetailMusicContract.Presenter presenter) {
        mDetailMusicPresenter = presenter;
    }

    //根据当前播放时间获取歌词索引
    private int getLyricIndex(ArrayList<LyricContent> lyricList,int duration, int currentTime) {
        if (currentTime < duration) {
            int lyricIndex = 0;
            if (lyricList != null) {
                int index = 0;
                for (int i = 0; i < lyricList.size(); i++) {
                    if (lyricList.get(i).getLyricTime() < currentTime) {
                        index++;
                    }
                    lyricIndex = index - 1;
                    if (lyricIndex < 0) {
                        lyricIndex = 0;
                    }
                }
            }
            return lyricIndex;
        } else {
            return 0;
        }
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
