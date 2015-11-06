package net.jejer.hipda.glide;

import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import com.bumptech.glide.Glide;
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

import de.greenrobot.event.EventBus;

/**
 * Image loading job
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageJob extends Job {

    private String mUrl;

    public GlideImageJob(String url, int priority, String tag) {
        super(new Params(priority).setPersistent(false).setRequiresNetwork(false).addTags(tag));
        mUrl = url;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        try {
            FutureTarget<File> future = Glide.with(getApplicationContext())
                    .load(GlideHelper.getGlideUrl(mUrl))
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

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
            Logger.e(e);
            EventBus.getDefault().post(new GlideImageEvent(mUrl, -1, Constants.STATUS_FAIL));
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

}
