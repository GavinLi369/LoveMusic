package gavin.lovemusic.model;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * ID3V2标签处于MP3文件开头，长度不固定<br>
 * 由于长度不固定，所以该标签包含了歌曲的大部分信息<br>
 * 如果想获取歌曲更多的信息，请使用该类。
 *
 * @author Gavin
 * @version 1.0
 */
public class Mp3ID3V2 {
    private InputStream in;
    private boolean musicFile = true;
    private Map<String, byte[]> tags = new HashMap<>();

    /**
     * @param in 需要解析的mp3的Stream
     */
    public Mp3ID3V2(InputStream in) {
        this.in = in;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析mp3的Stream数据，找出ID3V2标签<br>
     * 分析ID3V2取出ID3V2中存储的信息，并将其存储在Map中
     *
     * @throws Exception
     */
    private void init() throws Exception {
        byte[] header = new byte[10];
        if (in.read(header) == 0) {
            return;
        }

        if (header[0] != 'I' || header[1] != 'D' || header[2] != '3') {
            musicFile = false;
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

    public boolean isMusicFile(){
        return musicFile;
    }

    /**
     * 返回歌曲的专辑图片信息<br>
     * 通过分析album数组的二进制文件发现，在图片数据之前还有一段数据（应该是表示图片格式的），
     * 长度为13字节,所以返回从第13个字节开始的数组
     *
     * @return 歌曲专辑图片
     */
    public byte[] getAlbumByteArray() {
        byte[] apic = tags.get("APIC");
        byte[] album = new byte[apic.length - 13];
        System.arraycopy(apic, 13, album, 0, apic.length - 13);
        return album;
    }

    /**
     * 返回mp3的歌曲名称，如果ID3V2中不包含此信息则返回unknown
     *
     * @return 歌曲名称
     */
    public String getTitle() {
        String artist = "unknown";
        byte[] artistArray = tags.get("TIT2");
        if (artistArray != null) {
            try {
                artist = new String(artistArray, 1, artistArray.length - 1, getEncoding(artistArray[0]));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return artist;
    }

    /**
     * 返回mp3的艺术家，如果ID3V2中不包含此信息则返回unknown
     *
     * @return 艺术家
     */
    public String getArtist() {
        String artist = "unknown";
        byte[] artistArray = tags.get("TPE2");
        if (artistArray != null) {
            try {
                artist = new String(artistArray, 1, artistArray.length - 1, getEncoding(artistArray[0]));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return artist;
    }

    /**
     * 返回mp3的乐队，如果ID3V2中不包含此信息则返回unknown
     *
     * @return 乐队
     */
    public String getBand() {
        String artist = "unknown";
        byte[] artistArray = tags.get("TPE1");
        if (artistArray != null) {
            try {
                artist = new String(artistArray, 1, artistArray.length - 1, getEncoding(artistArray[0]));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return artist;
    }

    /**
     * 返回mp3的艺术家，如果ID3V2中不包含此信息则返回unknown
     *
     * @return 艺术家
     */
    public String getGenre() {
        String artist = "unknown";
        byte[] artistArray = tags.get("TCON");
        if (artistArray != null) {
            try {
                artist = new String(artistArray, 1, artistArray.length - 1, getEncoding(artistArray[0]));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return artist;
    }

    /**
     * 返回mp3的发行人，如果ID3V2中不包含此信息则返回unknown
     *
     * @return 发行人
     */
    public String getPublisher() {
        String artist = "unknown";
        byte[] artistArray = tags.get("TPUB");
        if (artistArray != null) {
            try {
                artist = new String(artistArray, 1, artistArray.length - 1, getEncoding(artistArray[0]));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return artist;
    }

    /**
     * 返回mp3的歌曲专辑名称，如果ID3V2中不包含此信息则返回unknown
     *
     * @return 歌曲专辑名称
     */
    public String getAlbum() {
        String artist = "unknown";
        byte[] artistArray = tags.get("TALB");
        if (artistArray != null) {
            try {
                artist = new String(artistArray, 1, artistArray.length - 1, getEncoding(artistArray[0]));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return artist;
    }

    /**
     * 返回mp3的歌曲曲号，如果ID3V2中不包含此信息则返回unknown
     *
     * @return 歌曲曲号
     */
    public String getTrack() {
        String artist = "unknown";
        byte[] artistArray = tags.get("TRCK");
        if (artistArray != null) {
            try {
                artist = new String(artistArray, 1, artistArray.length - 1, getEncoding(artistArray[0]));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return artist;
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

    /**
     * 为了让GC能尽快释放内存，请在使用完该类后调用此方法。
     */
    public void close() {
        in = null;
        tags = null;
    }
}
