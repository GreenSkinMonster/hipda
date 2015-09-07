package net.jejer.hipda.bean;

public class UserInfoBean {

    private String mAvatarUrl;
    private String mDetail;
    private String mUsername;
    private String mUid;
    private boolean mOnline;

    public UserInfoBean() {
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String mAvatarUrl) {
        this.mAvatarUrl = mAvatarUrl;
    }

    public String getDetail() {
        return mDetail;
    }

    public void setDetail(String mDetail) {
        this.mDetail = mDetail;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public boolean isOnline() {
        return mOnline;
    }

    public void setOnline(boolean online) {
        this.mOnline = online;
    }
}
