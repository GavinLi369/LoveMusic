package gavin.lovemusic.mainview;

import dagger.Component;
import gavin.lovemusic.localmusic.LocalMusicPresenterModule;
import gavin.lovemusic.networkmusic.NetworkMusicPresenterModule;

/**
 * Created by GavinLi
 * on 16-10-23.
 */
@Component(modules = {LocalMusicPresenterModule.class, NetworkMusicPresenterModule.class})
public interface MainViewComponent {
    void inject(MainActivity mainActivity);
}
