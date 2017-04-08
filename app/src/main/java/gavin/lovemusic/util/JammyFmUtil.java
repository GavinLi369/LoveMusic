package gavin.lovemusic.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gavin.lovemusic.musicnews.NewsEntry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 4/5/17.
 */

public class JammyFmUtil {
    private static final String JAMMY_URL = "http://www.jammyfm.com/";

    private static final String NEWS_PATTERN =
            "<article id=\"\\d+?[\\s\\S]+?href=\"(.+?)\" title=\"(.+?)\"[\\s\\S]+?<img src=\"(.+?)\"[\\s\\S]+?<div class=\"index-intro[\\s\\S]+?title=\"(.+?)\"[\\s\\S]+?</article>";

    public List<NewsEntry> getNews() throws IOException {
        List<NewsEntry> newsEntries = new ArrayList<>();
        Request request = new Request.Builder()
                .url(JAMMY_URL)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        Pattern pattern = Pattern.compile(NEWS_PATTERN);
        Matcher matcher = pattern.matcher(response.body().string());
        while(matcher.find()) {
            NewsEntry newsEntry = new NewsEntry();
            newsEntry.setLinkUrl(matcher.group(1));
            newsEntry.setTitle(matcher.group(2));
            newsEntry.setImageUrl(matcher.group(3));
            newsEntry.setSubTitle(matcher.group(4));
            newsEntries.add(newsEntry);
        }
        return newsEntries;
    }
}
