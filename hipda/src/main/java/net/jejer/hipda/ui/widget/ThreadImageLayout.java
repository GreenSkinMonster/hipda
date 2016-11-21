package net.jejer.hipda.ui.widget;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
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
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Layout contains thread image
 * Created by GreenSkinMonster on 2015-11-07.
 */
public class ThreadImageLayout extends RelativeLayout {

    private static final int MIN_WIDTH = 120;

    private GlideImageView mImageView;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private String mUrl;
    private long mParsedFileSize;
    private RequestManager mRequestManager;
    private String mParentSessionId;
    private int mImageIndex;
    private ThreadDetailFragment mFragment;

    public ThreadImageLayout(ThreadDetailFragment fragment, String url) {
        super(fragment.getActivity(), null);

        LayoutInflater.from(fragment.getActivity()).inflate(R.layout.layout_thread_image, this, true);

        mFragment = fragment;
        mImageView = (GlideImageView) findViewById(R.id.thread_image);
        mProgressBar = (ProgressBar) findViewById(R.id.thread_image_progress);
        mTextView = (TextView) findViewById(R.id.thread_image_info);
        mRequestManager = Glide.with(mFragment);
        mUrl = url;

        ImageInfo imageInfo = ImageContainer.getImageInfo(url);
        if (!imageInfo.isReady()) {
            mImageView.setImageDrawable(ContextCompat.getDrawable(mFragment.getActivity(), R.drawable.ic_action_image));
        }
        mImageView.setFragment(mFragment);
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageIndex(mImageIndex);
        mImageView.setUrl(mUrl);
    }

    public void setParsedFileSize(long parsedFileSize) {
        mParsedFileSize = parsedFileSize;
    }

    public void setParentSessionId(String parentSessionId) {
        mParentSessionId = parentSessionId;
    }

    public void setImageIndex(int imageIndex) {
        mImageIndex = imageIndex;
        mImageView.setImageIndex(mImageIndex);
    }

    private void loadImage() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.getStatus() == ImageInfo.SUCCESS) {
            if (getLayoutParams().height != imageInfo.getDisplayHeight()) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageInfo.getDisplayHeight());
                setLayoutParams(params);
            }
            if (imageInfo.getWidth() >= MIN_WIDTH || imageInfo.isGif()) {
                mImageView.setImageInfo(imageInfo);
                mImageView.setClickToViewBigImage();
            }

            if (imageInfo.isGif()) {
                mRequestManager
                        .load(mUrl)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new GifTransformation(getContext()))
                        .into(new GlideBitmapTarget(mImageView, imageInfo.getDisplayWidth(), imageInfo.getDisplayHeight()));
            } else {
                mRequestManager
                        .load(mUrl)
                        .asBitmap()
                        .cacheDecoder(new FileToStreamDecoder<>(new ThreadImageDecoder(ThreadDetailFragment.mMaxImageDecodeWidth, imageInfo)))
                        .imageDecoder(new ThreadImageDecoder(ThreadDetailFragment.mMaxImageDecodeWidth, imageInfo))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new GlideBitmapTarget(mImageView, imageInfo.getDisplayWidth(), imageInfo.getDisplayHeight()));
            }
        } else {
            mImageView.setImageResource(R.drawable.image_broken);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.getStatus() == ImageInfo.SUCCESS) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageInfo.getHeight());
            setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.dpToPx(getContext(), 150));
            setLayoutParams(params);
        }
        if (imageInfo.getStatus() == ImageInfo.SUCCESS) {
            loadImage();
        } else if (imageInfo.getStatus() == ImageInfo.FAIL) {
            mImageView.setImageResource(R.drawable.image_broken);
        } else if (imageInfo.getStatus() == ImageInfo.IN_PROGRESS) {
            if (mProgressBar.getVisibility() != View.VISIBLE)
                mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(imageInfo.getProgress());
        } else {
            boolean imageLoadable = HiSettingsHelper.getInstance().isImageLoadable(mParsedFileSize);
            if (mParsedFileSize > 0) {
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText(Utils.toSizeText(mParsedFileSize));
            }
            if (!imageLoadable) {
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        JobMgr.addJob(new GlideImageJob(mRequestManager, mUrl, JobMgr.PRIORITY_LOW, mParentSessionId, true));
                        mImageView.setOnClickListener(null);
                    }
                });
            }
            JobMgr.addJob(new GlideImageJob(
                    mRequestManager,
                    mUrl,
                    JobMgr.PRIORITY_LOW,
                    mParentSessionId,
                    imageLoadable));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlideImageEvent event) {
        if (!event.getImageUrl().equals(mUrl))
            return;
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (event.getStatus() == ImageInfo.IN_PROGRESS) {
            if (mProgressBar.getVisibility() != View.VISIBLE)
                mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(event.getProgress());
            imageInfo.setProgress(event.getProgress());
            imageInfo.setStatus(ImageInfo.IN_PROGRESS);
        } else if (event.getStatus() == ImageInfo.SUCCESS) {
            if (mProgressBar.getVisibility() == View.VISIBLE)
                mProgressBar.setVisibility(View.GONE);
            if (mTextView.getVisibility() == View.VISIBLE)
                mTextView.setVisibility(View.GONE);
            if (GlideHelper.isOkToLoad(mFragment))
                loadImage();
        } else {
            mProgressBar.setVisibility(GONE);
            mImageView.setImageResource(R.drawable.image_broken);
        }
    }

}
