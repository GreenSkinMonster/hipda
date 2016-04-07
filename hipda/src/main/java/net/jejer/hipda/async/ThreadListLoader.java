package net.jejer.hipda.async;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.okhttp.NetworkError;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParserThreadList;
import net.jejer.hipda.utils.HiUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ThreadListLoader extends AsyncTaskLoader<ThreadListBean> {
    private Context mCtx;
    private int mForumId = 0;
    private int mPage = 1;
    private Handler mHandler;
    private ThreadListBean data;

    public ThreadListLoader(Context context, Handler handler, int forumId, int page) {
        super(context);
        mCtx = context;
        mHandler = handler;
        mForumId = forumId;
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
    public ThreadListBean loadInBackground() {
        if (mForumId == 0)
            return null;

        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                String resp = fetchForumList();
                if (resp != null) {
                    if (!LoginHelper.checkLoggedin(mCtx, resp)) {
                        int status = new LoginHelper(mCtx, mHandler).login();
                        if (status == Constants.STATUS_FAIL_ABORT) {
                            break;
                        }
                    } else {
                        Document doc = Jsoup.parse(resp);
                        data = HiParserThreadList.parse(mCtx, mHandler, doc);
                        return data;
                    }
                }
            } catch (Exception e) {
                Message msg = Message.obtain();
                msg.what = ThreadListFragment.STAGE_ERROR;
                Bundle b = new Bundle();

                NetworkError message = OkHttpHelper.getErrorMessage(e);
                b.putString(ThreadListFragment.STAGE_ERROR_KEY, "无法访问HiPDA : " + message.getMessage());
                b.putString(ThreadListFragment.STAGE_DETAIL_KEY, message.getDetail());

                msg.setData(b);
                mHandler.sendMessage(msg);
            }
        }
        return null;
    }

    private String fetchForumList() throws Exception {
        String mUrl = HiUtils.ThreadListUrl + mForumId + "&page=" + mPage;
        if (mForumId == HiUtils.FID_BS && TextUtils.isDigitsOnly(HiSettingsHelper.getInstance().getBSTypeId())) {
            mUrl += "&filter=type&typeid=" + HiSettingsHelper.getInstance().getBSTypeId();
        }
        if (HiSettingsHelper.getInstance().isSortByPostTime(mForumId)) {
            mUrl += "&orderby=dateline";
        }
        return OkHttpHelper.getInstance().get(mUrl);
    }
}
