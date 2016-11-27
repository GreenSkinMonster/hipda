package net.jejer.hipda.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import net.jejer.hipda.okhttp.OkHttpHelper;

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
            return "";
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

    public static void download(Context ctx, String url, String filename) {
        String authCookie = OkHttpHelper.getInstance().getAuthCookie();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filename)
                || (url.startsWith(HiUtils.BaseUrl) && TextUtils.isEmpty(authCookie))) {
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
            if (url.startsWith(HiUtils.BaseUrl)) {
                req.addRequestHeader("Cookie", "cdb_auth=" + authCookie);
            }
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            if (filename.toLowerCase().endsWith(".apk"))
                req.setMimeType("application/vnd.android.package-archive");
            dm.enqueue(req);
        }
    }

}
