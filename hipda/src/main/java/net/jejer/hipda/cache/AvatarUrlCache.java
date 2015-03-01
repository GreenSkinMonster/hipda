package net.jejer.hipda.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AvatarUrlCache {

    private final static String LOG_TAG = AvatarUrlCache.class.getSimpleName();

	private final static String PREFS_NAME = "AvatarPrefsFile";

    private static SharedPreferences avatarPrefs;
	private static LinkedHashMap<String, String> avatarMap;
	private static Context mCtx;


    public static void init(Context ctx) {
		mCtx = ctx;
		load();
	}

	private static void load() {
		if (avatarPrefs == null) {
			long start = System.currentTimeMillis();
			avatarPrefs = mCtx.getSharedPreferences(PREFS_NAME, 0);
			Map<String, ?> keys = avatarPrefs.getAll();
			avatarMap = new LinkedHashMap<String, String>();
			for (Map.Entry<String, ?> entry : keys.entrySet()) {
				avatarMap.put(entry.getKey(), entry.getValue().toString());
			}
            Log.v(LOG_TAG, "load avatarPrefs, size=" + avatarMap.size() + ", time used : " + (System.currentTimeMillis() - start) + " ms");
        }
	}

	public static void put(String uid, String url) {
		avatarMap.put(uid, url);
	}

	public static String get(String uid) {
		String v = avatarMap.get(uid);
		if (v == null)
			v = "";
		return v;
	}

	public static void save() {
		if (avatarMap != null && avatarPrefs != null) {
			long start = System.currentTimeMillis();
			Set<String> keys = avatarMap.keySet();

			SharedPreferences.Editor prefsWriter = avatarPrefs.edit();
			for (String key : keys) {
				prefsWriter.putString(key, avatarMap.get(key));
			}
			prefsWriter.commit();
            Log.v(LOG_TAG, "save avatarPrefs, size=" + avatarMap.size() + ", time used : " + (System.currentTimeMillis() - start) + " ms");
        }
	}

}
