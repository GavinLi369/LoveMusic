package gavin.lovemusic.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gavin.lovemusic.entity.Music;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-3-21.
 */

public class QqMusicUtil {
    private static final int LYRIC_FIND_NUM = 5;

    /*
        搜索歌曲API：http://s.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=${num}&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8&notice=0&platform=jqminiframe.json&needNewCode=0&p=1&catZhida=0&remoteplace=sizer.newclient.next_song&w=${name}
        {num}=返回歌曲数量
        {name}=需要搜索的歌曲

        歌曲地址API：http://ws.stream.qqmusic.qq.com/${id}.m4a?fromtag=46
        {id}=歌曲ID

        歌曲图片API：http://imgcache.qq.com/music/photo/album_${width}/${image_id%100}/${width}_albumpic_${image_id}_0.jpg
        {width}=图片大小
        {image_id}=图片ID

        歌词API：http://music.qq.com/miniportal/static/lyric/${id%100}/${id}.xml
        {id}=歌词ID
    */
    public List<Music> findMusic(String name) throws IOException {
        String url = "http://s.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=5"
                + "&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8&notice=0&platform=jqminiframe.json&needNewCode=0&p=1&catZhida=0&remoteplace=sizer.newclient.next_song&w=" + name;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        if(!response.isSuccessful()) {
            response.body().close();
            throw new IOException("网络连接失败");
        }
        try {
            List<Music> musics = new ArrayList<>();
            JSONArray songs = new JSONObject(response.body().string())
                    .getJSONObject("data")
                    .getJSONObject("song")
                    .getJSONArray("list");
            for(int i = 0; i < songs.length(); i++) {
                JSONObject song = songs.getJSONObject(i);
                String[] infoes = song.getString("f").split("\\|");
                Music music = new Music();
                music.setId(Long.parseLong(infoes[0]));
                music.setTitle(infoes[1].replaceAll("&#39;", "'"));
                music.setArtist(infoes[3]);
                music.setAlbum(infoes[5]);
                int width = 300;
                music.setImage("http://imgcache.qq.com/music/photo/album_" + width +"/" + Long.parseLong(infoes[4]) % 100
                        + "/" + width + "_albumpic_" + infoes[4] + "_0.jpg");
                //网页数据中没有duration，延迟至播放时设置
                music.setDuration(0);
                music.setPath("http://ws.stream.qqmusic.qq.com/" + infoes[0] + ".m4a?fromtag=46");
                musics.add(music);
            }
            return musics;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IOException("网页返回数据异常");
        }
    }

    public String getLyric(String name, String artist) throws IOException {
        String url = "http://s.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=" + LYRIC_FIND_NUM
                + "&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8&notice=0&platform=jqminiframe.json&needNewCode=0&p=1&catZhida=0&remoteplace=sizer.newclient.next_song&w=" + name;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        if (!response.isSuccessful()) {
            response.body().close();
            throw new IOException("网络连接失败");
        }
        try {
            JSONArray songs = new JSONObject(response.body().string())
                    .getJSONObject("data")
                    .getJSONObject("song")
                    .getJSONArray("list");
            for (int i = 0; i < songs.length(); i++) {
                JSONObject song = songs.getJSONObject(i);
                String[] infoes = song.getString("f").split("\\|");
                if(artist.equals(infoes[3])) {
                    String lyricUrl = "http://music.qq.com/miniportal/static/lyric/" +
                            Long.parseLong(infoes[0]) % 100 + "/"
                            + Long.parseLong(infoes[0]) + ".xml";
                    Request lyricRequest = new Request.Builder()
                            .url(lyricUrl)
                            .build();
                    Response lyricResponse = new OkHttpClient().newCall(lyricRequest).execute();
                    if(!lyricResponse.isSuccessful()) {
                        lyricResponse.body().close();
                        throw new IOException("网络连接失败");
                    }
                    Pattern pattern = Pattern.compile("\\[CDATA\\[([\\s\\S]+?)]]>");
                    Matcher matcher = pattern.matcher(lyricResponse.body().string());
                    if(matcher.find()) return matcher.group(1).replace("&apos;", "'");
                    lyricResponse.body().close();
                }
            }
            return "";
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IOException("网页返回数据异常");
        } finally {
            response.body().close();
        }
    }
}
