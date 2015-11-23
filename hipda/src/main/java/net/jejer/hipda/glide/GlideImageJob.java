package net.jejer.hipda.glide;

import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.greenrobot.event.EventBus;

/**
 * Image loading job
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageJob extends Job {

    private String mUrl;
    private Fragment mFragment;
    private boolean mNetworkFetch;

    public GlideImageJob(Fragment fragment, String url, int priority, String tag, boolean networkFetch) {
        this(fragment, url, priority, tag, networkFetch, 0);
    }

    public GlideImageJob(Fragment fragment, String url, int priority, String tag, boolean networkFetch, long delay) {
        super(new Params(priority)
                .setPersistent(false)
                .setRequiresNetwork(false)
                .addTags(tag)
                .delayInMs(delay > 0 ? delay : 0));
        mFragment = fragment;
        mUrl = url;
        mNetworkFetch = networkFetch;
    }

    @Override
    public void onAdded() {
        if (mNetworkFetch)
            EventBus.getDefault().post(new GlideImageEvent(mUrl, 0, Constants.STATUS_IN_PROGRESS));
    }

    @Override
    public void onRun() throws Throwable {
        try {
            FutureTarget<File> future;
            if (mNetworkFetch) {
                future = Glide.with(mFragment)
                        .load(mUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            } else {
                future = Glide.with(mFragment)
                        .using(cacheOnlyStreamLoader)
                        .load(mUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            }

            File cacheFile = future.get();
            Glide.clear(future);

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

            ImageReadyInfo imageReadyInfo = new ImageReadyInfo(cacheFile.getPath(), width, height, mime);
            if (orientation > 0)
                imageReadyInfo.setOrientation(orientation);
            ImageContainer.markImageReady(mUrl, imageReadyInfo);

            EventBus.getDefault().post(new GlideImageEvent(mUrl, -1, Constants.STATUS_SUCCESS));
        } catch (Exception e) {
            if (mNetworkFetch) {
                Logger.e(e);
                EventBus.getDefault().post(new GlideImageEvent(mUrl, -1, Constants.STATUS_FAIL));
            }
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable,
                                                     int runCount,
                                                     int maxRunCount) {
        return RetryConstraint.CANCEL;
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
