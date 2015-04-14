package net.jejer.hipda.utils;

/**
 * simple bean for selected image file
 * Created by GreenSkinMonster on 2015-04-14.
 */
public class ImageFileInfo {
    private String filePath;
    private int orientation = -1;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getFileName() {
        if (filePath != null && filePath.contains("/"))
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        return filePath;
    }
}
