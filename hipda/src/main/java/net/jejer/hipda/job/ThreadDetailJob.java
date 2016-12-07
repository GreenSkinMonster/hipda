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

    public final static String FIND_AUTHOR_ID = "-1";
    private final static int MIN_JOB_TIME_MS = 300;

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

        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                String resp = fetchDetail();
                if (resp != null) {
                    if (!LoginHelper.checkLoggedin(mCtx, resp)) {
                        int status = new LoginHelper(mCtx).login();
                        if (status == Constants.STATUS_FAIL_ABORT) {
                            eventStatus = Constants.STATUS_FAIL_RELOGIN;
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
                eventDetail = networkError.getDetail();
                if (isCancelled())
                    break;
            }
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
        return OkHttpHelper.getInstance().get(mUrl, mSessionId,
                mFetchType == ThreadDetailFragment.FETCH_REFRESH ? OkHttpHelper.FORCE_NETWORK : OkHttpHelper.PREFER_CACHE);
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
