package net.jejer.hipda.bean;

public class ContentImg extends ContentAbs {
    private String mUrl;
    private int mFloor;
    private int mIndexInPage;
    private String mAuthor;
    private long mFileSize;
    private String mId;

    public ContentImg(String url, String id) {
        mUrl = url;
        mId = id;
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

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    public String getId() {
        return mId;
    }

}
