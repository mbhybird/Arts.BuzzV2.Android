package com.buzz.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by NickChung on 3/17/15.
 */
public class ImageHelper {

    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fs = new FileInputStream(url);
            Bitmap bitmap = BitmapFactory.decodeStream(fs);
            try {
                fs.close();
            } catch (IOException e) {

            }
            return bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.print(e.toString());
            return null;
        }
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    // 缩放图片
    public static Bitmap scaleImage(Bitmap bm, int newWidth) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = scaleWidth;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newImage = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newImage;
    }

    public static Bitmap cropCenterImage(String srcImagePath, int cropWidth, int cropHeight) {
        try {
            FileInputStream fs = new FileInputStream(srcImagePath);
            Bitmap srcImage = BitmapFactory.decodeStream(fs);
            try {
                fs.close();
            } catch (IOException e) {

            }
            Bitmap scaleImage = scaleImage(srcImage, cropWidth);
            // 返回源图片的宽度。
            int srcW = scaleImage.getWidth();
            // 返回源图片的高度。
            int srcH = scaleImage.getHeight();
            int x = 0, y = 0;
            // 使截图区域居中
            x = srcW / 2 - cropWidth / 2;
            y = srcH / 2 - cropHeight / 2;
            srcW = srcW / 2 + cropWidth / 2;
            srcH = srcH / 2 + cropHeight / 2;
            // 生成图片
            return Bitmap.createBitmap(scaleImage, x, y, srcW, srcH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
