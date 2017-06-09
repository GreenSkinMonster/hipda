package com.vanniktech.emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.style.ImageSpan;

import com.vanniktech.emoji.emoji.Default;
import com.vanniktech.emoji.emoji.Dumb;
import com.vanniktech.emoji.emoji.DumbDark;
import com.vanniktech.emoji.emoji.Monkey;
import com.vanniktech.emoji.emoji.MonkeyDark;

import java.util.HashMap;
import java.util.Map;

public final class EmojiHandler {
    private static final Map<String, Integer> EMOJIS_MAP = new HashMap<>(Default.EMOJIS.length + Monkey.EMOJIS.length + Dumb.EMOJIS.length);
    private static Map<String, Bitmap> IMAGE_MAP;

    private final static String IMG_MATCH_START = "[attachimg]";
    private final static String IMG_MATCH_END = "[/attachimg]";

    public static void init(boolean isLightTheme) {
        EMOJIS_MAP.clear();
        if (isLightTheme) {
            for (int i = 0; i < Default.EMOJIS.length; i++) {
                EMOJIS_MAP.put(Default.EMOJIS[i], Default.DRAWABLES[i]);
            }
            for (int i = 0; i < Monkey.EMOJIS.length; i++) {
                EMOJIS_MAP.put(Monkey.EMOJIS[i], Monkey.DRAWABLES[i]);
            }
            for (int i = 0; i < Dumb.EMOJIS.length; i++) {
                EMOJIS_MAP.put(Dumb.EMOJIS[i], Dumb.DRAWABLES[i]);
            }
        } else {
            for (int i = 0; i < Default.EMOJIS.length; i++) {
                EMOJIS_MAP.put(Default.EMOJIS[i], Default.DRAWABLES[i]);
            }
            for (int i = 0; i < MonkeyDark.EMOJIS.length; i++) {
                EMOJIS_MAP.put(MonkeyDark.EMOJIS[i], MonkeyDark.DRAWABLES[i]);
            }
            for (int i = 0; i < DumbDark.EMOJIS.length; i++) {
                EMOJIS_MAP.put(DumbDark.EMOJIS[i], DumbDark.DRAWABLES[i]);
            }
        }
    }

    public static void addEmojis(final Context context, final Spannable text, final int emojiSize) {
        final int textLength = text.length();

        // remove spans throughout all text
        final EmojiSpan[] oldSpans = text.getSpans(0, textLength, EmojiSpan.class);
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < oldSpans.length; i++) {
            text.removeSpan(oldSpans[i]);
        }

        int index = 0;
        while (index < textLength) {
            int icon = 0;
            Bitmap bitmap = null;
            int skip = 1;

            String matchStr = text.subSequence(index, Math.min(index + 32, textLength)).toString();
            if (EMOJIS_MAP.containsKey(matchStr)) {
                icon = EMOJIS_MAP.get(matchStr);
                skip = matchStr.length();
            } else if (matchStr.charAt(0) == ':') {
                //match default emoji, max length is 12
                for (int j = 0; j < Default.DATA.length; j++) {
                    if (matchStr.startsWith(Default.EMOJIS[j])) {
                        skip = Default.EMOJIS[j].length();
                        icon = Default.DRAWABLES[j];
                    }
                }
            } else if (matchStr.length() >= 8
                    && matchStr.subSequence(0, 2).equals("{:")) {
                //other emoji, fixed length = 8
                String emoji = matchStr.substring(0, 8);
                if (EMOJIS_MAP.containsKey(emoji)) {
                    skip = 8;
                    icon = EMOJIS_MAP.get(emoji);
                }
            } else if (IMAGE_MAP != null
                    && IMAGE_MAP.size() > 0
                    && matchStr.startsWith(IMG_MATCH_START)
                    && matchStr.indexOf(IMG_MATCH_END) > 0) {
                String imgId = matchStr.substring(IMG_MATCH_START.length(), matchStr.indexOf(IMG_MATCH_END));
                bitmap = IMAGE_MAP.get(imgId);
                if (bitmap != null) {
                    skip = IMG_MATCH_START.length() + imgId.length() + IMG_MATCH_END.length();
                }
            }

            if (icon > 0) {
                text.setSpan(new EmojiSpan(context, icon, emojiSize), index, index + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (bitmap != null) {
                text.setSpan(new ImageSpan(context, bitmap), index, index + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            index += skip;
        }
    }

    public static void addImage(String imgId, Bitmap bitmap) {
        if (IMAGE_MAP == null)
            IMAGE_MAP = new HashMap<>();
        IMAGE_MAP.put(imgId, bitmap);
    }

    private EmojiHandler() {
        throw new AssertionError("No instances.");
    }

    public static void cleanup() {
        if (IMAGE_MAP == null)
            return;
        for (String key : IMAGE_MAP.keySet()) {
            Bitmap bitmap = IMAGE_MAP.get(key);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        IMAGE_MAP.clear();
    }
}
