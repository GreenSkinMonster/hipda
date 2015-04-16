package net.jejer.hipda.bean;

import android.text.TextUtils;

import net.jejer.hipda.utils.HiUtils;

public class ContentAttach extends ContentAbs {
    private String mUrl;
    private String mTitle;
    private String mDesc;

    public ContentAttach(String url, String title, String desc) {
        mUrl = url;
        mTitle = title;
        mDesc = desc;
    }

    @Override
    public String getContent() {
        String cnt = "<a href=\"" + HiUtils.getFullUrl(mUrl) + "\">" + mTitle + "</a>";
        if (!TextUtils.isEmpty(mDesc))
            cnt += "    " + mDesc;
        return cnt;
    }

    @Override
    public String getCopyText() {
        return "[附件:" + mTitle + "]";
    }

}
