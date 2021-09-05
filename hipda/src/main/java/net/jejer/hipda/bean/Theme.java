package net.jejer.hipda.bean;

public class Theme {

    private String name;
    private int themeId;
    private int colorId;
    private int textColorId;

    Theme(String name, int themeId, int colorId, int textColorId) {
        this.name = name;
        this.themeId = themeId;
        this.colorId = colorId;
        this.textColorId = textColorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public int getTextColorId() {
        return textColorId;
    }

    public void setTextColorId(int textColorId) {
        this.textColorId = textColorId;
    }
}
