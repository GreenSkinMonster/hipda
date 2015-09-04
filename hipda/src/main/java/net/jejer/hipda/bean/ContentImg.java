package net.jejer.hipda.bean;

import net.jejer.hipda.utils.HiUtils;

public class ContentImg extends ContentAbs {
    private String mUrl;
    private String mFloor;
    private int mIndexInPage;
    private String mAuthor;

    public ContentImg(String url, boolean isInternal) {
        if (isInternal) {
            mUrl = HiUtils.getFullUrl(url);
        } else {
            mUrl = url;
        }
    }

    @Override
    public String getContent() {
        return mUrl;
    }

    @Override
    public String getCopyText() {
        return "[图片:" + mUrl + "]";
    }

    public String getFloor() {
        return mFloor;
    }

    public void setFloor(String floor) {
        mFloor = floor;
    }

    public int getIndexInPage() {
        return mIndexInPage;
    }

    public void setIndexInPage(int indexInPage) {
        mIndexInPage = indexInPage;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }
}
