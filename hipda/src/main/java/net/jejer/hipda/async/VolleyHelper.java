package net.jejer.hipda.async;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class VolleyHelper {

	private Context mCtx;
	private RequestQueue mRequestQueue;

	public void init(Context ctx) {
		mCtx = ctx;
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(mCtx);
		}
	}

	public <T> void add(Request<T> req) {
		mRequestQueue.add(req);
	}

	public void cancelAll() {
		mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
			@Override
			public boolean apply(Request<?> request) {
				return true;
			}
		});
	}

	private VolleyHelper() {
	}

	private static class SingletonHolder {
		public static final VolleyHelper INSTANCE = new VolleyHelper();
	}

	public static VolleyHelper getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
