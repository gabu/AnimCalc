
package com.example.animcalc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Images;

/**
 * Bitmap を扱う便利なメソッドを提供します。
 * 
 * @author gabu
 */
public class BitmapUtils {
    /**
     * 指定された Uri から Bitmap を生成します。 画像の本来の向きが取得できた場合には、正しく回転した Bitmap を返します。
     * また、安全に Bitmap を生成できるように指定された長辺の最低サイズを下回らない 最小のサイズに画像を縮小します。
     * 
     * @param context Context
     * @param uri 画像のUri
     * @param size 長辺の最低サイズ
     * @return 正しく回転された Bitmap
     */
    public static Bitmap decodeUri(Context context, Uri uri, int size) {
        try {
            // 縮小する倍率を計算する
            int sampleSize = calcSampleSize(context, uri, size);

            BitmapFactory.Options options = new BitmapFactory.Options();
            // 縮小する倍率をセット
            options.inSampleSize = sampleSize;

            InputStream is = context.getContentResolver().openInputStream(uri);
            // Bitmapを生成！
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();

            // 行列計算のためにMatrixを作って
            Matrix matrix = new Matrix();
            // 回転するべき角度を取得して
            int orientation = getOrientation(context, uri);
            // Matrixに回転するべき角度をセット、回転する中心座標はBitmapの中心
            matrix.postRotate(orientation, bitmap.getWidth() / 2,
                    bitmap.getHeight() / 2);
            // 回転したBitmapを生成して呼び元に返す
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 縮小する倍率を計算します。 具体的には、長辺の最低サイズを下回らない最小のサイズになるような倍率を計算します。
     * 
     * @param context Context
     * @param uri 画像のUri
     * @param size 長辺の最低サイズ
     * @return 縮小する倍率
     */
    private static int calcSampleSize(Context context, Uri uri, int size) {
        int sampleSize = 1;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            // Bitmapは生成せずにサイズを測るだけの設定
            options.inJustDecodeBounds = true;
            // 測定！
            BitmapFactory.decodeStream(is, null, options);
            is.close();

            int imageSize;
            if (options.outWidth < options.outHeight) {
                // Portrait（縦長写真）
                imageSize = options.outHeight;
            } else {
                // Landscape（横長写真）
                imageSize = options.outWidth;
            }
            // 長辺を指定されたサイズで割る
            // int同士の除算なので自動的に小数点以下は切り捨てられる
            sampleSize = imageSize / size;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sampleSize;
    }

    /**
     * 画像の本来の向きを調べて回転するべき角度を取得します。
     * 
     * @param context Context
     * @param uri 画像のUri
     * @return 回転するべき角度
     */
    private static int getOrientation(Context context, Uri uri) {
        if (uri.getScheme().equals("content")) {
            // content://の場合は
            // MediaStore.Imagesから向きを取得する
            String[] projection = {
                    Images.ImageColumns.ORIENTATION
            };
            Cursor c = context.getContentResolver().query(uri, projection,
                    null, null, null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } else if (uri.getScheme().equals("file")) {
            // file://の場合は
            // Exifから向きを取得する
            try {
                ExifInterface exif = new ExifInterface(uri.getPath());
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    return 90;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    return 180;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    return 270;
                } else {
                    return 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return 0;
    }
}
