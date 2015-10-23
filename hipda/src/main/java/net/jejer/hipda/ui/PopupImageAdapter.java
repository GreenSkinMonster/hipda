package net.jejer.hipda.ui;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.glide.GlideImageEvent;
import net.jejer.hipda.glide.GlideImageJob;
import net.jejer.hipda.glide.GlideImageManager;
import net.jejer.hipda.glide.ImageReadyInfo;
import net.jejer.hipda.utils.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * adapter for image gallery
 * Created by GreenSkinMonster on 2015-05-20.
 */
public class PopupImageAdapter extends PagerAdapter {

    private Context mCtx;
    private List<ContentImg> mImages;
    private LayoutInflater mInflater;

    private Map<String, View> imageViewMap = new HashMap<>();
    private String mSessionId;

    public PopupImageAdapter(Context context, List<ContentImg> images, String sessionId) {
        mCtx = context;
        mImages = images;
        mSessionId = sessionId;
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

        if (mInflater == null)
            mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rootView = mInflater.inflate(R.layout.popup_image_page, container, false);
        ContentLoadingProgressBar progressBar = (ContentLoadingProgressBar) rootView.findViewById(R.id.loadingPanel);
        progressBar.show();

        final String imageUrl = mImages.get(position).getContent();
        ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(imageUrl);

        if (imageReadyInfo == null || !(new File(imageReadyInfo.getPath())).exists()) {
            GlideImageManager.addJob(new GlideImageJob(mCtx, imageUrl, GlideImageManager.PRIORITY_HIGH, mSessionId));
        } else {
            displayImage(rootView, imageReadyInfo);
        }
        container.addView(rootView);
        imageViewMap.put(imageUrl, rootView);
        return rootView;
    }

    private void displayImage(View rootView, ImageReadyInfo imageReadyInfo) {

        final SubsamplingScaleImageView wvImage = (SubsamplingScaleImageView) rootView.findViewById(R.id.wv_image);
        final ContentLoadingProgressBar progressBar = (ContentLoadingProgressBar) rootView.findViewById(R.id.loadingPanel);
        final ImageView gifImageView = (ImageView) rootView.findViewById(R.id.gif_image);

        //imageView could be null if display image on GlideImageEvent
        if (wvImage == null || gifImageView == null)
            return;

        gifImageView.setBackgroundColor(mCtx.getResources().getColor(R.color.night_background));
        wvImage.setBackgroundColor(mCtx.getResources().getColor(R.color.night_background));
        progressBar.show();

        if (imageReadyInfo == null) {
            gifImageView.setVisibility(View.VISIBLE);
            wvImage.setVisibility(View.GONE);
            progressBar.hide();
            gifImageView.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.image_broken));
        } else {
            if (imageReadyInfo.isGif()) {
                gifImageView.setVisibility(View.VISIBLE);
                wvImage.setVisibility(View.GONE);
                progressBar.hide();
                Glide.with(mCtx)
                        .load(imageReadyInfo.getPath())
                        .asGif()
                        .priority(Priority.IMMEDIATE)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.image_broken)
                        .into(gifImageView);
            } else {
                gifImageView.setVisibility(View.GONE);
                wvImage.setVisibility(View.VISIBLE);

                wvImage.setMinimumDpi(100);
                wvImage.setMinimumTileDpi(160);
                wvImage.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
                wvImage.setImage(ImageSource.uri(imageReadyInfo.getPath()));

                wvImage.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
                    @Override
                    public void onImageLoaded() {
                        progressBar.hide();
                    }

                    @Override
                    public void onImageLoadError(Exception e) {
                        progressBar.hide();
                        wvImage.setImage(ImageSource.resource(R.drawable.image_broken));
//                        Toast.makeText(mCtx, "图片加载失败", Toast.LENGTH_LONG).show();
                        Logger.e("loading error", e);
                    }
                });
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(GlideImageEvent event) {
        if (event.isInProgress())
            return;
        ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(event.getImageUrl());
        View rootView = imageViewMap.get(event.getImageUrl());
        if (rootView != null
                && (Build.VERSION.SDK_INT < 19 ||
                (Build.VERSION.SDK_INT >= 19 && rootView.isAttachedToWindow())))
            displayImage(rootView, imageReadyInfo);
    }

}
