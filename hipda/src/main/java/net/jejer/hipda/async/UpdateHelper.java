package net.jejer.hipda.async;

import android.app.Activity;
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
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.HiProgressDialog;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Set;

/**
 * check and download update file
 * Created by GreenSkinMonster on 2015-03-09.
 */
public class UpdateHelper {

    private Activity mCtx;
    private boolean mSilent;

    private HiProgressDialog pd;

    private String checkSite = "";
    private String checkUrl = "";
    private String downloadUrl = "";

    public UpdateHelper(Activity ctx, boolean isSilent) {
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

        String version = HiApplication.getAppVersion();

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

            if (!Utils.isFromGooglePlay(mCtx)) {
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

                if (!mCtx.isFinishing())
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
        String currentVersion = HiApplication.getAppVersion();

        if (!currentVersion.equals(installedVersion)) {
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

                Utils.clearCache(context);
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

            if (newer(installedVersion, "2.2.01")) {
                String blacklist = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_BLANKLIST_USERNAMES, "");
                if (blacklist.length() > 0 && blacklist.contains(" ") && !blacklist.contains("\n")) {
                    String[] usernames = blacklist.split(" ");
                    ArrayList<String> names = new ArrayList<>();
                    for (String username : usernames) {
                        if (!TextUtils.isEmpty(username) && !names.contains(username))
                            names.add(username);
                    }
                    HiSettingsHelper.getInstance().setBlanklistUsernames(names);
                }
            }

            HiSettingsHelper.getInstance().setInstalledVersion(currentVersion);
        }
    }

}
