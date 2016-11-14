package net.jejer.hipda.job;

import android.content.Context;
import android.text.TextUtils;

import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.okhttp.NetworkError;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParserThreadDetail;
import net.jejer.hipda.utils.HiUtils;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by GreenSkinMonster on 2016-11-11.
 */

public class ThreadDetailJob extends BaseJob {

    private Context mCtx;
    private String mTid;
    private String mGotoPostId;
    private int mPage;
    private int mFetchType;

    private ThreadDetailEvent mEvent;

    public ThreadDetailJob(Context context, String sessionId, String tid, String gotoPostId, int page, int fetchType) {
        super(sessionId);
        mCtx = context;
        mTid = tid;
        mGotoPostId = gotoPostId;
        mPage = page;
        mFetchType = fetchType;

        mEvent = new ThreadDetailEvent();
        mEvent.mSessionId = mSessionId;
        mEvent.mFectchType = fetchType;
        mEvent.mPage = page;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        DetailListBean data = null;
        int eventStatus = Constants.STATUS_SUCCESS;
        String eventMessage = "";

        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                String resp = fetchDetail();
                if (resp != null) {
                    if (!LoginHelper.checkLoggedin(mCtx, resp)) {
                        int status = new LoginHelper(mCtx, null).login();
                        if (status == Constants.STATUS_FAIL_ABORT) {
                            eventStatus = Constants.STATUS_FAIL_ABORT;
                            eventMessage = "请重新登录";
                            break;
                        }
                    } else {
                        Document doc = Jsoup.parse(resp);
                        data = HiParserThreadDetail.parse(mCtx, doc, mTid == null);
                        if (data == null || data.getCount() == 0) {
                            eventStatus = Constants.STATUS_FAIL_ABORT;
                            eventMessage = "页面加载失败";
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                NetworkError networkError = OkHttpHelper.getErrorMessage(e);
                eventStatus = Constants.STATUS_FAIL;
                eventMessage = networkError.getMessage();
            }
        }

        mEvent.mData = data;
        mEvent.mStatus = eventStatus;
        mEvent.mMessage = eventMessage;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    protected void onCancel() {
    }

    private String fetchDetail() throws Exception {
        String mUrl;
        if (!TextUtils.isEmpty(mGotoPostId)) {
            if (TextUtils.isEmpty(mTid))
                mUrl = HiUtils.GotoPostUrl.replace("{pid}", mGotoPostId);
            else
                mUrl = HiUtils.RedirectToPostUrl.replace("{tid}", mTid).replace("{pid}", mGotoPostId);
        } else if (mPage == ThreadDetailFragment.LAST_PAGE) {
            mUrl = HiUtils.LastPageUrl + mTid;
        } else {
            mUrl = HiUtils.DetailListUrl + mTid + "&page=" + mPage;
        }
        return OkHttpHelper.getInstance().get(mUrl,
                mFetchType == ThreadDetailFragment.FETCH_REFRESH ? OkHttpHelper.FORCE_NETWORK : OkHttpHelper.PREFER_CACHE);
    }

}
