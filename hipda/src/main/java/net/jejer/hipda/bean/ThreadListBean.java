package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.List;

public class ThreadListBean {

    private boolean mParsed = false;
    private String mUid;
    private List<ThreadBean> mThreads = new ArrayList<>();

    public ThreadListBean() {
    }

    public void add(ThreadBean thread) {
        mThreads.add(thread);
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public int getCount() {
        return mThreads.size();
    }

    public boolean isParsed() {
        return mParsed;
    }

    public void setParsed(boolean parsed) {
        mParsed = parsed;
    }

    public List<ThreadBean> getThreads() {
        return mThreads;
    }

    public void setThreads(List<ThreadBean> threads) {
        mThreads = threads;
    }
}
