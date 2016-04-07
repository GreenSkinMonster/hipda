package net.jejer.hipda.async;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.okhttp.NetworkError;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParserThreadDetail;
import net.jejer.hipda.utils.HiUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DetailListLoader extends AsyncTaskLoader<DetailListBean> {

    private Context mCtx;
    private Handler mHandler;

    private String mTid;
    private String mGotoPostId;
    private int mPage;
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
        if (TextUtils.isEmpty(mTid) && TextUtils.isEmpty(mGotoPostId))
            return null;

        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                String resp = fetchDetail();
                if (resp != null) {
                    if (!LoginHelper.checkLoggedin(mCtx, resp)) {
                        int status = new LoginHelper(mCtx, mHandler).login();
                        if (status == Constants.STATUS_FAIL_ABORT) {
                            break;
                        }
                    } else {
                        Document doc = Jsoup.parse(resp);
                        data = HiParserThreadDetail.parse(mCtx, mHandler, doc, mTid == null);
                        return data;
                    }
                }
            } catch (Exception e) {
                Message msg = Message.obtain();
                msg.what = ThreadListFragment.STAGE_ERROR;
                Bundle b = new Bundle();

                NetworkError message = OkHttpHelper.getErrorMessage(e);
                b.putString(ThreadListFragment.STAGE_ERROR_KEY, "无法访问HiPDA  : " + message.getMessage());
                b.putString(ThreadListFragment.STAGE_DETAIL_KEY, message.getDetail());

                msg.setData(b);
                mHandler.sendMessage(msg);
            }
        }
        return null;
    }

    private String fetchDetail() throws Exception {
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

        return OkHttpHelper.getInstance().get(mUrl);
    }

}
