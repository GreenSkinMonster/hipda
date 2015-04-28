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

import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParserThreadDetail;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DetailListLoader extends AsyncTaskLoader<DetailListBean> {

    private Context mCtx;
    private Handler mHandler;

    private Object mLocker;
    private String mTid;
    private String mGotoPostId;
    private int mPage;
    private String mRsp;

    public DetailListLoader(Context context, Handler handler, String tid, String gotoPostId, int page) {
        super(context);
        mCtx = context;
        mHandler = handler;
        mLocker = this;
        mTid = tid;
        mGotoPostId = gotoPostId;
        mPage = page;
    }

    @Override
    public DetailListBean loadInBackground() {

        if (TextUtils.isEmpty(mTid) && TextUtils.isEmpty(mGotoPostId)) {
            return null;
        }

        int try_count = 0;
        boolean fetch_done = false;
        do {
            fetchDetail();
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
                    fetch_done = true;
                }
            }
            try_count++;
        } while (!fetch_done && try_count < 3);
        if (!fetch_done) {
            Logger.e("Load Detail Fail");
            return null;
        }

        Document doc = Jsoup.parse(mRsp);
        return HiParserThreadDetail.parse(mCtx, mHandler, doc, mTid == null);
    }

    private void fetchDetail() {
        Message msg = Message.obtain();
        msg.what = ThreadListFragment.STAGE_GET_WEBPAGE;
        Bundle b = new Bundle();
        b.putInt(ThreadDetailFragment.LOADER_PAGE_KEY, mPage);
        msg.setData(b);
        mHandler.sendMessage(msg);

        String url;
        if (!TextUtils.isEmpty(mGotoPostId)) {
            //volley will fetch content automaticly if response is a 302 redirect
            if (TextUtils.isEmpty(mTid))
                url = HiUtils.GotoPostUrl.replace("{pid}", mGotoPostId);
            else
                url = HiUtils.RedirectToPostUrl.replace("{tid}", mTid).replace("{pid}", mGotoPostId);
        } else if (mPage == ThreadDetailFragment.LAST_PAGE) {
            url = HiUtils.LastPageUrl + mTid;
        } else {
            url = HiUtils.DetailListUrl + mTid + "&page=" + mPage;
        }
        StringRequest sReq = new HiStringRequest(mCtx, url,
                new DetailListListener(), new ThreadDetailErrorListener());
        VolleyHelper.getInstance().add(sReq);
    }

    private class DetailListListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            mRsp = response;
            synchronized (mLocker) {
                mLocker.notify();
            }
        }
    }

    private class ThreadDetailErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Logger.e(error);

            Message msg = Message.obtain();
            msg.what = ThreadListFragment.STAGE_ERROR;
            Bundle b = new Bundle();
            b.putString(ThreadListFragment.STAGE_ERROR_KEY, "无法访问HiPDA," + VolleyHelper.getErrorReason(error));
            msg.setData(b);
            mHandler.sendMessage(msg);

            synchronized (mLocker) {
                mRsp = null;
                mLocker.notify();
            }
        }
    }
}
