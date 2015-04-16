package net.jejer.hipda.bean;

public class ContentText extends ContentAbs {

    public final static int TEXT = 0;
    public final static int NOTICE = 1;

    private StringBuilder mSb;
    private int mType = TEXT;

    public ContentText(String text) {
        mSb = new StringBuilder();
        mSb.append(text);
    }

    public ContentText(String text, int type) {
        mSb = new StringBuilder();
        mSb.append(text);
        mType = type;
    }

    public void append(String txt) {
        mType = TEXT;
        mSb.append(txt);
    }

    @Override
    public String getContent() {
        return mSb.toString();
    }

    @Override
    public String getCopyText() {
        return mSb.toString().replaceAll("<br>", "\n");
    }

    public int getType() {
        return mType;
    }

}
