package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.glide.GifTransformation;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageEvent;
import net.jejer.hipda.job.GlideImageJob;
import net.jejer.hipda.job.JobMgr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;

/**
 * Created by GreenSkinMonster on 2019-03-07.
 */
public abstract class BaseImageLayout extends RelativeLayout {

    private static WeakReference<BaseImageLayout> mCurrentViewHolder;

    protected RequestManager mRequestManager;

    protected ImageView mImageView;
    protected DownloadProgressBar mProgressBar;
    protected String mJobId;
    protected String mUrl;
    protected ContentImg mContentImg;

    public BaseImageLayout(Context context) {
        super(context);
    }

    protected abstract boolean isNetworkFetch();

    protected abstract void displayImage();

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        EventBus.getDefault().register(this);

        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);

        if (imageInfo.isSuccess()) {
            displayImage();
        } else if (imageInfo.isFail()) {
            onError();
        } else if (imageInfo.isInProgress() && mJobId != null) {
            onProgress(imageInfo);
        } else {
            if (mProgressBar.getVisibility() != View.VISIBLE)
                mProgressBar.setVisibility(View.VISIBLE);
            fetchImage(isNetworkFetch());
        }
    }

    protected void fetchImage(boolean networkFetch) {
        if (networkFetch) {
            if (mProgressBar.getCurrState() != DownloadProgressBar.STATE_INDETERMINATE)
                mProgressBar.setIndeterminate();
            if (mProgressBar.getVisibility() != View.VISIBLE)
                mProgressBar.setVisibility(View.VISIBLE);
        }
        GlideImageJob job = new GlideImageJob(
                mUrl,
                JobMgr.PRIORITY_LOW,
                String.valueOf(hashCode()),
                networkFetch);
        mJobId = job.getId();
        JobMgr.addJob(job);
    }

    private void onProgress(ImageInfo imageInfo) {
        if (mProgressBar.getCurrState() != DownloadProgressBar.STATE_DETERMINATE)
            mProgressBar.setDeterminate();
        mProgressBar.setCurrentProgress(imageInfo.getProgress());
        if (mProgressBar.getVisibility() != View.VISIBLE)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void onError() {
        mProgressBar.setError();
        if (mProgressBar.getVisibility() != View.VISIBLE)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void loadGif() {
        if (GlideHelper.isOkToLoad(getContext())) {
            mCurrentViewHolder = new WeakReference<>(BaseImageLayout.this);
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
        if (imageInfo.isGif()) {
            mProgressBar.setVisibility(VISIBLE);
            mRequestManager.clear(mImageView);
            mRequestManager
                    .asBitmap()
                    .load(mUrl)
                    .transform(new GifTransformation())
                    .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                    .into(mImageView);
        }
    }

    protected void playGif() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        BaseImageLayout lastGifLayout = mCurrentViewHolder != null ? mCurrentViewHolder.get() : null;
        if (lastGifLayout != null) {
            lastGifLayout.stopGif();
        }

        if (!BaseImageLayout.this.equals(lastGifLayout)) {
            if (imageInfo.isSuccess()) {
                if (imageInfo.isGif()) {
                    loadGif();
                }
            } else if (imageInfo.isFail() || imageInfo.isIdle()) {
                fetchImage(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);
        mRequestManager.clear(mImageView);
        super.onDetachedFromWindow();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlideImageEvent event) {
        if (event.getStatus() == ImageInfo.SUCCESS
                && event.getImageUrl().equals(mContentImg.getContent())
                && !mUrl.equals(mContentImg.getContent())) {
            mUrl = event.getImageUrl();
            displayImage();
            return;
        }
        if (!event.getImageUrl().equals(mUrl))
            return;
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);

        if (event.getStatus() == ImageInfo.SUCCESS || imageInfo.isSuccess()) {
            displayImage();
        } else if (event.getStatus() == ImageInfo.IN_PROGRESS) {
            if (event.getProgress() > imageInfo.getProgress()) {
                imageInfo.setProgress(event.getProgress());
                imageInfo.setStatus(ImageInfo.IN_PROGRESS);
                onProgress(imageInfo);
            }
        } else if (event.getStatus() == ImageInfo.FAIL) {
            onError();
        }
    }

}
