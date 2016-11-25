package net.jejer.hipda.ui;

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
import net.jejer.hipda.job.GlideImageJob;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.utils.Logger;

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
public class PopupImageAdapter extends PagerAdapter {

    private PopupImageDialog mDialog;
    private List<ContentImg> mImages;
    private RequestManager mRequestManager;

    private Map<String, PopupImageLayout> imageViewMap = new HashMap<>();
    private String mSessionId;

    public PopupImageAdapter(PopupImageDialog dialog, List<ContentImg> images, String sessionId) {
        mDialog = dialog;
        mImages = images;
        mSessionId = sessionId;
        mRequestManager = Glide.with(dialog);
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

        final PopupImageLayout imageLayout = new PopupImageLayout(mDialog.getActivity());
        final String imageUrl = mImages.get(position).getContent();
        ImageInfo imageInfo = ImageContainer.getImageInfo(imageUrl);

        if (!imageInfo.isReady() || !(new File(imageInfo.getPath())).exists()) {
            imageLayout.getProgressBar().setVisibility(View.VISIBLE);
            imageLayout.getProgressBar().setIndeterminate(true);
            JobMgr.addJob(new GlideImageJob(mRequestManager, imageUrl, JobMgr.PRIORITY_HIGH, mSessionId, true));
        } else {
            displayImage(imageLayout, imageUrl);
        }
        imageLayout.setUrl(imageUrl);
        container.addView(imageLayout);
        imageViewMap.put(imageUrl, imageLayout);
        return imageLayout;
    }

    private void displayImage(final PopupImageLayout imageLayout, String imageUrl) {

        imageLayout.getProgressBar().setVisibility(View.GONE);

        final SubsamplingScaleImageView scaleImageView = imageLayout.getScaleImageView();
        final GlideImageView gifImageView = imageLayout.getGlideImageView();

        //imageView could be null if display image on GlideImageEvent
        if (scaleImageView == null || gifImageView == null)
            return;

        ImageInfo imageInfo = ImageContainer.getImageInfo(imageUrl);

        gifImageView.setBackgroundColor(ContextCompat.getColor(mDialog.getActivity(), R.color.night_background));
        scaleImageView.setBackgroundColor(ContextCompat.getColor(mDialog.getActivity(), R.color.night_background));

        if (!imageInfo.isReady()) {
            gifImageView.setVisibility(View.VISIBLE);
            scaleImageView.setVisibility(View.GONE);
            gifImageView.setImageDrawable(ContextCompat.getDrawable(mDialog.getActivity(), R.drawable.image_broken));
        } else {
            if (imageInfo.isGif()) {
                gifImageView.setVisibility(View.VISIBLE);
                scaleImageView.setVisibility(View.GONE);

                if (GlideHelper.isOkToLoad(mDialog)) {
                    Glide.with(mDialog)
                            .load(imageUrl)
                            .asBitmap()
                            .priority(Priority.IMMEDIATE)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transform(new GifTransformation(mDialog.getActivity()))
                            .error(R.drawable.image_broken)
                            .into(new GlideBitmapTarget(gifImageView, imageInfo.getDisplayWidth(), imageInfo.getDisplayHeight()));
                }

                gifImageView.setUrl(imageUrl);
                gifImageView.setSingleClickListener();

            } else {
                gifImageView.setVisibility(View.GONE);
                scaleImageView.setVisibility(View.VISIBLE);

                scaleImageView.setMinimumDpi(100);
                scaleImageView.setMinimumTileDpi(160);
                scaleImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
                scaleImageView.setImage(ImageSource.uri(imageInfo.getPath()));

                scaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
                    @Override
                    public void onImageLoaded() {
                    }

                    @Override
                    public void onImageLoadError(Exception e) {
                        scaleImageView.setImage(ImageSource.resource(R.drawable.image_broken));
                        Logger.e("loading error", e);
                    }
                });
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        PopupImageLayout imageLayout = (PopupImageLayout) object;
        container.removeView(imageLayout);
        imageViewMap.remove(imageLayout.getUrl());
        imageLayout.recycle();
        imageLayout = null;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlideImageEvent event) {
        PopupImageLayout imageLayout = imageViewMap.get(event.getImageUrl());
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
