package net.jejer.hipda.ui.widget;

import android.app.Activity;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.CompatDecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageRegionDecoder;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.utils.Utils;

import androidx.core.content.ContextCompat;

/**
 * Created by GreenSkinMonster on 2015-11-07.
 */
public class ImageViewerLayout extends BaseImageLayout {

    private final SubsamplingScaleImageView mScaleImageView;

    public ImageViewerLayout(Activity activity, ContentImg contentImg) {
        super(activity);

        mRequestManager = Glide.with(activity);
        mUrl = contentImg.getContent();
        mContentImg = contentImg;

        mImageView = new ImageView(getContext());
        addView(mImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mScaleImageView = new SubsamplingScaleImageView(getContext());
        addView(mScaleImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mProgressBar = new DownloadProgressBar(getContext());
        int progressbarWidth = Utils.dpToPx(45);
        RelativeLayout.LayoutParams pbLayoutParams = new RelativeLayout.LayoutParams(progressbarWidth, progressbarWidth);
        pbLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mProgressBar.setCancelable(false);
        mProgressBar.setFinishIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_action_play));
        mProgressBar.setVisibility(GONE);
        addView(mProgressBar, pbLayoutParams);
    }

    @Override
    protected boolean isNetworkFetch() {
        return true;
    }

    @Override
    protected OnClickListener getOnClickListener() {
        return v -> {
            if (getContext() instanceof Activity) {
                Activity activity = (Activity) getContext();
                activity.finish();
            }
        };
    }

    @Override
    protected OnLongClickListener getOnLongClickListener() {
        return null;
    }

    public ImageView getImageView() {
        return mImageView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !mScaleImageView.isImageLoaded();
    }

    @Override
    protected void onDetachedFromWindow() {
        mScaleImageView.recycle();
        super.onDetachedFromWindow();
    }

    @Override
    protected void displayImage() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.isSuccess()) {
            if (imageInfo.isGif()) {
                mProgressBar.setFinish();
                mProgressBar.setVisibility(VISIBLE);
                mImageView.setVisibility(VISIBLE);
                mScaleImageView.setVisibility(GONE);
                mRequestManager
                        .load(mUrl)
                        .dontAnimate()
                        .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mImageView);
            } else {
                mProgressBar.setVisibility(View.GONE);
                displayScaleImage();
            }
        } else {
            mProgressBar.setError();
            mProgressBar.setVisibility(VISIBLE);
        }
    }

    protected void displayScaleImage() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        mScaleImageView.setMinimumDpi(36);
        mScaleImageView.setMinimumTileDpi(160);
        mScaleImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        mScaleImageView.setImage(ImageSource.uri(imageInfo.getPath()));
        mScaleImageView.setBitmapDecoderFactory(new CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder.class, Utils.getBitmapConfig()));
        mScaleImageView.setRegionDecoderFactory(new CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder.class, Utils.getBitmapConfig()));
        mScaleImageView.setOnClickListener(getOnClickListener());

        mScaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
            @Override
            public void onImageLoaded() {
                if (mImageView.getVisibility() == VISIBLE) {
                    mRequestManager.clear(mImageView);
                    mImageView.setVisibility(View.GONE);
                }
                mScaleImageView.setVisibility(View.VISIBLE);
                if (imageInfo.isLongImage()) {
                    float scale = (float) mScaleImageView.getWidth() / imageInfo.getWidth();
                    mScaleImageView.animateScaleAndCenter(scale, new PointF(mScaleImageView.getWidth() / 2, 0))
                            .withDuration(500)
                            .withEasing(SubsamplingScaleImageView.EASE_OUT_QUAD)
                            .withInterruptible(false)
                            .start();
                    mScaleImageView.setDoubleTapZoomScale(scale);
                }
            }

            @Override
            public void onImageLoadError(Exception e) {
                mRequestManager.clear(mImageView);
                mImageView.setVisibility(View.GONE);
                mProgressBar.setError();
            }
        });
    }

}