package net.jejer.hipda.glide;

/**
 * store loaded image's size
 * Created by GreenSkinMonster on 2015-04-24.
 */
public class ImageReadyInfo {
    private int width;
    private int height;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isReady() {
        return width > 0 && height > 0;
    }
}
