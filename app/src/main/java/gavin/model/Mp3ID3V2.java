package gavin.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gavin on 2015/12/10.
 * Mp3文件的ID3V2处理模型
 */
public class Mp3ID3V2 {
    private InputStream in;
    private Map<String, byte[]> tags = new HashMap<>();

    public Mp3ID3V2(InputStream in) {
        this.in = in;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() throws Exception {
        byte[] header = new byte[10];
        if (in.read(header) == 0) {
            return;
        }

        if (header[0] != 'I' || header[1] != 'D' || header[2] != '3') {
            throw new Exception("not invalid mp3 ID3V2 tag");
        }
        int tagSize = ((header[6] & 0xff) << 21) + ((header[7] & 0xff) << 14) +
                ((header[8] & 0xff) << 7) + (header[9] & 0xff);
        int pos = 10;
        while (pos < tagSize) {
            byte[] tag = new byte[10];
            if (in.read(tag) == 0) {
                break;
            }
            String tagName = new String(tag, 0, 4);
            int length = ((tag[4] & 0xff) << 24) + ((tag[5] & 0xff) << 16) +
                    ((tag[6] & 0xff) << 8) + (tag[7] & 0xff);
            byte[] data = new byte[length];
            if (in.read(data) != 0) {
                tags.put(tagName, data);
            }
            pos = pos + length + 10;
        }
        in.close();
    }

    /**
     * 通过分析album数组的二进制文件发现，在图片数据之前还有一段数据（应该是表示图片格式的），
     * 长度为13字节,所以在解析Bitmap时从第13个字节开始
     */
    public Bitmap getAlbumByteArray() {
        byte[] album = tags.get("APIC");
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(album, 13, album.length - 13);
        } catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        return bitmap;
    }

    public String getMusicName() {
        byte[] title = tags.get("TIT2");
        try {
            return new String(title, 1, title.length - 1, getEncoding(title[0]));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getArtist() {
        byte[] artist = tags.get("TPE1");
        try {
            return new String(artist, 1, artist.length - 1, getEncoding(artist[0]));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAlbumName() {
        byte[] albumName = tags.get("TALB");
        try {
            return new String(albumName, 1, albumName.length - 1, getEncoding(albumName[0]));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getEncoding(byte data) {
        String encoding;
        switch (data) {
            case 0:
                encoding = "ISO-8859-1";
                break;
            case 1:
                encoding = "UTF-16";
                break;
            case 2:
                encoding = "UTF-16BE";
                break;
            case 3:
                encoding = "UTF-8";
                break;
            default:
                encoding = "ISO-8859-1";
        }
        return encoding;
    }

    public void close(){
        in = null;
        tags = null;
    }
}
