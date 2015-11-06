package net.jejer.hipda.async;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.squareup.okhttp.Request;

import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.okhttp.OkHttpHelper;
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

    private final static int MAX_TIMES = 3;
    private int count = 0;

    private final Object mLocker = new Object();
    private String mTid;
    private String mGotoPostId;
    private int mPage;
    private String mRsp;
    private DetailListBean data;

    public DetailListLoader(Context context, Handler handler, String tid, String gotoPostId, int page) {
        super(context);
        mCtx = context;
        mHandler = handler;
        mTid = tid;
        mGotoPostId = gotoPostId;
        mPage = page;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (data != null) {
            deliverResult(data);
        }
        if (data == null || takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    public DetailListBean loadInBackground() {

        if (TextUtils.isEmpty(mTid) && TextUtils.isEmpty(mGotoPostId)) {
            return null;
        }

        boolean getOk = false;
        do {
            count++;
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
                    getOk = true;
                }
            }
        } while (!getOk && count <= MAX_TIMES);
        if (!getOk) {
            Logger.e("Load Detail Fail");
            return null;
        }

        Document doc = Jsoup.parse(mRsp);
        data = HiParserThreadDetail.parse(mCtx, mHandler, doc, mTid == null);
        return data;
    }

    private void fetchDetail() {
        Message msg = Message.obtain();
        msg.what = ThreadListFragment.STAGE_GET_WEBPAGE;
        Bundle b = new Bundle();
        b.putInt(ThreadDetailFragment.LOADER_PAGE_KEY, mPage);
        msg.setData(b);
        mHandler.sendMessage(msg);

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

        OkHttpHelper.getInstance().asyncGet(mUrl, new DetailListCallback(), TextUtils.isEmpty(mTid) ? null : "tid=" + mTid);
    }

    private class DetailListCallback implements OkHttpHelper.ResultCallback {

        @Override
        public void onError(Request request, Exception e) {
            Logger.e(e);

            Message msg = Message.obtain();
            msg.what = ThreadListFragment.STAGE_ERROR;
            Bundle b = new Bundle();
            String text = "无法访问HiPDA : " + OkHttpHelper.getErrorMessage(e);
            b.putString(ThreadListFragment.STAGE_ERROR_KEY, text);
            msg.setData(b);
            mHandler.sendMessage(msg);

            synchronized (mLocker) {
                mRsp = null;
                mLocker.notify();
            }
        }

        @Override
        public void onResponse(String response) {
            mRsp = response;
            synchronized (mLocker) {
                mLocker.notify();
            }
        }
    }

}
