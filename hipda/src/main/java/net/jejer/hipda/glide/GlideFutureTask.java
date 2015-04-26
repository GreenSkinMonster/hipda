package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;

/**
 * Load image and get information
 * Created by GreenSkinMonster on 2015-04-24.
 */
public class GlideFutureTask extends AsyncTask<Void, Void, ImageReadyInfo> {

    private Context mCtx;
    private GlideImageView mGlideImageView;
    private int mMaxViewWidth;
    private String mUrl;
    private ImageContainer mImageContainer;

    public GlideFutureTask(Context context, ImageContainer imageContainer, GlideImageView giv, int maxViewWidth, String url) {
        mCtx = context;
        mGlideImageView = giv;
        mMaxViewWidth = maxViewWidth;
        mUrl = url;
        mImageContainer = imageContainer;
    }

    @Override
    protected ImageReadyInfo doInBackground(Void... voids) {
        try {
            FutureTarget<File> future =
                    Glide.with(mCtx)
                            .load(mUrl)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            File cacheFile = future.get();
            Glide.clear(future);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            //Returns null, sizes are in the options variable
            BitmapFactory.decodeFile(cacheFile.getPath(), options);
            int width = options.outWidth;
            int height = options.outHeight;
            String mime = options.outMimeType;

            //calculate display size for image

            //leave 12dp on both left and right side, this should match layout setup
            int maxViewWidth = mMaxViewWidth - dpToPx(12 * 2);

            //if image width < half maxViewWidth, scale it up for better view
            int maxScaleWidth = Math.round(maxViewWidth * 0.5f);

            double scaleRate = getScaleRate(width);
            int scaledWidth = Math.round((int) (width * scaleRate));
            int scaledHeight = Math.round((int) (height * scaleRate));

            int displayWidth;
            int displayHeight;
            if (scaledWidth >= maxScaleWidth || mime.toLowerCase().contains("gif")) {
                displayWidth = maxViewWidth;
                displayHeight = Math.round(maxViewWidth * 1.0f * height / width);
            } else {
                displayWidth = scaledWidth;
                displayHeight = scaledHeight;
            }

            return new ImageReadyInfo(cacheFile.getPath(), displayWidth, displayHeight, mime);
        } catch (Exception e) {
            Log.e("GlideFutureTask", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(ImageReadyInfo imageReadyInfo) {
        if (imageReadyInfo != null) {
            mImageContainer.markImageReady(mUrl, imageReadyInfo);
        }
        if (imageReadyInfo != null && mImageContainer != null)
            mImageContainer.loadImage(mUrl, mGlideImageView);

    }

    private int dpToPx(int dp) {
        float density = mCtx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    //Math! http://www.mathsisfun.com/data/function-grapher.php
    private double getScaleRate(int x) {
        return Math.pow(x, 1.2) / x;
    }

}
