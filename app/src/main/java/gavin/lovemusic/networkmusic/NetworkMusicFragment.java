package gavin.lovemusic.networkmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi
 * on 16-9-18.
 */
public class NetworkMusicFragment extends Fragment implements NetworkMusicContract.View,
        NetworkRecyclerAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{
    @BindView(R.id.musicList) RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeRefreshLayout;

    private GridLayoutManager mLayoutManager;
    private NetworkRecyclerAdapter mAdapter;

    private NetworkMusicContract.Presenter mNetworkMusicPresenter;

    @Nullable
    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_network, container, false);
        ButterKnife.bind(this, rootView);
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new ScrollRefreshListener());
        mAdapter = new NetworkRecyclerAdapter();
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setDistanceToTriggerSync(200);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        new NetworkMusicPresenter(this, getContext());
        mNetworkMusicPresenter.subscribe();
        return rootView;
    }

    private class ScrollRefreshListener extends RecyclerView.OnScrollListener {
        private int lastVisibleItem;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE &&
                    lastVisibleItem + 1 == mAdapter.getItemCount())
                mNetworkMusicPresenter.loadMoreMusic();
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
        }
    }

    @Override
    public void showMoreMusics(ArrayList<Music> musics) {
        mAdapter.addMoreMusics(musics);
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
        Toast.makeText(getContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position) {
        EventBus.getDefault().post(new PlayService.ChangeMusicEvent(mAdapter.getMusicList().get(position)));
    }

    @Override
    public void onRefresh() {
        mNetworkMusicPresenter.refreshMusicList();
    }



    @Override
    public void onDestroyView() {
        mNetworkMusicPresenter.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public void setPresenter(NetworkMusicContract.Presenter presenter) {
        this.mNetworkMusicPresenter = presenter;
    }
}
