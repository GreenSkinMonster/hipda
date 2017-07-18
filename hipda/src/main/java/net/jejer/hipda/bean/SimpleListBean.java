package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.List;

public class SimpleListBean {
    private List<SimpleListItemBean> mSimpleListItemBeans = new ArrayList<>();
    private String mSearchId;
    private int mMaxPage;

    public void add(SimpleListItemBean item) {
        mSimpleListItemBeans.add(item);
    }

    public int getCount() {
        return mSimpleListItemBeans.size();
    }

    public List<SimpleListItemBean> getAll() {
        return mSimpleListItemBeans;
    }

    public String getSearchId() {
        return mSearchId;
    }

    public void setSearchId(String searchId) {
        mSearchId = searchId;
    }

    public int getMaxPage() {
        return mMaxPage;
    }

    public void setMaxPage(int maxPage) {
        mMaxPage = maxPage;
    }
}
