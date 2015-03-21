package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.List;

public class SimpleListBean {
    private List<SimpleListItemBean> mSimpleListItemBeans
            = new ArrayList<SimpleListItemBean>();
    private int mCount = 0;

    public void add(SimpleListItemBean item) {
        mSimpleListItemBeans.add(item);
        mCount++;
    }

    public int getCount() {
        return mCount;
    }

    public List<SimpleListItemBean> getAll() {
        return mSimpleListItemBeans;
    }
}
