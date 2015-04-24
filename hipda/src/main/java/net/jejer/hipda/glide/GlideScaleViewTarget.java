package net.jejer.hipda.glide;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import net.jejer.hipda.ui.GlideImageView;

public class GlideScaleViewTarget extends BitmapImageViewTarget {

    private final String LOG_TAG = getClass().getSimpleName();

    private int mMaxViewWidth;
    private String mUrl;
    ImageContainer mImageContainer;

    public GlideScaleViewTarget(ImageView view, ImageContainer imageContainer, int maxViewWidth, String url) {
        super(view);

        mMaxViewWidth = maxViewWidth;
        mUrl = url;
        mImageContainer = imageContainer;
    }

    @Override
    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
        super.onResourceReady(resource, glideAnimation);

        if (resource.getWidth() <= 0)
            return;

        //leave 12dp on both left and right side, this should match layout setup
        int maxViewWidth = mMaxViewWidth - dpToPx(12 * 2);

        //if image width < half maxViewWidth, scale it up for better view
        int maxScaleWidth = Math.round(maxViewWidth * 0.5f);

        double scaleRate = getScaleRate(resource.getWidth());
        int scaledWidth = Math.round((int) (resource.getWidth() * scaleRate));
        int scaledHeight = Math.round((int) (resource.getHeight() * scaleRate));

        int displayWidth;
        int displayHeight;
        if (scaledWidth >= maxScaleWidth || mUrl.toLowerCase().endsWith(".gif")) {
            displayWidth = maxViewWidth;
            displayHeight = Math.round(maxViewWidth * 1.0f * resource.getHeight() / resource.getWidth());
        } else {
            displayWidth = scaledWidth;
            displayHeight = scaledHeight;
        }

        //only set width/height at first time
        if (!mImageContainer.isImageReady(mUrl)
                || getView().getLayoutParams().width != displayWidth) {
            getView().getLayoutParams().width = displayWidth;
            getView().getLayoutParams().height = displayHeight;

            mImageContainer.markImageReady(mUrl, displayWidth, displayHeight);
        }

        if (getView() instanceof GlideImageView) {
            if (scaledWidth > GlideImageView.MIN_SCALE_WIDTH || mUrl.toLowerCase().endsWith(".gif"))
                ((GlideImageView) getView()).setClickToViewBigImage();
        }

//        Log.e(LOG_TAG, "mVW=" + maxViewWidth + " mSW=" + maxScaleWidth + ", size=" + resource.getWidth() + "x" + resource.getHeight() + "," + mUrl.substring(mUrl.lastIndexOf("/") + 1));

    }

    private int dpToPx(int dp) {
        float density = getView().getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    //Math! http://www.mathsisfun.com/data/function-grapher.php
    private double getScaleRate(int x) {
        return Math.pow(x, 1.2) / x;
    }

}
