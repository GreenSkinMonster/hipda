package net.jejer.hipda.glide;

import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class GlideScaleViewTarget extends GlideDrawableImageViewTarget {

	private final String LOG_TAG = getClass().getSimpleName();

	private int mMaxViewWidth;
	private String mUrl;

	public GlideScaleViewTarget(ImageView view, int lowerImageWidth, int maxViewWidth, String url) {
		super(view);

		//set a lower width to make Gllide load low resolution image to ram
		//we will change view's size later in onResourceReady according to image's size
		view.getLayoutParams().width = lowerImageWidth;
		view.getLayoutParams().height = lowerImageWidth;

		mMaxViewWidth = maxViewWidth;
		mUrl = url;
	}

	@Override
	public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
		super.onResourceReady(resource, animation);

		if (resource.getIntrinsicWidth() <= 0)
			return;

		//leave 12dp on both left and right side, this should match layout setup
		int maxViewWidth = mMaxViewWidth - dpToPx(12 * 2);

		//if image width < half maxViewWidth, scale it up for better view
		int maxScaleWidth = Math.round(maxViewWidth * 0.5f);

		double scaleRate = getScaleRate(resource.getIntrinsicWidth());
		int scaledWidth = Math.round((int) (resource.getIntrinsicWidth() * scaleRate));
		int scaledHeight = Math.round((int) (resource.getIntrinsicHeight() * scaleRate));

		if (scaledWidth < maxScaleWidth) {
			getView().getLayoutParams().width = scaledWidth;
			getView().getLayoutParams().height = scaledHeight;
		} else {
			getView().getLayoutParams().width = maxViewWidth;
			getView().getLayoutParams().height = Math.round(maxViewWidth * 1.0f * resource.getIntrinsicHeight() / resource.getIntrinsicWidth());
		}
		if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
			Log.v(LOG_TAG, "mVW=" + maxViewWidth + " mSW=" + maxScaleWidth + ", size=" + resource.getIntrinsicWidth() + "x" + resource.getIntrinsicHeight() + "," + mUrl.substring(mUrl.lastIndexOf("/") + 1));
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
