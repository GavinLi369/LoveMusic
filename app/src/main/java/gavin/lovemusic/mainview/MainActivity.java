package gavin.lovemusic.mainview;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.musicdetail.DetailMusicFragment;
import gavin.lovemusic.musicdetail.DetailMusicModel;
import gavin.lovemusic.musicdetail.DetailMusicPresenter;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.PlayService;
import gavinli.slidinglayout.OnViewStatusChangedListener;
import gavinli.slidinglayout.SlidingLayout;

/**
 * Created by GavinLi
 * on 16-9-18.
 */
public class MainActivity extends AppCompatActivity implements MainViewContract.View,
        View.OnClickListener, OnViewStatusChangedListener {
    private RelativeLayout mMainLayout;
    private ImageButton mPlayButton;
    private TextView mMusicName;
    private TextView mArtist;
    private SlidingLayout mSlidingLayout;
    private RelativeLayout mDragView;

    private MainViewContract.Presenter mPresenter;
    private PlayService mPlayService;

    private boolean mPlaying = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        Intent intent = new Intent(this, PlayService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mPlayService = ((PlayService.PlayServiceBinder) iBinder).getService();

            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            tm.listen(new exPhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE);

            ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
            SectionPagerAdapter adapter = new SectionPagerAdapter(
                    getSupportFragmentManager(),
                    MainActivity.this, mPlayService);
            viewPager.setAdapter(adapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
            mMainLayout = (RelativeLayout) findViewById(R.id.layout_main);

            new MainViewPresenter(MainActivity.this, mPlayService);
            mPlayService.registerListener(
                    (PlayService.OnMusicStatListener) mPresenter);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mPlayService.unregisterListener(
                    (PlayService.OnMusicStatListener) mPresenter);
        }
    };

    @Override
    public void onClick(View v) {
        if(v.getId() == mPlayButton.getId())
            mPresenter.onPlayButtonClicked(this);
    }

    @Override
    public void changeDragViewColor(Palette.Swatch swatch) {
        if(mSlidingLayout != null) {
            mDragView.setBackgroundColor(swatch.getRgb());
            mMusicName.setTextColor(swatch.getTitleTextColor());
            mArtist.setTextColor(swatch.getBodyTextColor());
        }
    }

    @Override
    public void changeDragViewColorDefault() {
        if(mSlidingLayout != null) {
            mDragView.setBackgroundColor(getResources().getColor(R.color.playColumnDefault));
            mMusicName.setTextColor(getResources().getColor(R.color.titleTextColorDefault));
            mArtist.setTextColor(getResources().getColor(R.color.bodyTextColorDefault));
        }
    }

    @Override
    public void changePlaying2Pause() {
        mPlaying = false;
        if(mPlayButton != null) {
            mPlayButton.setBackgroundResource(R.drawable.play_prey);
        }
    }

    @Override
    public void changePause2Playing() {
        mPlaying = true;
        if(mPlayButton != null) {
            mPlayButton.setBackgroundResource(R.drawable.pause);
        }
    }

    @Override
    public void changeMusicInfoes(Music currentMusic) {
        if(mSlidingLayout != null) {
            mMusicName.setText(currentMusic.getTitle());
            mArtist.setText(currentMusic.getArtist());
        }
    }

    @Override
    public void showMusicPlayView(Music music) {
        if(mSlidingLayout == null) {
            mSlidingLayout = (SlidingLayout) View.inflate(this, R.layout.music_player, null);
            mSlidingLayout.setOnViewStatusChangedListener(this);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            DetailMusicFragment detailMusicFragment = new DetailMusicFragment();
            transaction.replace(R.id.fragment_music_detail, detailMusicFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            new DetailMusicPresenter(detailMusicFragment, new DetailMusicModel(this), music, mPlayService);

            mMainLayout.addView(mSlidingLayout);
            mDragView = (RelativeLayout) mSlidingLayout.findViewById(R.id.view_drag);
            mMusicName = (TextView) mSlidingLayout.findViewById(R.id.musicName);
            mArtist = (TextView) mSlidingLayout.findViewById(R.id.artist);
        }
    }

    @Override
    public void onViewMaximized() {
        ((ViewGroup) mPlayButton.getParent()).removeView(mPlayButton);
    }

    @Override
    public void onViewMinimized() {
        mPlayButton = new ImageButton(this);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
        int top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
        layoutParams.setMargins(left, top, 0, 0);
        mPlayButton.setLayoutParams(layoutParams);
        if(mPlaying) {
            mPlayButton.setBackground(getResources().getDrawable(R.drawable.pause));
        } else {
            mPlayButton.setBackground(getResources().getDrawable(R.drawable.play_prey));
        }
        mPlayButton.setOnClickListener(this);
        mDragView.addView(mPlayButton);
    }

    @Override
    public void onViewRemoved() {
        mPresenter.changeMusicStatus(MainActivity.this, ActivityCommand.PAUSE_MUSIC);
        mSlidingLayout.removeView(findViewById(R.id.fragment_music_detail));
        mMainLayout.removeView(mSlidingLayout);
        mSlidingLayout = null;
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
                        mPresenter.changeMusicStatus(MainActivity.this, ActivityCommand.RESUME_MUSIC);
                        musicWaitPlay = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (PlayService.musicState == PlayService.PLAYING) {
                        mPresenter.changeMusicStatus(MainActivity.this, ActivityCommand.PAUSE_MUSIC);
                        musicWaitPlay = true;
                    }
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    @Override
    public void setPresenter(MainViewContract.Presenter musicListPresenter) {
        mPresenter = musicListPresenter;
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
