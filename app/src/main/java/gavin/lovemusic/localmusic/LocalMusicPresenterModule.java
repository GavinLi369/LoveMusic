package gavin.lovemusic.localmusic;

import dagger.Module;
import dagger.Provides;

/**
 * Created by GavinLi
 * on 16-10-23.
 */
@Module
public class LocalMusicPresenterModule {
    private final LocalMusicContract.View mView;
    private final LocalMusicContract.Model mModel;

    public LocalMusicPresenterModule(LocalMusicContract.View view, LocalMusicContract.Model model) {
        this.mView = view;
        this.mModel = model;
    }

    @Provides
    LocalMusicContract.View provideView() {
        return mView;
    }

    @Provides
    LocalMusicContract.Model provideModel() {
        return mModel;
    }
}
