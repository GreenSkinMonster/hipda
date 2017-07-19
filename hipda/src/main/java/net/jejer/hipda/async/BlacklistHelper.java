package net.jejer.hipda.async;

import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.okhttp.ParamsMap;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.UIUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.List;

import okhttp3.Request;

/**
 * Created by GreenSkinMonster on 2017-07-14.
 */

public class BlacklistHelper {

    public static void addBlacklist(String formhash, final String username) {
        ParamsMap params = new ParamsMap();
        params.put("formhash", formhash);
        params.put("user", username);
        try {
            OkHttpHelper.getInstance().asyncPost(HiUtils.AddBlackUrl, params, new OkHttpHelper.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                }

                @Override
                public void onResponse(String response) {
                    try {
                        Document doc = Jsoup.parse(response);
                        Elements errors = doc.select("div.alert_error");
                        if (errors.size() > 0) {
                            Element el = errors.first();
                            el.select("a").remove();
                            UIUtils.toast(el.text());
                        } else {
                            HiSettingsHelper.getInstance().addToBlacklist(username);
                            UIUtils.toast("已经将用户 " + username + " 添加至黑名单");
                        }
                    } catch (Exception e) {
                        UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                    }
                }
            });
        } catch (Exception e) {
            UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
        }
    }

    public static void delBlacklist(final String formhash, final String username, OkHttpHelper.ResultCallback callback) {
        ParamsMap params = new ParamsMap();
        params.put("formhash", formhash);
        params.put("user", username);
        try {
            OkHttpHelper.getInstance().asyncPost(HiUtils.DelBlackUrl, params, callback);
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static void getBlacklists(OkHttpHelper.ResultCallback callback) {
        OkHttpHelper.getInstance().asyncGet(HiUtils.ViewBlackUrl, callback);
    }

    public static void syncBlacklists() {
        BlacklistHelper.getBlacklists(new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(String response) {
                try {
                    Document doc = Jsoup.parse(response);
                    String errorMsg = HiParser.parseErrorMessage(doc);
                    if (TextUtils.isEmpty(errorMsg)) {
                        List<String> list = HiParser.parseBlacklist(doc);
                        Collections.sort(list);
                        HiSettingsHelper.getInstance().setBlacklists(list);
                        HiSettingsHelper.getInstance().setBlacklistSyncTime();
                    } else {
                        UIUtils.toast(errorMsg);
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        });
    }

    public static String addBlacklist2(String formhash, String username) throws Exception {
        ParamsMap params = new ParamsMap();
        params.put("formhash", formhash);
        params.put("user", username);
        String response = OkHttpHelper.getInstance().post(HiUtils.AddBlackUrl, params);
        Document doc = Jsoup.parse(response);
        Elements errors = doc.select("div.alert_error");
        if (errors.size() > 0) {
            Element el = errors.first();
            el.select("a").remove();
            return el.text();
        } else {
            HiSettingsHelper.getInstance().addToBlacklist(username);
        }
        return "";
    }

}
