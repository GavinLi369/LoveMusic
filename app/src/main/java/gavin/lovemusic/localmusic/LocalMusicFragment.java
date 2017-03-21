package gavin.lovemusic.localmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.service.Music;

public class LocalMusicFragment extends Fragment implements LocalMusicContract.View,
        RecyclerViewAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{
    private RecyclerView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LocalMusicContract.Presenter mLocalMusicPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_local, container, false);
        mListView = (RecyclerView) rootView.findViewById(R.id.musicList);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSwipeRefreshLayout.setDistanceToTriggerSync(200);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLocalMusicPresenter.unsubscribe();
    }

    //初始化ListView视图
    @Override
    public void setMusicListView(List<Music> musicList) {
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(musicList);
        adapter.setOnItemClickListener(this);
        mListView.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        mLocalMusicPresenter.refreshMusicList(getContext());
    }

    @Override
    public void hideRefreshing() {
        if(mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onItemClick(int position) {
        mLocalMusicPresenter.playNewMusic(position);
    }

    @Override
    public void setPresenter(LocalMusicContract.Presenter presenter) {
        mLocalMusicPresenter = presenter;
        mLocalMusicPresenter.subscribe();
    }
}
