package gavin.lovemusic.detailmusic.view;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gavin.lovemusic.constant.R;
import gavin.lovemusic.detailmusic.presenter.IPlayDetailPresenter;
import gavin.lovemusic.detailmusic.presenter.PlayDetailPresenter;
import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi
 * on 16-9-20.
 */
public class PlayDetailFragment extends Fragment implements IPlayDetailView {
    @BindView(R.id.bgImageView) ImageView mBgImageView;
    @BindView(R.id.playButton) ImageButton mPlayButton;
//    @BindView(R.id.playModeButton) ImageButton mPlayModeButton;
    @BindView(R.id.lyricSeekBar) SeekBar mLyricSeekBar;
    @BindView(R.id.currentTime) TextView mCurrentTime;
    @BindView(R.id.duration) TextView mDuration;
    @BindView(R.id.lyricView) LyricView lyricView;

    private ArrayList<LyricContent> lyricList;

    private IPlayDetailPresenter playDetailPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_detail, container, false);
        ButterKnife.bind(this, rootView);

        playDetailPresenter = new PlayDetailPresenter(this);

        setListener();

        return rootView;
    }

    public void init() {
        new Thread(() -> {
            while (true) {
                try {
                    Lyric lyric = new Lyric(PlayService.currentMusic);
                    lyricList = lyric.getLyricList();
                    lyricView.setLyricList(lyricList);
                    lyricView.setIndex(getLyricIndex());
                    if (PlayService.prepared) {
                        mLyricSeekBar.setProgress((int) getCurrentTime());
                    }
                    getActivity().runOnUiThread(() -> lyricView.invalidate());
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    /**
     * 进度条监听
     */
    class LyricSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (PlayService.prepared) {
                mCurrentTime.setText(getCurrentTimeStr());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (PlayService.prepared) {
                playDetailPresenter.setMusicProgress(seekBar.getProgress(), getActivity());
                mCurrentTime.setText(getCurrentTimeStr());
                mDuration.setText(getDurationStr());
            }
        }
    }

    @OnClick(R.id.playButton) void onPlayButtonClick() {
        playDetailPresenter.onPlayButtonClick(getActivity());
    }

    @OnClick({R.id.previousButton, R.id.nextButton})
    void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.previousButton:
                playDetailPresenter.changeMusic(getActivity(), ActivityCommand.PREVIOUS_MUSIC);
                break;
            case R.id.nextButton:
                playDetailPresenter.changeMusic(getActivity(), ActivityCommand.NEXT_MUSIC);
                break;
//            case R.id.playModeButton:
//                changePlayMode();
//                break;
        }
    }

    /*
    private void changePlayMode() {
        switch (PlayService.playMode) {
            case PlayService.REPEAT:
                PlayService.playMode = PlayService.REPEAT_ONE;
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_repeatone);
                Toast.makeText(getActivity(), "单曲循环", Toast.LENGTH_SHORT).show();
                break;
            case PlayService.REPEAT_ONE:
                PlayService.playMode = PlayService.SHUFFLE;
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_shuffle);
                Toast.makeText(getActivity(), "随机播放", Toast.LENGTH_SHORT).show();
                break;
            case PlayService.SHUFFLE:
                PlayService.playMode = PlayService.REPEAT;
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_repeat);
                Toast.makeText(getActivity(), "顺序播放", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    */

    /**
     * 根据当前播放时间获取歌词索引
     */
    private int getLyricIndex() {
        int lyricIndex = 0;
        long currentTime = 0;
        long duration = 0;
        if (PlayService.musicState == PlayService.PLAYING) {
            currentTime = getCurrentTime();
            duration = getDuration();
        }

        if (currentTime < duration) {
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
        }
        return lyricIndex;
    }

    /**
     * UI更新
     */
    @Override
    public void updateUI() {
        switch (PlayService.musicState) {
            case PlayService.PLAYING:
                mPlayButton.setBackgroundResource
                        (R.drawable.img_button_notification_play_pause_grey);
                break;
            case PlayService.PAUSE:
                mPlayButton.setBackgroundResource
                        (R.drawable.img_button_notification_play_play_grey);
                break;
            case PlayService.STOP:
                mPlayButton.setBackgroundResource
                        (R.drawable.img_button_notification_play_play_grey);
                break;
        }

        /*
        switch (PlayService.playMode) {
            case PlayService.REPEAT:
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_repeat);
                break;
            case PlayService.REPEAT_ONE:
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_repeatone);
                break;
            case PlayService.SHUFFLE:
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_shuffle);
                break;
        }
        */

        mBgImageView.setImageBitmap(PlayService.currentMusic.getAlbum());

        if (PlayService.prepared) {
            mLyricSeekBar.setMax((int) getDuration());
            mLyricSeekBar.setProgress((int) getCurrentTime());
            mCurrentTime.setText(getCurrentTimeStr());
            mDuration.setText(getDurationStr());
        }
    }

    /**
     * 绑定监听器
     */
    private void setListener() {
        mLyricSeekBar.setOnSeekBarChangeListener(new LyricSeekBarListener());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayService.SERVICE_COMMAND);
    }

    /**
     * 以mm:ss形式获取歌曲已播放的时间
     */
    private String getCurrentTimeStr() {
        String currentTimeStr;
        int currentTimeMinute = PlayService.mediaPlayer.getCurrentPosition() / 60000;
        int currentTimeSecond = (PlayService.mediaPlayer.getCurrentPosition() / 1000) % 60;
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

    /**
     * 以mm:ss形式获取歌曲时长
     */
    public String getDurationStr() {
        String durationStr;
        int durationMinute = PlayService.mediaPlayer.getDuration() / 60000;
        int durationSecond = (PlayService.mediaPlayer.getDuration() / 1000) % 60;
        if (durationMinute < 10) {
            durationStr = "0" + durationMinute;
        } else {
            durationStr = "" + durationMinute;
        }
        if (durationSecond < 10) {
            durationStr = durationStr + ":0" + durationSecond;
        } else {
            durationStr = durationStr + ":" + durationSecond;
        }
        return durationStr;
    }

    /**
     * 获取歌曲已播放时间
     */
    public long getCurrentTime() {
        return PlayService.mediaPlayer.getCurrentPosition();
    }

    /**
     * 获取歌曲时长
     */
    public long getDuration() {
        return PlayService.currentMusic.getDuration();
    }

    public IPlayDetailPresenter getPlayDetailPresenter() {
        return playDetailPresenter;
    }
}
