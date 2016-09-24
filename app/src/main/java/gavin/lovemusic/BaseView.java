package gavin.lovemusic;

/**
 * Created by GavinLi
 * on 16-9-22.
 */
public interface BaseView<P extends BasePresenter> {
    void setPresenter(P presenter);
}
