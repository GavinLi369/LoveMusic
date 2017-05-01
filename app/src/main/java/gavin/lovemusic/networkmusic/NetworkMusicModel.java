package gavin.lovemusic.networkmusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import gavin.lovemusic.database.MusicCacheDb;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.util.MusicPleerUtil;

/**
 * Created by GavinLi
 * on 16-10-21.
 */
public class NetworkMusicModel implements NetworkMusicContract.Model {
    private static final String BILLBOARD_HOT_URL = "http://www.billboard.com/rss/charts/hot-100";

    private final MusicCacheDb mCacheDb;
    private static final int CACHE_SIZE = 200;

    private MusicPleerUtil mMusicFinder = new MusicPleerUtil();
    private List<Music> mPreMusics = new ArrayList<>();

    public NetworkMusicModel(Context context) {
        mCacheDb = new MusicCacheDb(context);
    }

    /*
        step 1 : 加载热门歌曲列表
        step 2 : 查找缓存
        缓存未命中 :
        step 3 : 从网络获取歌曲信息
        step 4 : 将歌曲信息加入数据库
     */
    @Override
    public List<Music> getHotMusic(int size, int offset) throws IOException {
        final CountDownLatch latch = new CountDownLatch(size);
        ExecutorService threadPool = Executors.newFixedThreadPool(size);

        //加载热门歌曲列表
        if(mPreMusics.isEmpty()) initPreMusics();

        //查找缓存
        SQLiteDatabase database = mCacheDb.getWritableDatabase();
        Music[] hotMusics = new Music[size];
        for(int i = offset; i < offset + size; i++) {
            if(mPreMusics.size() <= i) {
                latch.countDown();
                continue;
            }
            Music preMusic = mPreMusics.get(i);
            String[] columns = {MusicCacheDb.Entry.ID, MusicCacheDb.Entry.ALBUM,
                    MusicCacheDb.Entry.IMAGE, MusicCacheDb.Entry.PATH};
            String selection = MusicCacheDb.Entry.ID + " = ?";
            String[] selecionArg = {caculateMd5(preMusic.getTitle() + preMusic.getArtist())};
            Cursor cursor = database.query(MusicCacheDb.Entry.TABLE_NAME,
                    columns, selection, selecionArg,
                    null, null, null);
            if(cursor.moveToFirst()) {
                //缓存命中
                preMusic.setAlbum(cursor.getString(
                        cursor.getColumnIndexOrThrow(MusicCacheDb.Entry.ALBUM)));
                preMusic.setImage(cursor.getString(
                        cursor.getColumnIndexOrThrow(MusicCacheDb.Entry.IMAGE)));
                preMusic.setPath(cursor.getString(
                        cursor.getColumnIndexOrThrow(MusicCacheDb.Entry.PATH)));
                hotMusics[i % size] = preMusic;
                //更新该条数据的Alive
                updateDatabase(database, cursor, preMusic);
                latch.countDown();
            } else {
                //使用线程池，优化网络数据加载性能
                final int postion = i % size;
                threadPool.execute(() -> {
                    try {
                        List<Music> musics = mMusicFinder
                                .findMusic(preMusic.getTitle(), preMusic.getArtist());
                        if (musics.size() > 0) {
                            hotMusics[postion] = musics.get(0);
                            //缓存歌曲信息
                            ContentValues values = new ContentValues();
                            values.put(MusicCacheDb.Entry.ID,
                                    caculateMd5(preMusic.getTitle()
                                            + preMusic.getArtist()));
                            //为保证统一这里使用PreMusic的歌曲标题和歌手
                            values.put(MusicCacheDb.Entry.TITLE, preMusic.getTitle());
                            values.put(MusicCacheDb.Entry.ARTIST, preMusic.getArtist());
                            values.put(MusicCacheDb.Entry.ALBUM, musics.get(0).getAlbum());
                            values.put(MusicCacheDb.Entry.IMAGE, musics.get(0).getImage());
                            values.put(MusicCacheDb.Entry.PATH, musics.get(0).getPath());
                            values.put(MusicCacheDb.Entry.ALIVE, System.currentTimeMillis());
                            synchronized (mCacheDb) {
                                checkAndClearDatabase(database);
                                database.insert(MusicCacheDb.Entry.TABLE_NAME,
                                            null, values);
                            }
                        } else {
                            //未找到网络资源
                            hotMusics[postion] = preMusic;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            cursor.close();
        }

        //等待网络数据加载完成
        List<Music> musics = new ArrayList<>();
        try {
            latch.await();
            for (Music music : hotMusics) {
                if (music != null) musics.add(music);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        database.close();
        threadPool.shutdown();
        return musics;
    }

    private void initPreMusics() throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(BILLBOARD_HOT_URL);
            Element root = document.getDocumentElement();
            NodeList items = root.getElementsByTagName("item");
            for(int i = 0; i < items.getLength(); i++) {
                Element element = (Element) items.item(i);
                //将歌名里的&#039;还原为'
                String title = element.getElementsByTagName("chart_item_title").item(0).getFirstChild()
                        .getNodeValue().replaceAll("&#039;", "'");
                //将歌手里的&amp;还原为&
                String artist = element.getElementsByTagName("artist").item(0).getFirstChild()
                        .getNodeValue().replaceAll("&amp;", "&");
                Music preMusic = new Music();
                //根据歌曲名获取歌曲ID
                preMusic.setId(title.hashCode() & 0xFF);
                preMusic.setTitle(title);
                preMusic.setArtist(artist);
                mPreMusics.add(preMusic);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new IOException("网络数据出错");
        }
    }

    private void updateDatabase(SQLiteDatabase database, Cursor cursor, Music music) {
        ContentValues values = new ContentValues();
        values.put(MusicCacheDb.Entry.TITLE, music.getTitle());
        values.put(MusicCacheDb.Entry.ARTIST, music.getArtist());
        values.put(MusicCacheDb.Entry.ALBUM, music.getAlbum());
        values.put(MusicCacheDb.Entry.IMAGE, music.getImage());
        values.put(MusicCacheDb.Entry.PATH, music.getPath());
        values.put(MusicCacheDb.Entry.ALIVE, System.currentTimeMillis());

        String selection = MusicCacheDb.Entry.ID + " =?";
        String[] selectionArg = {cursor.getString(
                cursor.getColumnIndexOrThrow(MusicCacheDb.Entry.ID))};
        database.update(MusicCacheDb.Entry.TABLE_NAME, values, selection, selectionArg);
    }

    /**
     * 根据使用时间清理database数据
     */
    private void checkAndClearDatabase(SQLiteDatabase database) {
        String[] columns = {MusicCacheDb.Entry.ID, MusicCacheDb.Entry.ALIVE};
        Cursor cursor = database.query(MusicCacheDb.Entry.TABLE_NAME,
                columns, null, null,
                null, null, MusicCacheDb.Entry.ALIVE);
        if(cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount() - CACHE_SIZE; i++) {
                String selection = MusicCacheDb.Entry.ID + " = ?";
                String[] selectionArg = {cursor.getString(
                        cursor.getColumnIndexOrThrow(MusicCacheDb.Entry.ID))};
                database.delete(MusicCacheDb.Entry.TABLE_NAME, selection, selectionArg);
            }
        }
        cursor.close();
    }

    private String caculateMd5(String target) {
        try {
            StringBuilder result = new StringBuilder();
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] md5 = digest.digest(target.getBytes());
            for(byte aMd5 : md5) {
                if((0xFF & aMd5) < 0x10) {
                    result.append('0');
                }
                result.append(Integer.toHexString(0xFF & aMd5));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
