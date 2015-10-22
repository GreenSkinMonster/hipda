package net.jejer.hipda.ui.textstyle;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

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

    private static Map<String, String> COLORS = new HashMap<>();

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
        if (!TextUtils.isEmpty(color)) {
            if (!color.startsWith("#") && COLORS.containsKey(color)) {
                color = COLORS.get(color.toLowerCase());
            } else if (color.length() == 5) {
                //color code is #1234 format
                color = color + "00";
            }
            //ignore too light and too dark colors
            if (!color.equals("#000000")
                    && !color.equals("#000")
                    && !color.equals("#ffffff")) {
                this.color = color;
            }
        }
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

    static {

        //there are 140+ color names
        //http://www.w3schools.com/html/html_colornames.asp
        //choose only what we need
        // http://www.hi-pda.com/forum/forumdata/cache/common.js
        //var coloroptions line

//        COLORS.put("aliceblue", "#f0f8ff");
//        COLORS.put("antiquewhite", "#faebd7");
//        COLORS.put("aqua", "#00ffff");
//        COLORS.put("aquamarine", "#7fffd4");
//        COLORS.put("azure", "#f0ffff");
//        COLORS.put("beige", "#f5f5dc");
//        COLORS.put("bisque", "#ffe4c4");
//        COLORS.put("black", "#000000");
//        COLORS.put("blanchedalmond", "#ffebcd");
        COLORS.put("blue", "#0000ff");
//        COLORS.put("blueviolet", "#8a2be2");
//        COLORS.put("brown", "#a52a2a");
//        COLORS.put("burlywood", "#deb887");
//        COLORS.put("cadetblue", "#5f9ea0");
//        COLORS.put("chartreuse", "#7fff00");
//        COLORS.put("chocolate", "#d2691e");
//        COLORS.put("coral", "#ff7f50");
//        COLORS.put("cornflowerblue", "#6495ed");
//        COLORS.put("cornsilk", "#fff8dc");
//        COLORS.put("crimson", "#dc143c");
        COLORS.put("cyan", "#00ffff");
//        COLORS.put("darkblue", "#00008b");
//        COLORS.put("darkcyan", "#008b8b");
//        COLORS.put("darkgoldenrod", "#b8860b");
//        COLORS.put("darkgray", "#a9a9a9");
        COLORS.put("darkgreen", "#006400");
//        COLORS.put("darkkhaki", "#bdb76b");
//        COLORS.put("darkmagenta", "#8b008b");
//        COLORS.put("darkolivegreen", "#556b2f");
        COLORS.put("darkorange", "#ff8c00");
        COLORS.put("darkorchid", "#9932cc");
        COLORS.put("darkred", "#8b0000");
//        COLORS.put("darksalmon", "#e9967a");
//        COLORS.put("darkseagreen", "#8fbc8f");
        COLORS.put("darkslateblue", "#483d8b");
        COLORS.put("darkslategray", "#2f4f4f");
//        COLORS.put("darkturquoise", "#00ced1");
//        COLORS.put("darkviolet", "#9400d3");
//        COLORS.put("deeppink", "#ff1493");
        COLORS.put("deepskyblue", "#00bfff");
        COLORS.put("dimgray", "#696969");
//        COLORS.put("dodgerblue", "#1e90ff");
//        COLORS.put("firebrick", "#b22222");
//        COLORS.put("floralwhite", "#fffaf0");
//        COLORS.put("forestgreen", "#228b22");
//        COLORS.put("fuchsia", "#ff00ff");
//        COLORS.put("gainsboro", "#dcdcdc");
//        COLORS.put("ghostwhite", "#f8f8ff");
//        COLORS.put("gold", "#ffd700");
//        COLORS.put("goldenrod", "#daa520");
        COLORS.put("gray", "#808080");
        COLORS.put("green", "#008000");
//        COLORS.put("greenyellow", "#adff2f");
//        COLORS.put("honeydew", "#f0fff0");
//        COLORS.put("hotpink", "#ff69b4");
//        COLORS.put("indianred ", "#cd5c5c");
        COLORS.put("indigo ", "#4b0082");
//        COLORS.put("ivory", "#fffff0");
//        COLORS.put("khaki", "#f0e68c");
//        COLORS.put("lavender", "#e6e6fa");
//        COLORS.put("lavenderblush", "#fff0f5");
//        COLORS.put("lawngreen", "#7cfc00");
        COLORS.put("lemonchiffon", "#fffacd");
        COLORS.put("lightblue", "#add8e6");
//        COLORS.put("lightcoral", "#f08080");
//        COLORS.put("lightcyan", "#e0ffff");
//        COLORS.put("lightgoldenrodyellow", "#fafad2");
//        COLORS.put("lightgray", "#d3d3d3");
//        COLORS.put("lightgreen", "#90ee90");
//        COLORS.put("lightpink", "#ffb6c1");
//        COLORS.put("lightsalmon", "#ffa07a");
//        COLORS.put("lightseagreen", "#20b2aa");
//        COLORS.put("lightskyblue", "#87cefa");
//        COLORS.put("lightslategray", "#778899");
//        COLORS.put("lightsteelblue", "#b0c4de");
//        COLORS.put("lightyellow", "#ffffe0");
        COLORS.put("lime", "#00ff00");
//        COLORS.put("limegreen", "#32cd32");
//        COLORS.put("linen", "#faf0e6");
        COLORS.put("magenta", "#ff00ff");
//        COLORS.put("maroon", "#800000");
//        COLORS.put("mediumaquamarine", "#66cdaa");
//        COLORS.put("mediumblue", "#0000cd");
//        COLORS.put("mediumorchid", "#ba55d3");
//        COLORS.put("mediumpurple", "#9370db");
//        COLORS.put("mediumseagreen", "#3cb371");
//        COLORS.put("mediumslateblue", "#7b68ee");
//        COLORS.put("mediumspringgreen", "#00fa9a");
        COLORS.put("mediumturquoise", "#48d1cc");
//        COLORS.put("mediumvioletred", "#c71585");
//        COLORS.put("midnightblue", "#191970");
//        COLORS.put("mintcream", "#f5fffa");
//        COLORS.put("mistyrose", "#ffe4e1");
//        COLORS.put("moccasin", "#ffe4b5");
//        COLORS.put("navajowhite", "#ffdead");
        COLORS.put("navy", "#000080");
//        COLORS.put("oldlace", "#fdf5e6");
        COLORS.put("olive", "#808000");
//        COLORS.put("olivedrab", "#6b8e23");
        COLORS.put("orange", "#ffa500");
//        COLORS.put("orangered", "#ff4500");
//        COLORS.put("orchid", "#da70d6");
//        COLORS.put("palegoldenrod", "#eee8aa");
        COLORS.put("palegreen", "#98fb98");
        COLORS.put("paleturquoise", "#afeeee");
//        COLORS.put("palevioletred", "#db7093");
//        COLORS.put("papayawhip", "#ffefd5");
//        COLORS.put("peachpuff", "#ffdab9");
//        COLORS.put("peru", "#cd853f");
        COLORS.put("pink", "#ffc0cb");
        COLORS.put("plum", "#dda0dd");
//        COLORS.put("powderblue", "#b0e0e6");
        COLORS.put("purple", "#800080");
//        COLORS.put("rebeccapurple", "#663399");
        COLORS.put("red", "#ff0000");
//        COLORS.put("rosybrown", "#bc8f8f");
        COLORS.put("royalblue", "#4169e1");
//        COLORS.put("saddlebrown", "#8b4513");
//        COLORS.put("salmon", "#fa8072");
        COLORS.put("sandybrown", "#f4a460");
        COLORS.put("seagreen", "#2e8b57");
//        COLORS.put("seashell", "#fff5ee");
        COLORS.put("sienna", "#a0522d");
        COLORS.put("silver", "#c0c0c0");
//        COLORS.put("skyblue", "#87ceeb");
//        COLORS.put("slateblue", "#6a5acd");
        COLORS.put("slategray", "#708090");
//        COLORS.put("snow", "#fffafa");
//        COLORS.put("springgreen", "#00ff7f");
//        COLORS.put("steelblue", "#4682b4");
//        COLORS.put("tan", "#d2b48c");
        COLORS.put("teal", "#008080");
//        COLORS.put("thistle", "#d8bfd8");
//        COLORS.put("tomato", "#ff6347");
//        COLORS.put("turquoise", "#40e0d0");
//        COLORS.put("violet", "#ee82ee");
        COLORS.put("wheat", "#f5deb3");
//        COLORS.put("white", "#ffffff");
//        COLORS.put("whitesmoke", "#f5f5f5");
        COLORS.put("yellow", "#ffff00");
        COLORS.put("yellowgreen", "#9acd32");
    }
}
