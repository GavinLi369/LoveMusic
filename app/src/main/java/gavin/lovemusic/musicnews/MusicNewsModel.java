package gavin.lovemusic.musicnews;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.util.JammyFmUtil;

/**
 * Created by GavinLi
 * on 4/5/17.
 */

public class MusicNewsModel implements MusicNewsContract.Model {
    private Context mContext;

    public MusicNewsModel(Context context) {
        mContext = context;
    }

    @Override
    public List<NewsEntry> getNews() throws IOException {
        return new JammyFmUtil().getNews();
    }
}
