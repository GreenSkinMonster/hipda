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
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by GreenSkinMonster on 2016-11-11.
 */

public class ThreadDetailJob extends BaseJob {

    public final static String FIND_AUTHOR_ID = "-1";
    private final static int MIN_JOB_TIME_MS = 150;

    private Context mCtx;
    private String mTid;
    private String mAuthorId;
    private String mGotoPostId;
    private int mPage;
    private int mFetchType;

    private ThreadDetailEvent mEvent;

    public ThreadDetailJob(Context context, String sessionId, String tid, String authorId, String gotoPostId, int page, int fetchType, int loadingPosition) {
        super(sessionId);
        mCtx = context;
        mTid = tid;
        mAuthorId = authorId;
        mGotoPostId = gotoPostId;
        mPage = page;
        mFetchType = fetchType;

        mEvent = new ThreadDetailEvent();
        mEvent.mSessionId = mSessionId;
        mEvent.mFectchType = fetchType;
        mEvent.mPage = page;
        mEvent.mLoadingPosition = loadingPosition;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        long start = System.currentTimeMillis();
        DetailListBean data = null;
        int eventStatus = Constants.STATUS_SUCCESS;
        String eventMessage = "";
        String eventDetail = "";

        try {
            String resp = fetchDetail();
            Document doc = Jsoup.parse(resp);
            boolean loggedin = LoginHelper.checkLoggedin(doc);
            if (!loggedin) {
                int status = new LoginHelper().login();
                if (status == Constants.STATUS_FAIL_ABORT) {
                    eventStatus = Constants.STATUS_FAIL_RELOGIN;
                    eventMessage = "请重新登录";
                } else if (status == Constants.STATUS_SUCCESS) {
                    resp = fetchDetail();
                    doc = Jsoup.parse(resp);
                    loggedin = LoginHelper.checkLoggedin(doc);
                }
            }
            if (loggedin) {
                String tid = HiUtils.isValidId(mTid) ? mTid : Utils.getMiddleString(resp, "tid = parseInt('", "')");
                data = HiParserThreadDetail.parse(mCtx, doc, tid);
                if (data == null || data.getCount() == 0) {
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

        long delta = System.currentTimeMillis() - start;
        if (delta < MIN_JOB_TIME_MS) {
            Thread.sleep(MIN_JOB_TIME_MS - delta);
        }

        mEvent.mData = data;
        mEvent.mStatus = eventStatus;
        mEvent.mMessage = eventMessage;
        mEvent.mDetail = eventDetail;
        mEvent.mAuthorId = mAuthorId;
        EventBus.getDefault().postSticky(mEvent);

        if (data != null && data.getPage() == data.getLastPage()
                && mAuthorId == null
                && HiUtils.isForumValid(data.getFid()) && data.getTid() != null) {
            ThreadUpdatedEvent tuEvent = new ThreadUpdatedEvent();
            tuEvent.mFid = data.getFid();
            tuEvent.mTid = data.getTid();
            tuEvent.mTitle = data.getTitle();
            tuEvent.mReplyCount = data.getAll().get(data.getCount() - 1).getFloor() - 1;
            EventBus.getDefault().postSticky(tuEvent);
        }
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
            if (FIND_AUTHOR_ID.equals(mAuthorId)) {
                mAuthorId = getThreadAuthorId();
            }
            if (HiUtils.isValidId(mAuthorId))
                mUrl += "&authorid=" + mAuthorId;

        }
        return OkHttpHelper.getInstance().get(mUrl, mSessionId, OkHttpHelper.FORCE_NETWORK);
    }

    private String getThreadAuthorId() {
        try {
            String url = HiUtils.DetailListUrl + mTid + "&page=1";
            String response = OkHttpHelper.getInstance().get(url, mSessionId, OkHttpHelper.PREFER_CACHE);
            Document doc = Jsoup.parse(response);
            return HiParserThreadDetail.getThreadAuthorId(doc);
        } catch (Exception e) {
            return "";
        }
    }

}
