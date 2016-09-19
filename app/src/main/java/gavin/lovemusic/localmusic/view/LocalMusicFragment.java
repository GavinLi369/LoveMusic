package gavin.lovemusic.localmusic.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnItemClick;
import gavin.lovemusic.constant.R;
import gavin.lovemusic.localmusic.presenter.IMusicListPresenter;
import gavin.lovemusic.localmusic.presenter.MusicListPresenter;
import gavin.lovemusic.service.ActivityCommand;

public class LocalMusicFragment extends Fragment {
    @BindView(R.id.musicList) ListView mListView;

    private IMusicListPresenter musicListPresenter;

    private View mHeader;
    private TextView mSongNum;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_local, container, false);
        mHeader = inflater.inflate(R.layout.song_list_header, null);
        mSongNum = (TextView) mHeader.findViewById(R.id.songNum);
        ButterKnife.bind(this, rootView);

        musicListPresenter = new MusicListPresenter((IMusicListView) getActivity());
        setSongListView();

        return rootView;
    }

    @OnItemClick(R.id.musicList) void onItemClick(int position) {
        if (position == 0) {
            musicListPresenter.changeCurrentMusic(position);
        } else {
            musicListPresenter.changeCurrentMusic(position - 1);
        }

        musicListPresenter.changeMusicStatus(getActivity(), ActivityCommand.PLAY_MUSIC);
    }

    /**
     * 初始化ListView视图
     */
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
        ListViewAdapter listViewAdapter = new ListViewAdapter(items, getActivity());
        mListView.addHeaderView(mHeader);
        mListView.setAdapter(listViewAdapter);
    }
}
