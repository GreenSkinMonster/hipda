package net.jejer.hipda.async;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class PostSmsAsyncTask extends AsyncTask<String, Void, Void> {

	private String LOG_TAG = getClass().getSimpleName();

	private Context mCtx;
	private String mUid;

	private String mFormhash;
	private int mStatus = Constants.STATUS_FAIL;
	private String mResult = "";
	private String mText = "";
	private PostListener mPostListenerCallback;

	public PostSmsAsyncTask(Context ctx, String uid, PostListener postListener) {
		mCtx = ctx;
		mUid = uid;
		mPostListenerCallback = postListener;
	}

	@Override
	protected Void doInBackground(String... arg0) {
		String content = arg0[0];

		// prepare http client
		CookieStore cookieStore = HttpUtils.restoreCookie(mCtx);
		HttpContext localContext = new BasicHttpContext();
		AndroidHttpClient client = AndroidHttpClient.newInstance(HiUtils.UserAgent);
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		// fetch a new page and parse formhash
		String rsp_str;
		Boolean done = false;
		int retry = 0;
		do {
			rsp_str = getPostPage(client, localContext, HiUtils.SMSPreparePostUrl + mUid);
			if (!LoginHelper.checkLoggedin(mCtx, rsp_str)) {
				int status = new LoginHelper(mCtx, null).login();
				if (status > LoginHelper.FAIL_RETRY) {
					break;
				}
			} else {
				done = true;
			}
			retry++;
		} while (!done && retry < 3);

		Document doc = Jsoup.parse(rsp_str);
		Elements formhashES = doc.select("input#formhash");
		if (formhashES.size() == 0) {
			mResult = "SMS send fail, can not get formhash.";
			client.close();
			return null;
		} else {
			mFormhash = formhashES.first().attr("value");
		}

		// do post
		doPost(client, localContext, content);

		client.close();
		return null;
	}

	private String getPostPage(AndroidHttpClient client, HttpContext ctx, String url) {
		HttpGet req = new HttpGet(url);
		String rsp_str;
		try {
			HttpResponse rsp = client.execute(req, ctx);
			HttpEntity rsp_ent = rsp.getEntity();
			rsp_str = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
		} catch (Exception e) {
			Log.e(LOG_TAG, "Network error", e);
			mResult = "发生网络错误";
			return null;
		}
		return rsp_str;
	}

	private String doPost(AndroidHttpClient client, HttpContext ctx, String content) {
		String url = HiUtils.SMSPostUrl + mUid;
		HttpPost req = new HttpPost(url);

		Map<String, String> post_param = new HashMap<String, String>();
		post_param.put("formhash", mFormhash);
		post_param.put("lastdaterange", String.valueOf(System.currentTimeMillis()));
		post_param.put("handlekey", "pmreply");
		post_param.put("message", content);

		mText = content;

		try {
			String encoded = HttpUtils.buildHttpString(post_param);
			StringEntity entity = new StringEntity(encoded, HiSettingsHelper.getInstance().getEncode());
			entity.setContentType("application/x-www-form-urlencoded");
			req.setEntity(entity);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Network error", e);
			mResult = "编码错误";
			return null;
		}

		String rsp_str;
		try {
			HttpResponse rsp = client.execute(req, ctx);
			HttpEntity rsp_ent = rsp.getEntity();
			rsp_str = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
		} catch (Exception e) {
			Log.e(LOG_TAG, "Network error", e);
			mResult = "发生网络错误";
			return null;
		}

		if (!rsp_str.contains("class=\"summary\"")) {
			mResult = "短消息发送失败.";
		} else {
			mResult = "短消息发送成功.";
			mStatus = Constants.STATUS_SUCCESS;
		}
		return rsp_str;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mPostListenerCallback != null)
			mPostListenerCallback.onPrePost();
		else
			Toast.makeText(mCtx, "正在发送...", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPostExecute(Void avoid) {
		super.onPostExecute(avoid);
		if (mStatus != Constants.STATUS_SUCCESS && !TextUtils.isEmpty(mText)) {
			ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("AUTO SAVE FROM HiPDA", mText);
			clipboard.setPrimaryClip(clip);
			mResult += "\n请注意：发表失败的短消息已经复制到粘贴板";
		}
		if (mPostListenerCallback != null) {
			mPostListenerCallback.onPostDone(mStatus, mResult);
		} else {
			Toast.makeText(mCtx, mResult, Toast.LENGTH_LONG).show();
		}
	}

	public interface PostListener {
		public void onPrePost();

		public void onPostDone(int status, String message);
	}

}
