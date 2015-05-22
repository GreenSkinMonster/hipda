package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.ImageSizeUtils;

/**
 * draw a mark on gif
 * Created by GreenSkinMonster on 2015-04-12.
 */
public class GifTransformation extends BitmapTransformation {

    private static Bitmap scaledGifMark;

    private Context mCtx;

    public GifTransformation(Context context) {
        super(context);
        mCtx = context;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {

        int resultWidth = ImageSizeUtils.GIF_DECODE_WIDTH;
        int markWidth = resultWidth / 5;

        Bitmap result = toTransform.copy(Bitmap.Config.RGB_565, true);

        if (result.getWidth() != resultWidth) {
            float resultScale = 1.0f * resultWidth / result.getWidth();
            Matrix resultMatrix = new Matrix();
            resultMatrix.postScale(resultScale, resultScale);

            Bitmap tmp = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), resultMatrix, true);
            result.recycle();
            result = tmp;
        }

        if (scaledGifMark == null || scaledGifMark.isRecycled()) {
            Bitmap gifMark = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.ic_play_gif);
            float markScale = 1.0f * markWidth / gifMark.getWidth();
            Matrix markMatrix = new Matrix();
            markMatrix.postScale(markScale, markScale);
            scaledGifMark = Bitmap.createBitmap(gifMark, 0, 0, gifMark.getWidth(), gifMark.getHeight(), markMatrix, true);
        }

        int markHeight = scaledGifMark.getHeight();

        Canvas canvas = new Canvas(result);
        int x = (result.getWidth() - markWidth) / 2;
        int y = (result.getHeight() - markHeight) / 2;
        canvas.drawBitmap(scaledGifMark, x, y, null);
        return result;
    }

    @Override
    public String getId() {
        return "GifTransformation.net.jejer.hipda.glide";
    }
}
