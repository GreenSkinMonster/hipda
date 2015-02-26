package net.jejer.hipda.ui;

import net.jejer.hipda.R;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

public class AboutDialogPreference extends DialogPreference {
	private final String LOG_TAG = getClass().getSimpleName();

	public AboutDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Log.v(LOG_TAG, "onClick");
	}

	@Override
	protected View onCreateDialogView () {
		Log.v(LOG_TAG, "onCreateDialogView");
		//return null;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.dialog_about, null);
	}

	@Override
	protected void onBindDialogView (View view) {
		Log.v(LOG_TAG, "onBindDialogView");
		WebView wv = (WebView)view;
		wv.loadUrl("file:///android_asset/html/about.html");
	}
}
