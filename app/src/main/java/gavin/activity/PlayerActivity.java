package gavin.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import gavin.lovemusic.R;
import gavin.model.Lyric;
import gavin.model.LyricContent;
import gavin.service.PlayService;
import gavin.view.LyricView;


/**
 * Created by Gavin on 2015/8/24.
 * 播放界面
 */
public class PlayerActivity extends Activity {
    private TextView mMusicName;
    private TextView mArtist;
    private ImageView mBgImageView;
    private ImageButton mPreviousButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ImageButton mBackButton;
    private ImageButton mPlayModeButton;
    private SeekBar mLyricSeekBar;
    private TextView mCurrentTime;
    private TextView mDuration;

    private LyricView lyricView;
    private ArrayList<LyricContent> lyricList = null;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_player);

        this.findView();
        this.setListener();
        this.updateUI();
        new Thread(new lyricRunnable()).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * 开始一首新的歌曲
     */
    private void startMusic() {
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("musicCommand", PlayService.PLAY_MUSIC);
        this.startService(intent);
    }

    /**
     * 暂停后开始播放歌曲
     */
    private void resumeMusic() {
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("musicCommand", PlayService.RESUME_MUSIC);
        this.startService(intent);
    }

    /**
     * 暂停正在播放的歌曲
     */
    private void pauseMusic() {
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("musicCommand", PlayService.PAUSE_MUSIC);
        this.startService(intent);
    }

    /**
     * 播放下一首歌曲
     */
    private void nextMusic() {
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("musicCommand", PlayService.NEXT_MUSIC);
        this.startService(intent);
    }

    /**
     * 播放上一首歌曲
     */
    private void previousMusic() {
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("musicCommand", PlayService.PREVIOUS_MUSIC);
        this.startService(intent);
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
                setMusicProgress(seekBar.getProgress());
                mCurrentTime.setText(getCurrentTimeStr());
                mDuration.setText(getDurationStr());
            }
        }
    }

    /**
     * 歌词视图更新设置
     */
    class lyricRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Lyric lyric = new Lyric(PlayService.currentMusic, PlayerActivity.this);
                    lyricList = lyric.getLyricList();
                    lyricView.setLyricList(lyricList);
                    lyricView.setIndex(getLyricIndex());
                    if (PlayService.prepared) {
                        mLyricSeekBar.setProgress((int) getCurrentTime());
                    }
                    handler.post(updateLyricRunnable);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    /**
     * 歌词视图更新
     */
    Runnable updateLyricRunnable = new Runnable() {
        @Override
        public void run() {
            lyricView.invalidate();              //更新视图
        }
    };

    /**
     * 按钮监听器
     */
    class myListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.previousButton:
                    previousMusic();
                    break;
                case R.id.nextButton:
                    nextMusic();
                    break;
                case R.id.backButton:
                    PlayerActivity.this.finish();
                    break;
            }
        }
    }

    /**
     * 播放模式切换
     */
    private void changePlayMode() {
        switch (PlayService.playMode) {
            case PlayService.REPEAT:
                PlayService.playMode = PlayService.REPEAT_ONE;
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_repeatone);
                Toast.makeText(PlayerActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
                break;
            case PlayService.REPEAT_ONE:
                PlayService.playMode = PlayService.SHUFFLE;
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_shuffle);
                Toast.makeText(PlayerActivity.this, "随机播放", Toast.LENGTH_SHORT).show();
                break;
            case PlayService.SHUFFLE:
                PlayService.playMode = PlayService.REPEAT;
                mPlayModeButton.setBackgroundResource
                        (R.drawable.img_appwidget_playmode_repeat);
                Toast.makeText(PlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
                break;
        }
    }

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
     * 播放按钮监听器
     */
    class PlayButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (PlayService.musicState) {
                case PlayService.STOP:
                    startMusic();
                    break;
                case PlayService.PLAYING:
                    pauseMusic();
                    break;
                case PlayService.PAUSE:
                    resumeMusic();
                    break;
            }
        }
    }

    /**
     * UI更新
     */
    public void updateUI() {
        switch (PlayService.musicState) {
            case PlayService.PLAYING:
                mMusicName.setText(PlayService.currentMusic.getMusicName());
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

        mMusicName.setText(PlayService.currentMusic.getMusicName());
        mArtist.setText(PlayService.currentMusic.getArtist());
        mBgImageView.setImageBitmap(PlayService.currentMusic.getAlbum());

        if (PlayService.prepared) {
            mLyricSeekBar.setMax((int) getDuration());
            mLyricSeekBar.setProgress((int) getCurrentTime());
            mCurrentTime.setText(getCurrentTimeStr());
            mDuration.setText(getDurationStr());
        }
    }

    /**
     * 初始化控件
     */
    private void findView() {
        mMusicName = (TextView) findViewById(R.id.musicName);
        mArtist = (TextView) findViewById(R.id.artist);
        mBgImageView = (ImageView) findViewById(R.id.bgImageView);
        mPreviousButton = (ImageButton) findViewById(R.id.previousButton);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);
        mBackButton = (ImageButton) findViewById(R.id.backButton);
        mPlayModeButton = (ImageButton) findViewById(R.id.playModeButton);
        lyricView = (LyricView) findViewById(R.id.lyricView);
        mLyricSeekBar = (SeekBar) findViewById(R.id.lyricSeekBar);
        mCurrentTime = (TextView) findViewById(R.id.currentTime);
        mDuration = (TextView) findViewById(R.id.duration);
    }

    /**
     * 绑定监听器
     */
    private void setListener() {
        mPreviousButton.setOnClickListener(new myListener());
        mNextButton.setOnClickListener(new myListener());
        mBackButton.setOnClickListener(new myListener());
        mPlayButton.setOnClickListener(new PlayButtonListener());
        mPlayModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayMode();
            }
        });
        mLyricSeekBar.setOnSeekBarChangeListener(new LyricSeekBarListener());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayService.SERVICE_COMMAND);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PlayService.SERVICE_COMMAND)) {
                if (intent.getExtras() == null) {
                    updateUI();
                }

            }
        }
    };

    /**
     * 设置播放进度
     */
    public void setMusicProgress(int progress) {
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("musicCommand", PlayService.CHANGE_PROGRESS);
        intent.putExtra("progress", progress);
        startService(intent);
    }

    /**
     * 以mm:ss形式获取歌曲已播放的时间
     */
    public String getCurrentTimeStr() {
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
}
