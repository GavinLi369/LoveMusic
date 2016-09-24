package gavin.lovemusic.mainview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi
 * on 16-9-18.
 */
public class MainActivity extends AppCompatActivity implements MainViewContract.View {
    @BindView(R.id.playButton) ImageButton mPlayButton;
    @BindView(R.id.musicName) TextView mMusicName;
    @BindView(R.id.artist) TextView mArtist;
    @BindView(R.id.view_drag) LinearLayout mDragView;

    SectionPagerAdapter mAdapter;

    private MainViewContract.Presenter musicListPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        ButterKnife.bind(this);

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new exPhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE);

        Intent intent = new Intent(this, PlayService.class);
        startService(intent);

        new MainViewPresenter(this);
        musicListPresenter.subscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicListPresenter.unsubscribe();
    }

    @OnClick(R.id.playButton) void onPlayButtonClick() {
        musicListPresenter.onPlayButtonClick(this);
    }

    @Override
    public void changeDragViewColor(Palette.Swatch swatch) {
        mDragView.setBackgroundColor(swatch.getRgb());
        mMusicName.setTextColor(swatch.getTitleTextColor());
        mArtist.setTextColor(swatch.getBodyTextColor());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void changeDragViewColorDefault() {
        mDragView.setBackgroundColor(getResources().getColor(R.color.playColumnDefault));
        mMusicName.setTextColor(getResources().getColor(R.color.titleTextColorDefault));
        mArtist.setTextColor(getResources().getColor(R.color.bodyTextColorDefault));
    }

    @Override
    public void changePlayButton2Pause() {
        mPlayButton.setBackgroundResource(R.drawable.pause);
    }

    @Override
    public void changePauseButton2Play() {
        mPlayButton.setBackgroundResource(R.drawable.play_prey);
    }

    @Override
    public void changeMusicInfo(Music currentMusic) {
        mMusicName.setText(currentMusic.getTitle());
        mArtist.setText(currentMusic.getArtist());
    }

    /**
     * 来电监听，当播放音乐时，如果有来电则暂停音乐，当通话结束时继续播放
     */
    private class exPhoneCallListener extends PhoneStateListener {
        boolean musicWaitPlay = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (musicWaitPlay) {
                        musicListPresenter.changeMusicStatus(MainActivity.this, ActivityCommand.RESUME_MUSIC);
                        musicWaitPlay = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (PlayService.musicState == PlayService.PLAYING) {
                        musicListPresenter.changeMusicStatus(MainActivity.this, ActivityCommand.PAUSE_MUSIC);
                        musicWaitPlay = true;
                    }
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    @Override
    public void setPresenter(MainViewContract.Presenter musicListPresenter) {
        this.musicListPresenter = musicListPresenter;
    }

    //用户点击返回键后不会销毁Activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
