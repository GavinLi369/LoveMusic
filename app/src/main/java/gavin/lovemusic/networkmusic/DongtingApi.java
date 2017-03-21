package gavin.lovemusic.networkmusic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gavin.lovemusic.service.Music;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 16-9-25.
 */
public class DongtingApi {
    private Matcher mMatcher;

    /*
        搜索歌曲API：http://search.dongting.com/song/search?q={0}&page={1}&size={2}
        {0}=需要搜索的歌曲或歌手
        {1}=查询的页码数
        {2}=当前页的返回数量
    */
    private ArrayList<Music> findMusicByName(String name, int size) throws IOException{
        String url = "http://search.dongting.com/song/search?q=" + name + "&page=1&size=" + size;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            ArrayList<Music> musicList = new ArrayList<>();
            Response response = okHttpClient.newCall(request).execute();
            JSONArray musics = new JSONObject(response.body().string()).getJSONArray("data");
            for(int i = 0; i < musics.length(); i++) {
                JSONObject musicJson = musics.getJSONObject(i);
                Music music = new Music();
                music.setId(musicJson.getLong("songId"));
                music.setTitle(musicJson.getString("name"));
                music.setArtist(musicJson.getJSONArray("singers").getJSONObject(0).getString("singerName"));
                music.setAlbum(musicJson.getString("albumName"));
                music.setImage(musicJson.getString("picUrl"));
                if(musicJson.getJSONArray("auditionList").length() != 0) {
                    music.setPath(musicJson.getJSONArray("auditionList").getJSONObject(0).getString("url"));
                    music.setDuration(musicJson.getJSONArray("auditionList").getJSONObject(0).getInt("duration"));
                    musicList.add(music);
                }
            }
            return musicList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
        <h2 class="chart-row__song">Perfect Illusion</h2>
        <a class="chart-row__artist" href="http://www.billboard.com/artist/306341/lady-gaga" data-tracklabel="Artist Name">Lady Gaga</a>
     */
    public ArrayList<Music> getBillboardHot(int size, int offset) throws IOException{
        if(offset == 0) getBillboardHotMatcher();
        ArrayList<Music> hotMusic = new ArrayList<>();
        for(int i = 0; i < size;) {
            if (mMatcher.find()) {
                String title = mMatcher.group(1);
                String artist = mMatcher.group(2);
                ArrayList<Music> musics = findMusicByName(title, 5);
                if(musics != null) {
                    for (Music music : musics) {
                        if (artist.contains(music.getArtist())) {
                            hotMusic.add(music);
                            i++;
                            break;
                        }
                    }
                }
            }
        }
        return hotMusic;
    }

    private void getBillboardHotMatcher() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://www.billboard.com/charts/hot-100")
                .build();
        Response response = okHttpClient.newCall(request).execute();
        String html = response.body().string();
        String regex = "row__song\">([\\s\\S]+?)<[\\s\\S]+?Artist Name\">([\\s\\S]+?)<";
        Pattern pattern = Pattern.compile(regex);
        mMatcher = pattern.matcher(html);
    }
}
