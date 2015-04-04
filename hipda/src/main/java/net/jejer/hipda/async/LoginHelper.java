package net.jejer.hipda.async;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class LoginHelper {
    private final static String LOG_TAG = "LOGIN_TASK";

    private Context mCtx;
    private Handler mHandler;

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

        int status = Constants.STATUS_FAIL_ABORT;

        // Step2 get formhash
        String formhash = loginStep2();

        // Step3 do login and get auth
        if (!TextUtils.isEmpty(formhash)) {
            status = loginStep3(formhash);
        }

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

        String rstStr = VolleyHelper.getInstance().synchronousGet(HiUtils.LoginStep2,
                VolleyHelper.getInstance().getSimpleErrorListener());

        if (!TextUtils.isEmpty(rstStr)) {
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
        return "";
    }

    private int loginStep3(String formhash) {
        Map<String, String> post_param = new HashMap<String, String>();
        post_param.put("m_formhash", formhash);
        post_param.put("referer", "http://www.hi-pda.com/forum/index.php");
        post_param.put("loginfield", "username");
        post_param.put("username", HiSettingsHelper.getInstance().getUsername());
        post_param.put("password", HiSettingsHelper.getInstance().getPassword());
        post_param.put("questionid", HiSettingsHelper.getInstance().getSecQuestion());
        post_param.put("answer", HiSettingsHelper.getInstance().getSecAnswer());
        post_param.put("cookietime", "2592000");

        String rspStr;

        rspStr = VolleyHelper.getInstance().synchronousPost(HiUtils.LoginStep3, post_param,
                VolleyHelper.getInstance().getSimpleErrorListener());
        Log.v(LOG_TAG, rspStr);

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

    public static boolean checkLoggedin(Context context, String mRsp) {
        return !mRsp.contains(context.getString(R.string.not_login));
    }

}
