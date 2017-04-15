package gavin.lovemusic.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 4/15/17.
 */

public class NeteaseUtil {
    private static final String SEARCH_URL = "http://music.163.com/api/search/get/";
    private static final String LYRIC_URL = "http://music.163.com/api/song/media?id=";

    public String getLyric(String name, String artist) throws IOException {
        long id = searchMusic(name, artist);
        return getLyric(id);
    }

    public String getLyric(long id) throws IOException {
        Request request = new Request.Builder()
                .url(LYRIC_URL + id)
                .build();
        Response response = null;
        try {
            response = new OkHttpClient().newCall(request).execute();
            String result = response.body().string();
            try {
                JSONObject jsonObject = new JSONObject(result);
                return jsonObject.getString("lyric");
            } catch (JSONException e) {
                e.printStackTrace();
                throw new IOException("网络数据出错");
            }
        } finally {
            if(response != null) response.body().close();
        }
    }

    private long searchMusic(String name, String artist) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("s", name + " " + artist)
                .add("limit", "20")
                .add("type", "1")
                .add("offset", "0")
                .build();
        Request request = new Request.Builder()
                .url(SEARCH_URL)
                .post(formBody)
                .build();
        Response response = null;
        try {
            response = new OkHttpClient().newCall(request).execute();
            String result = response.body().string();
            try {
                JSONArray jsonArray = new JSONObject(result).getJSONObject("result").getJSONArray("songs");
                if (jsonArray.length() == 0)
                    throw new IOException("网络数据出错");
                return jsonArray.getJSONObject(0).getLong("id");
            } catch (JSONException e) {
                e.printStackTrace();
                throw new IOException("网络数据出错");
            }
        } finally {
            if(response != null) response.body().close();
        }
    }
}
