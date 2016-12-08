package net.jejer.hipda.ui.widget;

import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
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
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedHashMap;

/**
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
    private ContentImg mContentImg;

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
        mImageView.setSingleClickListener();
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

    public void setContentImg(ContentImg contentImg) {
        mContentImg = contentImg;
    }

    private void loadImage() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        mProgressBar.setVisibility(View.GONE);
        if (imageInfo.getStatus() == ImageInfo.SUCCESS) {
            mTextView.setVisibility(GONE);
            if (getLayoutParams().height != imageInfo.getDisplayHeight()) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageInfo.getDisplayHeight());
                setLayoutParams(params);
            }
            if (imageInfo.getWidth() >= MIN_WIDTH || imageInfo.isGif()) {
                mImageView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showImageActionDialog();
                        return true;
                    }
                });
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

        EventBus.getDefault().register(this);

        boolean isThumb = !TextUtils.isEmpty(mContentImg.getThumbUrl())
                && !mContentImg.getThumbUrl().equals(mContentImg.getContent());

        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.getStatus() == ImageInfo.SUCCESS) {
            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageInfo.getHeight());
            setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.dpToPx(getContext(), 150));
            setLayoutParams(params);
            if (mParsedFileSize > 0 && mTextView.getVisibility() != VISIBLE) {
                mTextView.setVisibility(View.VISIBLE);
                if (isThumb)
                    mTextView.setText(Utils.toSizeText(mParsedFileSize) + "↑");
                else
                    mTextView.setText(Utils.toSizeText(mParsedFileSize));
            }
        }
        if (imageInfo.getStatus() == ImageInfo.SUCCESS) {
            loadImage();
        } else if (imageInfo.getStatus() == ImageInfo.FAIL) {
            mImageView.setImageResource(R.drawable.image_broken);
            mProgressBar.setVisibility(View.GONE);
        } else if (imageInfo.getStatus() == ImageInfo.IN_PROGRESS) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(imageInfo.getProgress());
        } else {
            boolean autoload = HiSettingsHelper.getInstance().isImageLoadable(mParsedFileSize, isThumb);
            JobMgr.addJob(new GlideImageJob(
                    mUrl,
                    JobMgr.PRIORITY_LOW,
                    mParentSessionId,
                    autoload));
        }
    }

    private void showImageActionDialog() {
        LinkedHashMap<String, String> actions = new LinkedHashMap<>();
        actions.put("save", getResources().getString(R.string.action_save));
        actions.put("share", getResources().getString(R.string.action_share));
        actions.put("gallery", getResources().getString(R.string.action_image_gallery));

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long row) {
                String action = (String) view.getTag();
                if ("save".equals(action)) {
                    UIUtils.saveImage(mFragment.getActivity(), UIUtils.getSnackView(mFragment.getActivity()), mContentImg.getContent());
                } else if ("share".equals(action)) {
                    UIUtils.shareImage(mFragment.getActivity(), UIUtils.getSnackView(mFragment.getActivity()), mUrl);
                } else if ("gallery".equals(action)) {
                    mFragment.startImageGallery(mImageIndex, mImageView);
                }
            }
        };

        SimplePopupMenu popupMenu = new SimplePopupMenu(getContext());
        popupMenu.setActions(actions);
        popupMenu.setListener(listener);
        popupMenu.show();
    }


    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GlideImageEvent event) {
        if (!event.getImageUrl().equals(mUrl))
            return;
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (event.getStatus() == ImageInfo.IN_PROGRESS
                && imageInfo.getStatus() != ImageInfo.SUCCESS) {
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
