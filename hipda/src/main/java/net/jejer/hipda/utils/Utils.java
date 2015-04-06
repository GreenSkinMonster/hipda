package net.jejer.hipda.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Common utils
 * Created by GreenSkinMonster on 2015-03-23.
 */
public class Utils {

    public static String nullToText(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return text;
    }

    public static String shortyTime(String time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d", Locale.US);
        String today = formatter.format(new Date());
        if (time.contains(today)) {
            return time.replace(today, "今天");
        }
        return time;
    }

    public static String trimByClause(String text, int length) {
        if (text.length() <= length)
            return text;
        StringBuilder sb = new StringBuilder();
        sb.append(text.substring(0, length));
        for (int i = length; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '，'
                    && c != '。'
                    && c != '？'
                    && c != '!'
                    && c != '！'
                    && c != '['
                    && c != '\n') {
                sb.append(c);
            } else {
                sb.append(c);
                sb.append(" ....");
                break;
            }
        }
        return sb.toString();
    }

}
