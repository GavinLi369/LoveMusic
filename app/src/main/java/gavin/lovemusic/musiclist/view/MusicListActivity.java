package gavin.lovemusic.musiclist.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gavin.lovemusic.playdetail.view.PlayDetailActivity;
import gavin.lovemusic.constant.R;
import gavin.lovemusic.musiclist.presenter.IMusicListPresenter;
import gavin.lovemusic.musiclist.presenter.MusicListPresenter;
import gavin.lovemusic.service.PlayService;

public class MusicListActivity extends AppCompatActivity implements IMusicListView{
    @BindView(R.id.musicList) ListView mListView;
    @BindView(R.id.musicAlbum) ImageView mMusicAlbum;
    @BindView(R.id.playButton) ImageButton mPlayButton;
    @BindView(R.id.musicName) TextView mMusicName;
    @BindView(R.id.artist) TextView mArtist;
    private View mHeader;
    private TextView mSongNum;

    private IMusicListPresenter musicListPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ButterKnife.bind(this);
        mHeader = getLayoutInflater().inflate(R.layout.song_list_header, null);
        mSongNum = (TextView) mHeader.findViewById(R.id.songNum);
        musicListPresenter = new MusicListPresenter(this, this);
        //绑定监听器
        setListener();
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
            musicListPresenter.refreshMusicList(this);
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
                        musicListPresenter.resumeMusic(MusicListActivity.this);
                        musicWaitPlay = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (PlayService.musicState == PlayService.PLAYING) {
                        musicListPresenter.pauseMusic(MusicListActivity.this);
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
                PlayService.currentMusic = musicListPresenter.getMusicList().get(position);
            } else {
                PlayService.currentMusic = musicListPresenter.getMusicList().get(position - 1);
            }

            musicListPresenter.startMusic(MusicListActivity.this);
        }
    }

    @OnClick(R.id.playColumn) void onPlayColumnClick() {
        Intent intent = new Intent();
        intent.setClass(MusicListActivity.this, PlayDetailActivity.class);
        startActivityForResult(intent, 0);
    }

    @OnClick(R.id.playButton) void onPlayButtonClick() {
        musicListPresenter.onPlayButtonClick(this);
    }

    @OnClick(R.id.nextButton) void onNextButtonClick() {
        musicListPresenter.onNextButtonClick(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateUI();
    }

    /**
     * 添加监听器
     */
    private void setListener() {
        mListView.setOnItemClickListener(new MusicListViewListener());    //建立列表点击监听
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
        mMusicName.setText(PlayService.currentMusic.getMusicName());
        mArtist.setText(PlayService.currentMusic.getArtist());
        mMusicAlbum.setImageBitmap(PlayService.currentMusic.getAlbum());
    }

    /**
     * 初始化歌曲文件列表
     */
    @Override
    public void setSongListView() {
        mListView.removeHeaderView(mHeader);
        String musicNum = "（共" + musicListPresenter.getMusicList().size() + "首）";
        mSongNum.setText(musicNum);

        ArrayList<HashMap<String, String>> items = new ArrayList<>();
        for (int i = 0; i < musicListPresenter.getMusicList().size(); i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("itemId", "" + (i + 1));
            map.put("musicName", musicListPresenter.getMusicList().get(i).getMusicName());
            map.put("musicInfo", musicListPresenter.getMusicList().get(i).getArtist() +
                    " - " + musicListPresenter.getMusicList().get(i).getAlbumName());
            items.add(map);
        }
        ListViewAdapter listViewAdapter = new ListViewAdapter(items, MusicListActivity.this);
        mListView.addHeaderView(mHeader);
        mListView.setAdapter(listViewAdapter);
    }
}
