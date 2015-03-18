package net.jejer.hipda.async;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
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
import java.util.List;
import java.util.Map;

public class PostAsyncTask extends AsyncTask<PostBean, Void, Void> {
	private final String LOG_TAG = getClass().getSimpleName();

	public static final int MODE_REPLY_THREAD = 0;
	public static final int MODE_REPLY_POST = 1;
	public static final int MODE_QUOTE_POST = 2;
	public static final int MODE_NEW_THREAD = 3;
	public static final int MODE_QUICK_REPLY = 4;
	public static final int MODE_EDIT_POST = 5;

	private int mMode;
	private String mResult;
	private int mStatus = Constants.STATUS_FAIL;
	private Context mCtx;
	private Map<String, List<String>> mInfo;

	private PostListener mPostListenerCallback;
	private String mContent;
	private String mTid;
	private String mTitle;
	private String mFloor;

	public PostAsyncTask(Context ctx, int mode, Map<String, List<String>> info, PostListener postListenerCallback) {
		mCtx = ctx;
		mMode = mode;
		mInfo = info;
		mPostListenerCallback = postListenerCallback;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mPostListenerCallback != null)
			mPostListenerCallback.onPrePost();
	}

	@Override
	protected Void doInBackground(PostBean... postBeans) {

		PostBean postBean = postBeans[0];
		String reply_text = postBean.getContent();
		String tid = postBean.getTid();
		String pid = postBean.getPid();
		String fid = postBean.getFid();
		String floor = postBean.getFloor();
		String subject = postBean.getSubject();
		String typeid = postBean.getTypeid();

		if (mInfo == null) {
			mInfo = new PrePostAsyncTask(mCtx, null, mMode).doInBackground(postBean);
		}
		if (!TextUtils.isEmpty(floor) && TextUtils.isDigitsOnly(floor))
			mFloor = floor;

		mContent = reply_text;

		if (mMode != MODE_EDIT_POST) {
			String tail_text = HiSettingsHelper.getInstance().getTailText();
			if (!tail_text.isEmpty() && HiSettingsHelper.getInstance().isAddTail()) {
				String tail_url = HiSettingsHelper.getInstance().getTailUrl();
				if (!tail_url.isEmpty()) {
					if ((!tail_url.startsWith("http")) && (!tail_url.startsWith("https"))) {
						tail_url = "http://" + tail_url;
					}
					reply_text = reply_text + "  [url=" + tail_url + "][size=1]" + tail_text + "[/size][/url]";
				} else {
					reply_text = reply_text + "  [size=1]" + tail_text + "[/size]";
				}
			}
		}

		CookieStore cookieStore = HttpUtils.restoreCookie(mCtx);
		HttpContext localContext = new BasicHttpContext();
		AndroidHttpClient client = AndroidHttpClient.newInstance(HiUtils.UserAgent);
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		String url = HiUtils.ReplyUrl + tid + "&replysubmit=yes";
		// do send
		switch (mMode) {
			case MODE_REPLY_THREAD:
			case MODE_QUICK_REPLY:
				doPost(client, localContext, url, reply_text);
				break;
			case MODE_REPLY_POST:
			case MODE_QUOTE_POST:
				doPost(client, localContext, url, mInfo.get("text").get(0) + "\n\n    " + reply_text);
				break;
			case MODE_NEW_THREAD:
				url = HiUtils.NewThreadUrl + fid + "&typeid=" + typeid + "&topicsubmit=yes";
				doPost(client, localContext, url, reply_text, subject);
				break;
			case MODE_EDIT_POST:
				if (TextUtils.isEmpty(fid)) fid = "2";
				url = HiUtils.EditUrl + "&extra=&editsubmit=yes&mod=&editsubmit=yes" + "&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";
				doPost(client, localContext, url, reply_text, subject);
				break;
		}

		client.close();
		return null;
	}

	@Override
	protected void onPostExecute(Void avoid) {
		if (mStatus != Constants.STATUS_SUCCESS && !TextUtils.isEmpty(mContent)) {
			ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("AUTO SAVE FROM HiPDA", mContent);
			clipboard.setPrimaryClip(clip);
			mResult += "\n请注意：发表失败的内容已经复制到粘贴板";
		}
		PostBean postBean = new PostBean();
		postBean.setSubject(mTitle);
		postBean.setFloor(mFloor);
		postBean.setTid(mTid);
		if (mPostListenerCallback != null)
			mPostListenerCallback.onPostDone(mMode, mStatus, mResult, postBean);
	}

	private void doPost(AndroidHttpClient client, HttpContext ctx, String url, String... text) {
		HttpPost req = new HttpPost(url);

		Map<String, String> post_param = new HashMap<String, String>();
		post_param.put("formhash", mInfo.get("formhash").get(0));
		post_param.put("posttime", String.valueOf(System.currentTimeMillis()));
		post_param.put("wysiwyg", "0");
		post_param.put("checkbox", "0");
		post_param.put("message", text[0]);
		for (String attach : mInfo.get("attaches")) {
			post_param.put("attachnew[" + attach + "][description]", "" + attach);
		}
		if (mMode == MODE_NEW_THREAD) {
			post_param.put("subject", text[1]);
			mTitle = text[1];
		} else if (mMode == MODE_EDIT_POST) {
			if (!TextUtils.isEmpty(text[1])) {
				post_param.put("subject", text[1]);
				mTitle = text[1];
			}
		}

		try {
			String encoded = HttpUtils.buildHttpString(post_param);
			StringEntity entity = new StringEntity(encoded, HiSettingsHelper.getInstance().getEncode());
			entity.setContentType("application/x-www-form-urlencoded");
			req.setEntity(entity);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Encoding error", e);
			mResult = "编码错误!";
			return;
		}

		String rsp_str;
		int rsp_code;
		HttpResponse rsp;
		try {
			rsp = client.execute(req, ctx);
			HttpEntity rsp_ent = rsp.getEntity();
			rsp_code = rsp.getStatusLine().getStatusCode();
			rsp_str = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
		} catch (Exception e) {
			Log.e(LOG_TAG, " Network related error", e);
			mResult = "发生网络错误!";
			return;
		}
		//Log.e(LOG_TAG, rsp_str);

		if (rsp_code == 302) {
			mResult = "发表成功!";
			mStatus = Constants.STATUS_SUCCESS;

			//viewthread.php?tid=123456&extra=
			String location = rsp.getFirstHeader("Location") != null ? rsp.getFirstHeader("Location").toString() : "";
			if (!TextUtils.isEmpty(location))
				mTid = HttpUtils.getMiddleString(location, "viewthread.php?tid=", "&");

		} else {
			if (rsp_code >= 400) {
				mResult = "服务器错误代码 " + rsp_code + "!";
			} else {
				mResult = "发表失败!";
				Document doc = Jsoup.parse(rsp_str);
				Elements error = doc.select("div.alert_info");
				if (!error.isEmpty()) {
					mResult += error.text();
				}
			}
		}
	}

	public interface PostListener {
		void onPrePost();

		void onPostDone(int mode, int status, String message, PostBean postBean);
	}
}
