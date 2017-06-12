package com.vanniktech.emoji.emoji;

import com.vanniktech.emoji.R;

/**
 * Created by GreenSkinMonster on 2016-04-11.
 */
public class Dumb {
    public final static String[] EMOJIS = {
            "{:3_57:}",
            "{:3_58:}",
            "{:3_59:}",
            "{:3_60:}",
            "{:3_61:}",
            "{:3_62:}",
            "{:3_63:}",
            "{:3_64:}",
            "{:3_65:}",
            "{:3_66:}",
            "{:3_67:}",
            "{:3_68:}",
            "{:3_69:}",
            "{:3_70:}",
            "{:3_71:}",
            "{:3_72:}",
            "{:3_73:}",
            "{:3_74:}",
            "{:3_75:}",
            "{:3_76:}",
            "{:3_77:}",
            "{:3_78:}",
            "{:3_79:}",
            "{:3_80:}"
    };

    public final static String[] IMG_SRCS = {
            "grapeman_01",
            "grapeman_02",
            "grapeman_03",
            "grapeman_04",
            "grapeman_05",
            "grapeman_06",
            "grapeman_07",
            "grapeman_08",
            "grapeman_09",
            "grapeman_10",
            "grapeman_11",
            "grapeman_12",
            "grapeman_13",
            "grapeman_14",
            "grapeman_15",
            "grapeman_16",
            "grapeman_17",
            "grapeman_18",
            "grapeman_19",
            "grapeman_20",
            "grapeman_21",
            "grapeman_22",
            "grapeman_23",
            "grapeman_24_dark"
    };

    public final static int[] DRAWABLES = {
            R.drawable.grapeman_01,
            R.drawable.grapeman_02,
            R.drawable.grapeman_03,
            R.drawable.grapeman_04,
            R.drawable.grapeman_05,
            R.drawable.grapeman_06,
            R.drawable.grapeman_07,
            R.drawable.grapeman_08,
            R.drawable.grapeman_09,
            R.drawable.grapeman_10,
            R.drawable.grapeman_11,
            R.drawable.grapeman_12,
            R.drawable.grapeman_13,
            R.drawable.grapeman_14,
            R.drawable.grapeman_15,
            R.drawable.grapeman_16,
            R.drawable.grapeman_17,
            R.drawable.grapeman_18,
            R.drawable.grapeman_19,
            R.drawable.grapeman_20,
            R.drawable.grapeman_21,
            R.drawable.grapeman_22,
            R.drawable.grapeman_23,
            R.drawable.grapeman_24
    };

    public final static Emoji[] DATA = new Emoji[EMOJIS.length];

    static {
        for (int i = 0; i < EMOJIS.length; i++) {
            DATA[i] = Emoji.fromEmoji(EMOJIS[i]);
        }
    }
}
