package com.buzz.utils;

/**
 * Created by NickChung on 3/4/15.
 */

import android.content.Context;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
    private String SDPATH;

    /**
     *
     */
    public FileHelper() {
        // TODO Auto-generated constructor stub
        //获得当前外部存储设备的目录
        SDPATH = GlobalConst.PATH_SDCARD;
    }

    /**
     * 在SD卡上创建文件
     *
     * @param fileName
     * @return
     */
    public File createSdFile(String fileName) {
        File file = new File(SDPATH + fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return file;
    }

    /**
     * 创建SD卡目录
     *
     * @param dirName
     * @return
     */
    public File createSDDir(String dirName) {
        File file = new File(SDPATH + dirName);
        file.mkdirs();

        return file;
    }

    public boolean isFileExist(String fileName) {
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    public boolean isFileExistIncludeSDPath(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public boolean deleteFile(String fileName){
        File file = new File(SDPATH + fileName);
        return file.delete();
    }

    public String readFromSDFile(String fileFullPathName) {
        FileInputStream input = null;
        String res = "";
        try {
            File file = new File(fileFullPathName);
            if (file.exists()) {
                input = new FileInputStream(file);
                int length = input.available();
                byte[] buffer = new byte[length];
                input.read(buffer);
                res = EncodingUtils.getString(buffer, "UTF-8");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    public File appendToSDFile(String path, String fileName, String text) {
        File file = null;
        OutputStream output = null;
        byte[] buffer = text.getBytes();

        try {
            createSDDir(path);
            file = createSdFile(path + fileName);
            output = new FileOutputStream(file, true);
            output.write(buffer);
            output.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    public File writeToSDFromJson(String path, String fileName, String text) {
        File file = null;
        OutputStream output = null;
        byte[] buffer = text.getBytes();

        try {
            createSDDir(path);
            file = createSdFile(path + fileName);
            output = new FileOutputStream(file);
            output.write(buffer);
            output.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    public File writeToSDFromInput(Context context, String path, String fileName, InputStream input) {
        File file = null;
        OutputStream output = null;

        try {
            createSDDir(path);
            file = createSdFile(path + fileName);
            output = new FileOutputStream(file);

            byte[] buffer = new byte[4 * 1024];
            int total = 0;
            while ((input.read(buffer)) != -1) {
                total = total + buffer.length;
                output.write(buffer);
                //更新下载进度条
                //((DownloadActivity) context).sendMsg(1, total);
            }
            output.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //下载完成
        //((DownloadActivity) context).sendMsg(2, 0);
        return file;
    }

}