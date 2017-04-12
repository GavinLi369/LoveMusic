package gavin.lovemusic.networkmusic;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import gavin.lovemusic.App;
import gavin.lovemusic.entity.DaoSession;
import gavin.lovemusic.entity.MusicDao;
import gavin.lovemusic.entity.Music;
import gavin.lovemusic.util.MusicPleerUtil;

/**
 * Created by GavinLi
 * on 16-10-21.
 */
public class NetworkMusicModel implements NetworkMusicContract.Model {
    private static final String BILLBOARD_HOT_URL = "http://www.billboard.com/rss/charts/hot-100";

    private Context mContext;

    private MusicPleerUtil mMusicFinder = new MusicPleerUtil();
    private List<Music> mPreMusics = new ArrayList<>();

    public NetworkMusicModel(Context context) {
        this.mContext = context;
    }

    /*
        step 1 : 加载热门歌曲列表
        step 2 : 查找缓存
        缓存未命中 :
        step 3 : 获取网络数据
        step 4 : 缓存歌曲图片到本地，并将歌曲信息加入数据库
     */
    @Override
    public List<Music> getHotMusic(int size, int offset) throws IOException {
        ExecutorService mThreadPool = Executors.newCachedThreadPool();

        //加载热门歌曲列表
        if(mPreMusics.isEmpty()) initPreMusics(BILLBOARD_HOT_URL);

        //查找缓存
        DaoSession daoSession = ((App) mContext.getApplicationContext()).getCacheSession();
        MusicDao musicDao = daoSession.getMusicDao();
        Music[] hotMusics = new Music[10];
        for(int i = offset; i < offset + size; i++) {
            Music preMusic = mPreMusics.get(i);
            List<Music> cacheMusics = musicDao.queryBuilder()
                    .where(MusicDao.Properties.Id.eq(preMusic.getId()))
                    .build()
                    .list();
            //缓存歌曲图片
            if(cacheMusics.isEmpty() || !new File(cacheMusics.get(0).getImage()).exists()) {
                //使用线程池，优化网络数据加载性能
                final int postion = i % 10;
                mThreadPool.execute(() -> {
                    try {
                        List<Music> musics = mMusicFinder
                                .findMusic(preMusic.getTitle(), preMusic.getArtist());
                        if (musics.size() > 0) {
                            Music music = musics.get(0);
                            hotMusics[postion] = musics.get(0);
                            String imagePath = saveMusicImage(mContext, music);
                            music.setImage(imagePath);
                            musicDao.insert(music);
                        }
                    } catch (IOException | InterruptedException
                            | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            } else if(cacheMusics.get(0).getTitle().equals(preMusic.getTitle())) {
                hotMusics[i] = cacheMusics.get(0);
            } else {
                removeMusicImage(cacheMusics.get(0).getId());
            }
        }

        //等待网络数据加载完成
        mThreadPool.shutdown();
        while(true) {
            if(mThreadPool.isTerminated()) return Arrays.asList(hotMusics);
            else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String saveMusicImage(Context context, Music music) throws IOException, InterruptedException, ExecutionException {
        Bitmap bitmap = Glide.with(context).load(music.getImage()).asBitmap().into(-1, -1).get();
        File file = new File(App.APP_DIR + File.separator + "Cache" + File.separator + Long.toString(music.getId()));
        if(!file.exists()) {
            if(!file.createNewFile())
                throw new IOException("file can not create");
        }
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        out.close();
        return file.getPath();
    }

    private void removeMusicImage(long id) throws IOException {
        File file = new File(App.APP_DIR + File.separator + "Cache" + File.separator + id);
        if(file.exists()) {
            if(!file.delete()) throw new IOException("file can not delete");
        }
    }

    //TODO 时间花费过长 10s左右
    private void initPreMusics(String url) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Logger.d("start: " + System.currentTimeMillis());
            Document document = builder.parse(url);
            Logger.d("end: " + System.currentTimeMillis());
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
}
