package net.jejer.hipda.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.okhttp.OkHttpHelper;

import java.io.File;

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

    public static void saveImage(Context context, String url) {
        try {
            if (UIUtils.askForPermission(context)) {
                return;
            }

            ImageInfo imageInfo = ImageContainer.getImageInfo(url);
            if (imageInfo == null || !imageInfo.isReady()) {
                Toast.makeText(context, "文件还未下载完成", Toast.LENGTH_SHORT).show();
                return;
            }

            String filename = Utils.getImageFileName("Hi_IMG", imageInfo.getMime());
            File destFile = new File(getSaveFolder(), filename);
            Utils.copy(new File(imageInfo.getPath()), destFile);
            Toast.makeText(context, "图片已经保存 <" + filename + ">", Toast.LENGTH_SHORT).show();
            //HttpUtils.download(mCtx, url, filename);

            MediaScannerConnection.scanFile(context, new String[]{destFile.getPath()}, null, null);
        } catch (Exception e) {
            Logger.e(e);
            Toast.makeText(context, "保存图片文件时发生错误，请使用浏览器下载\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static File getSaveFolder() {
        String saveFolder = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_SAVE_FOLDER, "");
        File dir = new File(saveFolder);
        if (saveFolder.startsWith("/") && dir.exists() && dir.isDirectory() && dir.canWrite()) {
            return dir;
        }
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        HiSettingsHelper.getInstance().setStringValue(HiSettingsHelper.PERF_SAVE_FOLDER, dir.getAbsolutePath());
        return dir;
    }

}
