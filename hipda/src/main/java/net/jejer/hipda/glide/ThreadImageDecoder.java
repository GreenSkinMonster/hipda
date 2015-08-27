package net.jejer.hipda.glide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;

import net.jejer.hipda.utils.ImageSizeUtils;
import net.jejer.hipda.utils.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * pre scale bitmap before load to ImageView
 * Created by GreenSkinMonster on 2015-03-18.
 */
public class ThreadImageDecoder implements ResourceDecoder<InputStream, Bitmap> {

    private int mMaxWidth = ImageSizeUtils.NORMAL_IMAGE_DECODE_WIDTH;

    public ThreadImageDecoder(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    @Override
    public Resource<Bitmap> decode(InputStream source, int width, int height) throws IOException {
        Resource<Bitmap> result = null;
        try {
            BitmapFactory.Options bitmapLoadingOptions = new BitmapFactory.Options();
            bitmapLoadingOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap original = BitmapFactory.decodeStream(new BufferedInputStream(source), null, bitmapLoadingOptions);

            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();

            if (originalWidth <= mMaxWidth) {
                return new SimpleResource<>(original);
            }

            if (ImageSizeUtils.isLongImage(originalWidth, originalHeight))
                mMaxWidth = ImageSizeUtils.LONG_IMAGE_DECODE_WIDTH;

            int newWidth = originalWidth > mMaxWidth ? mMaxWidth : originalWidth;
            float scale = ((float) newWidth) / originalWidth;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap bitmap = Bitmap.createBitmap(original, 0, 0, originalWidth, originalHeight, matrix, true);
            result = new SimpleResource<>(bitmap);

            original.recycle();

        } catch (Exception e) {
            Logger.e("error when decoding image", e);
        }
        return result;
    }

    @Override
    public String getId() {
        return "ThreadImageDecoder.net.jejer.hipda.glide";
    }
}
