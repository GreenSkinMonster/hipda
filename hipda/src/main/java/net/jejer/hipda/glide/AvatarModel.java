package net.jejer.hipda.glide;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GreenSkinMonster on 2019-02-28.
 */
public class AvatarModel {

    private static final Map<String, String> AVATAR_CACHE_KEYS = new HashMap<>();

    final private String mUrl;

    public AvatarModel(String url) {
        String cacheKey = AVATAR_CACHE_KEYS.get(url);
        if (cacheKey != null) {
            url = url + "?" + cacheKey;
        }
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public String toString() {
        return mUrl;
    }

    @Override
    public int hashCode() {
        return mUrl.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AvatarModel) {
            return mUrl.equals(((AvatarModel) o).getUrl());
        }
        return false;
    }

    static void markClearCache(String url) {
        AVATAR_CACHE_KEYS.put(url, System.currentTimeMillis() + "");
    }

}
