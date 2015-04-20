package net.jejer.hipda.utils;

import android.util.Log;

public class HttpUtils {

    public static String getMiddleString(String source, String start, String end) {
        int start_idx = source.indexOf(start) + start.length();
        int end_idx = 0;
        if (end.isEmpty()) {
            end_idx = source.length();
        } else {
            end_idx = source.indexOf(end, start_idx);
            if (end_idx <= 0) {
                end_idx = source.length();
            }
        }

        if (start_idx <= 0 || end_idx <= 0 || end_idx <= start_idx) {
            Log.e(HttpUtils.class.getName(), "getValue fail");
            return null;
        }

        return source.substring(start_idx, end_idx);
    }

    public static int getIntFromString(String s) {
        String tmp = s.replaceAll("[^\\d]", "");
        if (!tmp.isEmpty()) {
            return Integer.parseInt(tmp);
        } else {
            return 0;
        }
    }
}
