package net.jejer.hipda.ui.textstyle;

import android.util.SparseArray;

/**
 * text style holder by dom level
 * Created by GreenSkinMonster on 2015-04-23.
 */
public class TextStyleHolder {

    private SparseArray<TextStyle> textStyles = new SparseArray<>();

    public void addLevel(int level) {
        if (textStyles.get(level - 1) != null)
            textStyles.put(level, textStyles.get(level - 1).newInstance());
        else
            textStyles.put(level, new TextStyle());
    }

    public void removeLevel(int level) {
        textStyles.remove(level);
    }

    public TextStyle getTextStyle(int level) {
        return textStyles.get(level);
    }

    public void addStyle(int level, String style) {
        if (level > 0 && textStyles.get(level) != null)
            textStyles.get(level).addStyle(style);
    }

    public void setColor(int level, String color) {
        if (level > 0 && textStyles.get(level) != null)
            textStyles.get(level).setColor(color);
    }

}
