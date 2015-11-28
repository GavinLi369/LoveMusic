package gavin.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import gavin.database.DBOperation;
import gavin.lovemusic.AppConstant;
import gavin.lovemusic.R;
import gavin.service.PlayService;
import gavin.utils.FileUtils;

public class MainActivity extends Activity {
    private ListView mListView;
    private TextView mSongNum;
    private ImageView mMusicAlbum;
    private View mHeader;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private TextView mSongName;
    private TextView mArtist;
    private LinearLayout mPlayColumn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_main);

        checkAppDir();                               //检查软件目录
        findView();                                     //初始化控件

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayService.SERVICE_COMMAND);
        registerReceiver(broadcastReceiver, intentFilter);

        int musicId = 0;
        int playMode = 0;
        SharedPreferences sharedPreferences =
                getSharedPreferences("service_info", Service.MODE_PRIVATE);
        if (sharedPreferences != null) {
            musicId = sharedPreferences.getInt("musicId", 0);
            playMode = sharedPreferences.getInt("playMode", 0);
        }

        if (serviceRunning()){
            if (PlayService.prepared) {
                setSongListView();                          //初始化歌曲列表
                updateUI();                                   //更新UI视图
                setListener();                               //绑定监听器
            } else {
                initService(musicId);
            }
            PlayService.playMode = playMode;
        } else {
            initService(musicId);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(false);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * 创建歌曲播放服务，等待下一步指令
     */
    private void initService(int musicId){
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("musicCommand", PlayService.INIT_SERVICE);
        intent.putExtra("musicId", musicId);
        this.startService(intent);
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
    private void nextMusic(){
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("musicCommand", PlayService.NEXT_MUSIC);
        this.startService(intent);
    }

    /**
     * 来电监听，当播放音乐时，如果有来电则暂停音乐，当通话结束时继续播放
     */
    class exPhoneCallListener extends PhoneStateListener {
        boolean musicWaitPlay = false;
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (musicWaitPlay) {
                        resumeMusic();
                        musicWaitPlay = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (PlayService.musicState == PlayService.PLAYING) {
                        pauseMusic();
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

    class MyListViewListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                PlayService.currentMusic = PlayService.musicList.get(position);
            } else {
                PlayService.currentMusic = PlayService.musicList.get(position - 1);
            }

            startMusic();
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
                    startMusic();
                    break;
                case PlayService.PLAYING:
                    pauseMusic();
                    break;
                case PlayService.PAUSE:
                    resumeMusic();
            }
        }

    }
    /**
     * 下一曲按钮监听器
     */
    class NextButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            nextMusic();
        }

    }

    /**
     * 初始化控件
     */
    private void findView() {
        mHeader = getLayoutInflater().inflate(R.layout.song_list_header, null);
        mListView = (ListView) findViewById(R.id.musicList);
        mSongNum = (TextView) mHeader.findViewById(R.id.songNum);
        mMusicAlbum = (ImageView)findViewById(R.id.musicAlbum);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);
        mSongName = (TextView) findViewById(R.id.musicName);
        mArtist = (TextView) findViewById(R.id.artist);
        mPlayColumn = (LinearLayout) findViewById(R.id.playColumn);
    }

    /**
     * 添加监听器
     */
    private void setListener(){
        mListView.setOnItemClickListener(new MyListViewListener());    //建立列表点击监听
        mPlayButton.setOnClickListener(new PlayButtonListener());
        mNextButton.setOnClickListener(new NextButtonListener());
        mPlayColumn.setOnClickListener(new PlayColumnListener());

        exPhoneCallListener exPhoneCallListener = new exPhoneCallListener();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(exPhoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    /**
     * 歌曲播放服务的广播接受器
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PlayService.SERVICE_COMMAND)){
                if (intent.getExtras() == null){
                    updateUI();
                    return;
                }
                switch (intent.getStringExtra("command")){
                    case "onCreate":
                        setSongListView();                          //初始化歌曲列表
                        updateUI();                                   //更新UI视图
                        setListener();                               //绑定监听器
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /**
     * UI视图更新
     */
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
        mSongName.setText(PlayService.currentMusic.getMusicName());     //初始化播放栏歌曲信息
        mArtist.setText(PlayService.currentMusic.getArtist());
        mMusicAlbum.setImageBitmap(PlayService.currentMusic.getAlbum());
    }

    /**
     * 初始化歌曲文件列表,并且初始化MusicPlay播放控制器
     */
    private void setSongListView() {
        DBOperation dbOperation = new DBOperation(this);
        dbOperation.openOrCreateDataBase();

        mSongNum.setText("（共" + PlayService.musicList.size() + "首）");

        ArrayList<HashMap<String, String>> items = new ArrayList<>();
        for (int i = 0; i < PlayService.musicList.size(); i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("itemId", "" + (i + 1));
            map.put("songName", PlayService.musicList.get(i).getMusicName());
            map.put("songInfo", PlayService.musicList.get(i).getArtist() +
                    " - " + PlayService.musicList.get(i).getAlbumName());
            items.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, items,
                R.layout.song_listview,
                new String[]{"itemId", "songName", "songInfo"},
                new int[]{R.id.itemId, R.id.musicName, R.id.songInfo});
        mListView.setAdapter(simpleAdapter);
        mListView.addHeaderView(mHeader);
    }

    /**
     * 判断歌曲播放服务是否正在运行
     */
    private boolean serviceRunning(){
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningServices =
                (ArrayList<ActivityManager.RunningServiceInfo>)activityManager.getRunningServices(30);
        for (int i = 0; i < runningServices.size(); i++){
            if (runningServices.get(i).service.getClassName().equals("gavin.service.PlayService")){
                return true;
            }
        }
        return false;
    }

    /**
     * 检查软件目录是否存在,如果不存在则创建该目录
     */
    private void checkAppDir() {
        FileUtils fileUtils = new FileUtils(MainActivity.this);
        if (!fileUtils.isDirExist(AppConstant.APP_DIR)) {
            try {
                fileUtils.createDirInSDCard(AppConstant.APP_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
