package net.jejer.hipda.db;

/**
 * Created by GreenSkinMonster on 2016-07-23.
 */
public class History {

    private String mTid;
    private String mFid;
    private String mTitle;
    private String mUid;
    private String mUsername;
    private String mPostTime;
    private long mVisitTime;

    public History() {
    }

    public String getFid() {
        return mFid;
    }

    public void setFid(String fid) {
        mFid = fid;
    }

    public String getPostTime() {
        return mPostTime;
    }

    public void setPostTime(String postTime) {
        mPostTime = postTime;
    }

    public String getTid() {
        return mTid;
    }

    public void setTid(String tid) {
        mTid = tid;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public long getVisitTime() {
        return mVisitTime;
    }

    public void setVisitTime(long visitTime) {
        mVisitTime = visitTime;
    }
}
