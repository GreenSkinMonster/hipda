package net.jejer.hipda.ui;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.jejer.hipda.R;

import java.io.File;

public class GlideImageView extends ImageView {

    public static int MIN_SCALE_WIDTH = 600;

    private Context mCtx;
    private String mUrl;

    private static ImageView currentImageView;
    private static String currentUrl;

    public GlideImageView(Context context) {
        super(context);
        mCtx = context;
    }

    public void setUrl(String url) {
        mUrl = url;
        setOnClickListener(new GlideImageViewClickHandler());
        setClickable(true);
    }

    private class GlideImageViewClickHandler implements OnClickListener {
        @Override
        public void onClick(View view) {
            if (mUrl.equals(currentUrl)) {
                stopCurrentGif();
            } else if (mUrl.toLowerCase().endsWith(".gif")) {
                stopCurrentGif();
                loadGif();
            } else if (view.getLayoutParams().width < MIN_SCALE_WIDTH) {
                //do nothing
            } else {
                stopCurrentGif();
                loadBitmap();
            }
        }

        private void loadBitmap() {

            LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_image, null);

            final Dialog dialog = new Dialog(mCtx, android.R.style.Theme_Black_NoTitleBar);
            dialog.setContentView(layout);
            dialog.getWindow().setWindowAnimations(android.R.anim.slide_in_left);
            dialog.show();

            final SubsamplingScaleImageView wvImage = (SubsamplingScaleImageView) layout.findViewById(R.id.wv_image);
            wvImage.setBackgroundColor(mCtx.getResources().getColor(R.color.night_background));

            new AsyncTask<Void, Void, File>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected File doInBackground(Void... voids) {
                    try {
                        FutureTarget<File> future =
                                Glide.with(mCtx)
                                        .load(mUrl)
                                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                        File cacheFile = future.get();
                        Glide.clear(future);
                        return cacheFile;
                    } catch (Exception ignored) {
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(File cacheFile) {
                    super.onPostExecute(cacheFile);
                    wvImage.setImageUri(Uri.fromFile(cacheFile));
                    wvImage.setMinimumDpi(100);
                }
            }.execute();

            ImageButton btnDownload = (ImageButton) layout.findViewById(R.id.btn_download_image);
            btnDownload.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    DownloadManager dm = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request req = new DownloadManager.Request(Uri.parse(mUrl));
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mUrl.substring(mUrl.lastIndexOf("/") + 1));
                    dm.enqueue(req);
                }
            });
        }
    }

    private void loadGif() {
        currentUrl = mUrl;
        currentImageView = this;
        Glide.with(mCtx).pauseRequests();
        Glide.with(mCtx)
                .load(mUrl)
                .priority(Priority.IMMEDIATE)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .skipMemoryCache(true)
                .error(R.drawable.tapatalk_image_broken)
                .into(this);
        Glide.with(mCtx).resumeRequests();
    }

    private void stopCurrentGif() {
        try {
            if (currentImageView != null) {
                Glide.with(mCtx)
                        .load(currentUrl)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.tapatalk_image_broken)
                        .into(currentImageView);
            }
        } catch (Exception ignored) {
        }
        currentUrl = null;
        currentImageView = null;
    }

}
