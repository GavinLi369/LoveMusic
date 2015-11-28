package gavin.utils;

import gavin.model.MusicInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;

public class FileUtils {
    private final String mSDCardRoot = "/storage/sdcard1";
    private Context context;

    public FileUtils(Context context) {
//        得到当前内部存储卡的路径
//        mSDCardRoot = Environment.getExternalStorageDirectory()
//                .getAbsolutePath();
//        mSDCardRoot = Environment.getExternalStorageDirectory().getPath();
        this.context = context;
    }

    /**
     * 取得当前外部存储卡的路径
     *
     * @return
     */
    public String getSDCardRoot() {
        return mSDCardRoot;
    }

    /**
     * 在SD卡上创建文件
     *
     * @throws IOException
     */
    public File createFileInSDCard(String fileName, String dir)
            throws IOException {
        File file = new File(mSDCardRoot + dir + File.separator + fileName);

        return file;
    }

    /**
     * 在SD卡上创建目录
     *
     * @throws IOException
     */
    public File createDirInSDCard(String dir) throws IOException {
        File dirFile = new File(mSDCardRoot + dir + File.separator);
        dirFile.mkdir();

        return dirFile;
    }

    /**
     * 将文本文件转换为String
     */
    public String parseFile2String (File file) {
        if (file == null) {
            return "";
        }

        BufferedReader reader;
        String line;
        String buffer = "";
        try {
            reader = new BufferedReader
                    (new InputStreamReader(new FileInputStream(file), "GBK"));
            while ((line = reader.readLine()) != null) {
                buffer = buffer + line + "\n";
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }


    /**
     * 判断SD卡上的文件是否存在
     */
    public boolean isFileExist(String fileName, String path) {
        File file = new File(mSDCardRoot + path + File.separator + fileName);

        return file.exists();
    }

    /**
     * 判断SD卡上的文件夹是否存在
     */
    public boolean isDirExist(String path) {
        File file = new File(mSDCardRoot + path);

        return file.exists();
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中
     */
    public File writeToSDFromInput(String path, String fileName,
                                   InputStream inputStream) {
        File file = null;
        OutputStream outputStream;
        try {
            createDirInSDCard(path);
            file = createFileInSDCard(fileName, path);
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 4];
            int temp;
            while ((temp = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, temp);
            }
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }



    /**
     * 获取path目录里面的所有mp3文件
     */

    public ArrayList<MusicInfo> getSongFiles(String path) {
        ArrayList<MusicInfo> musicInfoList = new ArrayList<>();
        File filePath = new File(mSDCardRoot + path);
        if (filePath.listFiles().length != 0){
            File[] files = filePath.listFiles();
            for(File file : files) {
                if (file.getName().endsWith("mp3")) {
                    MusicInfo mp3Info = new MusicInfo(file, context);
                    musicInfoList.add(mp3Info);
                }
            }
        }

        return musicInfoList;
    }

    /**
     * 获取MP3文件的详细信息
     */
    public byte[] getMp3Info(File file) {
        byte[] buffer = new byte[128];
        RandomAccessFile mp3File;
        try {
            mp3File = new RandomAccessFile(file, "r");
            mp3File.seek(mp3File.length() - 128);
            mp3File.read(buffer);
            mp3File.close();

            if (buffer.length != 128) {
                throw new Exception("MP3文件长度不合法!");
            }

            if (!"TAG".equalsIgnoreCase(new String(buffer, 0, 3))) {
                throw new Exception("MP3标签信息数据格式不正确！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer;
    }
}
