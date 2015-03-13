package net.jejer.hipda.async;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrePostAsyncTask extends AsyncTask<String, Void, Map<String, List<String>>> {
	private final String LOG_TAG = getClass().getSimpleName();

	private PrePostListener mListener;
	private Context mCtx;
	private int mMode;

	public PrePostAsyncTask(Context ctx, PrePostListener listener, int mode) {
		mCtx = ctx;
		mListener = listener;
		mMode = mode;
	}

	@Override
	protected Map<String, List<String>> doInBackground(String... arg0) {
		String tid = arg0[0];
		String pid = arg0[1];
		String fid = "2";

		String url = HiUtils.ReplyUrl + tid;
		switch (mMode) {
			case PostAsyncTask.MODE_REPLY_THREAD:
			case PostAsyncTask.MODE_QUICK_REPLY:
				break;
			case PostAsyncTask.MODE_REPLY_POST:
				url += "&reppost=" + pid;
				break;
			case PostAsyncTask.MODE_QUOTE_POST:
				url += "&repquote=" + pid;
				break;
			case PostAsyncTask.MODE_NEW_THREAD:
				url = HiUtils.NewThreadUrl + fid;
				break;
		}

		// get infos
		CookieStore cookieStore = HttpUtils.restoreCookie(mCtx);
		HttpContext localContext = new BasicHttpContext();
		AndroidHttpClient client = AndroidHttpClient.newInstance(HiUtils.UserAgent);
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		String rsp_str;
		Boolean rspOk = false;
		int retry = 0;
		do {
			rsp_str = getReplyPage(client, localContext, url);
			if (rsp_str != null) {
				if (!LoginHelper.checkLoggedin(mCtx, rsp_str)) {
					int status = new LoginHelper(mCtx, null).login();
					if (status > Constants.STATUS_FAIL) {
						break;
					}
				} else {
					rspOk = true;
				}
			}
			retry++;
		} while (!rspOk && retry < 3);

		client.close();

		if (!rspOk) {
			return null;
		}

		Document doc = Jsoup.parse(rsp_str);
		return parseRsp(doc);
	}

	private String getReplyPage(AndroidHttpClient client, HttpContext ctx, String url) {
		HttpGet req = new HttpGet(url);

		String rsp_str;
		try {
			HttpResponse rsp = client.execute(req, ctx);
			HttpEntity rsp_ent = rsp.getEntity();
			rsp_str = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
		} catch (Exception e) {
			Log.e(LOG_TAG, "Network related error", e);
			return null;
		}

		return rsp_str;
	}

	private Map<String, List<String>> parseRsp(Document doc) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		result.put("formhash", new ArrayList<String>());
		result.put("text", new ArrayList<String>());
		result.put("uid", new ArrayList<String>());
		result.put("hash", new ArrayList<String>());
		result.put("attaches", new ArrayList<String>());

		Elements formhashES = doc.select("input[name=formhash]");
		if (formhashES.size() < 1) {
			return result;
		} else {
			result.get("formhash").add(formhashES.first().attr("value"));
		}

		Elements addtextES = doc.select("textarea");
		if (addtextES.size() < 1) {
			return result;
		} else {
			result.get("text").add(addtextES.first().text());
		}

		Elements scriptES = doc.select("script");
		if (scriptES.size() < 1) {
			return result;
		} else {
			result.get("uid").add(HttpUtils.getMiddleString(scriptES.first().data(), "discuz_uid = ", ","));
		}

		Elements hashES = doc.select("input[name=hash]");
		if (hashES.size() < 1) {
			return result;
		} else {
			result.get("hash").add(hashES.first().attr("value"));
		}

		return result;
	}

	@Override
	protected void onPostExecute(Map<String, List<String>> result) {
		if (result == null) {
			mListener.PrePostComplete(false, null);
			return;
		}
		mListener.PrePostComplete(!result.get("formhash").isEmpty(), result);
	}

	public interface PrePostListener {
		public void PrePostComplete(boolean result, Map<String, List<String>> info);
	}

}
