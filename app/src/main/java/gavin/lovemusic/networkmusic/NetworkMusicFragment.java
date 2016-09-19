package gavin.lovemusic.networkmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gavin.lovemusic.constant.R;

/**
 * Created by GavinLi
 * on 16-9-18.
 */
public class NetworkMusicFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_network, container, false);
        return rootView;
    }
}
