package net.jejer.hipda.async;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.widget.HiProgressDialog;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.Date;

import okhttp3.Request;

/**
 * check and download update file
 * Created by GreenSkinMonster on 2015-03-09.
 */
public class UpdateHelper {

    private Activity mCtx;
    private boolean mSilent;

    private HiProgressDialog pd;

    private String checkUrl = "";
    private String downloadUrl = "";

    public UpdateHelper(Activity ctx, boolean isSilent) {
        mCtx = ctx;
        mSilent = isSilent;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            checkUrl = "https://coding.net/u/GreenSkinMonster/p/hipda/git/raw/master/hipda-ng.md";
        } else {
            checkUrl = "https://coding.net/u/GreenSkinMonster/p/hipda/git/raw/master/hipda-ng-v5.md";
        }
        downloadUrl = "https://coding.net/u/GreenSkinMonster/p/hipda/git/raw/master/releases/hipda-ng-release-{version}.apk";
    }

    public void check() {
        HiSettingsHelper.getInstance().setAutoUpdateCheck(true);
        HiSettingsHelper.getInstance().setLastUpdateCheckTime(new Date());

        if (mSilent) {
            doCheck();
        } else {
            new Thread(this::doCheck).start();
        }
    }

    private void doCheck() {
        if (!mSilent) {
            new Handler(Looper.getMainLooper()).post(() -> pd = HiProgressDialog.show(mCtx, "正在检查新版本，请稍候..."));
        }
        OkHttpHelper.getInstance().asyncGet(checkUrl, new UpdateCheckCallback());
    }

    private class UpdateCheckCallback implements OkHttpHelper.ResultCallback {

        @Override
        public void onError(Request request, Exception e) {
            Logger.e(e);
            if (!HiApplication.isAppVisible())
                return;
            if (!mSilent) {
                pd.dismissError("检查新版本时发生错误 : " + OkHttpHelper.getErrorMessage(e));
            }
        }

        @Override
        public void onResponse(final String response) {
            processUpdate(response);
        }
    }

    private void processUpdate(String response) {
        if (!HiApplication.isAppVisible())
            return;

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
                && newer(newVersion, version);

        if (found) {
            if (!mSilent) {
                pd.dismiss();
            }

            if (!Utils.isFromGooglePlay(mCtx)) {
                final String url = downloadUrl.replace("{version}", newVersion);
                final String filename = (url.contains("/")) ? url.substring(url.lastIndexOf("/") + 1) : "";

                Dialog dialog = new AlertDialog.Builder(mCtx).setTitle("发现新版本 : " + newVersion)
                        .setMessage(updateNotes)
                        .setPositiveButton("下载",
                                (dialog1, which) -> {
                                    try {
                                        Utils.download(mCtx, url, filename);
                                    } catch (Exception e) {
                                        Logger.e(e);
                                        UIUtils.toast("下载出现错误，请到客户端发布帖中手动下载。\n" + e.getMessage());
                                    }
                                })
                        .setNegativeButton("暂不", (dialog2, which) -> {
                        })
                        .setNeutralButton("不再提醒", (dialog3, which) -> HiSettingsHelper.getInstance().setAutoUpdateCheck(false))
                        .create();

                if (!mCtx.isFinishing())
                    dialog.show();
            } else {
                Dialog dialog = new AlertDialog.Builder(mCtx)
                        .setTitle("发现新版本 : " + newVersion)
                        .setMessage(updateNotes).
                        setPositiveButton("前往Google Play",
                                (dialog4, which) -> openGooglePlay(mCtx)).create();

                if (!mCtx.isFinishing())
                    dialog.show();
            }
        } else {
            if (!mSilent) {
                pd.dismiss("没有发现新版本");
            }
        }
    }

    private static void openGooglePlay(Context context) {
        final String appPackageName = HiApplication.getAppContext().getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private static boolean newer(String version1, String version2) {
        //version format #.#.##
        if (TextUtils.isEmpty(version2))
            return false;
        try {
            return Integer.parseInt(version1.replace(".", "")) > Integer.parseInt(version2.replace(".", ""));
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean updateApp() {
        HiSettingsHelper.getInstance().migrateEncrytSettings();

        final String installedVersion = HiSettingsHelper.getInstance().getInstalledVersion();
        final String currentVersion = HiApplication.getAppVersion();

        if (!currentVersion.equals(installedVersion)) {
            if (newer("5.0.03", installedVersion)) {
                HiSettingsHelper.getInstance().setForumServer(HiUtils.ForumServer);
                HiSettingsHelper.getInstance().setImageHost(HiUtils.ImageHost);
                String tailUrl = HiSettingsHelper.getInstance().getTailUrl();
                HiSettingsHelper.getInstance()
                        .setTailUrl(HiUtils.replaceOldDomain(tailUrl));
            }
            HiSettingsHelper.getInstance().setInstalledVersion(currentVersion);
        }
        return newer(currentVersion, installedVersion);
    }

}
