package net.jejer.hipda.glide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;

import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * pre scale bitmap before load to ImageView
 * Created by GreenSkinMonster on 2015-03-18.
 */
public class ThreadImageDecoder implements ResourceDecoder<InputStream, Bitmap> {

    private final static int IN_SAMPLE_LIMIT = 10000;
    private final static int MAX_WIDTH = getMaxWidth();
    private final static int MAX_HEIGHT = 2 * Utils.getScreenHeight();

    private ImageInfo mImageInfo;

    public ThreadImageDecoder(ImageInfo imageInfo) {
        mImageInfo = imageInfo;
    }

    @Override
    public Resource<Bitmap> decode(InputStream source, int width, int height) throws IOException {
        Resource<Bitmap> result = null;
        BufferedInputStream bis = new BufferedInputStream(source);

        try {

            //if source is too large, we should use inSampleSize to decode it
            int inSampleSize = 1;
            if (Math.max(mImageInfo.getWidth(), mImageInfo.getHeight()) > IN_SAMPLE_LIMIT) {
                bis.mark(bis.available());
                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                newOpts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(bis, null, newOpts);

                inSampleSize = (int) Math.ceil((float) Math.max(newOpts.outWidth, newOpts.outHeight) / IN_SAMPLE_LIMIT);
                bis.reset();
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Utils.getBitmapConfig();
            if (inSampleSize > 1)
                options.inSampleSize = inSampleSize;
            Bitmap original = BitmapFactory.decodeStream(bis, null, options);

            if (original == null) {
                Logger.e("decode bitmap failed, image format may not be supported");
                return null;
            }

            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();
            int rotationDegree = getRotationDegree();

            if (originalWidth <= MAX_WIDTH
                    && originalHeight <= MAX_HEIGHT
                    && rotationDegree == 0) {
                return new SimpleResource<>(original);
            }

            int newWidth = originalWidth > MAX_WIDTH ? MAX_WIDTH : originalWidth;
            float scale = ((float) newWidth) / originalWidth;
            int newHeight = Math.round(originalHeight * scale);
            if (newHeight > MAX_HEIGHT) {
                newHeight = MAX_HEIGHT;
                scale = ((float) newHeight) / originalHeight;
            }

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            if (rotationDegree != 0)
                matrix.postRotate(rotationDegree);

            Bitmap bitmap = Bitmap.createBitmap(original, 0, 0, originalWidth, originalHeight, matrix, true);
            result = new SimpleResource<>(bitmap);

            original.recycle();
        } catch (Exception e) {
            Logger.e("error when decoding image", e);
        }
        return result;
    }

    private int getRotationDegree() {
        int degree = 0;
        if (mImageInfo.getOrientation() == ExifInterface.ORIENTATION_ROTATE_90) {
            degree = 90;
        } else if (mImageInfo.getOrientation() == ExifInterface.ORIENTATION_ROTATE_180) {
            degree = 180;
        } else if (mImageInfo.getOrientation() == ExifInterface.ORIENTATION_ROTATE_270) {
            degree = 270;
        }
        return degree;
    }

    @Override
    public String getId() {
        return "ThreadImageDecoder.net.jejer.hipda.glide";
    }

    private static int getMaxWidth() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory <= 64 * 1024 * 1024) {
            return 460;
        } else if (maxMemory <= 128 * 1024 * 1024) {
            return 520;
        } else if (maxMemory <= 256 * 1024 * 1024) {
            return 560;
        }
        return 720;
    }

}
