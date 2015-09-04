package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailListBean {
    private List<DetailBean> mDetailBeans;
    private int mLastPage = 1;
    private int mPage = 0;
    private String mTitle;
    private String mFid;
    private String mTid;
    private HashMap<String, DetailBean> mPostIdBeans;
    private int mImagesCount = 0;

    public DetailListBean() {
        mDetailBeans = new ArrayList<>();
        mPostIdBeans = new HashMap<>();
    }

    public void add(DetailBean detailBean) {
        mDetailBeans.add(detailBean);
        mPostIdBeans.put(detailBean.getPostId(), detailBean);
        for (ContentImg contentImg : detailBean.getImages()) {
            contentImg.setFloor(detailBean.getFloor());
            contentImg.setAuthor(detailBean.getAuthor());
            contentImg.setIndexInPage(mImagesCount++);
        }
    }

    public int getCount() {
        return mDetailBeans.size();
    }

    public List<DetailBean> getAll() {
        return mDetailBeans;
    }

    public DetailBean getPostInPage(String postId) {
        return mPostIdBeans.get(postId);
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

    public String getTid() {
        return mTid;
    }

    public void setTid(String tid) {
        mTid = tid;
    }

    public int getImagesCount() {
        return mImagesCount;
    }

    public List<ContentImg> getContentImages() {
        List<ContentImg> result = new ArrayList<>();
        for (DetailBean detail : mDetailBeans) {
            result.addAll(detail.getImages());
        }
        return result;
    }


}
