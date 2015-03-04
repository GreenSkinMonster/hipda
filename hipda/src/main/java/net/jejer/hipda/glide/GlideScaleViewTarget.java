package net.jejer.hipda.glide;

import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;

public class GlideScaleViewTarget extends GlideDrawableImageViewTarget {

	private final String LOG_TAG = getClass().getSimpleName();

	private int mMaxWidth;
	private String mUrl;

	public GlideScaleViewTarget(ImageView view, int maxWidth, String url) {
		super(view);
		mMaxWidth = maxWidth;
		mUrl = url;
	}

	@Override
	public void getSize(SizeReadyCallback cb) {
		super.getSize(cb);
	}

	@Override
	public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
		super.onResourceReady(resource, animation);
		if (resource.getIntrinsicWidth() < mMaxWidth * 0.2f) {
			getView().getLayoutParams().width = Math.round(resource.getIntrinsicWidth() * 3.4f);
			getView().getLayoutParams().height = Math.round(resource.getIntrinsicHeight() * 3.4f);
		} else if (resource.getIntrinsicWidth() < mMaxWidth * 0.3f) {
			getView().getLayoutParams().width = Math.round(resource.getIntrinsicWidth() * 2.8f);
			getView().getLayoutParams().height = Math.round(resource.getIntrinsicHeight() * 2.8f);
		} else if (resource.getIntrinsicWidth() < mMaxWidth * 0.4f) {
			getView().getLayoutParams().width = Math.round(resource.getIntrinsicWidth() * 2.4f);
			getView().getLayoutParams().height = Math.round(resource.getIntrinsicHeight() * 2.4f);
		} else if (resource.getIntrinsicWidth() < mMaxWidth * 0.5f) {
			getView().getLayoutParams().width = Math.round(resource.getIntrinsicWidth() * 1.9f);
			getView().getLayoutParams().height = Math.round(resource.getIntrinsicHeight() * 1.9f);
		} else if (resource.getIntrinsicWidth() < mMaxWidth * 0.6f) {
			getView().getLayoutParams().width = Math.round(resource.getIntrinsicWidth() * 1.5f);
			getView().getLayoutParams().height = Math.round(resource.getIntrinsicHeight() * 1.5f);
		} else {
			getView().getLayoutParams().width = mMaxWidth;
			getView().getLayoutParams().height = Math.round(mMaxWidth * 1.0f * resource.getIntrinsicHeight() / resource.getIntrinsicWidth());
		}
		if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
			Log.v(LOG_TAG, mUrl + ", size=" + resource.getIntrinsicWidth() + "x" + resource.getIntrinsicHeight());
	}

	private int dpToPx(int dp) {
		float density = getView().getContext().getResources().getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}

}
