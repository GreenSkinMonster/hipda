package net.jejer.hipda.async;

import android.preference.Preference;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.SmallImages;
import net.jejer.hipda.db.ContentDao;
import net.jejer.hipda.db.HistoryDao;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiProgressDialog;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import okhttp3.Request;

/**
 * Created by GreenSkinMonster on 2016-07-24.
 */
public class TaskHelper {

    private final static String SERVER_SETTING_URL = "http://www.hi-pda.com/config.php";

    public static void runDailyTask(boolean force) {
        String millis = HiSettingsHelper.getInstance()
                .getStringValue(HiSettingsHelper.PERF_LAST_TASK_TIME, "0");
        Date last = null;
        if (millis.length() > 0) {
            try {
                last = new Date(Long.parseLong(millis));
            } catch (Exception ignored) {
            }
        }
        if (force || last == null || System.currentTimeMillis() > last.getTime() + 24 * 60 * 60 * 1000) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ContentDao.cleanup();
                    HistoryDao.cleanup();
                    FavoriteHelper.getInstance().fetchMyFavorites();
                    FavoriteHelper.getInstance().fetchMyAttention();
                }
            }).start();
            HiSettingsHelper.getInstance()
                    .setStringValue(HiSettingsHelper.PERF_LAST_TASK_TIME, System.currentTimeMillis() + "");
        }
    }

    public static void updateImageHost() {
        updateImageHost(null, null);
    }

    public static void updateImageHost(final HiProgressDialog dialog, final Preference preference) {
        String imageHost = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_IMAGE_HOST, "");
        long imageHostUpdateTime = HiSettingsHelper.getInstance().getLongValue(HiSettingsHelper.PERF_IMAGE_HOST_UPDATE_TIME, 0);
        if (dialog != null || TextUtils.isEmpty(imageHost) || System.currentTimeMillis() - imageHostUpdateTime > 30 * 60 * 1000) {
            OkHttpHelper.getInstance().asyncGet(SERVER_SETTING_URL, new OkHttpHelper.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    Logger.e(e);
                    if (dialog != null)
                        dialog.dismissError("发生错误 : " + OkHttpHelper.getErrorMessage(e));
                }

                @Override
                public void onResponse(String response) {
                    try {
                        Gson gson = new Gson();
                        Type stringStringMap = new TypeToken<Map<String, String>>() {
                        }.getType();
                        Map<String, String> map = gson.fromJson(response, stringStringMap);

                        String cdnStr = map.get("CDN");
                        String host = new URL(cdnStr).getHost();
                        if (!TextUtils.isEmpty(host)) {
                            HiUtils.updateImageHost(host);
                            HiSettingsHelper.getInstance().setStringValue(HiSettingsHelper.PERF_IMAGE_HOST, host);
                            HiSettingsHelper.getInstance().setLongValue(HiSettingsHelper.PERF_IMAGE_HOST_UPDATE_TIME, System.currentTimeMillis());
                            SmallImages.clear();
                        }
                        if (dialog != null)
                            dialog.dismiss("图片服务器已更新 \n" + host);
                        if (preference != null)
                            preference.setSummary(host);
                    } catch (Exception e) {
                        Logger.e(e);
                        if (dialog != null)
                            dialog.dismissError("发生错误 : " + OkHttpHelper.getErrorMessage(e));
                    }
                }
            });
        } else {
            HiUtils.updateImageHost(HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_IMAGE_HOST, HiUtils.ImageHost));
            SmallImages.clear();
        }
    }

}
