package net.jejer.hipda.ui.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;

import androidx.core.content.ContextCompat;

/**
 * a simple progress dialog
 * Created by GreenSkinMonster on 2015-03-11.
 */
public class HiProgressDialog extends ProgressDialog {

    private final static int INFO = 0;
    private final static int ERROR = 9;
    private final static int MIN_SHOW_TIME = 300;

    private boolean mAttachedToWindow = false;
    private boolean mDismissed = false;
    private long mMillisToWait = -1;

    private HiProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    public void dismiss(String message) {
        setCancelable(true);
        dismiss(message, 1000, INFO);
    }

    public void dismiss(String message, int millisToWait) {
        setCancelable(true);
        dismiss(message, millisToWait, INFO);
    }

    public void dismissError(String message) {
        setCancelable(true);
        dismiss(message, 3000, ERROR);
    }

    private void dismiss(String message, int millisToWait, int status) {
        setCancelable(true);
        if (message != null)
            setMessage(message);
        if (status == ERROR) {
            setIndeterminateDrawable(new IconicsDrawable(getContext(),
                    GoogleMaterial.Icon.gmd_error).sizeDp(48)
                    .color(ContextCompat.getColor(getContext(), R.color.red)));
        } else {
            setIndeterminateDrawable(new IconicsDrawable(getContext(),
                    GoogleMaterial.Icon.gmd_info).sizeDp(48)
                    .color(ContextCompat.getColor(getContext(), R.color.md_green_500)));
        }
        setIndeterminate(true);
        mMillisToWait = Math.max(millisToWait, MIN_SHOW_TIME);
        dismiss();
    }

    @Override
    public void show() {
        if (!mDismissed)
            super.show();
    }

    @Override
    public void dismiss() {
        mDismissed = true;
        mMillisToWait = Math.max(mMillisToWait, MIN_SHOW_TIME);
        new CountDownTimer(mMillisToWait, mMillisToWait) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (mAttachedToWindow && HiProgressDialog.this.isShowing())
                    HiProgressDialog.super.dismiss();
            }
        }.start();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        setCancelable(false);
        if (getWindow() != null)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void onDetachedFromWindow() {
        mAttachedToWindow = false;
        super.onDetachedFromWindow();
    }

    public static HiProgressDialog show(Context context, String message) {
        HiProgressDialog progressDialog = new HiProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
        progressDialog.setCancelable(false);
        return progressDialog;
    }

}
