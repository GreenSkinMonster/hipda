package net.jejer.hipda.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.CompatDecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageRegionDecoder;

import net.jejer.hipda.R;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.glide.GifTransformation;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageEvent;
import net.jejer.hipda.job.GlideImageJob;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Created by GreenSkinMonster on 2015-11-07.
 */
public class ImageViewerLayout extends RelativeLayout {

    private static WeakReference<ImageViewerLayout> mCurrentViewHolder;

    private final SubsamplingScaleImageView mScaleImageView;
    private final ImageView mImageView;
    private final DownloadProgressBar mProgressBar;
    private String mUrl;
    private RequestManager mRequestManager;

    public ImageViewerLayout(Context context) {
        this(context, null);
    }

    public ImageViewerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_image_viewer, this, true);

        mScaleImageView = findViewById(R.id.scale_image);
        mImageView = findViewById(R.id.glide_image);
        mProgressBar = findViewById(R.id.progressbar);
        mRequestManager = Glide.with(context);

        mProgressBar.setFinishIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_action_play));
        mProgressBar.setFinish();
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    private class ImageViewClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
            ImageViewerLayout lastGifLayout = mCurrentViewHolder != null ? mCurrentViewHolder.get() : null;
            if (lastGifLayout != null) {
                lastGifLayout.stopGif();
            }

            if (!ImageViewerLayout.this.equals(lastGifLayout)) {
                if (imageInfo.isReady() && imageInfo.isGif()) {
                    loadGif();
                }
            }
        }
    }

    private void loadGif() {
        if (GlideHelper.isOkToLoad(getContext())) {
            mCurrentViewHolder = new WeakReference<>(ImageViewerLayout.this);
            mProgressBar.setVisibility(GONE);
            ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
            mRequestManager
                    .asGif()
                    .load(mUrl)
                    .priority(Priority.IMMEDIATE)
                    .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            resource.startFromFirstFrame();
                            return false;
                        }
                    })
                    .into(mImageView);
        }
    }

    public void stopGif() {
        if (mCurrentViewHolder != null)
            mCurrentViewHolder.clear();
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        mProgressBar.setVisibility(VISIBLE);
        mRequestManager.clear(mImageView);
        mRequestManager
                .asBitmap()
                .load(mUrl)
                .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                .transform(new GifTransformation(getContext()))
                .into(mImageView);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);

        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.getStatus() == ImageInfo.SUCCESS || imageInfo.isReady()) {
            loadImage();
        } else if (imageInfo.getStatus() == ImageInfo.FAIL) {
            mImageView.setImageResource(R.drawable.image_broken);
            mProgressBar.setVisibility(View.GONE);
        } else if (imageInfo.getStatus() == ImageInfo.IN_PROGRESS) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setDeterminate();
            mProgressBar.setCurrentProgress(imageInfo.getProgress());
        } else {
            JobMgr.addJob(new GlideImageJob(
                    mUrl,
                    JobMgr.PRIORITY_LOW,
                    String.valueOf(hashCode()),
                    true));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mUrl != null && ImageContainer.getImageInfo(mUrl).isGif();
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);
        mScaleImageView.recycle();
        mRequestManager.clear(mImageView);
        super.onDetachedFromWindow();
    }

    private void loadImage() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.getStatus() == ImageInfo.SUCCESS || imageInfo.isReady()) {
            if (imageInfo.isGif()) {
                setOnClickListener(new ImageViewClickHandler());
                mProgressBar.setFinishIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_action_play));
                mProgressBar.setFinish();
                mProgressBar.setVisibility(VISIBLE);
                mRequestManager
                        .load(mUrl)
                        .dontAnimate()
                        .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mImageView);
            } else {
                mProgressBar.setVisibility(View.GONE);
                displayImage();
            }
        } else {
            mImageView.setImageResource(R.drawable.image_broken);
        }
    }

    private void displayImage() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        mScaleImageView.setMinimumDpi(36);
        mScaleImageView.setMinimumTileDpi(160);
        mScaleImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        mScaleImageView.setImage(ImageSource.uri(imageInfo.getPath()));
        mScaleImageView.setBitmapDecoderFactory(new CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder.class, Utils.getBitmapConfig()));
        mScaleImageView.setRegionDecoderFactory(new CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder.class, Utils.getBitmapConfig()));
        mScaleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity) {
                    Activity activity = (Activity) getContext();
                    activity.finish();
                }
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
                mScaleImageView.setImage(ImageSource.resource(R.drawable.image_broken));
            }
        });
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlideImageEvent event) {
        if (!event.getImageUrl().equals(mUrl))
            return;
        final ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        imageInfo.setMessage(event.getMessage());

        if (event.getStatus() == ImageInfo.SUCCESS
                || imageInfo.getStatus() == ImageInfo.SUCCESS) {
            mProgressBar.setCurrentProgress(100);
            if (!imageInfo.isGif())
                mProgressBar.fadeOut();
            if (GlideHelper.isOkToLoad(getContext()))
                loadImage();
        } else if (event.getStatus() == ImageInfo.IN_PROGRESS) {
            if (mProgressBar.getVisibility() != View.VISIBLE)
                mProgressBar.setVisibility(View.VISIBLE);
            if (event.getProgress() == 0) {
                mProgressBar.setIndeterminate();
            } else if (event.getProgress() > mProgressBar.getCurrentProgress()) {
                if (mProgressBar.getCurrState() != DownloadProgressBar.STATE_DETERMINATE)
                    mProgressBar.setDeterminate();
                mProgressBar.setCurrentProgress(event.getProgress());
            }

            imageInfo.setProgress(event.getProgress());
            imageInfo.setStatus(ImageInfo.IN_PROGRESS);
        } else {
            mProgressBar.fadeOut();
            mImageView.setImageResource(R.drawable.image_broken);
        }
    }
}