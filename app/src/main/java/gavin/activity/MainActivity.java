package gavin.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import gavin.adapter.ListViewAdapter;
import gavin.constant.R;
import gavin.controller.MusicPlayer;
import gavin.service.PlayService;

public class MainActivity extends BaseActivity {
    private ListView mListView;
    private TextView mSongNum;
    private ImageView mMusicAlbum;
    private View mHeader;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private TextView mSongName;
    private TextView mArtist;
    private LinearLayout mPlayColumn;

    private MusicPlayer musicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findView();                                     //初始化控件
        setListener();                               //绑定监听器
        musicPlayer = MusicPlayer.getInstance(this);
        musicPlayer.initMusicList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh_music_list) {
            musicPlayer.refreshMusicList();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }

        return super.onKeyDown(keyCode, event);
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
                        musicPlayer.resumeMusic();
                        musicWaitPlay = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (PlayService.musicState == PlayService.PLAYING) {
                        musicPlayer.pauseMusic();
                        musicWaitPlay = true;
                    }
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    /**
     * 歌曲列表监听器,当列表被点击时，开始播放本行对应的歌曲
     */

    class MusicListViewListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                PlayService.currentMusic = musicPlayer.getMusicList().get(position);
            } else {
                PlayService.currentMusic = musicPlayer.getMusicList().get(position - 1);
            }

            musicPlayer.startMusic();
        }
    }

    /**
     * 播放栏监听器，当播放栏被点击时进入播放界面
     */
    class PlayColumnListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, PlayerActivity.class);
            startActivityForResult(intent, 0);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateUI();
    }

    /**
     * 播放按钮监听器
     */
    class PlayButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (PlayService.musicState) {
                case PlayService.STOP:
                    musicPlayer.startMusic();
                    break;
                case PlayService.PLAYING:
                    musicPlayer.pauseMusic();
                    break;
                case PlayService.PAUSE:
                    musicPlayer.resumeMusic();
            }
        }
    }

    /**
     * 下一曲按钮监听器
     */
    class NextButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            musicPlayer.nextMusic();
        }

    }

    /**
     * 初始化控件
     */
    private void findView() {
        mHeader = getLayoutInflater().inflate(R.layout.song_list_header, null);
        mListView = (ListView) findViewById(R.id.musicList);
        mSongNum = (TextView) mHeader.findViewById(R.id.songNum);
        mMusicAlbum = (ImageView) findViewById(R.id.musicAlbum);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);
        mSongName = (TextView) findViewById(R.id.musicName);
        mArtist = (TextView) findViewById(R.id.artist);
        mPlayColumn = (LinearLayout) findViewById(R.id.playColumn);
    }

    /**
     * 添加监听器
     */
    private void setListener() {
        mListView.setOnItemClickListener(new MusicListViewListener());    //建立列表点击监听
        mPlayButton.setOnClickListener(new PlayButtonListener());
        mNextButton.setOnClickListener(new NextButtonListener());
        mPlayColumn.setOnClickListener(new PlayColumnListener());

        exPhoneCallListener exPhoneCallListener = new exPhoneCallListener();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(exPhoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * UI视图更新
     */
    @Override
    public void updateUI() {
        switch (PlayService.musicState) {
            case PlayService.PLAYING:
                mSongName.setText(PlayService.currentMusic.getMusicName());
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
        mSongName.setText(PlayService.currentMusic.getMusicName());
        mArtist.setText(PlayService.currentMusic.getArtist());
        mMusicAlbum.setImageBitmap(PlayService.currentMusic.getAlbum());
    }

    /**
     * 初始化歌曲文件列表
     */
    public void setSongListView() {
        mListView.removeHeaderView(mHeader);
        String musicNum = "（共" + musicPlayer.getMusicList().size() + "首）";
        mSongNum.setText(musicNum);

        ArrayList<HashMap<String, String>> items = new ArrayList<>();
        for (int i = 0; i < musicPlayer.getMusicList().size(); i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("itemId", "" + (i + 1));
            map.put("musicName", musicPlayer.getMusicList().get(i).getMusicName());
            map.put("musicInfo", musicPlayer.getMusicList().get(i).getArtist() +
                    " - " + musicPlayer.getMusicList().get(i).getAlbumName());
            items.add(map);
        }
        ListViewAdapter listViewAdapter = new ListViewAdapter(items, MainActivity.this);
        mListView.setAdapter(listViewAdapter);
        mListView.addHeaderView(mHeader);
    }

    /**
     * 判断歌曲播放服务是否正在运行
     */
    private boolean serviceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningServices =
                (ArrayList<ActivityManager.RunningServiceInfo>) activityManager.getRunningServices(30);
        for (int i = 0; i < runningServices.size(); i++) {
            if (runningServices.get(i).service.getClassName().equals("gavin.service.PlayService")) {
                return true;
            }
        }
        return false;
    }

}
