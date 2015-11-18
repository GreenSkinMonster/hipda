package net.jejer.hipda.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.bumptech.glide.Glide;

import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.ui.HiApplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Common utils
 * Created by GreenSkinMonster on 2015-03-23.
 */
public class Utils {

    private static Whitelist mWhitelist = null;
    private static int mScreenWidth = -1;
    private static int mScreenHeight = -1;

    private static String THIS_YEAR;
    private static String TODAY;
    private static String YESTERDAY;
    private static long UPDATE_TIME = 0;

    public static String nullToText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return text.toString();
    }

    public static String trim(String text) {
        return nullToText(text).replace(String.valueOf((char) 160), " ").trim();
    }

    public static int getWordCount(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        return s.length();
    }

    public static String shortyTime(String time) {
        if (TextUtils.isEmpty(time))
            return "";

        if (System.currentTimeMillis() - UPDATE_TIME > 10 * 60 * 1000 || THIS_YEAR == null) {
            SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-M-d", Locale.US);
            SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy", Locale.US);
            Date now = new Date();
            THIS_YEAR = yearFormatter.format(now) + "-";
            TODAY = dayFormatter.format(now);
            YESTERDAY = dayFormatter.format(new Date(now.getTime() - 24 * 60 * 60 * 1000));
            UPDATE_TIME = System.currentTimeMillis();
        }

        if (time.contains(TODAY)) {
            time = time.replace(TODAY, "今天");
        } else if (time.contains(YESTERDAY)) {
            time = time.replace(YESTERDAY, "昨天");
        } else if (time.startsWith(THIS_YEAR)) {
            time = time.substring(THIS_YEAR.length());
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
                    "b", "i", "strike", "strong", "u",
                    "font")

                    .addAttributes("a", "href")
                    .addAttributes("font", "color")

                    .addProtocols("a", "href", "http", "https");
        }
        return Jsoup.clean(html, "", mWhitelist, new Document.OutputSettings().prettyPrint(false));
    }

    public static CharSequence fromHtmlAndStrip(String s) {
        return Html.fromHtml(s).toString().replace((char) 160, (char) 32).replace((char) 65532, (char) 32);
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getImageFileName(String prefix, String mime) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss", Locale.US);
        String filename = prefix + "_" + formatter.format(new Date());

        String suffix = getImageFileSuffix(mime);
        return filename + "." + suffix;
    }

    public static String getImageFileSuffix(String mime) {
        String suffix = "jpg";
        if (mime.toLowerCase().contains("gif")) {
            suffix = "gif";
        } else if (mime.toLowerCase().contains("png")) {
            suffix = "png";
        } else if (mime.toLowerCase().contains("bmp")) {
            suffix = "bmp";
        }
        return suffix;
    }

    public static String formatDate(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatDate(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        return formatter.format(date);
    }

    public static boolean isInTimeRange(String begin, String end) {
        try {
            //format hh:mm
            String[] bPieces = begin.split(":");
            int bHour = Integer.parseInt(bPieces[0]);
            int bMinute = Integer.parseInt(bPieces[1]);

            String[] ePieces = end.split(":");
            int eHour = Integer.parseInt(ePieces[0]);
            int eMinute = Integer.parseInt(ePieces[1]);


            Calendar now = Calendar.getInstance();
            Calendar beginCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();

            beginCal.set(Calendar.HOUR_OF_DAY, bHour);
            beginCal.set(Calendar.MINUTE, bMinute);
            beginCal.set(Calendar.SECOND, 0);
            beginCal.set(Calendar.MILLISECOND, 0);

            endCal.set(Calendar.HOUR_OF_DAY, eHour);
            endCal.set(Calendar.MINUTE, eMinute);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);

            if (endCal.before(beginCal)) {
                endCal.add(Calendar.DATE, 1);
            }
            if (beginCal.after(now)) {
                beginCal.add(Calendar.DATE, -1);
                endCal.add(Calendar.DATE, -1);
            }

            return now.getTimeInMillis() >= beginCal.getTimeInMillis() && now.getTimeInMillis() <= endCal.getTimeInMillis();
        } catch (Exception e) {
            Logger.e(e);
            return false;
        }
    }

    public static String textToHtmlConvertingURLsToLinks(String text) {
        return nullToText(text).replaceAll("(\\A|\\s)((http|https):\\S+)(\\s|\\z)",
                "$1<a href=\"$2\">$2</a>$4");
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static int getScreenWidth() {
        if (mScreenWidth <= 0) {
            WindowManager wm = (WindowManager) HiApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mScreenWidth = Math.min(size.x, size.y);
            mScreenHeight = Math.max(size.x, size.y);
        }
        return mScreenWidth;
    }

    public static int getScreenHeight() {
        if (mScreenHeight <= 0) {
            WindowManager wm = (WindowManager) HiApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mScreenWidth = Math.min(size.x, size.y);
            mScreenHeight = Math.max(size.x, size.y);
        }
        return mScreenHeight;
    }

    public static void restartActivity(Activity activity) {
        ColorUtils.clear();
        activity.finish();
        activity.startActivity(new Intent(activity.getApplicationContext(), activity.getClass()));
        activity.overridePendingTransition(0, 0);
        System.exit(0);
    }

    public static void cleanShareTempFiles() {
        File destFile = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        if (destFile.exists() && destFile.isDirectory()) {
            File[] files = destFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(Constants.FILE_SHARE_PREFIX);
                }
            });
            for (File f : files) {
                f.delete();
            }
        }
    }

    public static boolean isMemoryUsageHigh() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) > 0.6f * runtime.maxMemory();
    }

    public static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        DecimalFormat df = new DecimalFormat("#.##");
        Logger.e("\nmax=" + df.format(runtime.maxMemory() * 1.0f / 1024 / 1024) + "M"
                + "\ntotal=" + df.format(runtime.totalMemory() * 1.0f / 1024 / 1024) + "M"
                + "\nfree=" + df.format(runtime.freeMemory() * 1.0f / 1024 / 1024) + "M"
                + "\nused=" + df.format((runtime.totalMemory() - runtime.freeMemory()) * 1.0f / 1024 / 1024) + "M"
                + "\nusage=" + df.format((runtime.totalMemory() - runtime.freeMemory()) * 100.0f / runtime.maxMemory()) + "%");
    }

    private static boolean deleteDir(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(file, aChildren));
                    if (!success) {
                        return false;
                    }
                }
            }
            return file.delete();
        }
        return false;
    }

    public static void clearCache(Context context) {
        try {
            File cache = context.getCacheDir();
            if (cache != null && cache.isDirectory()) {
                deleteDir(cache);
            }
        } catch (Exception ignored) {
        }
    }

    public static void clearOutdatedAvatars(Context context) {
        long deadline = System.currentTimeMillis() - GlideHelper.AVATAR_CACHE_MILLS;
        File cacheDir = Glide.getPhotoCacheDir(context, "avatar");
        for (File f : cacheDir.listFiles()) {
            if (f.lastModified() < deadline)
                f.delete();
        }
    }

    public static boolean isFromGooglePlay(Context context) {
        try {
            String installer = context.getPackageManager()
                    .getInstallerPackageName(context.getPackageName());
            return "com.android.vending".equals(installer);
        } catch (Throwable ignored) {
        }
        return false;
    }


    public static long parseSizeText(String sizeText) {
        //100.1 KB
        //2.22 MB
        sizeText = Utils.nullToText(sizeText).trim().toUpperCase();
        try {
            if (sizeText.endsWith("KB")) {
                return Math.round(Double.parseDouble(sizeText.replace("KB", "").trim()) * 1024);
            } else if (sizeText.endsWith("MB")) {
                return Math.round(Double.parseDouble(sizeText.replace("MB", "").trim()) * 1024 * 1024);
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    public static String toSizeText(long fileSize) {
        DecimalFormat df = new DecimalFormat("#.#");
        if (fileSize > 1024 * 1024) {
            return df.format(fileSize * 1.0 / 1024 / 1024) + " MB";
        }
        return df.format(fileSize * 1.0 / 1024) + " KB";
    }

}
