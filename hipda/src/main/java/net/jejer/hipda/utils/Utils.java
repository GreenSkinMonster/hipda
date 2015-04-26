package net.jejer.hipda.utils;

import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Common utils
 * Created by GreenSkinMonster on 2015-03-23.
 */
public class Utils {

    private static Whitelist mWhitelist = null;

    public static String nullToText(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return text;
    }

    public static String trim(String text) {
        return nullToText(text).replace(String.valueOf((char) 160), " ").trim();
    }

    public static String shortyTime(String time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d", Locale.US);
        String today = formatter.format(new Date());
        if (time.contains(today)) {
            return time.replace(today, "今天");
        }
        return time;
    }

    /**
     * return parsable html for TextViewWithEmoticon
     */
    public static String clean(String html) {
        if (mWhitelist == null) {
            mWhitelist = new Whitelist();
            mWhitelist.addTags(
                    "a",
                    "br", "p",
                    "b", "i", "strike", "strong", "u")

                    .addAttributes("a", "href")

                    .addProtocols("a", "href", "http", "https")
            ;
            if (!HiSettingsHelper.getInstance().isEinkModeUIEnabled()) {
                mWhitelist.addTags("font")
                        .addAttributes("font", "color");
            }
        }
        return Jsoup.clean(html, "", mWhitelist, new Document.OutputSettings().prettyPrint(false));
    }

}
