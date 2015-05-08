package net.jejer.hipda.async;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.HiProgressDialog;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.util.Date;

/**
 * Created by GreenSkinMonster on 2015-03-09.
 */
public class UpdateHelper {

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

        String updateUrl = "https://api.github.com/repos/GreenSkinMonster/hipda/releases/latest";
        StringRequest sReq = new HiStringRequest(mCtx, updateUrl, new SuccessListener(), new ErrorListener());
        VolleyHelper.getInstance().add(sReq);
    }

    private class SuccessListener implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {

            String version = HiSettingsHelper.getInstance().getAppVersion();
            String newVersion = HttpUtils.getMiddleString(response, "\"tag_name\":\"", "\"");
            String updateNotes = HttpUtils.getMiddleString(response, "\"body\":\"", "\"");

            final String url = Utils.nullToText(HttpUtils.getMiddleString(response, "\"browser_download_url\":\"", "\"")).trim();
            final String filename = (url.contains("/")) ? url.substring(url.lastIndexOf("/") + 1) : "";

            boolean found = !TextUtils.isEmpty(newVersion)
                    && !TextUtils.isEmpty(url)
                    && !TextUtils.isEmpty(updateNotes)
                    && newer(version, newVersion);

            if (found) {
                if (!mSilent) {
                    pd.dismiss();
                }

                Dialog dialog = new AlertDialog.Builder(mCtx).setTitle("发现新版本 : " + newVersion)
                        .setMessage(updateNotes.replace("\\r\\n", "\n")).
                                setPositiveButton("下载",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    HttpUtils.download(mCtx, url, filename);
                                                } catch (SecurityException e) {
                                                    Logger.e(e);
                                                    Toast.makeText(mCtx, "下载出现错误，请使用浏览器下载\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                if (!mSilent) {
                    pd.dismiss("没有发现新版本", 3000);
                }
            }

        }
    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Logger.e(error);
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
