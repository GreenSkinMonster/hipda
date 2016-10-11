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
import net.jejer.hipda.utils.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Date;
import java.util.Map;

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

    public static void updateImageHost(final Activity activity, final Preference preference) {
        final String imageHostPerf = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_IMAGE_HOST, "");
        final String avatarHostPerf = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_AVATAR_HOST, "");

        long imageHostUpdateTime = HiSettingsHelper.getInstance().getLongValue(HiSettingsHelper.PERF_IMAGE_HOST_UPDATE_TIME, 0);
        if (activity != null || TextUtils.isEmpty(imageHostPerf) || TextUtils.isEmpty(avatarHostPerf) || System.currentTimeMillis() - imageHostUpdateTime > 30 * 60 * 1000) {
            new AsyncTask<Void, Void, Exception>() {

                private String imageHost;
                private String avatarHost;
                private HiProgressDialog dialog;

                @Override
                protected Exception doInBackground(Void... voids) {
                    try {
                        imageHost = getImageHost();
                        avatarHost = getAvatarHost();
                        if (TextUtils.isEmpty(avatarHost)) {
                            avatarHost = imageHost;
                        }
                        HiSettingsHelper.getInstance().setImageHost(imageHost);
                        HiSettingsHelper.getInstance().setAvatarHost(avatarHost);
                        HiSettingsHelper.getInstance().updateBaseUrls();
                        HiSettingsHelper.getInstance().setLongValue(HiSettingsHelper.PERF_IMAGE_HOST_UPDATE_TIME, System.currentTimeMillis());
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
                            dialog.dismiss("服务器已更新 \n\n" +
                                    "图片 ：" + imageHost + "\n" +
                                    "头像 ：" + avatarHost, 3000);
                        if (preference != null)
                            preference.setSummary(imageHost);
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

    private static String getImageHost() throws Exception {
        String response = OkHttpHelper.getInstance().get(SERVER_SETTING_URL);
        Gson gson = new Gson();
        Type stringStringMap = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> map = gson.fromJson(response, stringStringMap);

        String cdnStr = map.get("CDN");
        return new URL(cdnStr).getHost();
    }

    private static String getAvatarHost() {
        if (!TextUtils.isEmpty(HiSettingsHelper.getInstance().getUid())) {
            try {
                String response = OkHttpHelper.getInstance().get(HiUtils.UserInfoUrl + HiSettingsHelper.getInstance().getUid());
                Document doc = Jsoup.parse(response);
                Elements avatarImgs = doc.select("div.avatar > img");
                if (avatarImgs.size() > 0) {
                    String imageUrl = avatarImgs.first().attr("src");
                    return new URL(imageUrl).getHost();
                }
            } catch (IOException e) {
                Logger.e(e);
            }
        }
        return null;
    }

}
