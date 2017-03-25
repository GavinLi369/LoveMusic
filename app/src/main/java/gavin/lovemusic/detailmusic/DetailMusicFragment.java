package gavin.lovemusic.detailmusic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.detailmusic.view.LyricRow;
import gavin.lovemusic.detailmusic.view.LyricView;
import gavin.lovemusic.detailmusic.view.OnLyricViewSeekListener;
import gavin.lovemusic.service.ActivityCommand;
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

    private DetailMusicContract.Presenter mDetailMusicPresenter;

    private UpdateViewHandler handler = new UpdateViewHandler(this);
    private boolean mPlaying = true;
    private int mCurrentTime = 0;

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
        mDetailMusicPresenter.subscribe();

        //每隔0.5秒更新一次视图
        new Thread(() -> {
            while(true) {
                if(mPlaying)
                    handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
        return view;
    }

    @Override
    public void onDestroyView() {
        mDetailMusicPresenter.unsubscribe();
        super.onDestroyView();
    }

    static class UpdateViewHandler extends Handler {
        private final WeakReference<DetailMusicFragment> mFragmentWeakReference;

        public UpdateViewHandler(DetailMusicFragment fragment) {
            this.mFragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            DetailMusicFragment detailMusicFragment = mFragmentWeakReference.get();
            detailMusicFragment.mCurrentTime += 500;
            detailMusicFragment.modifySeekBar(
                    (int) detailMusicFragment.mDetailMusicPresenter.getMusicDuration(),
                    detailMusicFragment.mCurrentTime);
            detailMusicFragment.mLyricView.setTime(detailMusicFragment.mCurrentTime);
        }
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
    public void changeLyricView(ArrayList<LyricRow> lyricList) {
        mLyricView.setLyricList(lyricList);
    }

    @Override
    public void modifySeekBar(int duration, int progress) {
        mCurrentTime = progress;
        mLyricSeekBar.setMax(duration);
        mLyricSeekBar.setProgress(progress);
        mCurrentTimeTv.setText(musicTimeFormat(progress));
        mDuration.setText(musicTimeFormat(duration));
    }

    @Override
    public void onLyricViewSeek(LyricRow lyricRow) {
        mCurrentTime = (int) lyricRow.getLyricTime();
        mDetailMusicPresenter.setMusicProgress((int) lyricRow.getLyricTime(), getContext());
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
            mCurrentTime = seekBar.getProgress();
            mDetailMusicPresenter.setMusicProgress(seekBar.getProgress(), getActivity());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previousButton:
                mDetailMusicPresenter.changeMusic(getActivity(), ActivityCommand.PREVIOUS_MUSIC);
                break;
            case R.id.playButton:
                mDetailMusicPresenter.onPlayButtonClick(getActivity());
                break;
            case R.id.nextButton:
                mDetailMusicPresenter.changeMusic(getActivity(), ActivityCommand.NEXT_MUSIC);
                break;
        }
    }

    @Override
    public void changePlayToPause() {
        mPlaying = false;
        mPlayButton.setBackgroundResource(R.drawable.play_prey);
    }

    @Override
    public void changePauseToPlay() {
        mPlaying = true;
        mPlayButton.setBackgroundResource(R.drawable.pause);
    }

    @Override
    public void updateBgImage(String bgImageUrl) {
        Glide.with(this)
                .load(bgImageUrl)
                .bitmapTransform(new BlurTransformation(getContext(), 25, 8))
                .into(mBgImageView);
    }

    @Override
    public void changeViewColor(Palette.Swatch swatch) {
        mPlayCoumn.setBackgroundColor(swatch.getRgb());
    }

    @Override
    public void changeViewColorDefault() {
        //noinspection deprecation
        mPlayCoumn.setBackgroundColor(getResources().getColor(R.color.playColumnDefault));
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
