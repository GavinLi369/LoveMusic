package gavin.lovemusic.musicnews;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import gavin.lovemusic.constant.R;

/**
 * Created by GavinLi
 * on 4/5/17.
 */

public class MusicNewsFragment extends Fragment implements MusicNewsContract.View {
    private NewsRecyclerAdapter mAdapter = new NewsRecyclerAdapter();
    private SwipeRefreshLayout mRefreshLayout;
    private boolean isLoaded = false;
    private boolean isShown = false;

    private MusicNewsContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music_news, container, false);
        RecyclerView newsRecyclerView = (RecyclerView) root.findViewById(R.id.rv_news);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecyclerView.setAdapter(mAdapter);

        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.layout_refresh);
        mRefreshLayout.setOnRefreshListener(() -> mPresenter.loadNews());
        mRefreshLayout.setDistanceToTriggerSync(200);

        if(isShown && !isLoaded) {
            mRefreshLayout.setRefreshing(true);
            isLoaded = true;
            mPresenter.loadNews();
        }
        return root;
    }

    //懒加载
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if(getView() != null && !isLoaded) {
                isLoaded = true;
                mRefreshLayout.setRefreshing(true);
                mPresenter.loadNews();
            }
            isShown = true;
        } else {
            isShown = false;
        }
    }

    @Override
    public void showNews(List<NewsEntry> newsEntries) {
        if(mRefreshLayout.isRefreshing()) mRefreshLayout.setRefreshing(false);
        mAdapter.setNewsEntries(newsEntries);
        mAdapter.notifyDataSetChanged();
        isLoaded = true;
    }

    @Override
    public void showNetworkError() {
        if(mRefreshLayout.isRefreshing()) mRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), "网络连接出错", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(MusicNewsContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
