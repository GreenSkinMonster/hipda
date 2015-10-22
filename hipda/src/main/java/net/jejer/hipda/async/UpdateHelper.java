package net.jejer.hipda.async;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.squareup.okhttp.Request;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiProgressDialog;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.Utils;

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.Set;

/**
 * check and download update file
 * Created by GreenSkinMonster on 2015-03-09.
 */
public class UpdateHelper {

    private Context mCtx;
    private boolean mSilent;

    private HiProgressDialog pd;

    private String checkSite = "";
    private String checkUrl = "";
    private String downloadUrl = "";

    public UpdateHelper(Context ctx, boolean isSilent) {
        mCtx = ctx;
        mSilent = isSilent;

        Random random = new Random();
        if (random.nextBoolean()) {
            checkSite = "gitcafe";
            checkUrl = "https://gitcafe.com/GreenSkinMonster/hipda/raw/master/hipda-ng.md";
            downloadUrl = "https://gitcafe.com/GreenSkinMonster/hipda/raw/master/releases/hipda-ng-release-{version}.apk";
        } else {
            checkSite = "coding";
            checkUrl = "https://coding.net/u/GreenSkinMonster/p/hipda/git/raw/master/hipda-ng.md";
            downloadUrl = "https://coding.net/u/GreenSkinMonster/p/hipda/git/raw/master/releases/hipda-ng-release-{version}.apk";
        }
    }

    public void check() {
        HiSettingsHelper.getInstance().setAutoUpdateCheck(true);
        HiSettingsHelper.getInstance().setLastUpdateCheckTime(new Date());

        if (mSilent) {
            doCheck();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doCheck();
                }
            }).start();
        }
    }

    private void doCheck() {
        if (!mSilent) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    pd = HiProgressDialog.show(mCtx, "正在检查新版本，请稍候...");
                }
            });
        }
        OkHttpHelper.getInstance().asyncGet(checkUrl, new UpdateCheckCallback());
    }

    private class UpdateCheckCallback implements OkHttpHelper.ResultCallback {

        @Override
        public void onError(Request request, Exception e) {
            Logger.e(e);
            if (!mSilent) {
                pd.dismissError("检查新版本时发生错误 (" + checkSite + ") : " + OkHttpHelper.getErrorMessage(e));
            }
        }

        @Override
        public void onResponse(final String response) {
            processUpdate(response);
        }
    }

    private void processUpdate(String response) {
        response = Utils.nullToText(response).replace("\r\n", "\n").trim();

        String version = HiSettingsHelper.getInstance().getAppVersion();

        String newVersion = "";
        String updateNotes = "";

        int firstLineIndex = response.indexOf("\n");
        if (response.startsWith("v") && firstLineIndex > 0) {
            newVersion = response.substring(1, firstLineIndex).trim();
            updateNotes = response.substring(firstLineIndex + 1).trim();
        }

        boolean found = !TextUtils.isEmpty(newVersion)
                && !TextUtils.isEmpty(updateNotes)
                && newer(version, newVersion);

        if (found) {
            if (!mSilent) {
                pd.dismiss();
            }

            if (!isFromGooglePlay(mCtx)) {
                final String url = downloadUrl.replace("{version}", newVersion);
                final String filename = (url.contains("/")) ? url.substring(url.lastIndexOf("/") + 1) : "";

                Dialog dialog = new AlertDialog.Builder(mCtx).setTitle("发现新版本 : " + newVersion)
                        .setMessage(updateNotes).
                                setPositiveButton("下载",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    HttpUtils.download(mCtx, url, filename);
                                                } catch (SecurityException e) {
                                                    Logger.e(e);
                                                    Toast.makeText(mCtx, "抱歉，下载出现错误，请到客户端发布帖中手动下载。\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).setNegativeButton("暂不", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setNeutralButton("不再提醒", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HiSettingsHelper.getInstance().setAutoUpdateCheck(false);
                            }
                        }).create();

                dialog.show();
            } else {
                Toast.makeText(mCtx, "发现新版本 : " + newVersion + "，请在应用商店中更新", Toast.LENGTH_SHORT).show();
            }

        } else {
            if (!mSilent) {
                pd.dismiss("没有发现新版本");
            }
        }
    }

    private static boolean newer(String version, String newVersion) {
        //version format #.#.##
        if (TextUtils.isEmpty(newVersion))
            return false;
        try {
            return Integer.parseInt(newVersion.replace(".", "")) > Integer.parseInt(version.replace(".", ""));
        } catch (Exception ignored) {
        }
        return false;
    }

    public static void updateApp(Context context) {
        String installedVersion = HiSettingsHelper.getInstance().getInstalledVersion();
        String currentVersion = HiSettingsHelper.getInstance().getAppVersion();
        if (TextUtils.isEmpty(installedVersion)) {
            // <= v2.0.02

            //add default forums, BS/Eink/Geek
            Set<String> forums = HiSettingsHelper.getInstance().getForums();
            if (!forums.contains("6"))
                forums.add("6");
            if (!forums.contains("7"))
                forums.add("7");
            if (!forums.contains("59"))
                forums.add("59");
            HiSettingsHelper.getInstance().setForums(forums);

            clearCache(context);
        }
        if (newer("2.0.10", currentVersion)) {
            if (TextUtils.isEmpty(HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_NOTI_SILENT_BEGIN, ""))) {
                HiSettingsHelper.getInstance()
                        .setStringValue(HiSettingsHelper.PERF_NOTI_SILENT_BEGIN, NotificationMgr.DEFAUL_SLIENT_BEGIN);
            }
            if (TextUtils.isEmpty(HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_NOTI_SILENT_END, ""))) {
                HiSettingsHelper.getInstance()
                        .setStringValue(HiSettingsHelper.PERF_NOTI_SILENT_END, NotificationMgr.DEFAUL_SLIENT_END);
            }
        }

        if (!currentVersion.equals(installedVersion)) {
            HiSettingsHelper.getInstance().setInstalledVersion(currentVersion);
        }
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

    public static boolean isFromGooglePlay(Context context) {
        try {
            String installer = context.getPackageManager()
                    .getInstallerPackageName(context.getPackageName());
            return "com.android.vending".equals(installer);
        } catch (Throwable ignored) {
        }
        return false;
    }

}
