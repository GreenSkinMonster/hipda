package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Image loading job
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageJob extends Job {

    private Context mCtx;
    private String mUrl;
    private View mImageView;

    public GlideImageJob(Context context, String url, int priority, View imageView) {
        super(new Params(priority).setPersistent(false).setRequiresNetwork(false));
        mCtx = context;
        mUrl = url;
        mImageView = imageView;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        try {
            FutureTarget<File> future = Glide.with(mCtx)
                    .load(GlideHelper.getGlideUrl(mUrl))
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

            File cacheFile = future.get();
            Glide.clear(future);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            //Returns null, sizes are in the options variable
            BitmapFactory.decodeFile(cacheFile.getPath(), options);
            int width = options.outWidth;
            int height = options.outHeight;
            String mime = Utils.nullToText(options.outMimeType);

            //calculate display size for image

            //leave 12dp on both left and right side, this should match layout setup
            int maxViewWidth = ThreadDetailFragment.MAX_VIEW_WIDTH - dpToPx(12 * 2);

            //if image width < half maxViewWidth, scale it up for better view
            int maxScaleWidth = Math.round(maxViewWidth * 0.5f);

            double scaleRate = getScaleRate(width);
            int scaledWidth = Math.round((int) (width * scaleRate));
            int scaledHeight = Math.round((int) (height * scaleRate));

            int displayWidth;
            int displayHeight;
            if (scaledWidth >= maxScaleWidth ||
                    (mime.toLowerCase().contains("gif") && scaledWidth >= maxScaleWidth / 2)) {
                displayWidth = maxViewWidth;
                displayHeight = Math.round(maxViewWidth * 1.0f * height / width);
            } else {
                displayWidth = scaledWidth;
                displayHeight = scaledHeight;
            }

            ImageReadyInfo imageReadyInfo = new ImageReadyInfo(cacheFile.getPath(), displayWidth, displayHeight, mime);
            ImageContainer.markImageReady(mUrl, imageReadyInfo);

            EventBus.getDefault().post(new GlideImageEvent(mUrl, mImageView, Constants.STATUS_SUCCESS));

        } catch (Exception e) {
            Logger.e(e);
            EventBus.getDefault().post(new GlideImageEvent(mUrl, mImageView, Constants.STATUS_FAIL));
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
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
