package net.jejer.hipda.ui.widget;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.glide.GifTransformation;
import net.jejer.hipda.ui.ImageViewerActivity;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by GreenSkinMonster on 2015-11-07.
 */
public class ThreadImageLayout extends BaseImageLayout {

    private Activity mActivity;
    private TextView mTextView;

    private boolean mIsThumb;
    private int mImageIndex;
    private ArrayList<ContentImg> mImages;

    public ThreadImageLayout(Activity activity, ContentImg contentImg, ArrayList<ContentImg> images, boolean isThumb) {
        super(activity);

        mActivity = activity;
        mRequestManager = Glide.with(mActivity);
        mContentImg = contentImg;
        mImages = images;
        mIsThumb = isThumb;
        mUrl = isThumb ? contentImg.getThumbUrl() : contentImg.getContent();
        mImageIndex = contentImg.getIndexInPage();

        mImageView = new ImageView(getContext());
        mImageView.setImageDrawable(Utils.getDrawableFromAttr(getContext(), R.attr.quote_background));
        addView(mImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mProgressBar = new DownloadProgressBar(getContext());
        int progressbarWidth = Utils.dpToPx(45);
        RelativeLayout.LayoutParams pbLayoutParams = new RelativeLayout.LayoutParams(progressbarWidth, progressbarWidth);
        pbLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mProgressBar.setCancelable(false);
        mProgressBar.setVisibility(GONE);
        mProgressBar.setFinishIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_action_play));
        addView(mProgressBar, pbLayoutParams);

        mTextView = new TextView(getContext());
        RelativeLayout.LayoutParams tvLayoutParams
                = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvLayoutParams.bottomMargin = Utils.dpToPx(16);
        tvLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        tvLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        if (mContentImg.getFileSize() > 0) {
            mTextView.setVisibility(View.VISIBLE);
            if (mIsThumb)
                mTextView.setText(Utils.toSizeText(mContentImg.getFileSize()) + "↑");
            else
                mTextView.setText(Utils.toSizeText(mContentImg.getFileSize()));
        } else {
            mTextView.setVisibility(GONE);
        }
        addView(mTextView, tvLayoutParams);

        doLayout();
    }

    @Override
    protected boolean isNetworkFetch() {
        return HiSettingsHelper.getInstance().isImageLoadable(mContentImg.getFileSize(), mIsThumb);
    }

    @Override
    protected OnClickListener getOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startImageGallery();
            }
        };
    }

    @Override
    protected OnLongClickListener getOnLongClickListener() {
        return new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showImageActionDialog();
                return true;
            }
        };
    }

    private void doLayout() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        if (imageInfo.isSuccess()) {
            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageInfo.getViewHeight());
            setLayoutParams(params);
        } else {
            int margin = Utils.dpToPx(1);
            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.dpToPx(150));
            params.setMargins(0, margin, 0, margin);
            setLayoutParams(params);
        }
    }

    @Override
    protected void displayImage() {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        mTextView.setVisibility(GONE);
        if (imageInfo.isGif()) {
            if (mProgressBar.getVisibility() != VISIBLE)
                mProgressBar.setVisibility(VISIBLE);
            mProgressBar.setFinish();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }

        doLayout();

        if (imageInfo.isGif()) {
            mRequestManager
                    .asBitmap()
                    .load(mUrl)
                    .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                    .transform(new GifTransformation())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mImageView);
        } else {
            mRequestManager
                    .asBitmap()
                    .load(mUrl)
                    .override(imageInfo.getBitmapWidth(), imageInfo.getBitmapHeight())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mImageView);
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

}