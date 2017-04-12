package gavin.lovemusic.networkmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-18.
 */
public class NetworkMusicFragment extends Fragment implements NetworkMusicContract.View,
        NetworkRecyclerAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GridLayoutManager mLayoutManager;
    private NetworkRecyclerAdapter mAdapter = new NetworkRecyclerAdapter();

    private boolean isLoaded = false;
    private boolean isShown = false;

    private NetworkMusicContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_network, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.musicList);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addOnScrollListener(new ScrollRefreshListener());

        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setDistanceToTriggerSync(200);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        if(!isLoaded && isShown) {
            isLoaded = true;
            mPresenter.loadMusics();
        }
        return rootView;
    }

    //懒加载
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if(getView() != null && !isLoaded) {
                isLoaded = true;
                mPresenter.loadMusics();
            }
            isShown = true;
        } else {
            isShown = false;
        }
    }

    private class ScrollRefreshListener extends RecyclerView.OnScrollListener {
        private int lastVisibleItem;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE &&
                    lastVisibleItem + 1 == mAdapter.getItemCount())
                mPresenter.loadMoreMusic();

            if(mAdapter.getItemCount() != 0) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    mSwipeRefreshLayout.setEnabled(true);
                } else {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
        }
    }

    @Override
    public void showMoreMusics(List<Music> musics) {
        mAdapter.addMusics(musics);
        mAdapter.notifyItemRangeInserted(mAdapter.getItemCount(), musics.size());
    }

    @Override
    public void resetMusics(List<Music> musics) {
        mAdapter.setMusics(musics);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showRefreshView() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideRefreshView() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showNetworkConnetionError() {
        if(getView() != null) {
            Snackbar.make(getView(), "网络连接失败",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(int position) {
        mPresenter.startNewMusic(mAdapter.getMusicList(), position);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mPresenter.refreshMusicList();
    }

    @Override
    public void setPresenter(NetworkMusicContract.Presenter presenter) {
        this.mPresenter = presenter;
    }
}
