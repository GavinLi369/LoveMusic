package gavin.lovemusic.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.entity.Music;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 4/10/17.
 */

public class MusicPleerUtil {
    //歌曲搜索返回的歌曲数目
    private static final int SEARCH_NUM = 1;

    private static final String MUSIC_LINK_URL = "http://databrainz.com/api/data_api_new.cgi?format=json&id=";
    private static final String MUSIC_FIND_URL = "http://databrainz.com/api/search_api.cgi?format=json&mh=" + SEARCH_NUM + "&where=mpl&qry=";

    /*
        搜索歌曲API:http://databrainz.com/api/search_api.cgi?format=json&mh={num}&where=mpl&qry={name}
        {num}：数目
        {name}：歌曲名(其中空格用+代替) e.g. Shape+of+you

        歌曲地址API:http://databrainz.com/api/data_api_new.cgi?format=json&id={id}
        {id}：歌曲id(即搜索结果中的url)
     */
    public List<Music> findMusic(String name) throws IOException {
        List<Music> musics = new ArrayList<>();
        String urlStr = MUSIC_FIND_URL + name.replaceAll("[\\s]+", "+")
                .replaceAll("'", "").replaceAll("&", "%26");
        Request request = new Request.Builder()
                .url(urlStr)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        String resultJson = response.body().string();
        response.body().close();

        if(resultJson.isEmpty()) return musics;

        try {
            JSONArray results = new JSONObject(resultJson).getJSONArray("results");
            for(int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                Music music = new Music();
                music.setTitle(result.getString("title"));
                music.setArtist(result.getString("artist"));
                music.setAlbum(result.getString("album"));
                music.setImage(result.getString("albumart"));
                music.setPath(getMusicLink(result.getString("url")));
                //根据歌曲名获取歌曲ID
                music.setId(music.getTitle().hashCode() & 0xFF);
                musics.add(music);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IOException("网络数据出现异常");
        }
        return musics;
    }

    private String getMusicLink(String url) throws IOException {
        Request request = new Request.Builder()
                .url(MUSIC_LINK_URL + url)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        try {
            String result = response.body().string();
            return new JSONObject(result)
                    .getJSONObject("song").getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IOException("网络数据出现异常");
        } finally {
            response.body().close();
        }
    }

    public List<Music> findMusic(String name, String artist) throws IOException {
        //歌手放在歌名后有可能搜索不到
        return findMusic(artist + " " + name);
    }
}
