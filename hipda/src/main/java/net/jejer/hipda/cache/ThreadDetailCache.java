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

    private SparseArray<DetailListBean> mCache = new SparseArray<>();
    private HashMap<String, Integer> mPostIdToPageMap = new HashMap<>();

    public void put(int page, DetailListBean detailListBean) {
        mCache.put(page, detailListBean);
        for (DetailBean detailBean : detailListBean.getAll()) {
            mPostIdToPageMap.put(detailBean.getPostId(), page);
        }
    }

    public void remove(int page) {
        mCache.remove(page);
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

}
