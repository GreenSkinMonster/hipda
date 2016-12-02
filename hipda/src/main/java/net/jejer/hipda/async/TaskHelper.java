package net.jejer.hipda.async;

import android.os.AsyncTask;
import android.text.TextUtils;

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

    private static final String SETTING_URL = "https://coding.net/u/GreenSkinMonster/p/hipda/git/raw/master/hipda.json";

    public static void updateImageHost() {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... voids) {
                try {
                    updateCustSetting();
                } catch (Exception e) {
                    return e;
                }
                return null;
            }
        }.execute();
    }

    private static void updateCustSetting() throws Exception {
        String response = VolleyHelper.getInstance().synchronousGet(SETTING_URL, null);
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
    }

}
