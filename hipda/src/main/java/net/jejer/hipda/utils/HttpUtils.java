package net.jejer.hipda.utils;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.MainFrameActivity;

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

    public static void download(Context ctx, String url, String filename) throws SecurityException {
        String authCookie = OkHttpHelper.getInstance().getAuthCookie();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filename)
                || (url.startsWith(HiUtils.BaseUrl) && TextUtils.isEmpty(authCookie))) {
            Toast.makeText(ctx, "下载信息不完整，无法进行下载", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ctx, "需要在权限管理中授权存储空间权限", Toast.LENGTH_SHORT).show();
            if (ctx instanceof Activity)
                ActivityCompat.requestPermissions((Activity) ctx,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MainFrameActivity.PERMISSIONS_REQUEST_CODE);
            return;
        }

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
