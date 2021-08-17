package net.jejer.hipda.ui.widget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

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

        mImageView = new AppCompatImageView(getContext());
        mImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.quote_background));
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

        setOnClickListener(view -> {
            ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
            if (imageInfo.isSuccess()) {
                if (imageInfo.isGif()) {
                    playGif();
                } else {
                    startImageGallery();
                }
            } else if (imageInfo.isFail() || imageInfo.isIdle()) {
                fetchImage(true);
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
                if (imageInfo.isFail()) {
                    UIUtils.showMessageDialog(getContext(), "错误信息", imageInfo.getMessage(), true);
                } else if (imageInfo.isSuccess()) {
                    showImageActionDialog();
                }
                return true;
            }
        });

    }

    @Override
    protected boolean isNetworkFetch() {
        return HiSettingsHelper.getInstance().isImageLoadable(mContentImg.getFileSize(), mIsThumb);
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
            Rect rectf = new Rect();
            mImageView.getGlobalVisibleRect(rectf);

            int x = rectf.left;
            int y = rectf.top;
            int w = rectf.width();
            int h = rectf.height();

            int screenWidth = UIUtils.getScreenWidth(getContext());
            int screenHeight = UIUtils.getScreenHeight(getContext());

            int startWidth = w;
            int startHeight = Math.round((float) startWidth / screenWidth * screenHeight);

            int imageCenterX = x + w / 2;
            int imageCenterY = y + h / 2;
            int newActivityX = imageCenterX - startWidth / 2;
            int newActivityY = imageCenterY - startHeight / 2;
            int startX = newActivityX - x;
            int startY = newActivityY - y;

            Intent intent = new Intent(getContext(), ImageViewerActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeScaleUpAnimation(mImageView, startX, startY, startWidth, startHeight);
            intent.putExtra(ImageViewerActivity.KEY_IMAGE_INDEX, mImageIndex);
            intent.putParcelableArrayListExtra(ImageViewerActivity.KEY_IMAGES, mImages);
            ActivityCompat.startActivity(getContext(), intent, options.toBundle());
        }
    }

}