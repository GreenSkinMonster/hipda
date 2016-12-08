package net.jejer.hipda.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.MyGlideModule;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiApplication;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

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

    public final static String URL_REGEX = "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
    public final static Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    public final static String REPLACE_URL_REGEX = "(" + URL_REGEX + ")";

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
        return HtmlCompat.fromHtml(s).toString().replace((char) 160, (char) 32).replace((char) 65532, (char) 32);
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
        ColorHelper.clear();
        activity.finish();
        activity.startActivity(new Intent(activity.getApplicationContext(), activity.getClass()));
        activity.overridePendingTransition(0, 0);
        System.exit(0);
    }

    public static void cleanShareTempFiles() {
        File destFile = HiApplication.getAppContext().getExternalCacheDir();
        if (destFile != null && destFile.exists() && destFile.isDirectory() && destFile.canWrite()) {
            File[] files = destFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(Constants.FILE_SHARE_PREFIX);
                }
            });
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
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

    public static void clearInternalCache() {
        try {
            File cache = HiApplication.getAppContext().getCacheDir();
            if (cache != null && cache.isDirectory()) {
                deleteDir(cache);
            }
        } catch (Exception ignored) {
        }
    }

    public static void clearOkhttpCache() {
        try {
            File cache = Glide.getPhotoCacheDir(HiApplication.getAppContext(), OkHttpHelper.CACHE_DIR_NAME);
            if (cache != null && cache.isDirectory()) {
                deleteDir(cache);
            }
        } catch (Exception ignored) {
        }
    }

    public static void clearExternalCache() {
        try {
            File cache = HiApplication.getAppContext().getExternalCacheDir();
            if (cache != null && cache.isDirectory()) {
                deleteDir(cache);
            }
        } catch (Exception ignored) {
        }
    }

    public static void clearOutdatedAvatars() {
        long deadline = System.currentTimeMillis() - GlideHelper.AVATAR_CACHE_MILLS;
        File cacheDir = Glide.getPhotoCacheDir(HiApplication.getAppContext(), MyGlideModule.AVATAR_CACHE_DIR_NAME);
        for (File f : cacheDir.listFiles()) {
            if (f.lastModified() < deadline)
                f.delete();
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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
        //708 Bytes
        //100.1 KB
        //2.22 MB
        sizeText = Utils.nullToText(sizeText).trim().toUpperCase();
        try {
            if (sizeText.endsWith("KB")) {
                return Math.round(Double.parseDouble(sizeText.replace("KB", "").trim()) * 1024);
            } else if (sizeText.endsWith("MB")) {
                return Math.round(Double.parseDouble(sizeText.replace("MB", "").trim()) * 1024 * 1024);
            } else if (sizeText.endsWith("BYTES")) {
                return Long.parseLong(sizeText.replace("BYTES", "").trim());
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

    public static String toCountText(String countText) {
        if (countText.length() > 5
                && TextUtils.isDigitsOnly(countText)) {
            long count = Long.parseLong(countText);
            if (count > 99999) {
                DecimalFormat df = new DecimalFormat("#.#");
                return df.format(count * 1.0 / 10000) + "万";
            }
        }
        return countText;
    }

    public static String removeLeadingBlank(String s) {
        int cutIndex = 0;
        boolean match = true;
        String[] blanks = {"<br>", (char) 160 + "", (char) 32 + ""};
        while (match) {
            match = false;
            for (String blank : blanks) {
                if (s.length() > cutIndex + blank.length()
                        && s.substring(cutIndex, cutIndex + blank.length()).equals(blank)) {
                    cutIndex += blank.length();
                    match = true;
                    break;
                }
            }
        }
        if (cutIndex > 0)
            s = s.substring(cutIndex);
        return s;
    }

    public static String replaceUrlWithTag(String content) {
        if (!TextUtils.isEmpty(content) && !content.contains("[/")) {
            return content.replaceAll(REPLACE_URL_REGEX, "[url]$1[/url]");
        }
        return content;
    }

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
            return "";
        }
        return source.substring(start_idx, end_idx);
    }

    public static int getIntFromString(String s) {
        String tmp = s.replaceAll("[^\\d]", "");
        if (!TextUtils.isEmpty(tmp) && TextUtils.isDigitsOnly(tmp)) {
            return Integer.parseInt(tmp);
        } else {
            return 0;
        }
    }

    public static void download(Context ctx, String url, String filename) {
        String authCookie = OkHttpHelper.getInstance().getAuthCookie();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filename)
                || (url.contains(HiUtils.ForumUrlPattern) && TextUtils.isEmpty(authCookie))) {
            Toast.makeText(ctx, "下载信息不完整，无法进行下载", Toast.LENGTH_SHORT).show();
            return;
        }

        if (UIUtils.askForPermission(ctx)) {
            return;
        }

        if (DownloadManagerResolver.resolve(ctx)) {
            DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.addRequestHeader("User-agent", HiUtils.getUserAgent());
            if (url.contains(HiUtils.ForumUrlPattern)) {
                req.addRequestHeader("Cookie", "cdb_auth=" + authCookie);
            }
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            if (filename.toLowerCase().endsWith(".apk"))
                req.setMimeType("application/vnd.android.package-archive");
            dm.enqueue(req);
        }
    }

    public static String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }

}
