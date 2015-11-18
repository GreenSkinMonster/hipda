package net.jejer.hipda.utils;

/**
 * simple bean for selected image file
 * Created by GreenSkinMonster on 2015-04-14.
 */
public class ImageFileInfo {
    private String filePath;
    private int orientation = -1;
    private long fileSize;
    private String mime;
    private int width;
    private int height;

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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMime() {
        return Utils.nullToText(mime).toLowerCase();
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getFileName() {
        if (filePath != null && filePath.contains("/"))
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        return filePath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isGif() {
        return getMime().contains("gif");
    }
}
