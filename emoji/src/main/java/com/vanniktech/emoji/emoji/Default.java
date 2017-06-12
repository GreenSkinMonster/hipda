package com.vanniktech.emoji.emoji;

import com.vanniktech.emoji.R;

/**
 * Created by GreenSkinMonster on 2016-04-11.
 */
public class Default {
    public final static String[] EMOJIS = {
            ":)",
            ":sweat:",
            ":huffy:",
            ":cry:",
            ":titter:",
            ":handshake:",
            ":victory:",
            ":curse:",
            ":dizzy:",
            ":shutup:",
            ":funk:",
            ":loveliness:",
            ":(",
            ":D",
            ":cool:",
            ":mad:",
            ":o",
            ":P",
            ":lol:",
            ":shy:",
            ":sleepy:"
    };

    public final static String[] IMG_SRCS = {
            "default_smile",
            "default_sweat",
            "default_huffy",
            "default_cry",
            "default_titter",
            "default_handshake",
            "default_victory",
            "default_curse",
            "default_dizzy",
            "default_shutup",
            "default_funk",
            "default_loveliness",
            "default_sad",
            "default_biggrin",
            "default_cool",
            "default_mad",
            "default_shocked",
            "default_tongue",
            "default_lol",
            "default_shy",
            "default_sleepy"
    };

    public final static int[] DRAWABLES = {
            R.drawable.default_smile,
            R.drawable.default_sweat,
            R.drawable.default_huffy,
            R.drawable.default_cry,
            R.drawable.default_titter,
            R.drawable.default_handshake,
            R.drawable.default_victory,
            R.drawable.default_curse,
            R.drawable.default_dizzy,
            R.drawable.default_shutup,
            R.drawable.default_funk,
            R.drawable.default_loveliness,
            R.drawable.default_sad,
            R.drawable.default_biggrin,
            R.drawable.default_cool,
            R.drawable.default_mad,
            R.drawable.default_shocked,
            R.drawable.default_tongue,
            R.drawable.default_lol,
            R.drawable.default_shy,
            R.drawable.default_sleepy
    };

    public final static Emoji[] DATA = new Emoji[EMOJIS.length];

    static {
        for (int i = 0; i < EMOJIS.length; i++) {
            DATA[i] = Emoji.fromEmoji(EMOJIS[i]);
        }
    }
}
