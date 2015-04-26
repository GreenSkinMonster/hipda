package net.jejer.hipda.glide;

import android.text.TextUtils;

/**
 * store loaded image's size
 * Created by GreenSkinMonster on 2015-04-24.
 */
public class ImageReadyInfo {
    private int width;
    private int height;
    private String path;
    private String mime;

    public ImageReadyInfo(String path, int width, int height, String mime) {
        this.width = width;
        this.height = height;
        this.path = path;
        this.mime = mime;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getPath() {
        return path;
    }

    public String getMime() {
        return mime;
    }

    public boolean isReady() {
        return !TextUtils.isEmpty(path) && width > 0 && height > 0;
    }

    public boolean isGif() {
        return mime != null && mime.contains("gif");
    }
}
