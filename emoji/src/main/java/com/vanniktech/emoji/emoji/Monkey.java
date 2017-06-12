package com.vanniktech.emoji.emoji;

import com.vanniktech.emoji.R;

/**
 * Created by GreenSkinMonster on 2016-04-11.
 */
public class Monkey {
    public final static String[] EMOJIS = {
            "{:2_41:}",
            "{:2_42:}",
            "{:2_43:}",
            "{:2_44:}",
            "{:2_45:}",
            "{:2_46:}",
            "{:2_47:}",
            "{:2_48:}",
            "{:2_49:}",
            "{:2_50:}",
            "{:2_51:}",
            "{:2_52:}",
            "{:2_53:}",
            "{:2_54:}",
            "{:2_55:}",
            "{:2_56:}"
    };

    public final static String[] IMG_SRCS = {
            "coolmonkey_01",
            "coolmonkey_02",
            "coolmonkey_03",
            "coolmonkey_04",
            "coolmonkey_05",
            "coolmonkey_06",
            "coolmonkey_07",
            "coolmonkey_08",
            "coolmonkey_09",
            "coolmonkey_10",
            "coolmonkey_11",
            "coolmonkey_12",
            "coolmonkey_13",
            "coolmonkey_14",
            "coolmonkey_15",
            "coolmonkey_16"
    };

    public final static int[] DRAWABLES = {
            R.drawable.coolmonkey_01,
            R.drawable.coolmonkey_02,
            R.drawable.coolmonkey_03,
            R.drawable.coolmonkey_04,
            R.drawable.coolmonkey_05,
            R.drawable.coolmonkey_06,
            R.drawable.coolmonkey_07,
            R.drawable.coolmonkey_08,
            R.drawable.coolmonkey_09,
            R.drawable.coolmonkey_10,
            R.drawable.coolmonkey_11,
            R.drawable.coolmonkey_12,
            R.drawable.coolmonkey_13,
            R.drawable.coolmonkey_14,
            R.drawable.coolmonkey_15,
            R.drawable.coolmonkey_16
    };

    public final static Emoji[] DATA = new Emoji[EMOJIS.length];

    static {
        for (int i = 0; i < EMOJIS.length; i++) {
            DATA[i] = Emoji.fromEmoji(EMOJIS[i]);
        }
    }
}
