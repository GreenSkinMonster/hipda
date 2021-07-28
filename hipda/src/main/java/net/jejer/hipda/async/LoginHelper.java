package net.jejer.hipda.async;

import android.content.Context;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.okhttp.ParamsMap;
import net.jejer.hipda.service.NotiHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;


public class LoginHelper {

    private Context mCtx;

    private String mErrorMsg = "";

    public LoginHelper(Context ctx) {
        mCtx = ctx;
    }

    public int login() {
        return login(false);
    }

    public int login(boolean manual) {
        int status = Constants.STATUS_FAIL_ABORT;

        if (HiSettingsHelper.getInstance().isLoginInfoValid()) {
            String formhash = getFormhash();
            if (!TextUtils.isEmpty(formhash)) {
                status = doLogin(formhash);
            }
        } else {
            mErrorMsg = "登录信息不完整";
        }

        if (status == Constants.STATUS_SUCCESS) {
            LoginEvent event = new LoginEvent();
            event.mManual = manual;
            EventBus.getDefault().post(event);
        }
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
                        mErrorMsg = "无法获取登录凭据";
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
        try {
            ParamsMap params = new ParamsMap();
            params.put("m_formhash", formhash);
            params.put("referer", HiUtils.BaseUrl + "index.php");
            params.put("loginfield", "username");
            params.put("username", HiSettingsHelper.getInstance().getUsername());
            params.put("password", processedPassword());
            params.put("questionid", HiSettingsHelper.getInstance().getSecQuestion());
            params.put("answer", HiSettingsHelper.getInstance().getSecAnswer());
            params.put("cookietime", "2592000");

            String rspStr = OkHttpHelper.getInstance().post(HiUtils.LoginSubmit, params);
            Logger.v(rspStr);

            // response is in XML format
            if (rspStr.contains(mCtx.getString(R.string.login_success))) {
                Logger.v("Login SUCCESS!");
                return Constants.STATUS_SUCCESS;
            } else if (rspStr.contains(mCtx.getString(R.string.login_fail))) {
                Logger.v("Login FAIL");
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
        return !mRsp.contains(context.getString(R.string.not_login));
    }

    public static boolean isLoggedIn() {
        return OkHttpHelper.getInstance().isLoggedIn();
    }

    public static void logout() {
        HiSettingsHelper.getInstance().setUsername("");
        HiSettingsHelper.getInstance().setPassword("");
        HiSettingsHelper.getInstance().setSecQuestion("");
        HiSettingsHelper.getInstance().setSecAnswer("");
        HiSettingsHelper.getInstance().setUid("");
        NotiHelper.clearNotification();
        OkHttpHelper.getInstance().clearCookies();
        FavoriteHelper.getInstance().clearAll();
        HiSettingsHelper.getInstance().setBlacklists(new ArrayList<String>());
    }

    public String getErrorMsg() {
        return mErrorMsg;
    }

    private String processedPassword() {
        String pass = HiSettingsHelper.getInstance().getPassword();
        if (TextUtils.isEmpty(pass) || pass.length() == 32)
            return pass;
        try {
            return Utils.md5(pass.replace("\\", "\\\\")
                    .replace("'", "\'")
                    .replace("\"", "\\\""));
        } catch (Exception e) {
            return pass;
        }
    }

}
