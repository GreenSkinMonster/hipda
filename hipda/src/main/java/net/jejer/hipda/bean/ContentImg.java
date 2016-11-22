package net.jejer.hipda.bean;

public class ContentImg extends ContentAbs {
    private String mUrl;
    private String mThumbUrl;
    private int mFloor;
    private int mIndexInPage;
    private String mAuthor;
    private long mFileSize;

    public ContentImg(String url, long fileSize, String thumbUrl) {
        mUrl = url;
        mFileSize = fileSize;
        mThumbUrl = thumbUrl;
    }

    @Override
    public String getContent() {
        return mUrl;
    }

    @Override
    public String getCopyText() {
        return "[图片:" + mUrl + "]";
    }

    public int getFloor() {
        return mFloor;
    }

    public void setFloor(int floor) {
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


    public long getFileSize() {
        return mFileSize;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }
}
