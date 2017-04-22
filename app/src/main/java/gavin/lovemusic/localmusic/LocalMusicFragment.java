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
import android.widget.Button;

import java.util.List;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;

public class LocalMusicFragment extends Fragment implements LocalMusicContract.View,
        LocalRecyclerAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{
    private RecyclerView mMusicRecycler;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FileScannerDialog mDialog;

    private LocalMusicContract.Presenter mPresenter;
    private LocalRecyclerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_local, container, false);
        mMusicRecycler = (RecyclerView) rootView.findViewById(R.id.musicList);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mMusicRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mPresenter.loadMusicList();
        return rootView;
    }

    //初始化ListView视图
    @Override
    public void setMusicListView(List<Music> musicList) {
        mAdapter = new LocalRecyclerAdapter(musicList);
        mAdapter.setOnItemClickListener(this);
        mMusicRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        mPresenter.refreshMusicList(getContext());
    }

    @Override
    public void hideRefreshing() {
        if(mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showScanningFile() {
        mDialog = new FileScannerDialog(getContext());
        mDialog.setCancelable(false);
        mDialog.show();
        Button cancalButton = (Button) mDialog.findViewById(R.id.btn_cancal);
        if(cancalButton != null)
            cancalButton.setOnClickListener(view -> mPresenter.cancalScanning());
    }

    @Override
    public void updateScanningFile(String path) {
        mDialog.updateFilePath(path);
    }

    @Override
    public void removeScanningFile() {
        mDialog.cancel();
    }

    @Override
    public void onItemClick(int position) {
        mPresenter.startNewMusic(mAdapter.getMusicList(), position);
    }

    @Override
    public void setPresenter(LocalMusicContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
