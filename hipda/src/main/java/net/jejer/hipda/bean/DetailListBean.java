package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.List;

public class DetailListBean {
    private List<DetailBean> mDetailBeans;
    private Boolean mHaveNext;
    private int mLastPage = 1;
    private int mCount = 0;
    private int mPage = 0;
    private String mTitle;
    private String mFid;

    public DetailListBean() {
        mDetailBeans = new ArrayList<DetailBean>();
        mHaveNext = false;
    }

    public void add(DetailBean detailBean) {
        mDetailBeans.add(detailBean);
        mCount++;
    }

    public int getCount() {
        return mCount;
    }

    public List<DetailBean> getAll() {
        return mDetailBeans;
    }

    public String getLastId() {
        if (mDetailBeans.size() == 0) {
            return "";
        }
        return mDetailBeans.get(mDetailBeans.size() - 1).getPostId();
    }

    public Boolean getHaveNext() {
        return mHaveNext;
    }

    public void setHaveNext(Boolean mHaveNext) {
        this.mHaveNext = mHaveNext;
    }

    public int getLastPage() {
        return mLastPage;
    }

    public void setLastPage(int mLastPage) {
        this.mLastPage = mLastPage;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getFid() {
        return mFid;
    }

    public void setFid(String fid) {
        mFid = fid;
    }
}
