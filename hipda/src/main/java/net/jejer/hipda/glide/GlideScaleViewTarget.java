package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import net.jejer.hipda.R;
import net.jejer.hipda.ui.GlideImageView;

public class GlideScaleViewTarget extends BitmapImageViewTarget {

    private final String LOG_TAG = getClass().getSimpleName();

    private int mMaxViewWidth;
    private String mUrl;
    private TextView mTextView;
    private Context mCtx;

    public GlideScaleViewTarget(Context ctx, ImageView view, TextView textView, int maxViewWidth, String url) {
        super(view);
        mCtx = ctx;

        mMaxViewWidth = maxViewWidth;
        mUrl = url;
        mTextView = textView;
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

        if (scaledWidth < maxScaleWidth) {
            getView().getLayoutParams().width = scaledWidth;
            getView().getLayoutParams().height = scaledHeight;
        } else {
            getView().getLayoutParams().width = maxViewWidth;
            getView().getLayoutParams().height = Math.round(maxViewWidth * 1.0f * resource.getHeight() / resource.getWidth());
        }
        if (mUrl.toLowerCase().endsWith(".gif")) {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(mCtx.getResources().getString(R.string.image_click_to_play));
        } else if (scaledWidth >= GlideImageView.MIN_SCALE_WIDTH && scaledHeight >= 3 * scaledWidth) {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(mCtx.getResources().getString(R.string.image_click_to_view_long));
        } else if (scaledWidth >= GlideImageView.MIN_SCALE_WIDTH) {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(mCtx.getResources().getString(R.string.image_click_to_view));
        } else {
            mTextView.setVisibility(View.INVISIBLE);
        }

        if (getView() instanceof GlideImageView)
            ((GlideImageView) getView()).setClickToViewBigImage();

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
            Log.v(LOG_TAG, "mVW=" + maxViewWidth + " mSW=" + maxScaleWidth + ", size=" + resource.getWidth() + "x" + resource.getHeight() + "," + mUrl.substring(mUrl.lastIndexOf("/") + 1));
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
