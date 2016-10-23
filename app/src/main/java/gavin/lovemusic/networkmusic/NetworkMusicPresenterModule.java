package gavin.lovemusic.networkmusic;

import dagger.Module;
import dagger.Provides;

/**
 * Created by GavinLi
 * on 16-10-23.
 */
@Module
public class NetworkMusicPresenterModule {
    private NetworkMusicContract.View mView;
    private NetworkMusicContract.Model mModel;

    public NetworkMusicPresenterModule(NetworkMusicContract.View view,
                                       NetworkMusicContract.Model model) {
        mView = view;
        mModel = model;
    }

    @Provides NetworkMusicContract.View provideView() {
        return mView;
    }

    @Provides NetworkMusicContract.Model provideModel() {
        return mModel;
    }
}
