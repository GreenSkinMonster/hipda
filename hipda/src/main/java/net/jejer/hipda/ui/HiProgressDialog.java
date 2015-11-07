package net.jejer.hipda.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;

/**
 * a simple progress dialog
 * Created by GreenSkinMonster on 2015-03-11.
 */
public class HiProgressDialog extends ProgressDialog {

    public final static int INFO = 0;
    public final static int ERROR = 9;

    public HiProgressDialog(Context context) {
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
                    GoogleMaterial.Icon.gmd_alert_octagon).sizeDp(48)
                    .color(ContextCompat.getColor(getContext(), R.color.red)));
        } else {
            setIndeterminateDrawable(new IconicsDrawable(getContext(),
                    GoogleMaterial.Icon.gmd_info).sizeDp(48)
                    .color(ContextCompat.getColor(getContext(), R.color.md_green_500)));
        }
        setIndeterminate(true);
        new CountDownTimer(millisToWait, millisToWait) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                dismiss();
            }
        }.start();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setCancelable(false);
        if (getWindow() != null)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    public static HiProgressDialog show(Context context, String message) {
        HiProgressDialog progressDialog = new HiProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
        progressDialog.setCancelable(false);
        return progressDialog;
    }

}
