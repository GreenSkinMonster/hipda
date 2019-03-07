package net.jejer.hipda.ui.widget;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.glide.GifTransformation;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageEvent;
import net.jejer.hipda.job.GlideImageJob;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.ui.ImageViewerActivity;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

/**
 * Created by GreenSkinMonster on 2015-11-07.
 */
public class ThreadImageLayout extends RelativeLayout {

    private static final int MIN_WIDTH = 120;

    private static WeakReference<ThreadImageLayout> mCurrentViewHolder;

    private Activity mActivity;
    private ImageView mImageView;
    private DownloadProgressBar mProgressBar;
    private TextView mTextView;
    private String mUrl;
    private boolean mIsThumb;
    private long mParsedFileSize;
    private RequestManager mRequestManager;
    private int mImageIndex;
    private ContentImg mContentImg;
    private ArrayList<ContentImg> mImages;

    public ThreadImageLayout(Activity activity, ContentImg contentImg, ArrayList<ContentImg> images) {
        super(activity, null);
        LayoutInflater.from(activity).inflate(R.layout.layout_thread_image, this, true);

        mImageView = findViewById(R.id.thread_image);
        mProgressBar = findViewById(R.id.thread_image_progress);
        mTextView = findViewById(R.id.thread_image_info);

        mActivity = activity;
        mRequestManager = Glide.with(mActivity);
        mContentImg = contentImg;
        mImages = images;

        String policy = HiSettingsHelper.getInstance().getCurrectImagePolicy();
        String thumbUrl = mContentImg.getThumbUrl();
        String url = mContentImg.getContent();
        if (HiSettingsHelper.IMAGE_POLICY_ORIGINAL.equals(policy)
                || TextUtils.isEmpty(thumbUrl)
                || url.equals(thumbUrl)
                || ImageContainer.getImageInfo(url).isSuccess()) {
            mUrl = url;
            mIsThumb = false;
        } else {
            mUrl = thumbUrl;
            mIsThumb = true;
        }

        mImageIndex = contentImg.getIndexInPage();
        mParsedFileSize = contentImg.getFileSize();

        mImageView.setVisibility(View.VISIBLE);

        setOnClickListener(new ImageViewClickHandler());
    }

