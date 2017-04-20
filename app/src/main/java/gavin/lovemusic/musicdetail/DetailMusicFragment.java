package gavin.lovemusic.musicdetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.musicdetail.view.LyricRow;
import gavin.lovemusic.musicdetail.view.LyricView;
import gavin.lovemusic.musicdetail.view.OnLyricViewSeekListener;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by GavinLi
 * on 16-9-20.
 */
public class DetailMusicFragment extends Fragment implements DetailMusicContract.View,
        OnLyricViewSeekListener, View.OnClickListener{
    private ImageView mBgImageView;
    private RelativeLayout mPlayCoumn;
    private ImageButton mPlayButton;
    private TextView mCurrentTimeTv;
    private TextView mDuration;
    private LyricView mLyricView;
    private SeekBar mLyricSeekBar;

    private DetailMusicContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_detail, container, false);
        mBgImageView = (ImageView) view.findViewById(R.id.bgImageView);
        mPlayCoumn = (RelativeLayout) view.findViewById(R.id.playColumn);
        mPlayButton = (ImageButton) view.findViewById(R.id.playButton);
        mCurrentTimeTv = (TextView) view.findViewById(R.id.currentTime);
        mDuration = (TextView) view.findViewById(R.id.duration);
        mLyricView = (LyricView) view.findViewById(R.id.lyricView);
        mLyricSeekBar = (SeekBar) view.findViewById(R.id.lyricSeekBar);
        ImageButton previousButton = (ImageButton) view.findViewById(R.id.previousButton);
        ImageButton NextButton = (ImageButton) view.findViewById(R.id.nextButton);
        mPlayButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        NextButton.setOnClickListener(this);
        mLyricSeekBar.setOnSeekBarChangeListener(new MusicSeekBarListener());
        mLyricView.setOnLyricViewSeekListener(this);
        mPresenter.initMusicDetail();
        return view;
    }

    @Override
    public void onDestroyView() {
        mPresenter.release();
        super.onDestroyView();
    }

    @Override
    public void showFindingLyric() {
        mLyricView.setFindingLyric();
    }

    @Override
    public void showNotFoundLyric() {
        mLyricView.setNotFoundLyric();
    }

    @Override
    public void changeLyricView(List<LyricRow> lyricList) {
        mLyricView.setLyricRows(lyricList);
    }

    @Override
    public void modifySeekBar(int duration, int progress) {
        mLyricSeekBar.setMax(duration);
        mLyricSeekBar.setProgress(progress);
        mCurrentTimeTv.setText(musicTimeFormat(progress));
        mDuration.setText(musicTimeFormat(duration));
        mLyricView.setTime(progress);
    }

    @Override
    public void modifySeekBarBuffer(int progress) {
        mLyricSeekBar.setSecondaryProgress(progress);
    }

    @Override
    public void onLyricViewSeek(LyricRow lyricRow) {
        mPresenter.setMusicProgress((int) lyricRow.getLyricTime(), getContext());
    }

    /**
     * 进度条监听
     */
    class MusicSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mPresenter.setMusicProgress(seekBar.getProgress(), getActivity());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previousButton:
                mPresenter.previousMusic(getContext());
                break;
            case R.id.playButton:
                mPresenter.onPlayButtonClick();
                break;
            case R.id.nextButton:
                mPresenter.nextMusic(getContext());
                break;
        }
    }

    @Override
    public void changePlayToPause() {
        mPlayButton.setBackgroundResource(R.drawable.play);
    }

    @Override
    public void changePauseToPlay() {
        mPlayButton.setBackgroundResource(R.drawable.pause);
    }

    @Override
    public void updateBgImage(String bgImageUrl) {
        Glide.with(this)
                .load(bgImageUrl)
                .bitmapTransform(new BlurTransformation(getContext(), 25, 4))
                .into(mBgImageView);
    }

    @Override
    public void changeViewColor(Palette.Swatch swatch) {
        mPlayCoumn.setBackgroundColor(swatch.getRgb());
    }

    @Override
    public void changeViewColorDefault() {
        mPlayCoumn.setBackgroundColor(getResources().getColor(R.color.playColumnDefault));
    }

    @Override
    public void setPresenter(DetailMusicContract.Presenter presenter) {
        mPresenter = presenter;
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
