package net.jejer.hipda.cache;

import android.util.SparseArray;

import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailListBean;

import java.util.HashMap;

/**
 * keep thread detail in cache and manage a postId to page map
 * Created by GreenSkinMonster on 2015-03-31.
 */
public class ThreadDetailCache {

    private final SparseArray<DetailListBean> mCache = new SparseArray<>();
    private final HashMap<String, Integer> mPostIdToPageMap = new HashMap<>();

    public void put(DetailListBean detailListBean) {
        mCache.put(detailListBean.getPage(), detailListBean);
        for (DetailBean detailBean : detailListBean.getAll()) {
            mPostIdToPageMap.put(detailBean.getPostId(), detailListBean.getPage());
        }
    }

    public void remove(int page) {
        mCache.remove(page);
    }

    public void clear() {
        mCache.clear();
        mPostIdToPageMap.clear();
    }

    public DetailListBean get(int page) {
        return mCache.get(page);
    }

    public DetailBean getPostByPostId(String postId) {
        Integer page = mPostIdToPageMap.get(postId);
        if (page != null && page > 0 && mCache.get(page) != null) {
            return mCache.get(page).getPostInPage(postId);
        }
        return null;
    }

    public int getFirstFloorOfPage(int page) {
        DetailListBean detailListBean = mCache.get(page);
        if (detailListBean == null) return -1;
        return detailListBean.getAll().get(0).getFloor();
    }

    public int getLastFloorOfPage(int page) {
        DetailListBean detailListBean = mCache.get(page);
        if (detailListBean == null) return -1;
        return detailListBean.getAll().get(detailListBean.getCount() - 1).getFloor();
    }

}