    private void loadImage() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.isSuccess()) {
            mTextView.setVisibility(GONE);
            if (getLayoutParams().height != imageInfo.getViewHeight()) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageInfo.getViewHeight());
                setLayoutParams(params);
            }
            if (imageInfo.getWidth() >= MIN_WIDTH || imageInfo.isGif()) {
                setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showImageActionDialog();
                        return true;
                    }
                });
            }

            if (imageInfo.isGif()) {
                mProgressBar.setFinish();
                mRequestManager
                        .asBitmap()
                        .load(mUrl)
                        .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                        .transform(new GifTransformation(getContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mImageView);
            } else {
                mProgressBar.setVisibility(View.GONE);
                mRequestManager
                        .asBitmap()
                        .load(mUrl)
                        .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mImageView);
            }
        } else {
            mProgressBar.setError();
        }
    }

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
            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageInfo.getViewHeight());

            setLayoutParams(params);
        } else {
            int margin = Utils.dpToPx(getContext(), 1);
            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.dpToPx(getContext(), 150));
            params.setMargins(0, margin, 0, margin);
            setLayoutParams(params);
            if (mParsedFileSize > 0 && mTextView.getVisibility() != VISIBLE) {
                mTextView.setVisibility(View.VISIBLE);
                if (mIsThumb)
                    mTextView.setText(Utils.toSizeText(mParsedFileSize) + "↑");
                else
                    mTextView.setText(Utils.toSizeText(mParsedFileSize));
            }
        }
        if (imageInfo.isSuccess()) {
            loadImage();
        } else if (imageInfo.isFail()) {
            mProgressBar.setError();
            mProgressBar.setVisibility(View.VISIBLE);
        } else if (imageInfo.isInProgress()) {
            mProgressBar.setDeterminate();
            mProgressBar.setCurrentProgress(imageInfo.getProgress());
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            boolean autoload = HiSettingsHelper.getInstance().isImageLoadable(mParsedFileSize, mIsThumb);
            JobMgr.addJob(new GlideImageJob(
                    mUrl,
                    JobMgr.PRIORITY_LOW,
                    String.valueOf(hashCode()),
                    autoload));
        }
    }

    private void showImageActionDialog() {
        SimplePopupMenu popupMenu = new SimplePopupMenu(getContext());
        popupMenu.add("save", getResources().getString(R.string.action_save),
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        UIUtils.saveImage(mActivity, UIUtils.getSnackView(mActivity), mContentImg.getContent());
                    }
                });
        popupMenu.add("share", getResources().getString(R.string.action_share),
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        UIUtils.shareImage(mActivity, UIUtils.getSnackView(mActivity), mUrl);
                    }
                });
        popupMenu.add("gallery", getResources().getString(R.string.action_image_gallery),
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        stopGif();
                        startImageGallery();
                    }
                });
        popupMenu.show();
    }

    private class ImageViewClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
            ThreadImageLayout lastGifLayout = mCurrentViewHolder != null ? mCurrentViewHolder.get() : null;
            if (lastGifLayout != null) {
                lastGifLayout.stopGif();
            }

            if (!ThreadImageLayout.this.equals(lastGifLayout)) {
                if (imageInfo.isSuccess()) {
                    if (imageInfo.isGif()) {
                        loadGif();
                    } else {
                        startImageGallery();
                    }
                } else if (imageInfo.isFail() || imageInfo.isIdle()) {
                    JobMgr.addJob(new GlideImageJob(mUrl, JobMgr.PRIORITY_LOW, String.valueOf(hashCode()), true));
                }
            }
        }
    }

    private void startImageGallery() {
        if (mImages.size() > 0) {
            Intent intent = new Intent(getContext(), ImageViewerActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeScaleUpAnimation(mImageView, 0, 0, mImageView.getMeasuredWidth(), mImageView.getMeasuredHeight());
            intent.putExtra(ImageViewerActivity.KEY_IMAGE_INDEX, mImageIndex);
            intent.putParcelableArrayListExtra(ImageViewerActivity.KEY_IMAGES, mImages);
            ActivityCompat.startActivity(getContext(), intent, options.toBundle());
        }
    }

    private void loadGif() {
        if (GlideHelper.isOkToLoad(getContext())) {
            mCurrentViewHolder = new WeakReference<>(ThreadImageLayout.this);
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
        mProgressBar.setVisibility(VISIBLE);
        mRequestManager.clear(mImageView);
        mRequestManager
                .asBitmap()
                .load(mUrl)
                .transform(new GifTransformation(getContext()))
                .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                .into(mImageView);
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
        if (!event.getImageUrl().equals(mUrl))
            return;
        final ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        imageInfo.setMessage(event.getMessage());

        if (event.getStatus() == ImageInfo.SUCCESS
                || imageInfo.isSuccess()) {
            mProgressBar.setCurrentProgress(100);
            if (!imageInfo.isGif())
                mProgressBar.setVisibility(GONE);
            if (mTextView.getVisibility() == View.VISIBLE)
                mTextView.setVisibility(View.GONE);
            if (GlideHelper.isOkToLoad(getContext()))
                loadImage();
        } else if (event.getStatus() == ImageInfo.IN_PROGRESS) {
            if (event.getProgress() == 0) {
                mProgressBar.setIndeterminate();
            } else if (event.getProgress() > mProgressBar.getCurrentProgress()) {
                if (mProgressBar.getCurrState() != DownloadProgressBar.STATE_DETERMINATE)
                    mProgressBar.setDeterminate();
                mProgressBar.setCurrentProgress(event.getProgress());
            }
            if (mProgressBar.getVisibility() != View.VISIBLE)
                mProgressBar.setVisibility(View.VISIBLE);
            
            imageInfo.setProgress(event.getProgress());
            imageInfo.setStatus(ImageInfo.IN_PROGRESS);
        } else {
            mProgressBar.setError();
            if (mProgressBar.getVisibility() != View.VISIBLE)
                mProgressBar.setVisibility(View.VISIBLE);
        }
    }

}
