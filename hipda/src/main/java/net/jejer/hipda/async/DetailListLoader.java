package net.jejer.hipda.async;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.HiParserThreadDetail;
import net.jejer.hipda.utils.HiUtils;

public class DetailListLoader extends AsyncTaskLoader<DetailListBean> {
	private final String LOG_TAG = getClass().getSimpleName();

	private Context mCtx;
	private Handler mHandler;

	private Object mLocker;
	private String mTid;
	private int mPage;
	private String mRsp;

	public DetailListLoader(Context context, Handler handler, String tid, int page) {
		super(context);
		// TODO Auto-generated constructor stub
		mCtx = context;
		mHandler = handler;
		mLocker = this;
		mTid = tid;
		mPage = page;
	}

	@Override
	public DetailListBean loadInBackground() {
		// TODO Auto-generated method stub
		//Log.v(LOG_TAG, "loadInBackground");

		if (mTid.equals("")) {
			return null;
		}

		Document doc = null;
		int try_count = 0;
		boolean fetch_done = false;
		do {
			fetchDetail();
			synchronized(mLocker){
				try {
					mLocker.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Log.v(LOG_TAG, "loadInBackground got notified");

			if (mRsp != null) {
				doc = Jsoup.parse(mRsp);
				if (!LoginAsyncTask.checkLoggedin(mHandler, doc)) {
					new LoginAsyncTask(mCtx, mHandler).doInBackground();
				} else {
					fetch_done = true;
				}
			}
			try_count++;
		} while (!fetch_done && try_count < 3);
		//Log.v(LOG_TAG, "try_count = " + String.valueOf(try_count));
		if (!fetch_done) {
			Log.e(LOG_TAG, "Load Detail Fail");
			return null;
		}

		return HiParserThreadDetail.parse(mCtx, mHandler, mPage, doc);
	}


	private void fetchDetail() {
		Message msg = Message.obtain();
		msg.what = ThreadListFragment.STAGE_GET_WEBPAGE;
		Bundle b = new Bundle();
		b.putInt(ThreadDetailFragment.LOADER_PAGE_KEY, mPage);
		msg.setData(b);
		mHandler.sendMessage(msg);

		String url = HiUtils.DetailListUrl + mTid + "&page=" + mPage;
		StringRequest sReq = new HiStringRequest(mCtx, url, 
				new DetailListListener(), new ThreadDetailErrorListener());
		VolleyHelper.getInstance().add(sReq);
	}

	private class DetailListListener implements Response.Listener<String> {
		@Override
		public void onResponse(String response) {
			// TODO Auto-generated method stub
			//Log.v(LOG_TAG, "onResponse");

			mRsp = response;
			synchronized(mLocker){
				mLocker.notify();
			}
		}
	}
	private class ThreadDetailErrorListener implements Response.ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.e(LOG_TAG, error.toString());

			Message msg = Message.obtain();
			msg.what = ThreadListFragment.STAGE_ERROR;
			Bundle b = new Bundle();
			b.putString(ThreadListFragment.STAGE_ERROR_KEY, "无法访问HiPDA,请检查网络");
			msg.setData(b);
			mHandler.sendMessage(msg);

			synchronized(mLocker){
				mRsp = null;
				mLocker.notify();
			}
		}
	}
}
