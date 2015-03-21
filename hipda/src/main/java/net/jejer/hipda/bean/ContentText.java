package net.jejer.hipda.bean;

public class ContentText extends ContentAbs {
    private StringBuilder mSb;

    public ContentText(String text) {
        mSb = new StringBuilder();
        mSb.append(text);
    }

    public void append(String txt) {
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

}
