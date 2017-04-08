package gavin.lovemusic.musicnews;

/**
 * Created by GavinLi
 * on 4/5/17.
 */

public class NewsEntry {
    private String mImageUrl;
    private String mTitle;
    private String mSubTitle;
    private String mLinkUrl;

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }

    public String getLinkUrl() {
        return mLinkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        mLinkUrl = linkUrl;
    }
}
