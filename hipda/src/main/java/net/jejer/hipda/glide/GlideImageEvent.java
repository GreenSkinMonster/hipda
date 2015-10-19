package net.jejer.hipda.glide;

import net.jejer.hipda.utils.Constants;

/**
 * Image loading event
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageEvent {

    private int mStatus = -1;
    private String mUrl;
    private int mProgress;

    public GlideImageEvent(String url, int progress, int status) {
        mUrl = url;
        mStatus = status;
        mProgress = progress;
    }

    public String getImageUrl() {
        return mUrl;
    }

    public int getProgress() {
        return mProgress;
    }

    public int getStatus() {
        return mStatus;
    }

    public boolean isInProgress() {
        return mStatus == Constants.STATUS_IN_PROGRESS;
    }

}
