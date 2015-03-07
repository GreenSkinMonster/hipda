package net.jejer.hipda.async;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.cache.AvatarUrlCache;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import java.util.Collection;

public class AvatarUrlTask {

	private final String LOG_TAG = getClass().getSimpleName();

	private Context mCtx;

	public AvatarUrlTask(Context ctx) {
		mCtx = ctx;
	}

	public void fetch(Collection<String> uids) {
		for (String uid : uids) {
			String url = HiUtils.UserInfoUrl + uid;
			StringRequest sReq = new HiStringRequest(mCtx, url,
					new GetUrlListener(), new GetUrlErrorListener());
			VolleyHelper.getInstance().add(sReq);
		}
	}


	private class GetUrlListener implements Response.Listener<String> {
		@Override
		public void onResponse(String response) {
			String uid = HttpUtils.getMiddleString(response, "(UID: ", ")");
			if (uid != null && uid.length() > 0) {
				String avatarUrl = HttpUtils.getMiddleString(response, "<div class=\"avatar\"><img src=\"", "\"");
				if (avatarUrl != null && !avatarUrl.contains("noavatar") && avatarUrl.startsWith("http")) {
					AvatarUrlCache.getInstance().put(uid, avatarUrl);
				} else {
					AvatarUrlCache.getInstance().put(uid, "");
				}
				Log.v("AvatarUrlTask", "uid=" + uid + ", avatarUrl=" + avatarUrl);
			} else {
				Log.v("AvatarUrlTask", response);
			}
		}
	}

	private class GetUrlErrorListener implements Response.ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.e(LOG_TAG, error.toString());
		}
	}


}
