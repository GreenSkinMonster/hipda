package net.jejer.hipda.ui;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;

public class AboutDialogPreference extends DialogPreference {

    public AboutDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.dialog_about, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        String version = HiSettingsHelper.getInstance().getAppVersion();

        TextView versionView = (TextView) view.findViewById(R.id.version);
        versionView.setText(version);

        WebView wv = (WebView) view.findViewById(R.id.credits);
        wv.loadUrl("file:///android_asset/html/about.html");
    }
}
