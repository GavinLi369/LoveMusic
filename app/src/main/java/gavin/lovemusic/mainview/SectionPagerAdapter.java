package gavin.lovemusic.mainview;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.NoSuchElementException;

import gavin.lovemusic.localmusic.LocalMusicFragment;
import gavin.lovemusic.localmusic.LocalMusicModel;
import gavin.lovemusic.localmusic.LocalMusicPresenter;
import gavin.lovemusic.musicnews.MusicNewsFragment;
import gavin.lovemusic.musicnews.MusicNewsModel;
import gavin.lovemusic.musicnews.MusicNewsPresenter;
import gavin.lovemusic.networkmusic.NetworkMusicFragment;
import gavin.lovemusic.networkmusic.NetworkMusicModel;
import gavin.lovemusic.networkmusic.NetworkMusicPresenter;
import gavin.lovemusic.service.PlayService;

/**
 * Created by GavinLi
 * on 16-9-23.
 */
public class SectionPagerAdapter extends FragmentPagerAdapter {
    private NetworkMusicFragment mNetworkMusicFragment;
    private LocalMusicFragment mLocalMusicFragment;
    private MusicNewsFragment mMusicNewsFragment;

    private Context mContext;
    private PlayService mPlayService;

    public SectionPagerAdapter(FragmentManager fm,
                               Context context, PlayService playService) {
        super(fm);
        mContext = context;
        mPlayService = playService;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                if(mLocalMusicFragment == null) {
                    mLocalMusicFragment = new LocalMusicFragment();
                    new LocalMusicPresenter(mLocalMusicFragment,
                            new LocalMusicModel(mContext),
                            mPlayService);
                }
                return mLocalMusicFragment;
            }
            case 1: {
                if(mMusicNewsFragment == null) {
                    mMusicNewsFragment = new MusicNewsFragment();
                    new MusicNewsPresenter(mMusicNewsFragment,
                            new MusicNewsModel(mContext));
                }
                return mMusicNewsFragment;
            }
            case 2: {
                if(mNetworkMusicFragment == null) {
                    mNetworkMusicFragment = new NetworkMusicFragment();
                    new NetworkMusicPresenter(mNetworkMusicFragment,
                            new NetworkMusicModel(mContext),
                            mPlayService);
                }
                return mNetworkMusicFragment;
            }
            default: throw new NoSuchElementException("没有该标签页");
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return "本地";
            case 1: return "资讯";
            case 2: return "热门";
        }
        return null;
    }
}