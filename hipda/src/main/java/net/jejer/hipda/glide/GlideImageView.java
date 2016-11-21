package net.jejer.hipda.glide;

import android.app.Fragment;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.ui.OnSingleClickListener;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.utils.HttpUtils;

public class GlideImageView extends ImageView {

    private Fragment mFragment;
    private String mUrl;
    private ImageInfo mImageInfo;
    private int mImageIndex;

    private static ImageView currentImageView;
    private static String currentUrl;

    public GlideImageView(Context context) {
        super(context);
    }

    public GlideImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setImageInfo(ImageInfo imageInfo) {
        mImageInfo = imageInfo;
    }

    public void setImageIndex(int index) {
        mImageIndex = index;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    public void setClickToViewBigImage() {
        setOnClickListener(new GlideImageViewClickHandler());
        if (HiSettingsHelper.getInstance().getBooleanValue(HiSettingsHelper.PERF_LONG_CLICK_SAVE_IMAGE, false)) {
            setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    HttpUtils.saveImage(getContext(), mUrl);
                    return true;
                }
            });
        }
    }

    private class GlideImageViewClickHandler extends OnSingleClickListener {
        @Override
        public void onSingleClick(View view) {
            if (mImageInfo != null && mImageInfo.isReady()) {
                if (mUrl.equals(currentUrl)) {
                    boolean sameView = view.equals(currentImageView);
                    stopCurrentGif();
                    if (!sameView)
                        loadGif();
                } else if (mImageInfo.isGif()) {
                    stopCurrentGif();
                    loadGif();
                } else {
                    stopCurrentGif();
                    startImageGallery();
                }
            }
        }
    }

    private void startImageGallery() {
        if (mFragment != null && mFragment instanceof ThreadDetailFragment)
            ((ThreadDetailFragment) mFragment).startImageGallery(mImageIndex);
    }

    private void loadGif() {
        currentUrl = mUrl;
        currentImageView = this;
        Glide.clear(this);
        if (GlideHelper.isOkToLoad(getContext())) {
            Glide.with(getContext())
                    .load(mUrl)
                    .priority(Priority.IMMEDIATE)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .skipMemoryCache(true)
                    .error(R.drawable.image_broken)
                    .override(mImageInfo.getDisplayWidth(), mImageInfo.getDisplayHeight())
                    .into(this);
        }
    }

    private void stopCurrentGif() {
        try {
            if (currentImageView != null) {
                Glide.clear(currentImageView);
                if (GlideHelper.isOkToLoad(getContext())) {
                    Glide.with(getContext())
                            .load(currentUrl)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .transform(new GifTransformation(getContext()))
                            .error(R.drawable.image_broken)
                            .override(mImageInfo.getDisplayWidth(), mImageInfo.getDisplayHeight())
                            .into(currentImageView);
                }
            }
        } catch (Exception ignored) {
        }
        currentUrl = null;
        currentImageView = null;
    }

}
