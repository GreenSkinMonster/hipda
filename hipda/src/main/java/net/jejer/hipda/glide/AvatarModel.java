package net.jejer.hipda.glide;

/**
 * Created by GreenSkinMonster on 2019-02-28.
 */
public class AvatarModel {
    private String mUrl;

    public AvatarModel(String url) {
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
}
