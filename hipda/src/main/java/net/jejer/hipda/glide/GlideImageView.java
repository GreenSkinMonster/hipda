package net.jejer.hipda.glide;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.jejer.hipda.R;
import net.jejer.hipda.ui.ThreadDetailFragment;

public class GlideImageView extends ImageView {

    public static int MIN_SCALE_WIDTH = 600;

    private Context mCtx;
    private ThreadDetailFragment mDetailFragment;
    private String mUrl;
    private ImageReadyInfo mImageReadyInfo;
    private int mImageIndex;

    private static ImageView currentImageView;
    private static String currentUrl;

    public GlideImageView(Context context, ThreadDetailFragment detailFragment) {
        super(context);
        mCtx = context;
        mDetailFragment = detailFragment;

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

    public void setClickToViewBigImage() {
        setClickable(true);
        setOnClickListener(new GlideImageViewClickHandler());
    }

    private class GlideImageViewClickHandler implements OnClickListener {
        @Override
        public void onClick(View view) {
            if (mImageReadyInfo != null && mImageReadyInfo.isReady()) {
                if (mUrl.equals(currentUrl)) {
                    stopCurrentGif();
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
        mDetailFragment.startImageGallery(mImageIndex);
    }

    private void loadGif() {
        currentUrl = mUrl;
        currentImageView = this;
        Glide.with(mCtx)
                .load(mUrl)
                .priority(Priority.IMMEDIATE)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .skipMemoryCache(true)
                .error(R.drawable.tapatalk_image_broken)
                .override(mImageReadyInfo.getWidth(), mImageReadyInfo.getHeight())
                .into(this);
    }

    private void stopCurrentGif() {
        try {
            if (currentImageView != null) {
                Glide.with(mCtx)
                        .load(currentUrl)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .transform(new GifTransformation(mCtx))
                        .error(R.drawable.tapatalk_image_broken)
                        .override(mImageReadyInfo.getWidth(), mImageReadyInfo.getHeight())
                        .into(currentImageView);
            }
        } catch (Exception ignored) {
        }
        currentUrl = null;
        currentImageView = null;
    }

}
