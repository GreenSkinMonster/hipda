package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import net.jejer.hipda.R;

/**
 * draw a mark on gif
 * Created by GreenSkinMonster on 2015-04-12.
 */
public class GifTransformation extends BitmapTransformation {

    private Context mCtx;

    public GifTransformation(Context context) {
        super(context);
        mCtx = context;
    }


    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {

        Bitmap gifMark = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.ic_play_gif);

        Bitmap result = toTransform.copy(Bitmap.Config.RGB_565, true);

        Canvas canvas = new Canvas(result);

        float scale = 1.0f * result.getWidth() / outWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap scaledGifMark = Bitmap.createBitmap(gifMark, 0, 0, gifMark.getHeight(), gifMark.getWidth(), matrix, true);

        int bitmapHeight = scaledGifMark.getHeight();
        int bitmapWidth = scaledGifMark.getWidth();

        int x = (result.getWidth() - bitmapWidth) / 2;
        int y = (result.getHeight() - bitmapHeight) / 2;
        canvas.drawBitmap(scaledGifMark, x, y, null);
        return result;
    }

    @Override
    public String getId() {
        return "GifTransformation.net.jejer.hipda.glide";
    }
}
