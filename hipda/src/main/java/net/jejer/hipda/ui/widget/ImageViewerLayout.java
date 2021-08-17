package net.jejer.hipda.ui.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.GestureDetector;
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
import net.jejer.hipda.ui.ImageViewerActivity;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

/**
 * Created by GreenSkinMonster on 2015-11-07.
 */
public class ImageViewerLayout extends BaseImageLayout {

    private final ImageInfo mImageInfo;
    private final SubsamplingScaleImageView mScaleImageView;
    private final int mScreenHeight;
    private float mMinScale = 0;

    private final static int MIN_DRAG_DISTANCE = Utils.dpToPx(4);
    private float mOldX, mOldY, mMovY;
    private float mAlphaPercent = 1f;
    private boolean mToClose = false;
    private boolean mDragCloseable = true;
    private boolean mDragMoving = false;

    private final GestureDetector mDetector;
    GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mImageInfo.isGif()) {
                playGif();
            } else {
                ImageViewerActivity activity = (ImageViewerActivity) getContext();
                activity.toggleFullscreen();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mImageInfo.isFail())
                UIUtils.showMessageDialog(getContext(), "错误信息", mImageInfo.getMessage(), true);
        }
    };

    public ImageViewerLayout(Activity activity, ContentImg contentImg) {
        super(activity);

        mRequestManager = Glide.with(activity);
        mUrl = contentImg.getContent();
        mContentImg = contentImg;
        mImageInfo = ImageContainer.getImageInfo(mUrl);

        mImageView = new AppCompatImageView(getContext());
        addView(mImageView, new ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mScaleImageView = new SubsamplingScaleImageView(getContext());
        addView(mScaleImageView, new ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mProgressBar = new DownloadProgressBar(getContext());
        int progressbarWidth = Utils.dpToPx(45);
        RelativeLayout.LayoutParams pbLayoutParams = new RelativeLayout.LayoutParams(progressbarWidth, progressbarWidth);
        pbLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mProgressBar.setCancelable(false);
        mProgressBar.setFinishIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_action_play));
        mProgressBar.setVisibility(GONE);
        addView(mProgressBar, pbLayoutParams);

        mScreenHeight = UIUtils.getScreenHeight(activity);
        mDetector = new GestureDetector(activity, mGestureListener);
    }

    @Override
    protected boolean isNetworkFetch() {
        return true;
    }

    public ImageView getImageView() {
        return mImageView;
    }

    @Override
    protected void onDetachedFromWindow() {
        mScaleImageView.recycle();
        super.onDetachedFromWindow();
    }

    @Override
    protected void displayImage() {
        if (mImageInfo.isSuccess()) {
            if (mImageInfo.isGif()) {
                mProgressBar.setFinish();
                mProgressBar.setVisibility(VISIBLE);
                mImageView.setVisibility(VISIBLE);
                mScaleImageView.setVisibility(GONE);
                mRequestManager
                        .load(mUrl)
                        .dontAnimate()
                        .override(mImageInfo.getBitmapWidth(), mImageInfo.getBitmapHeight())
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
        mScaleImageView.setMinimumDpi(36);
        mScaleImageView.setMinimumTileDpi(160);
        mScaleImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        mScaleImageView.setImage(ImageSource.uri(mImageInfo.getPath()));
        mScaleImageView.setBitmapDecoderFactory(new CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder.class, Utils.getBitmapConfig()));
        mScaleImageView.setRegionDecoderFactory(new CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder.class, Utils.getBitmapConfig()));
        mScaleImageView.setOnStateChangedListener(new SubsamplingScaleImageView.OnStateChangedListener() {
            @Override
            public void onScaleChanged(float newScale, int origin) {
                mDragCloseable = newScale < mMinScale;
            }

            @Override
            public void onCenterChanged(PointF newCenter, int origin) {
            }
        });

        mScaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
            @Override
            public void onImageLoaded() {
                if (mImageView.getVisibility() == VISIBLE) {
                    mRequestManager.clear(mImageView);
                    mImageView.setVisibility(View.GONE);
                }
                mScaleImageView.setVisibility(View.VISIBLE);

                mMinScale = Math.min(((float) mScaleImageView.getWidth() / mImageInfo.getWidth()),
                        ((float) mScaleImageView.getHeight() / mImageInfo.getHeight())) + 0.01f;

                if (mImageInfo.isLongImage()) {
                    float scale = (float) mScaleImageView.getWidth() / mImageInfo.getWidth();
                    mScaleImageView
                            .animateScaleAndCenter(scale, new PointF((float) mScaleImageView.getWidth() / 2, 0))
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        if (!mDragMoving)
            mScaleImageView.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mOldX = event.getRawX();
                mOldY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mToClose) {
                    mToClose = false;
                    ((ImageViewerActivity) getContext()).finish();
                } else {
                    animRestoreImage();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDragCloseable) {
                    float movX = event.getRawX() - mOldX;
                    mMovY = event.getRawY() - mOldY;
                    float absMovX = Math.abs(movX);
                    mDragMoving = mMovY > absMovX && absMovX > MIN_DRAG_DISTANCE;
                    animMoveImage(movX, mMovY);
                    mToClose = mMovY > absMovX
                            && mMovY > ((float) mScreenHeight / 8);
                }
                return false;
        }
        return true;
    }

    private void animRestoreImage() {
        mDragMoving = false;
        animate().setDuration(200)
                .scaleX(1)
                .scaleY(1)
                .translationX(0)
                .translationY(0)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (mAlphaPercent < animation.getAnimatedFraction()) {
                            ((ViewGroup) getParent()).setBackgroundColor(convertPercentToBlackAlphaColor(animation.getAnimatedFraction()));
                        }
                    }
                })
                .start();
    }

    private void animMoveImage(float deltaX, float deltaY) {
        if (Math.abs(mMovY) < ((float) mScreenHeight / 4)) {
            float scale = 1 - Math.abs(mMovY) / mScreenHeight;
            mAlphaPercent = 1 - Math.abs(deltaY) / ((float) mScreenHeight / 2);
            setScaleX(scale);
            setScaleY(scale);
            ((ViewGroup) getParent()).setBackgroundColor(convertPercentToBlackAlphaColor(mAlphaPercent));
        }
        setTranslationX(deltaX);
        setTranslationY(deltaY);
    }

    private int convertPercentToBlackAlphaColor(float percent) {
        percent = Math.min(1, Math.max(0, percent));
        int intAlpha = (int) (percent * 255);
        String stringAlpha = Integer.toHexString(intAlpha).toLowerCase();
        String color = "#" + (stringAlpha.length() < 2 ? "0" : "") + stringAlpha + "000000";
        return Color.parseColor(color);
    }

}