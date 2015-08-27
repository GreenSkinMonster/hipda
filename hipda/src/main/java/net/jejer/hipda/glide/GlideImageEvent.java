package net.jejer.hipda.glide;

import android.view.View;

import net.jejer.hipda.utils.Constants;

/**
 * Image loading event
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageEvent {

    private int mStatus = -1;
    private String mUrl;
    private View mView;

    public GlideImageEvent(String url, View view, int status) {
        mUrl = url;
        mView = view;
        mStatus = status;
    }

    public String getImageUrl() {
        return mUrl;
    }

    public View getView() {
        return mView;
    }

    public boolean isSuccess() {
        return mStatus == Constants.STATUS_SUCCESS;
    }

}
