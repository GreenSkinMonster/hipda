package net.jejer.hipda.async;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

public class LoginAsyncTask extends AsyncTask<String, Void, Boolean> {
	private final static String LOG_TAG = "LOGIN_TASK";

	private Context mCtx;
	private Handler mHandler;

	private AndroidHttpClient client = AndroidHttpClient.newInstance(HiUtils.UserAgent);
	private CookieStore cookieStore = new BasicCookieStore();
	private HttpContext localContext = new BasicHttpContext();

	private String mErrorMsg = "";

	public LoginAsyncTask(Context ctx, Handler handler) {
		mCtx = ctx;
		mHandler = handler;
	}

	@Override
	protected Boolean doInBackground(String... arg0) {

		// Update UI
		if (mHandler != null) {
			Message msg = Message.obtain();
			msg.what = ThreadListFragment.STAGE_RELOGIN;
			mHandler.sendMessage(msg);
		}

		cookieStore = new BasicCookieStore();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);		

		boolean fail = false;


//		// Step1 get cdb_sid from cookie
//		String cdb_sid = null;
//		loginStep1();
//		List<Cookie> cookies = cookieStore.getCookies();
//		for (Cookie cookie : cookies) {
//			if (cookie.getName().equals("cdb_sid")) {
//				cdb_sid = cookie.getValue();
//				fail = false;
//			}
//		}
//		if (cdb_sid == null) {
//			mErrorMsg = "无法访问HiPDA,请检查网络";
//			fail = true;
//		}

		// Step2 get formhash
		String formhash = null;
		if (!fail) {
			formhash = loginStep2();
			if (formhash.equals("")) {
				fail = true;
			}
		}

		// Step3 do login and get auth
		if (!fail) {
			fail = loginStep3(formhash);
		}


		HttpUtils.saveAuth(mCtx, cookieStore);
		client.close();

		// Update UI
		if (fail && mHandler != null) {
			Message msg = Message.obtain();
			msg.what = ThreadListFragment.STAGE_ERROR;
			Bundle b = new Bundle();
			b.putString(ThreadListFragment.STAGE_ERROR_KEY, mErrorMsg);
			msg.setData(b);
			mHandler.sendMessage(msg);
		}

		return fail;
	}

//	private void loginStep1() {
//		HttpGet req = new HttpGet(HiUtils.LoginStep1);
//
//		try {
//			client.execute(req, localContext);
//			//HttpResponse rsp = client.execute(req, localContext);
//			//HttpEntity rsp_ent = (HttpEntity)rsp.getEntity();
//			//rstStr = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	private String loginStep2() {
		HttpGet req = new HttpGet(HiUtils.LoginStep2);

		String rstStr = null;
		try {
			HttpResponse rsp = client.execute(req, localContext);
			HttpEntity rsp_ent = (HttpEntity)rsp.getEntity();
			rstStr = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Log.v(LOG_TAG, mRspStr);
		Document doc = Jsoup.parse(rstStr);
		Elements elements = doc.select("input[name=formhash]");
		Element element = elements.first();

		if (element == null) {
			Elements alartES = doc.select("div.alert_info");
			if (alartES.size() > 0) {
				mErrorMsg = alartES.first().text();
			} else {
				mErrorMsg = "Can NOT get formhash";
			}
			return "";
		}
		return element.attr("value");
	}

	private boolean loginStep3(String formhash) {
		HttpPost req = new HttpPost(HiUtils.LoginStep3);
		Map<String, String> post_param = new HashMap<String, String>();
		post_param.put("m_formhash", formhash);
		post_param.put("referer", "http://www.hi-pda.com/forum/index.php");
		post_param.put("loginfield", "username");
		post_param.put("username", HiSettingsHelper.getInstance().getUsername());
		post_param.put("password", HiSettingsHelper.getInstance().getPassword());
		post_param.put("questionid", HiSettingsHelper.getInstance().getSecQuestion());
		post_param.put("answer", HiSettingsHelper.getInstance().getSecAnswer());
		post_param.put("cookietime", "2592000");

		Log.v(LOG_TAG, HttpUtils.buildHttpString(post_param));

		try {
			StringEntity entity = new StringEntity(HttpUtils.buildHttpString(post_param), HiSettingsHelper.getInstance().getEncode());
			entity.setContentType("application/x-www-form-urlencoded");
			req.setEntity(entity);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		String rspStr = null;

		try {
			HttpResponse rsp = client.execute(req, localContext);
			HttpEntity rsp_ent = (HttpEntity)rsp.getEntity();
			rspStr = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
			Log.v(LOG_TAG, rspStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		// response is not HTML, skip parse
		if (rspStr.contains(mCtx.getString(R.string.login_success))) {
			Log.v(LOG_TAG, "Login success!");
			return true;
		} else {
			Log.e(LOG_TAG, "Login FAIL");
			mErrorMsg = "登录失败, 请检查账户信息";
			return false;
		}
	}

	public static boolean checkLoggedin(Handler handler, Document doc) {
		Elements error = doc.select("div.alert_error");
		if (!error.isEmpty()) {
			if (handler != null) {
				Message msg = Message.obtain();
				msg.what = ThreadListFragment.STAGE_ERROR;
				Bundle b = new Bundle();
				b.putString(ThreadListFragment.STAGE_ERROR_KEY, "登录失败,请检查账户信息");
				msg.setData(b);
				handler.sendMessage(msg);
			}
			return false;
		}
		return true;
	}
}
