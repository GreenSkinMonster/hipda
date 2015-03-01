package net.jejer.hipda.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.jejer.hipda.async.AvatarUrlTask;
import net.jejer.hipda.ui.ThreadListAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AvatarUrlCache {

    private final static String LOG_TAG = AvatarUrlCache.class.getSimpleName();

	private final static String PREFS_NAME = "AvatarPrefsFile";

    private SharedPreferences avatarPrefs;
    private LinkedHashMap<String, String> avatarMap;
    private Context mCtx;

    private Collection<String> mDirtyUids = new ArrayList<String>();
    private boolean mUpdated = false;


    private static AvatarUrlCache ourInstance = new AvatarUrlCache();

    public static AvatarUrlCache getInstance() {
        return ourInstance;
    }

    private AvatarUrlCache() {
    }


    public void init(Context ctx) {
        mCtx = ctx;
		load();
	}

    private void load() {
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

    public void put(String uid, String url) {
        if (!avatarMap.containsKey(uid) || !avatarMap.get(uid).equals(url)) {
            avatarMap.put(uid, url);
            mDirtyUids.remove(uid);
            mUpdated = true;
        }
    }

    public String get(String uid) {
        if (!avatarMap.containsKey(uid)) {
            return "";
        }
        return avatarMap.get(uid);
    }

    public void markDirty(String uid) {
        if (!avatarMap.containsKey(uid) && !mDirtyUids.contains(uid)) {
            mDirtyUids.add(uid);
        }
    }

    public void fetchAvatarUrls(ThreadListAdapter threadListAdapter) {
        if (mDirtyUids.size() > 0) {
            Collection<String> temp = new ArrayList<String>();
            temp.addAll(mDirtyUids);
            mDirtyUids.clear();
            new AvatarUrlTask(mCtx, threadListAdapter, temp).execute();
        }
    }

    public boolean contains(String uid) {
        return avatarMap.containsKey(uid);
    }

    public boolean isUpdated() {
        return mUpdated;
    }

    public void setUpdated(boolean updated) {
        mUpdated = updated;
    }

    public void save() {
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
