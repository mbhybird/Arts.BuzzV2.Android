package com.buzz.utils;

/**
 * Created by NickChung on 3/5/15.
 */

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
@Deprecated
public class HttpDownloader {
    private URL url = null;

    /**
     * 根据URL下载文件，前提是文件当中的内容为文本，返回值就是文件当中的内容
     *
     * @param urlStr
     * @return
     */
    public String download(String urlStr) {

        StringBuffer buffer = new StringBuffer();
        String line = null;
        BufferedReader reader = null;

        try {
            url = new URL(urlStr);
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
            } catch (IOException e) {
                Log.e("io", "HttpURLConnection -> IOException");
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            Log.e("url", "url -> MalformedURLException");
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return buffer.toString();

    }

    /**
     * 该函数返回整形： -1代表下载出错，0代表下载成功，1代表下载文件已存在
     *
     * @param urlStr
     * @param path
     * @param fileName
     * @return
     */
    public int download(Context context, String urlStr, String path, String fileName) {
        InputStream input = null;
        FileHelper fileHelper = new FileHelper();
        if (fileHelper.isFileExist(path + fileName)) {
            //((DownloadActivity) context).sendMsg(2, 0);
            return 1;
        } else {
            try {
                input = getInputStreamFromUrl(context, urlStr);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            File resultFile = fileHelper.writeToSDFromInput(context, path, fileName, input);
            if (resultFile == null) {
                return -1;
            }
        }
        return 0;
    }

    public InputStream getInputStreamFromUrl(Context context, String urlStr) throws IOException {
        url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream s = conn.getInputStream();

        //((DownloadActivity) context).sendMsg(0, conn.getContentLength());

        return s;
    }
}