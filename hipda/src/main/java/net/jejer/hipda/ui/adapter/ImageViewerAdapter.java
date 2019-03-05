package net.jejer.hipda.ui.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.ui.widget.ImageViewerLayout;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

/**
 * adapter for image gallery
 * Created by GreenSkinMonster on 2015-05-20.
 */
public class ImageViewerAdapter extends PagerAdapter {

    private List<ContentImg> mImages;
    private Activity mActivity;

    private boolean mFirstShow = true;

    public ImageViewerAdapter(Activity activity, List<ContentImg> images) {
        mActivity = activity;
        mImages = images;
    }

    @Override
    public int getCount() {
        return mImages != null ? mImages.size() : 0;
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
                            .asBitmap()
                            .load(info.getUrl())
                            .override(info.getBitmapWidth(), info.getBitmapHeight())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageLayout.getImageView());
                }
            }
        }

        imageLayout.setUrl(imageUrl);
        container.addView(imageLayout);
        return imageLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ImageViewerLayout imageLayout = (ImageViewerLayout) object;
        container.removeView(imageLayout);
    }

}
