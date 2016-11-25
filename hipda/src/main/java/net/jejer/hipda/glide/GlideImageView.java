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
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.job.GlideImageJob;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.ui.OnSingleClickListener;
import net.jejer.hipda.ui.ThreadDetailFragment;

import java.lang.ref.WeakReference;

public class GlideImageView extends ImageView {

    private Fragment mFragment;
    private String mUrl;
    private int mImageIndex;

    private static WeakReference<ImageView> currentImageView;
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

    public void setImageIndex(int index) {
        mImageIndex = index;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    public void setSingleClickListener() {
        setOnClickListener(new GlideImageViewClickHandler());
    }

    private class GlideImageViewClickHandler extends OnSingleClickListener {
        @Override
        public void onSingleClick(View view) {
            ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
            if (imageInfo.isReady()) {
                if (mUrl.equals(currentUrl)) {
                    boolean sameView = view.equals(currentImageView);
                    stopCurrentGif();
                    if (!sameView)
                        loadGif();
                } else if (imageInfo.isGif()) {
                    stopCurrentGif();
                    loadGif();
                } else {
                    stopCurrentGif();
                    startImageGallery();
                }
            } else if (imageInfo.getStatus() == ImageInfo.FAIL || imageInfo.getStatus() == ImageInfo.IDLE) {
                if (mFragment instanceof ThreadDetailFragment)
                    JobMgr.addJob(new GlideImageJob(Glide.with(mFragment), mUrl, JobMgr.PRIORITY_LOW, ((ThreadDetailFragment) mFragment).mSessionId, true));
            }
        }
    }

    private void startImageGallery() {
        if (mFragment != null && mFragment instanceof ThreadDetailFragment)
            ((ThreadDetailFragment) mFragment).startImageGallery(mImageIndex);
    }

    private void loadGif() {
        currentUrl = mUrl;
        currentImageView = new WeakReference<ImageView>(this);
        Glide.clear(this);
        if (GlideHelper.isOkToLoad(getContext())) {
            ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
            Glide.with(getContext())
                    .load(mUrl)
                    .priority(Priority.IMMEDIATE)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .skipMemoryCache(true)
                    .error(R.drawable.image_broken)
                    .override(imageInfo.getDisplayWidth(), imageInfo.getDisplayHeight())
                    .into(this);
        }
    }

    private void stopCurrentGif() {
        try {
            if (currentImageView != null && currentImageView.get() != null) {
                ImageView lastView = currentImageView.get();
                Glide.clear(currentImageView.get());
                if (GlideHelper.isOkToLoad(getContext())) {
                    ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
                    Glide.with(getContext())
                            .load(currentUrl)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .transform(new GifTransformation(getContext()))
                            .error(R.drawable.image_broken)
                            .override(imageInfo.getDisplayWidth(), imageInfo.getDisplayHeight())
                            .into(lastView);
                }
            }
        } catch (Exception ignored) {
        }
        currentUrl = null;
        currentImageView = null;
    }

}
