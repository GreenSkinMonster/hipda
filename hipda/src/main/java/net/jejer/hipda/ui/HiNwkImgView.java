package net.jejer.hipda.ui;

import net.jejer.hipda.R;
import net.jejer.hipda.async.VolleyHelper;
import net.jejer.hipda.utils.HiUtils;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.android.volley.toolbox.NetworkImageView;

public class HiNwkImgView extends NetworkImageView {
	private final String LOG_TAG = getClass().getSimpleName();
	private String mUrl;
	private Context mCtx;

	public HiNwkImgView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		mCtx = context;
		setDefaultImageResId(R.drawable.ic_action_picture);
		this.setErrorImageResId(R.drawable.tapatalk_image_broken);
		setOnClickListener(new HiNwkImgViewClickHandler());
		setClickable(true);
	}

	public void setUrl(String url) {
		mUrl = url;
		if (HiUtils.isAutoLoadImg(mCtx)) {
			setImageUrl(url, VolleyHelper.getInstance().getImgLoader());
		} else {
			//set LayoutParams, otherwise default image will not show.
			setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 48));

			setImageUrl("", VolleyHelper.getInstance().getImgLoader());
		}
	}

	private class HiNwkImgViewClickHandler implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
//			Intent intent = new Intent(mCtx, ImgViewActivity.class);
//			intent.putExtra(Intent.EXTRA_TEXT, mUrl);
//
//			mCtx.startActivity(intent);
			
//			Intent i = new Intent(Intent.ACTION_VIEW);
//			i.setData(Uri.parse(mUrl));
//			mCtx.startActivity(i);
			
			LayoutInflater inflater = (LayoutInflater)mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.popup_image, null);
			
			WebView wvImage = (WebView)layout.findViewById(R.id.wv_image);
			wvImage.loadUrl(mUrl);
			wvImage.getSettings().setBuiltInZoomControls(true);
			wvImage.getSettings().setDisplayZoomControls(false);
			wvImage.setBackgroundColor(mCtx.getResources().getColor(R.color.night_background));
			
			ImageButton btnDownload = (ImageButton)layout.findViewById(R.id.btn_download_image);
			btnDownload.setOnClickListener(new ImageButton.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Log.v(LOG_TAG, "btnDownload.onClick");
					DownloadManager dm = (DownloadManager)mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
					DownloadManager.Request req = new DownloadManager.Request(Uri.parse(mUrl));
					req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mUrl.substring(mUrl.lastIndexOf("/")+1));
					dm.enqueue(req);
				}});
			
			PopupWindow popup = new PopupWindow(layout);
			popup.setFocusable(true);
			popup.setBackgroundDrawable(mCtx.getResources().getDrawable(R.drawable.ic_action_picture));
			popup.setOutsideTouchable(true);
			
			WindowManager wm = (WindowManager) mCtx.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			popup.setWidth(size.x-50);
			popup.setHeight(size.y-100);
			popup.showAtLocation(layout, Gravity.CENTER, 0, 25);
		}
	}
}
