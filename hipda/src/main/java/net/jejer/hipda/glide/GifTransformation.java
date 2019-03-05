package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

import androidx.annotation.NonNull;

/**
 * draw a mark on gif
 * Created by GreenSkinMonster on 2015-04-12.
 */
public class GifTransformation extends BitmapTransformation {

    private final static int GIF_DECODE_WIDTH = 460;

    public GifTransformation(Context context) {
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        int resultWidth = GIF_DECODE_WIDTH;
        Bitmap result = toTransform.copy(Bitmap.Config.ARGB_8888, true);

        if (result.getWidth() != resultWidth) {
            float resultScale = 1.0f * resultWidth / result.getWidth();
            Matrix resultMatrix = new Matrix();
            resultMatrix.postScale(resultScale, resultScale);

            Bitmap tmp = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), resultMatrix, true);
            result.recycle();
            result = tmp;
        }
        return result;
    }

}
