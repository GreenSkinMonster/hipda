package net.jejer.hipda.bean;

/**
 * bean for notification
 * Created by GreenSkinMonster on 2015-09-08.
 */
public class NotificationBean {
    private int mSmsCount;
    private int mThreadCount;
    private String mSenderUsername;
    private String mSenderUid;
    private String mContent;

    public int getSmsCount() {
        return mSmsCount;
    }

    public void setSmsCount(int smsCount) {
        this.mSmsCount = smsCount;
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

    public String getSenderUid() {
        return mSenderUid;
    }

    public void setSenderUid(String senderUid) {
        this.mSenderUid = senderUid;
    }

    public String getSenderUsername() {
        return mSenderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.mSenderUsername = senderUsername;
    }
}
