package net.jejer.hipda.async;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParserThreadList;
import net.jejer.hipda.utils.HiUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ThreadListLoader extends AsyncTaskLoader<ThreadListBean> {
    private final String LOG_TAG = getClass().getSimpleName();
    private Context mCtx;
    private int mForumId = 0;
    private int mPage = 1;
    private Object mLocker;
    private String mRsp;
    private Handler mHandler;

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
        //Log.v(LOG_TAG, "loadInBackground Enter");
        if (mForumId == 0) {
            return null;
        }

        int count = 0;
        boolean getOk = false;
        do {
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
            count++;
        } while (!getOk && count < 3);

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

        String url = HiUtils.ThreadListUrl + mForumId + "&page=" + mPage;
        if (HiSettingsHelper.getInstance().isSortByPostTime(mForumId)) {
            url += "&orderby=dateline";
        }
        StringRequest sReq = new HiStringRequest(mCtx, url,
                new ThreadListListener(), new ThreadListErrorListener());
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
            Log.e(LOG_TAG, error.toString());

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
