package net.jejer.hipda.async;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.ACRAUtils;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParserThreadList;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.volley.HiStringRequest;
import net.jejer.hipda.volley.VolleyHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ThreadListLoader extends AsyncTaskLoader<ThreadListBean> {
    private Context mCtx;
    private int mForumId = 0;
    private int mPage = 1;
    private Object mLocker;
    private String mRsp;
    private Handler mHandler;

    private final static int MAX_TIMES = 3;
    private int count = 0;

    private String mUrl;

    public ThreadListLoader(Context context, Handler handler, int forumId, int page) {
        super(context);
        mCtx = context;
        mHandler = handler;
        mForumId = forumId;
        mPage = page;
        mLocker = this;
    }

    @Override
    public ThreadListBean loadInBackground() {
        if (mForumId == 0) {
            return null;
        }

        boolean getOk = false;
        do {
            count++;
            fetchForumList();

            synchronized (mLocker) {
                try {
                    mLocker.wait();
                } catch (InterruptedException ignored) {
                }
            }

            if (mRsp != null) {
                if (!LoginHelper.checkLoggedin(mCtx, mRsp)) {
                    int status = new LoginHelper(mCtx, mHandler).login();
                    if (status > Constants.STATUS_FAIL) {
                        break;
                    }
                } else {
                    getOk = true;
                }
            }
        } while (!getOk && count <= MAX_TIMES);

        if (!getOk) {
            return null;
        }

        Document doc = Jsoup.parse(mRsp);
        return HiParserThreadList.parse(mCtx, mHandler, doc);
    }

    private void fetchForumList() {
        Message msg = Message.obtain();
        msg.what = ThreadListFragment.STAGE_GET_WEBPAGE;
        mHandler.sendMessage(msg);

        mUrl = HiUtils.ThreadListUrl + mForumId + "&page=" + mPage;
        if (mForumId == HiUtils.FID_BS && TextUtils.isDigitsOnly(HiSettingsHelper.getInstance().getBSTypeId())) {
            mUrl += "&filter=type&typeid=" + HiSettingsHelper.getInstance().getBSTypeId();
        }
        if (HiSettingsHelper.getInstance().isSortByPostTime(mForumId)) {
            mUrl += "&orderby=dateline";
        }
        StringRequest sReq = new HiStringRequest(mUrl, new ThreadListListener(), new ThreadListErrorListener());
        VolleyHelper.getInstance().add(sReq);
    }

    private class ThreadListListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            mRsp = response;
            synchronized (mLocker) {
                mLocker.notify();
            }
        }
    }

    private class ThreadListErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Logger.e(error);

            Message msg = Message.obtain();
            msg.what = ThreadListFragment.STAGE_ERROR;
            Bundle b = new Bundle();
            String text = "无法访问HiPDA, " + VolleyHelper.getErrorReason(error);
            b.putString(ThreadListFragment.STAGE_ERROR_KEY, text);
            msg.setData(b);
            mHandler.sendMessage(msg);

            if (HiSettingsHelper.getInstance().isErrorReportMode())
                ACRAUtils.acraReport(error, "url=" + mUrl);

            synchronized (mLocker) {
                mRsp = null;
                mLocker.notify();
            }
        }
    }
}
