package net.jejer.hipda.async;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.HiProgressDialog;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Utils;

import java.util.Date;

/**
 * Created by GreenSkinMonster on 2015-03-09.
 */
public class UpdateHelper {

    private String LOG_TAG = getClass().getName();

    private Context mCtx;
    private boolean mSilent;

    private HiProgressDialog pd;

    public UpdateHelper(Context ctx, boolean isSilent) {
        mCtx = ctx;
        mSilent = isSilent;
    }

    public void check() {
        if (mSilent) {
            doCheck();
        } else {
            pd = HiProgressDialog.show(mCtx, "正在检查新版本，请稍候...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doCheck();
                }
            }).start();
        }
    }

    private void doCheck() {
        HiSettingsHelper.getInstance().setAutoUpdateCheck(true);
        HiSettingsHelper.getInstance().setLastUpdateCheckTime(new Date());

        String url = HiUtils.UpdateUrl;

        StringRequest sReq = new HiStringRequest(mCtx, url, new SuccessListener(), new ErrorListener());
        VolleyHelper.getInstance().add(sReq);
    }

    private class SuccessListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {

            String version = HiSettingsHelper.getInstance().getAppVersion();
            String newVersion = "";
            final String url;
            final String filename;
            String updateNotes = "";
            if (response.contains("postnum29887924"))
                response = response.substring(response.indexOf("postnum29887924"));
            else
                return;

            boolean found = false;

            String firstAttachment = HttpUtils.getMiddleString(response, "<a href=\"attachment.php?", "</a>");
            if (firstAttachment != null && firstAttachment.contains("hipda-release-")) {
                String args = firstAttachment.substring(0, firstAttachment.indexOf("\""));
                url = HiUtils.BaseUrl + "attachment.php?" + args;
                filename = HttpUtils.getMiddleString(firstAttachment, "<strong>", "</strong>");
                newVersion = HttpUtils.getMiddleString(filename, "hipda-release-", ".apk");
                updateNotes = Utils.nullToText(HttpUtils.getMiddleString(response.substring(response.indexOf("更新记录")), "<ul>", "</ul>"));
                found = !TextUtils.isEmpty(args) && !TextUtils.isEmpty(filename) && newer(version, newVersion);
            } else {
                url = "";
                filename = "";
            }

            if (found) {
                if (!mSilent) {
                    pd.dismiss();
                }

                Dialog dialog = new AlertDialog.Builder(mCtx).setTitle("发现新版本 : " + newVersion).setMessage(
                        updateNotes.replace("<ul>", "").replace("<li>", "").replace("<br />", "")).
                        setPositiveButton("下载",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            DownloadManager dm = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
                                            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                                            req.addRequestHeader("Cookie", "cdb_auth=" + HiSettingsHelper.getInstance().getCookieAuth());
                                            req.addRequestHeader("User-agent", HiUtils.UserAgent);
                                            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                                            dm.enqueue(req);
                                        } catch (Exception e) {
                                            Log.e(LOG_TAG, e.getMessage());
                                            Toast.makeText(mCtx, "下载出现错误，请手动下载\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

                TextView titleView = (TextView) dialog.findViewById(mCtx.getResources().getIdentifier("alertTitle", "id", "android"));
                if (titleView != null) {
                    titleView.setGravity(Gravity.CENTER);
                }

            } else {
                if (!mSilent) {
                    pd.dismiss("没有发现新版本", 3000);
                }
            }

        }
    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(LOG_TAG, error.toString());
            if (!mSilent) {
                pd.dismiss("检查新版本时发生错误 : " + VolleyHelper.getErrorReason(error), 3000);
            }
        }
    }

    private boolean newer(String version, String newVersion) {
        //version format #.#.##
        if (TextUtils.isEmpty(newVersion))
            return false;
        try {
            return Integer.parseInt(newVersion.replace(".", "")) > Integer.parseInt(version.replace(".", ""));
        } catch (Exception ignored) {
        }
        return false;
    }

}
