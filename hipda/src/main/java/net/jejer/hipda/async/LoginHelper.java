package net.jejer.hipda.async;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.ThreadListFragment;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class LoginHelper {

    private Context mCtx;
    private Handler mHandler;

    private String mErrorMsg = "";

    public LoginHelper(Context ctx, Handler handler) {
        mCtx = ctx;
        mHandler = handler;
    }

    public int login() {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = ThreadListFragment.STAGE_RELOGIN;
            mHandler.sendMessage(msg);
        }

        int status = Constants.STATUS_FAIL_ABORT;

        if (HiSettingsHelper.getInstance().isLoginInfoValid()) {
            String formhash = getFormhash();
            if (!TextUtils.isEmpty(formhash)) {
                status = doLogin(formhash);
            }
        } else {
            mErrorMsg = "登录信息不完整";
        }

        if (mHandler != null) {
            Message msg = Message.obtain();
            if (status == Constants.STATUS_FAIL) {
                msg.what = ThreadListFragment.STAGE_ERROR;
            } else if (status == Constants.STATUS_FAIL_ABORT) {
                msg.what = ThreadListFragment.STAGE_NOT_LOGIN;
            }
            Bundle b = new Bundle();
            b.putString(ThreadListFragment.STAGE_ERROR_KEY, mErrorMsg);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }

        if (status == Constants.STATUS_SUCCESS)
            EventBus.getDefault().post(new LoginEvent());

        return status;
    }

    private String getFormhash() {
        String rstStr = null;
        try {
            rstStr = OkHttpHelper.getInstance().get(HiUtils.LoginGetFormHash);

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
        } catch (Exception e) {
            mErrorMsg = OkHttpHelper.getErrorMessage(e).getMessage();
        }
        return rstStr;
    }

    private int doLogin(String formhash) {
        Map<String, String> post_param = new HashMap<>();
        post_param.put("m_formhash", formhash);
        post_param.put("referer", HiUtils.BaseUrl + "index.php");
        post_param.put("loginfield", "username");
        post_param.put("username", HiSettingsHelper.getInstance().getUsername());
        post_param.put("password", HiSettingsHelper.getInstance().getPassword());
        post_param.put("questionid", HiSettingsHelper.getInstance().getSecQuestion());
        post_param.put("answer", HiSettingsHelper.getInstance().getSecAnswer());
        post_param.put("cookietime", "2592000");

        String rspStr;
        try {
            rspStr = OkHttpHelper.getInstance().post(HiUtils.LoginSubmit, post_param);
            Logger.v(rspStr);

            // response is in XML format
            if (rspStr.contains(mCtx.getString(R.string.login_success))) {
                Logger.v("Login success!");
                return Constants.STATUS_SUCCESS;
            } else if (rspStr.contains(mCtx.getString(R.string.login_fail))) {
                Logger.e("Login FAIL");
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
                return Constants.STATUS_FAIL_ABORT;
            }
        } catch (Exception e) {
            mErrorMsg = "登录失败 : " + OkHttpHelper.getErrorMessage(e);
            return Constants.STATUS_FAIL;
        }
    }

    public static boolean checkLoggedin(Context context, String mRsp) {
        boolean loggedIn = !mRsp.contains(context.getString(R.string.not_login));
        if (!loggedIn)
            logout();
        return loggedIn;
    }

    public static boolean isLoggedIn() {
        return OkHttpHelper.getInstance().isLoggedIn();
    }

    public static void logout() {
        OkHttpHelper.getInstance().clearCookies();
        FavoriteHelper.getInstance().clearAll();
    }

    public String getErrorMsg() {
        return mErrorMsg;
    }

}
