package net.jejer.hipda.bean;

import net.jejer.hipda.utils.HiUtils;

public class ContentAttach extends ContentAbs {
    private String mUrl;
    private String mTitle;

    public ContentAttach(String url, String title) {
        mUrl = url;
        mTitle = title;
    }

    @Override
    public String getContent() {
        // TODO Auto-generated method stub
        //return "[✚<a href=\""+HiUtils.getFullUrl(mUrl)+"\">"+mTitle+"</a>]";
        return "<a href=\"" + HiUtils.getFullUrl(mUrl) + "\">" + mTitle + "</a>";
    }

    @Override
    public String getCopyText() {
        return "[附件:" + mTitle + "]";
    }

}
