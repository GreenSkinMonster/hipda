package net.jejer.hipda.job;

import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.path.android.jobqueue.Params;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.glide.GlideImageEvent;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.concurrent.ExecutionException;

import androidx.annotation.Nullable;


/**
 * Image loading job
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageJob extends BaseJob {

    private String mUrl;
    private RequestManager mRequestManager;
    private boolean mNetworkFetch;

    public GlideImageJob(String url, int priority, String tag, boolean networkFetch) {
        super(new Params(priority)
                .setPersistent(false)
                .setRequiresNetwork(false)
                .addTags(tag));
        mRequestManager = Glide.with(HiApplication.getAppContext());
        mUrl = url;
        mNetworkFetch = networkFetch;
        mSessionId = tag;
    }

    @Override
    public void onAdded() {
        if (mNetworkFetch)
            EventBus.getDefault().post(new GlideImageEvent(mUrl, 0, ImageInfo.IN_PROGRESS));
    }

    @Override
    public void onRun() throws Throwable {
        ImageInfo imageInfo = ImageContainer.getImageInfo(mUrl);
        try {
            mRequestManager
                    .download(mUrl)
                    .listener(new RequestListener<File>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                            if (mNetworkFetch) {
                                Logger.e(e);
                                imageInfo.setStatus(ImageInfo.FAIL);
                                String message = "";
                                if (HiSettingsHelper.getInstance().isErrorReportMode()) {
                                    message = "url : " + mUrl + "\n\nmessage : " + e.getMessage();
                                }
                                EventBus.getDefault().post(new GlideImageEvent(mUrl, -1, ImageInfo.FAIL, message));
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                            processFile(imageInfo, resource);
                            return false;
                        }
                    })
                    .onlyRetrieveFromCache(!mNetworkFetch)
                    .submit()
                    .get();
        } catch (Exception e) {
            if (mNetworkFetch) {
                Logger.e(e);
                imageInfo.setStatus(ImageInfo.FAIL);
                String message = "";
                if (HiSettingsHelper.getInstance().isErrorReportMode()) {
                    message = "url : " + mUrl + "\n\nmessage : ";
                    if (e instanceof ExecutionException && e.getCause() != null) {
                        message += e.getCause().getMessage();
                    } else {
                        message += e.getMessage();
                    }
                }
                EventBus.getDefault().post(new GlideImageEvent(mUrl, -1, ImageInfo.FAIL, message));
            }
        }
    }

    private void processFile(ImageInfo imageInfo, File cacheFile) {
        if (!imageInfo.isSuccess()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            //Returns null, sizes are in the options variable
            BitmapFactory.decodeFile(cacheFile.getPath(), options);
            int width = options.outWidth;
            int height = options.outHeight;
            String mime = Utils.nullToText(options.outMimeType);

            int orientation = 0;
            if (mime.toLowerCase().contains("jpeg")
                    || mime.toLowerCase().contains("jpg")
                    || mime.toLowerCase().contains("png")) {
                try {
                    ExifInterface exif = new ExifInterface(cacheFile.getPath());
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                } catch (Exception e) {
                    Logger.e(e);
                }
            }

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90
                    || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                width = options.outHeight;
                height = options.outWidth;
            }

            imageInfo.setStatus(ImageInfo.SUCCESS);
            imageInfo.setProgress(100);
            imageInfo.setPath(cacheFile.getPath());
            imageInfo.setWidth(width);
            imageInfo.setHeight(height);
            imageInfo.setMime(mime);
            imageInfo.setFileSize(cacheFile.length());
            if (orientation > 0)
                imageInfo.setOrientation(orientation);
            ImageContainer.markImageReady(mUrl, imageInfo);
        }
        EventBus.getDefault().post(new GlideImageEvent(mUrl, 100, ImageInfo.SUCCESS));
    }

    @Override
    protected void onCancel() {
        ImageContainer.markImageIdle(mUrl);
    }

}
