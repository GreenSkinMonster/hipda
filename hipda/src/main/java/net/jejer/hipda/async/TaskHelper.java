package net.jejer.hipda.async;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by GreenSkinMonster on 2016-07-24.
 */
public class TaskHelper {

    public static void updateImageHost() {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... voids) {
                try {
                    updateSetting();
                } catch (Exception e) {
                    return e;
                }
                return null;
            }
        }.execute();
    }

    private static void updateSetting() throws Exception {
        HiSettingsHelper.getInstance().setForumServer(HiUtils.ForumServer);
        String response = VolleyHelper.getInstance().synchronousGet(HiUtils.ForumServer + "/config.php", null);
        Gson gson = new Gson();
        Type stringStringMap = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> map = gson.fromJson(response, stringStringMap);
        String imageHost = map.get("CDN");
        HiSettingsHelper.getInstance().setImageHost(imageHost);
        HiUtils.updateBaseUrls();
    }

}
