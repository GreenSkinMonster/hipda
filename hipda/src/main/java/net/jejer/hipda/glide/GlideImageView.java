package net.jejer.hipda.glide;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.Logger;

public class GlideImageView extends ImageView {

    public static int MIN_SCALE_WIDTH = 600;

    private Context mCtx;
    private String mUrl;
    private ImageReadyInfo mImgeReadyInfo;

    private static ImageView currentImageView;
    private static String currentUrl;

    public GlideImageView(Context context) {
        super(context);
        mCtx = context;

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
        mImgeReadyInfo = imageInfo;
    }

    public void setClickToViewBigImage() {
        setClickable(true);
        setOnClickListener(new GlideImageViewClickHandler());
    }

    private class GlideImageViewClickHandler implements OnClickListener {
        @Override
        public void onClick(View view) {
            if (mImgeReadyInfo != null && mImgeReadyInfo.isReady()) {
                if (mUrl.equals(currentUrl)) {
                    stopCurrentGif();
                } else if (mImgeReadyInfo.isGif()) {
                    stopCurrentGif();
                    loadGif();
                } else {
                    stopCurrentGif();
                    loadBitmap();
                }
            }
        }

        private void loadBitmap() {

            LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_image, null);

            final Dialog dialog = new Dialog(mCtx, android.R.style.Theme_Black_NoTitleBar);
            dialog.setContentView(layout);
            dialog.show();

            final RelativeLayout loadingPanel = (RelativeLayout) layout.findViewById(R.id.loadingPanel);

            final SubsamplingScaleImageView wvImage = (SubsamplingScaleImageView) layout.findViewById(R.id.wv_image);
            wvImage.setBackgroundColor(mCtx.getResources().getColor(R.color.night_background));
            wvImage.setImage(ImageSource.uri(mImgeReadyInfo.getPath()));
            wvImage.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
            wvImage.setMinimumDpi(100);

            wvImage.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
                @Override
                public void onImageLoaded() {
                    loadingPanel.setVisibility(GONE);
                    wvImage.setVisibility(VISIBLE);
                }

                @Override
                public void onImageLoadError(Exception e) {
                    loadingPanel.setVisibility(GONE);
                    wvImage.setImage(ImageSource.resource(R.drawable.tapatalk_image_broken));
                    Toast.makeText(mCtx, "图片加载失败", Toast.LENGTH_LONG).show();
                    Logger.e("loading error", e);
                }
            });

            ImageButton btnDownload = (ImageButton) layout.findViewById(R.id.btn_download_image);
            btnDownload.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    try {
                        DownloadManager dm = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(mUrl));
                        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mUrl.substring(mUrl.lastIndexOf("/") + 1));
                        dm.enqueue(req);
                    } catch (SecurityException e) {
                        Logger.e(e);
                        Toast.makeText(mCtx, "下载出现错误，请使用浏览器下载\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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
                .override(mImgeReadyInfo.getWidth(), mImgeReadyInfo.getHeight())
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
                        .override(mImgeReadyInfo.getWidth(), mImgeReadyInfo.getHeight())
                        .into(currentImageView);
            }
        } catch (Exception ignored) {
        }
        currentUrl = null;
        currentImageView = null;
    }

}
