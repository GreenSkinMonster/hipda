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
import net.jejer.hipda.ui.OnSingleClickListener;
import net.jejer.hipda.ui.ThreadDetailFragment;

public class GlideImageView extends ImageView {

    public static int MIN_SCALE_WIDTH = 600;

    private Fragment mFragment;
    private String mUrl;
    private ImageReadyInfo mImageReadyInfo;
    private int mImageIndex;

    private static ImageView currentImageView;
    private static String currentUrl;

    public GlideImageView(Context context) {
        super(context);

        setupListeners();
    }

    public GlideImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setupListeners();
    }

    private void setupListeners() {
        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                //clear Glide request reference
                Glide.clear(view);
                view.setTag(null);
            }
        });
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setImageReadyInfo(ImageReadyInfo imageInfo) {
        mImageReadyInfo = imageInfo;
    }

    public void setImageIndex(int index) {
        mImageIndex = index;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    public void setClickToViewBigImage() {
        setClickable(true);
        setOnClickListener(new GlideImageViewClickHandler());
    }

    private class GlideImageViewClickHandler extends OnSingleClickListener {
        @Override
        public void onSingleClick(View view) {
            if (mImageReadyInfo != null && mImageReadyInfo.isReady()) {
                if (mUrl.equals(currentUrl)) {
                    boolean sameView = view.equals(currentImageView);
                    stopCurrentGif();
                    if (!sameView)
                        loadGif();
                } else if (mImageReadyInfo.isGif()) {
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
                    .override(mImageReadyInfo.getDisplayWidth(), mImageReadyInfo.getDisplayHeight())
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
                            .override(mImageReadyInfo.getDisplayWidth(), mImageReadyInfo.getDisplayHeight())
                            .into(currentImageView);
                }
            }
        } catch (Exception ignored) {
        }
        currentUrl = null;
        currentImageView = null;
    }

}
