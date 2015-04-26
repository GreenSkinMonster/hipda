package net.jejer.hipda.bean;

import android.text.TextUtils;

public class SimpleListItemBean {
    private String mTid;
    private String mPid;
    private String mTitle;
    private String mInfo;
    private String mForum;
    private String mTime;
    private String mUid;
    private String mAuthor;
    private String mAvatarUrl = "";
    private boolean mNew = false;

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getTid() {
        return mTid;
    }

    public void setTid(String tid) {
        mTid = tid;
    }

    public String getPid() {
        return mPid;
    }

    public void setPid(String pid) {
        mPid = pid;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    public String getForum() {
        return mForum;
    }

    public void setForum(String forum) {
        mForum = forum;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public boolean isNew() {
        return mNew;
    }

    public void setNew(boolean n) {
        mNew = n;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        if (TextUtils.isEmpty(avatarUrl) || avatarUrl.contains("noavatar")) {
            this.mAvatarUrl = "";
        }
        mAvatarUrl = avatarUrl.replace("small", "middle");
    }

}
