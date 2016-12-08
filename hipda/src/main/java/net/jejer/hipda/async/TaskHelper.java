package net.jejer.hipda.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.preference.Preference;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.db.ContentDao;
import net.jejer.hipda.db.HistoryDao;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiProgressDialog;
import net.jejer.hipda.utils.HiUtils;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

/**
 * Created by GreenSkinMonster on 2016-07-24.
 */
public class TaskHelper {

    private static final String SETTING_URL = "https://coding.net/u/GreenSkinMonster/p/hipda/git/raw/master/hipda.json";

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
                    //FavoriteHelper.getInstance().fetchMyFavorites();
                    //FavoriteHelper.getInstance().fetchMyAttention();
                }
            }).start();
            HiSettingsHelper.getInstance()
                    .setStringValue(HiSettingsHelper.PERF_LAST_TASK_TIME, System.currentTimeMillis() + "");
        }
    }

    public static void updateImageHost() {
        updateImageHost(null, null);
    }

    public static void updateImageHost(final Activity activity, final Preference preference) {
        final String imageHostPerf = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_IMAGE_HOST, "");

        long imageHostUpdateTime = HiSettingsHelper.getInstance().getLongValue(HiSettingsHelper.PERF_IMAGE_HOST_UPDATE_TIME, 0);
        if (activity != null
                || TextUtils.isEmpty(imageHostPerf)
                || !imageHostPerf.contains("://")
                || System.currentTimeMillis() - imageHostUpdateTime > 60 * 60 * 1000) {
            new AsyncTask<Void, Void, Exception>() {

                private HiProgressDialog dialog;

                @Override
                protected Exception doInBackground(Void... voids) {
                    try {
                        updateSetting();
                    } catch (Exception e) {
                        return e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception e) {
                    super.onPostExecute(e);
                    if (e != null) {
                        if (dialog != null)
                            dialog.dismissError("发生错误 : " + OkHttpHelper.getErrorMessage(e));
                    } else {
                        if (dialog != null)
                            dialog.dismiss("服务器已更新 \n\n"
                                            + "论坛:" + HiSettingsHelper.getInstance().getForumServer() + "\n"
                                            + "图片:" + HiSettingsHelper.getInstance().getImageHost(),
                                    2000);
                        if (preference != null)
                            preference.setSummary(HiSettingsHelper.getInstance().getForumServer());
                    }
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (activity != null)
                        dialog = HiProgressDialog.show(activity, "正在更新...");
                }
            }.execute();
        }

    }

    private static void updateSetting() throws Exception {
        HiSettingsHelper.getInstance().setForumServer(HiUtils.ForumServerSsl);
        String response = OkHttpHelper.getInstance().get(HiUtils.ForumServerSsl + "/config.php");
        Gson gson = new Gson();
        Type stringStringMap = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> map = gson.fromJson(response, stringStringMap);
        String imageHost = map.get("CDN");
        HiSettingsHelper.getInstance().setImageHost(imageHost);
        HiUtils.updateBaseUrls();
        HiSettingsHelper.getInstance().setLongValue(HiSettingsHelper.PERF_IMAGE_HOST_UPDATE_TIME, System.currentTimeMillis());
    }

    private static void updateCustSetting() throws Exception {
        String response = OkHttpHelper.getInstance().get(SETTING_URL);
        Gson gson = new Gson();
        Type stringStringMap = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> map = gson.fromJson(response, stringStringMap);
        String protocol = map.get("protocol");
        String imageHost = map.get("image_host");

        if (!TextUtils.isEmpty(protocol) && !TextUtils.isEmpty(imageHost)) {
            if ("https".equals(protocol)) {
                HiSettingsHelper.getInstance().setForumServer(HiUtils.ForumServerSsl);
            } else {
                HiSettingsHelper.getInstance().setForumServer(HiUtils.ForumServer);
            }
            HiSettingsHelper.getInstance().setImageHost(imageHost);
            HiUtils.updateBaseUrls();
        }
        HiSettingsHelper.getInstance().setLongValue(HiSettingsHelper.PERF_IMAGE_HOST_UPDATE_TIME, System.currentTimeMillis());
    }

}
