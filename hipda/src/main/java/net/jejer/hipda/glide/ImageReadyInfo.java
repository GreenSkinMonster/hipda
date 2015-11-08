package net.jejer.hipda.glide;

import android.text.TextUtils;

import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Utils;

/**
 * store loaded image's size
 * Created by GreenSkinMonster on 2015-04-24.
 */
public class ImageReadyInfo {
    private int width;
    private int height;
    private String path;
    private String mime;
    private int orientation;

    int maxViewWidth;
    int displayWidth;
    int displayHeight;

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

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getDisplayHeight() {
        return getDisplaySize(false);
    }

    public int getDisplayWidth() {
        return getDisplaySize(true);
    }

    private int getDisplaySize(boolean isWidth) {
        //calculate ImageView size for image to display

        //leave 12dp on both left and right side, this should match layout setup
        int tmpMaxViewWidth = Utils.getScreenWidth() - Utils.dpToPx(HiApplication.getAppContext(), 12 * 2);

        if (maxViewWidth != tmpMaxViewWidth) {
            maxViewWidth = tmpMaxViewWidth;

            //if image width < half maxViewWidth, scale it up for better view
            int maxScaleWidth = Math.round(maxViewWidth * 0.5f);

            double scaleRate = getScaleRate(width);
            int scaledWidth = Math.round((int) (width * scaleRate));
            int scaledHeight = Math.round((int) (height * scaleRate));

            if (scaledWidth >= maxScaleWidth ||
                    (isGif() && scaledWidth >= maxScaleWidth / 2)) {
                displayWidth = maxViewWidth;
                displayHeight = Math.round(maxViewWidth * 1.0f * height / width);
            } else {
                displayWidth = scaledWidth;
                displayHeight = scaledHeight;
            }
            //at last, limit ImageView to 2xScreen.Long.Side size
            if (displayHeight > 2 * Utils.getScreenHeight())
                displayHeight = 2 * Utils.getScreenHeight();
        }

        if (isWidth)
            return displayWidth;
        else
            return displayHeight;
    }

    //Math! http://www.mathsisfun.com/data/function-grapher.php
    private double getScaleRate(int x) {
        return Math.pow(x, 1.2) / x;
    }
}
