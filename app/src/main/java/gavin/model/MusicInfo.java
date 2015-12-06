package gavin.model;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import gavin.database.DBOperation;
import gavin.lovemusic.R;

/**
 * Created by Gavin on 2015/8/23.
 *
 */
public class MusicInfo{
    private String musicName;   //歌曲名称
    private String artist;   //歌手名称
    private Bitmap album;   //歌曲专辑
    private String albumName;    //歌曲专辑名称
    private long duration;        //歌曲时长

    /**
     * 为了在歌曲列表中定位，特意设定歌曲ID，用于在下一首或上一首时定位
     */
    private int id;                  //歌曲Id
    private File musicFile;                 //歌曲文件
    private Context mContext;                //上下文
    private Cursor cursor = null;    //当前歌曲对应的Cursor
    long albumId;
    long musicId;

    /**
     * SongInfo的构造方法
     */
    public MusicInfo(File musicFile, Context context) {
        this.musicFile = musicFile;
        mContext = context;
        init();
    }

    /**
     * 得到歌曲对应的Cursor
     */
    private void getCursorFromPath() {
        cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DURATION},
                MediaStore.Audio.Media.DATA + "=?",
                new String[]{musicFile.getPath()}, null);
    }

    public byte[] getAlbumByteArray(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (album == null){
            Resources resources = mContext.getResources();
            BitmapFactory.decodeResource(resources, R.drawable.img_appwidget_album_cover)
                    .compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } else {
            album.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        }

        return outputStream.toByteArray();
    }

    /**
     * 初始化各种东西，在MusicInfo类创建时调用
     */
    private void init() {
        getCursorFromPath();
        if (cursor.moveToFirst()) {
            musicName = cursor.getString(0);                 // 歌曲名
            artist = cursor.getString(1);               // 歌手名
            albumName = cursor.getString(2);                 // 专辑名
            albumId = cursor.getLong(3);                //专辑ID
            musicId = cursor.getLong(4);
            duration = cursor.getLong(5);
//            initAlbum();
        }
        cursor.close();
        cursor = null;
    }

    public Bitmap getAlbum() {
        DBOperation dbOperation = new DBOperation(mContext);
        dbOperation.openOrCreateDataBase();
//        Cursor cursor = dbOperation.selectMusicInfo(DBOperation.NAME + "=?", new String[]{musicName});
//        if (cursor.moveToFirst()){
//            byte[] temp = cursor.getBlob(cursor.getColumnIndexOrThrow(DBOperation.ALBUM));
//            album = BitmapFactory.decodeByteArray(temp, 0, temp.length);
//            Log.i("gavin.test", "--------------------->getAlbumFromDataBase");
//            cursor.close();
//        } else {
        if (album==null) {
            try {
                album = getAlbumFromTag(new FileInputStream(musicFile));
//                dbOperation.insertMusicInfo(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        }

        return album;
    }

    public Bitmap getAlbumFromTag(InputStream mp3InputStream) throws Exception {
        int buffSize = 1024 * 500;
        try {
            if(buffSize > mp3InputStream.available()){
                buffSize = mp3InputStream.available();
            }
            byte[] buff = new byte[buffSize];
            mp3InputStream.read(buff, 0, buffSize);

            if (indexOf("ID3".getBytes(), buff, 512) == -1)
                throw new Exception("未发现ID3V2");

            //获取头像
            if (indexOf("APIC".getBytes(), buff, 512) != -1) {
                int searLen = indexOf(new byte[] { (byte) 0xFF,
                        (byte) 0xFB }, buff);
                int imgStart = indexOf(new byte[] { (byte) 0xFF,
                        (byte) 0xD8 }, buff);
                int imgEnd = lastIndexOf(new byte[] { (byte) 0xFF,
                        (byte) 0xD9 }, buff, searLen) + 2;
                byte[] img = cutBytes(imgStart, imgEnd, buff);
                if (img != null){
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    return BitmapFactory.decodeByteArray(img, 0, img.length, options);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            mp3InputStream.close();
        }

        Resources resources = mContext.getResources();
        return BitmapFactory.decodeResource(resources, R.drawable.img_appwidget_album_cover);
    }

    /**
     * 截取byte[]
     * */
    public static byte[] cutBytes(int start, int end, byte[] src) {
        if (end <= start || start < 0 || end > src.length) {
            try {
                throw new Exception("参数错误");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        byte[] tmp = new byte[end - start];
        for (int i = 0; i < end - start; i++) {
            tmp[i] = src[start + i];
        }
        return tmp;
    }

    /**
     * 正向索引
     * */
    public static int indexOf(byte[] tag, byte[] src) {
        return indexOf(tag, src, src.length);
    }

    /**
     * 获取第index个的位置
     * */
    public static int indexOf(byte[] tag, byte[] src, int len) {
        if (len > src.length) {
            try {
                throw new Exception("大于总个数");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int tagLen = tag.length;
        byte[] tmp = new byte[tagLen];
        for (int j = 0; j < len - tagLen + 1; j++) {
            for (int i = 0; i < tagLen; i++) {
                tmp[i] = src[j + i];
            }
            // 判断是否相等
            for (int i = 0; i < tagLen; i++) {
                if (tmp[i] != tag[i])
                    break;
                if (i == tagLen - 1) {
                    return j;
                }
            }

        }
        return -1;
    }

    /**
     * 倒序获取第index个的位置
     * */
    public static int lastIndexOf(byte[] tag, byte[] src, int len) {
        if (len > src.length) {
            try {
                throw new Exception("大于总个数");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int tagLen = tag.length;
        byte[] tmp = new byte[tagLen];
        for (int j = len - tagLen; j >= 0; j--) {
            for (int i = 0; i < tagLen; i++) {
                tmp[i] = src[j + i];
            }
            for (int i = 0; i < tagLen; i++) {
                if (tmp[i] != tag[i])
                    break;
                if (i == tagLen - 1) {
                    return j;
                }
            }

        }
        return -1;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getMusicPath() {
        return musicFile.getPath();
    }

    public String getArtist() {
        return artist;
    }

//    public Bitmap getAlbum() {
//        return album;
//    }

    public String getAlbumName() {
        return albumName;
    }

    public long getDuration() {
        return duration;
    }
}
