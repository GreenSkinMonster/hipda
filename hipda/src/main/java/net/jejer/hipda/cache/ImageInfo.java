package net.jejer.hipda.cache;

import android.text.TextUtils;

import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Utils;

/**
 * store loaded image's size
 * Created by GreenSkinMonster on 2015-04-24.
 */
public class ImageInfo {

    public static final int IDLE = 0;
    public static final int IN_PROGRESS = 1;
    public static final int FAIL = 2;
    public static final int SUCCESS = 3;

    private String mUrl;
    private int mWidth;
    private int mHeight;
    private String mPath;
    private String mMime;
    private long mFileSize;
    private int mOrientation;
    private double mSpeed;
    private int mProgress;
    private int mStatus = IDLE;

    private int maxViewWidth;
    private int displayWidth;
    private int displayHeight;

    public ImageInfo(String url) {
        mUrl = url;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setMime(String mime) {
        mMime = mime;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public String getPath() {
        return mPath;
    }

    public String getMime() {
        return mMime;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public boolean isReady() {
        return !TextUtils.isEmpty(mPath) && mWidth > 0 && mHeight > 0;
    }

    public boolean isGif() {
        return mMime != null && mMime.contains("gif");
    }

    public boolean isLongImage() {
        return !isGif() && mHeight >= 2.5 * mWidth;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public double getSpeed() {
        return mSpeed;
    }

    public void setSpeed(double speed) {
        mSpeed = speed;
    }

    public int getDisplayHeight() {
        return getDisplaySize(false);
    }

    public int getDisplayWidth() {
        return getDisplaySize(true);
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public String getUrl() {
        return mUrl;
    }

    private int getDisplaySize(boolean isWidth) {
        //calculate ImageView size for image to display

        //leave 12dp on both left and right side, this should match layout setup
        int tmpMaxViewWidth = Utils.getScreenWidth() - Utils.dpToPx(HiApplication.getAppContext(), 12 * 2);

        if (maxViewWidth != tmpMaxViewWidth) {
            maxViewWidth = tmpMaxViewWidth;

            //if image width < half maxViewWidth, scale it up for better view
            int maxScaleWidth = Math.round(maxViewWidth * 0.5f);

            double scaleRate = getScaleRate(mWidth);
            int scaledWidth = Math.round((int) (mWidth * scaleRate));
            int scaledHeight = Math.round((int) (mHeight * scaleRate));

            if (scaledWidth >= maxScaleWidth ||
                    (isGif() && scaledWidth >= maxScaleWidth / 3)) {
                displayWidth = maxViewWidth;
                displayHeight = Math.round(maxViewWidth * 1.0f * mHeight / mWidth);
            } else {
                displayWidth = scaledWidth;
                displayHeight = scaledHeight;
            }
            //at last, limit ImageView height for gif or very long images
            float maxHeightScale = !isLongImage() ? 0.75f : 2f;
            if (displayHeight > maxHeightScale * Utils.getScreenHeight()) {
                displayHeight = Math.round(maxHeightScale * Utils.getScreenHeight());
            }
        }

        if (isWidth)
            return displayWidth;
        else
            return displayHeight;
    }

    //Math! http://www.mathsisfun.com/data/function-grapher.php
    private double getScaleRate(int x) {
        return Math.pow(x, 1.2) / x;
    }
}
