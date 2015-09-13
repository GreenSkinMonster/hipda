package net.jejer.hipda.bean;

/**
 * bean for notification
 * Created by GreenSkinMonster on 2015-09-08.
 */
public class NotificationBean {
    private int mSmsCount;
    private int mThreadCount;
    private String mUsername;
    private String mUid;
    private String mContent;

    public int getSmsCount() {
        return mSmsCount;
    }

    public void setSmsCount(int smsCount) {
        this.mSmsCount = smsCount;
    }

    public void clearSmsCount() {
        this.mSmsCount = 0;
        this.mUid = "";
        this.mUsername = "";
        this.mContent = "";
    }

    public int getThreadCount() {
        return mThreadCount;
    }

    public void setThreadCount(int threadCount) {
        this.mThreadCount = threadCount;
    }

    public String toString() {
        return "SMS=" + mSmsCount + ", THREAD=" + mThreadCount;
    }

    public boolean hasNew() {
        return mSmsCount > 0 || mThreadCount > 0;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }
}
