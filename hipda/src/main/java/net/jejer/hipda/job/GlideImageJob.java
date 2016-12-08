package net.jejer.hipda.job;

import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.SystemClock;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.path.android.jobqueue.Params;

import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.glide.GlideImageEvent;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;


/**
 * Image loading job
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageJob extends BaseJob {

    private final static int MAX_TIME_SECS = 180;

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
        FutureTarget<File> future = null;
        try {
            long start = SystemClock.uptimeMillis();
            if (mNetworkFetch) {
                future = mRequestManager
                        .load(mUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            } else {
                future = mRequestManager
                        .using(cacheOnlyStreamLoader)
                        .load(mUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }

            File cacheFile = future.get(MAX_TIME_SECS, TimeUnit.SECONDS);

            double speed = -1;
            if (mNetworkFetch)
                speed = cacheFile.length() * 1.0f / 1024 / (SystemClock.uptimeMillis() - start) * 1000;

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
            imageInfo.setSpeed(speed);
            if (orientation > 0)
                imageInfo.setOrientation(orientation);
            ImageContainer.markImageReady(mUrl, imageInfo);

            EventBus.getDefault().post(new GlideImageEvent(mUrl, -1, ImageInfo.SUCCESS));
        } catch (Exception e) {
            if (mNetworkFetch) {
                Logger.e(e);
                imageInfo.setStatus(ImageInfo.FAIL);
                EventBus.getDefault().post(new GlideImageEvent(mUrl, -1, ImageInfo.FAIL));
            }
        } finally {
            try {
                if (future != null)
                    Glide.clear(future);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onCancel() {
        ImageContainer.markImageIdle(mUrl);
    }

    private static final StreamModelLoader<String> cacheOnlyStreamLoader = new StreamModelLoader<String>() {
        @Override
        public DataFetcher<InputStream> getResourceFetcher(final String model, int i, int i1) {
            return new DataFetcher<InputStream>() {
                @Override
                public InputStream loadData(Priority priority) throws Exception {
                    throw new IOException();
                }

                @Override
                public void cleanup() {

                }

                @Override
                public String getId() {
                    return model;
                }

                @Override
                public void cancel() {

                }
            };
        }
    };

}
