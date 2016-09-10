package gavin.lovemusic.musiclist.presenter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

import java.io.File;
import java.util.ArrayList;

import gavin.lovemusic.App;
import gavin.lovemusic.data.Music;
import gavin.lovemusic.data.database.DBOperation;
import gavin.lovemusic.musiclist.view.IMusicListView;
import gavin.lovemusic.service.ActivityCommand;
import gavin.lovemusic.service.PlayService;
import gavin.lovemusic.utils.FileUtils;

/**
 * Created by GavinLi on 16-9-10.
 * MusicListPresenter
 */
public class MusicListPresenter implements IMusicListPresenter {
    private IMusicListView musicListView;
    private static MusicListPresenter musicListPresenter;
    private App app;

    public static MusicListPresenter getMusicListPresenter() {
        return musicListPresenter;
    }

    public MusicListPresenter(IMusicListView musicListView, Context context) {
        this.musicListView = musicListView;
        musicListPresenter = this;
        app = (App) context.getApplicationContext();
        initMusicList(context);
    }

    private void initMusicList(Context context) {
        app.setMusicList(getMusicByDataBase(context));
        for (int i = 0; i < app.getMusicList().size(); i++) {
            app.getMusicList().get(i).setId(i);
        }
        initService(context);
    }

    private ArrayList<Music> getMusicByDataBase(Context context){
        ArrayList<Music> musicList = new ArrayList<>();
        DBOperation dbOperation = new DBOperation(context);
        dbOperation.openOrCreateDataBase();
        Cursor cursor = dbOperation.selectAll();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                File file = new File(cursor.getString(cursor.getColumnIndexOrThrow(DBOperation.PATH)));
                Music music = new Music(file, context);
                musicList.add(music);
            } while (cursor.moveToNext());
            cursor.close();
        }
        dbOperation.closeDataBase();
        return musicList;
    }

    @Override
    public void startMusic(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.PLAY_MUSIC);
        context.startService(intent);
    }

    @Override
    public void onPlayButtonClick(Context context) {
        switch (PlayService.musicState) {
            case PlayService.STOP:
                startMusic(context);
                break;
            case PlayService.PLAYING:
                pauseMusic(context);
                break;
            case PlayService.PAUSE:
                resumeMusic(context);
        }
    }

    @Override
    public void musicStatusChanged() {
        musicListView.updateUI();
    }

    @Override
    public void onNextButtonClick(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.NEXT_MUSIC);
        context.startService(intent);
    }

    @Override
    public ArrayList<Music> getMusicList() {
        return app.getMusicList();
    }

    @Override
    public void refreshMusicList(Context context) {
        new Thread(() -> {
            app.setMusicList(getMusicFromSDCard(context));
            for (int i = 0; i < app.getMusicList().size(); i++) {
                app.getMusicList().get(i).setId(i);
            }
            initService(context);
        }).start();
    }

    public void initService(Context context) {
        int musicId = 0;
        int playMode = 0;
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("service_info", Service.MODE_PRIVATE);
        if (sharedPreferences != null) {
            musicId = sharedPreferences.getInt("musicId", 0);
            playMode = sharedPreferences.getInt("playMode", 0);
        }

        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.INIT_SERVICE);
        intent.putExtra("musicId", musicId);
        intent.putExtra("playMode", playMode);
        context.startService(intent);
    }

    private ArrayList<Music> getMusicFromSDCard(Context context) {
        return FileUtils.getSongFiles(App.APP_DIR, context);
    }

    @Override
    public void resumeMusic(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.RESUME_MUSIC);
        context.startService(intent);
    }

    @Override
    public void pauseMusic(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        intent.putExtra("musicCommand", ActivityCommand.PAUSE_MUSIC);
        context.startService(intent);
    }

    @Override
    public void serviceCreated() {
        musicListView.setSongListView();
        musicListView.updateUI();
    }
}
