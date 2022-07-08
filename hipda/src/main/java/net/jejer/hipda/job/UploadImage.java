package net.jejer.hipda.job;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by GreenSkinMonster on 2016-04-13.
 */
public class UploadImage {

    private String mImgId;
    private Uri mUri;
    private Bitmap mThumb;
    private int mStatus;
    private String mMessage;

    public String getImgId() {
        return mImgId;
    }

    public void setImgId(String imgId) {
        mImgId = imgId;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public Bitmap getThumb() {
        return mThumb;
    }

    public void setThumb(Bitmap thumb) {
        mThumb = thumb;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
