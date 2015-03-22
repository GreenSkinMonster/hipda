package net.jejer.hipda.glide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * pre scale bitmap before load to ImageView
 * Created by GreenSkinMonster on 2015-03-18.
 */
public class ThreadImageDecoder implements ResourceDecoder<InputStream, Bitmap> {

    private static String LOG_TAG = ThreadImageDecoder.class.getSimpleName();

    @Override
    public Resource<Bitmap> decode(InputStream source, int width, int height) throws IOException {
        Resource<Bitmap> result = null;
        int maxWidth = 450;
        try {
            BitmapFactory.Options bitmapLoadingOptions = new BitmapFactory.Options();
            bitmapLoadingOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap original = BitmapFactory.decodeStream(new BufferedInputStream(source), null, bitmapLoadingOptions);

            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();

            if (originalWidth <= maxWidth && originalHeight < 4 * originalWidth) {
//                Log.e(LOG_TAG, "original " + originalWidth + "x" + originalHeight + " return");
                return new SimpleResource<>(original);
            }

            if (originalHeight > 4 * originalWidth)
                originalHeight = 3 * originalWidth;

            int newWidth = originalWidth > maxWidth ? maxWidth : originalWidth;
            float scale = ((float) newWidth) / originalWidth;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap bitmap = Bitmap.createBitmap(original, 0, 0, originalWidth, originalHeight, matrix, true);
            result = new SimpleResource<>(bitmap);

            original.recycle();
//            Log.e(LOG_TAG, "original " + originalWidth + "x" + originalHeight + ", new " + bitmap.getWidth() + "x" + bitmap.getHeight()+ " "+bitmap.getConfig().toString());

        } catch (Exception e) {
            Log.e(LOG_TAG, "error when decoding image", e);
        }
        return result;
    }

    @Override
    public String getId() {
        return "ThreadImageDecoder.net.jejer.hipda.glide";
    }
}
