package net.jejer.hipda.ui.textstyle;

import android.text.TextUtils;

/**
 * store text style
 * Created by GreenSkinMonster on 2015-04-22.
 */
public class TextStyle {
    private boolean bold;
    private boolean italic;
    private boolean strike;
    private boolean underline;
    private String color;

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        if (!"white".equalsIgnoreCase(color)
                && !"black".equalsIgnoreCase(color))
            this.color = color;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isStrike() {
        return strike;
    }

    public void setStrike(boolean strike) {
        this.strike = strike;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    public void addStyle(String nodeName) {
        if (TextUtils.isEmpty(nodeName))
            return;
        if ("i".equals(nodeName)) {
            setItalic(true);
        } else if ("strong".equals(nodeName)) {
            setBold(true);
        } else if ("u".equals(nodeName)) {
            setUnderline(true);
        } else if ("strike".equals(nodeName)) {
            setStrike(true);
        }
    }

    public String toHtml(String text) {
        //strike is not supported by Html.fromHtml()
        StringBuilder sb = new StringBuilder();
        if (bold)
            sb.append("<b>");
        if (italic)
            sb.append("<i>");
        if (underline)
            sb.append("<u>");
        if (strike)
            sb.append("<strike>");
        if (!TextUtils.isEmpty(color))
            sb.append("<font color=").append(color).append(">");
        sb.append(text);
        if (!TextUtils.isEmpty(color))
            sb.append("</font>");
        if (strike)
            sb.append("</strike>");
        if (underline)
            sb.append("</u>");
        if (italic)
            sb.append("</i>");
        if (bold)
            sb.append("</b>");
        return sb.toString();
    }

    public TextStyle newInstance() {
        TextStyle textStyle = new TextStyle();
        textStyle.bold = this.bold;
        textStyle.italic = this.italic;
        textStyle.strike = this.strike;
        textStyle.underline = this.underline;
        textStyle.color = this.color;
        return textStyle;
    }
}
