package net.jejer.hipda.glide;

/**
 * Created by GreenSkinMonster on 2019-02-28.
 */
public class CacheModel {
    private String mUrl;

    public CacheModel(String url) {
        mUrl = url;
    }

    @Override
    public String toString() {
        return mUrl;
    }
}
