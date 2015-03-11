package net.jejer.hipda.async;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.HiProgressDialog;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

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
		HiSettingsHelper.getInstance().setUpdateChecked(true);
		HiSettingsHelper.getInstance().setLastUpdateCheckTime(new Date());

		String url = HiUtils.UpdateUrl;

		StringRequest sReq = new HiStringRequest(mCtx, url, new SuccessListener(), new ErrorListener());
		VolleyHelper.getInstance().add(sReq);
	}

	private class SuccessListener implements Response.Listener<String> {
		@Override
		public void onResponse(String response) {

			String version = HiSettingsHelper.getInstance().getAppVersion();
			String newVersion = "", url = "", filename = "";
			String firstAttachment = HttpUtils.getMiddleString(response, "<a href=\"attachment.php?", "</a>");
			boolean found = false;

			if (firstAttachment != null && firstAttachment.contains("sid=") && firstAttachment.contains("hipda-release-")) {
				String args = firstAttachment.substring(0, firstAttachment.indexOf("\""));
				url = HiUtils.BaseUrl + "attachment.php?" + args;
				filename = HttpUtils.getMiddleString(firstAttachment, "<strong>", "</strong>");
				newVersion = HttpUtils.getMiddleString(filename, "hipda-release-", ".apk");

				found = !TextUtils.isEmpty(args) && !TextUtils.isEmpty(filename) && newer(version, newVersion);
			}

			if (found) {
				if (mSilent) {
					Toast.makeText(mCtx, "发现新版本 " + newVersion + "，请在设置中检查更新", Toast.LENGTH_LONG).show();
				} else {
					pd.setMessage("发现新版本 " + newVersion + "，正在下载...");
					DownloadManager dm = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
					DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
					req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
					dm.enqueue(req);
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
				pd.dismiss("检查新版本时发生错误", 3000);
			}
		}
	}

	private boolean newer(String version, String newVersion) {
		//version format #.#.##
		try {
			return Integer.parseInt(newVersion.replace(".", "")) > Integer.parseInt(version.replace(".", ""));
		} catch (Exception ignored) {
		}
		return false;
	}

}
