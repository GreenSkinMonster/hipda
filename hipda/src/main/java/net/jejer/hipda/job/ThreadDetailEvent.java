package net.jejer.hipda.job;

import net.jejer.hipda.bean.DetailListBean;

/**
 * Created by GreenSkinMonster on 2016-11-11.
 */

public class ThreadDetailEvent extends BaseEvent {
    public DetailListBean mData;
    public int mFectchType;
    public int mPage;
    public int mLoadingPosition;
    public String mAuthorId;
}
