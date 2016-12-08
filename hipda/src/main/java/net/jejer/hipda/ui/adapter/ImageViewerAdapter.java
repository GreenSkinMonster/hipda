package net.jejer.hipda.ui.adapter;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.glide.GifTransformation;
import net.jejer.hipda.glide.GlideBitmapTarget;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageEvent;
import net.jejer.hipda.glide.GlideImageView;
import net.jejer.hipda.glide.ThreadImageDecoder;
import net.jejer.hipda.job.GlideImageJob;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.ui.widget.ImageViewerLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * adapter for image gallery
 * Created by GreenSkinMonster on 2015-05-20.
 */
public class ImageViewerAdapter extends PagerAdapter {

    private List<ContentImg> mImages;
    private RequestManager mRequestManager;
    private Activity mActivity;

    private Map<String, ImageViewerLayout> imageViewMap = new HashMap<>();
    private String mSessionId;
    private boolean mFirstShow = true;

    public ImageViewerAdapter(Activity activity, List<ContentImg> images, String sessionId) {
        mActivity = activity;
        mImages = images;
        mSessionId = sessionId;
        mRequestManager = Glide.with(activity);
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        final ImageViewerLayout imageLayout = new ImageViewerLayout(mActivity);
        final ContentImg contentImg = mImages.get(position);
        final String imageUrl = contentImg.getContent();
        ImageInfo imageInfo = ImageContainer.getImageInfo(imageUrl);

        //ScaleImageView has about 100ms delay, so show image with normal ImageView first
        if (mFirstShow) {
            mFirstShow = false;
            if (!imageInfo.isGif() && GlideHelper.isOkToLoad(mActivity)) {
                ImageInfo thumbInfo = ImageContainer.getImageInfo(contentImg.getThumbUrl());
                ImageInfo info = thumbInfo.isReady() ? thumbInfo : imageInfo;
                //load argument must match ThreadDetailFragment to hit memory cache
                if (info.isReady()) {
                    Glide.with(mActivity)
                            .load(info.getUrl())
                            .asBitmap()
                            .cacheDecoder(new FileToStreamDecoder<>(new ThreadImageDecoder(ThreadDetailFragment.mMaxImageDecodeWidth, imageInfo)))
                            .imageDecoder(new ThreadImageDecoder(ThreadDetailFragment.mMaxImageDecodeWidth, imageInfo))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new GlideBitmapTarget(imageLayout.getGlideImageView(), info.getDisplayWidth(), info.getDisplayHeight()));
                }
            }
        }

        if (!imageInfo.isReady() || !(new File(imageInfo.getPath())).exists()) {
            imageLayout.getProgressBar().setVisibility(View.VISIBLE);
            imageLayout.getProgressBar().setIndeterminate(true);
            JobMgr.addJob(new GlideImageJob(imageUrl, JobMgr.PRIORITY_HIGH, mSessionId, true));
        } else {
            displayImage(imageLayout, imageUrl);
        }
        imageLayout.setUrl(imageUrl);
        container.addView(imageLayout);
        imageViewMap.put(imageUrl, imageLayout);
        return imageLayout;
    }

    private void displayImage(final ImageViewerLayout imageLayout, String imageUrl) {
        imageLayout.getProgressBar().setVisibility(View.GONE);

        final SubsamplingScaleImageView scaleImageView = imageLayout.getScaleImageView();
        final GlideImageView glideImageView = imageLayout.getGlideImageView();

        //imageView could be null if display image on GlideImageEvent
        if (scaleImageView == null || glideImageView == null)
            return;

        ImageInfo imageInfo = ImageContainer.getImageInfo(imageUrl);

        if (!imageInfo.isReady()) {
            glideImageView.setVisibility(View.VISIBLE);
            scaleImageView.setVisibility(View.GONE);
            glideImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.image_broken));
        } else {
            if (imageInfo.isGif()) {
                glideImageView.setVisibility(View.VISIBLE);
                scaleImageView.setVisibility(View.GONE);

                if (GlideHelper.isOkToLoad(mActivity)) {
                    Glide.with(mActivity)
                            .load(imageUrl)
                            .asBitmap()
                            .priority(Priority.IMMEDIATE)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transform(new GifTransformation(mActivity))
                            .error(R.drawable.image_broken)
                            .into(new GlideBitmapTarget(glideImageView, imageInfo.getDisplayWidth(), imageInfo.getDisplayHeight()));
                }

                glideImageView.setUrl(imageUrl);
                glideImageView.setSingleClickListener();
            } else {
                scaleImageView.setMinimumDpi(36);
                scaleImageView.setMinimumTileDpi(160);
                scaleImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
                scaleImageView.setImage(ImageSource.uri(imageInfo.getPath()));

                scaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
                    @Override
                    public void onImageLoaded() {
                        Glide.clear(glideImageView);
                        glideImageView.setVisibility(View.GONE);
                        scaleImageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onImageLoadError(Exception e) {
                        Glide.clear(glideImageView);
                        glideImageView.setVisibility(View.GONE);
                        scaleImageView.setImage(ImageSource.resource(R.drawable.image_broken));
                    }
                });
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ImageViewerLayout imageLayout = (ImageViewerLayout) object;
        container.removeView(imageLayout);
        imageViewMap.remove(imageLayout.getUrl());
        imageLayout.recycle();
        imageLayout = null;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlideImageEvent event) {
        ImageViewerLayout imageLayout = imageViewMap.get(event.getImageUrl());
        if (imageLayout != null
                && ViewCompat.isAttachedToWindow(imageLayout)) {
            ProgressBar bar = imageLayout.getProgressBar();
            if (event.isInProgress()) {
                if (bar.getVisibility() != View.VISIBLE)
                    bar.setVisibility(View.VISIBLE);
                if (bar.isIndeterminate())
                    bar.setIndeterminate(false);
                bar.setProgress(event.getProgress());
            } else {
                if (bar.getVisibility() == View.VISIBLE)
                    bar.setVisibility(View.GONE);
                displayImage(imageLayout, event.getImageUrl());
            }
        }
    }

}
