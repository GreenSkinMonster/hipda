package net.jejer.hipda.async;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.ThreadListFragment;
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
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginHelper {
    private final static String LOG_TAG = "LOGIN_TASK";

    private Context mCtx;
    private Handler mHandler;

    private AndroidHttpClient client = AndroidHttpClient.newInstance(HiUtils.UserAgent);
    private HttpContext localContext = new BasicHttpContext();

    private String mErrorMsg = "";

    public LoginHelper(Context ctx, Handler handler) {
        mCtx = ctx;
        mHandler = handler;
    }

    public int login() {

        // Update UI
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = ThreadListFragment.STAGE_RELOGIN;
            mHandler.sendMessage(msg);
        }

        CookieStore cookieStore = new BasicCookieStore();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        int status = Constants.STATUS_FAIL_ABORT;

        // Step2 get formhash
        String formhash = loginStep2();

        // Step3 do login and get auth
        if (formhash != null && formhash.length() > 0) {
            status = loginStep3(formhash);
        }


        HttpUtils.saveAuth(mCtx, cookieStore);
        client.close();

        // Update UI
        if (status != Constants.STATUS_SUCCESS && mHandler != null) {
            Message msg = Message.obtain();
            msg.what = ThreadListFragment.STAGE_ERROR;
            Bundle b = new Bundle();
            b.putString(ThreadListFragment.STAGE_ERROR_KEY, mErrorMsg);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }

        return status;
    }

    private String loginStep2() {
        HttpGet req = new HttpGet(HiUtils.LoginStep2);

        try {
            HttpResponse rsp = client.execute(req, localContext);

            HttpEntity rsp_ent = rsp.getEntity();
            String rstStr = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());

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

        } catch (IOException e) {
            mErrorMsg = "无法访问HiPDA,请检查网络";
            Log.e(LOG_TAG, "network error in loginStep2", e);
        }

        return "";

    }

    private int loginStep3(String formhash) {
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

        //Log.v(LOG_TAG, HttpUtils.buildHttpString(post_param));

        try {
            StringEntity entity = new StringEntity(HttpUtils.buildHttpString(post_param), HiSettingsHelper.getInstance().getEncode());
            entity.setContentType("application/x-www-form-urlencoded");
            req.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            Log.e(LOG_TAG, "encoding error", e1);
            return Constants.STATUS_FAIL;
        }

        String rspStr;

        try {
            HttpResponse rsp = client.execute(req, localContext);
            HttpEntity rsp_ent = rsp.getEntity();
            rspStr = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());
            Log.v(LOG_TAG, rspStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "network error", e);
            return Constants.STATUS_FAIL;
        }

        // response is in XML format
        if (rspStr.contains(mCtx.getString(R.string.login_success))) {
            Log.v(LOG_TAG, "Login success!");
            return Constants.STATUS_SUCCESS;
        } else if (rspStr.contains(mCtx.getString(R.string.login_fail))) {
            Log.e(LOG_TAG, "Login FAIL");
            int msgIndex = rspStr.indexOf(mCtx.getString(R.string.login_fail));
            int msgIndexEnd = rspStr.indexOf("次", msgIndex) + 1;
            if (msgIndexEnd > msgIndex) {
                mErrorMsg = rspStr.substring(msgIndex, msgIndexEnd);
            } else {
                mErrorMsg = "登录失败,请检查账户信息";
            }
            return Constants.STATUS_FAIL_ABORT;
        } else {
            mErrorMsg = "登录失败,未知错误";
            return Constants.STATUS_FAIL;
        }
    }

    public static boolean checkLoggedinOld(Handler handler, Document doc) {
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

    public static boolean checkLoggedin(Context context, String mRsp) {
        return !mRsp.contains(context.getString(R.string.not_login));
    }
}
