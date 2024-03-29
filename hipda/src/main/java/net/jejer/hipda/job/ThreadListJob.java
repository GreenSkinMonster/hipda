package net.jejer.hipda.job;

import android.content.Context;
import android.text.TextUtils;

import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.okhttp.NetworkError;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParserThreadList;
import net.jejer.hipda.utils.HiUtils;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by GreenSkinMonster on 2016-11-16.
 */

public class ThreadListJob extends BaseJob {

    private Context mCtx;
    private int mForumId;
    private int mPage;

    private ThreadListEvent mEvent;

    public ThreadListJob(Context context, String sessionId, int forumId, int page) {
        super(sessionId);
        mCtx = context;
        mForumId = forumId;
        mPage = page;

        mEvent = new ThreadListEvent();
        mEvent.mSessionId = mSessionId;
        mEvent.mForumId = forumId;
        mEvent.mPage = page;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        ThreadListBean data = null;
        int eventStatus = Constants.STATUS_SUCCESS;
        String eventMessage = "";
        String eventDetail = "";

        try {
            Document doc = Jsoup.parse(fetchForumList());
            boolean loggedin = LoginHelper.checkLoggedin(doc);
            if (!loggedin) {
                int status = new LoginHelper().login();
                if (status == Constants.STATUS_FAIL_ABORT) {
                    eventStatus = Constants.STATUS_FAIL_RELOGIN;
                    eventMessage = "请重新登录";
                } else if (status == Constants.STATUS_SUCCESS) {
                    doc = Jsoup.parse(fetchForumList());
                    loggedin = LoginHelper.checkLoggedin(doc);
                }
            }
            if (loggedin) {
                data = HiParserThreadList.parse(mCtx, doc);
                if (!data.isParsed()) {
                    eventStatus = Constants.STATUS_FAIL_ABORT;
                    eventMessage = "页面加载失败";
                } else {
                    eventStatus = Constants.STATUS_SUCCESS;
                }
            }
        } catch (Exception e) {
            NetworkError networkError = OkHttpHelper.getErrorMessage(e);
            eventStatus = Constants.STATUS_FAIL;
            eventMessage = networkError.getMessage();
            eventDetail = networkError.getDetail();
        }

        mEvent.mData = data;
        mEvent.mStatus = eventStatus;
        mEvent.mMessage = eventMessage;
        mEvent.mDetail = eventDetail;
        EventBus.getDefault().postSticky(mEvent);
    }

    private String fetchForumList() throws Exception {
        String mUrl = HiUtils.ThreadListUrl + mForumId + "&page=" + mPage;
        if (mForumId == HiUtils.FID_BS && TextUtils.isDigitsOnly(HiSettingsHelper.getInstance().getBSTypeId())) {
            mUrl += "&filter=type&typeid=" + HiSettingsHelper.getInstance().getBSTypeId();
        }
        if (HiSettingsHelper.getInstance().isSortByPostTime(mForumId)) {
            mUrl += "&orderby=dateline";
        }
        return OkHttpHelper.getInstance().get(mUrl, mSessionId);
    }
}
