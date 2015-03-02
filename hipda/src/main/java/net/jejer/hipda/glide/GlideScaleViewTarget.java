package net.jejer.hipda.glide;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;

public class GlideScaleViewTarget extends GlideDrawableImageViewTarget {

	public GlideScaleViewTarget(ImageView view) {
		super(view);
	}

	@Override
	public void getSize(SizeReadyCallback cb) {
		super.getSize(cb);
	}

	@Override
	public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
		super.onResourceReady(resource, animation);
		if (resource.getIntrinsicWidth() < 200) {
			getView().getLayoutParams().width = resource.getIntrinsicWidth() * 4;
			getView().getLayoutParams().height = resource.getIntrinsicHeight() * 4;
		} else if (resource.getIntrinsicWidth() < 400) {
			getView().getLayoutParams().width = Math.round(resource.getIntrinsicWidth() * 2.4f);
			getView().getLayoutParams().height = Math.round(resource.getIntrinsicHeight() * 2.4f);
		} else if (resource.getIntrinsicWidth() < 600) {
			getView().getLayoutParams().width = Math.round(resource.getIntrinsicWidth() * 1.5f);
			getView().getLayoutParams().height = Math.round(resource.getIntrinsicHeight() * 1.5f);
		} else {
			getView().getLayoutParams().width = 1000;
			getView().getLayoutParams().height = Math.round(1000.0f * resource.getIntrinsicHeight() / resource.getIntrinsicWidth());
		}
		Log.e("GlideScaleViewTarget", resource != null ? (resource.getIntrinsicWidth() + "x" + resource.getIntrinsicHeight()) : "NULL");
	}
}
