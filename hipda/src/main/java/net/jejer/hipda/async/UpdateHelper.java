package net.jejer.hipda.async;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.bean.HiSettingsHelper;
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

	private ProgressDialog pd;

	public UpdateHelper(Context ctx, boolean isSilent) {
		mCtx = ctx;
		mSilent = isSilent;
	}

	public void check() {
		if (mSilent) {
			doCheck();
		} else {
			pd = ProgressDialog.show(mCtx, "检查更新", "正在检查新版本，请稍候……");
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
			String firstAttachment = HttpUtils.getMiddleString(response, "<a href=\"attachment.php?", "</a>");
			boolean found = false;

			if (firstAttachment != null && firstAttachment.contains("sid=") && firstAttachment.contains("hipda-release-")) {

				final String url = HiUtils.BaseUrl + "attachment.php?" + firstAttachment.substring(0, firstAttachment.indexOf("\""));
				final String filename = HttpUtils.getMiddleString(firstAttachment, "<strong>", "</strong>");
				String newVersion = HttpUtils.getMiddleString(filename, "hipda-release-", ".apk");

				found = newer(version, newVersion);

				if (found) {
					if (mSilent) {
						Toast.makeText(mCtx, "发现新版本 " + newVersion +"，请在设置中检查更新", Toast.LENGTH_LONG).show();
					} else {
						pd.setMessage("发现新版本 " + newVersion+"，正在下载...");
						pd.setIndeterminateDrawable(null);
						DownloadManager dm = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
						DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
						req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
						req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
						dm.enqueue(req);
					}
				}

			}

			if (!found && !mSilent) {
				pd.setMessage("没有发现新版本");
				pd.setIndeterminateDrawable(null);
			}

			dismiss();
		}
	}

	private class ErrorListener implements Response.ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.e(LOG_TAG, error.toString());
			if (!mSilent) {
				pd.setMessage("检查新版本时发生错误");
				dismiss();
			}
		}
	}

	private void dismiss() {
		if (pd != null) {
			pd.setCancelable(true);
			new CountDownTimer(3000, 1000) {
				public void onTick(long millisUntilFinished) {
				}

				public void onFinish() {
					pd.dismiss();
				}
			}.start();
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
