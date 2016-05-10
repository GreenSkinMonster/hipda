package net.jejer.hipda.bean;

public class ContentImg extends ContentAbs {
    private String mUrl;

    public ContentImg(String url) {
        mUrl = url;
    }

    @Override
    public String getContent() {
        return mUrl;
    }

    @Override
    public String getCopyText() {
        return "[图片:" + mUrl + "]";
    }

}
