package gavin.lovemusic.mainview;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import gavin.lovemusic.localmusic.LocalMusicFragment;
import gavin.lovemusic.networkmusic.NetworkMusicFragment;

/**
 * Created by GavinLi
 * on 16-9-23.
 */
public class SectionPagerAdapter extends FragmentPagerAdapter {
    private NetworkMusicFragment networkMusicFragment;
    private LocalMusicFragment localMusicFragment;

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
        networkMusicFragment = new NetworkMusicFragment();
        localMusicFragment = new LocalMusicFragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return networkMusicFragment;
            case 1: return localMusicFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return "热门";
            case 1: return "本地";
        }
        return null;
    }
}