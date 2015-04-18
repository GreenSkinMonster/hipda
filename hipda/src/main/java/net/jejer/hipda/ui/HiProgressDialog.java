package net.jejer.hipda.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;

/**
 * a simple progress dialog
 * Created by GreenSkinMonster on 2015-03-11.
 */
public class HiProgressDialog extends ProgressDialog {

    public HiProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    public void dismiss(String message) {
        dismiss(message, 1000);
    }

    public void dismiss(String message, int millisToWait) {
        if (message != null)
            setMessage(message);
        setCancelable(true);
        setIndeterminateDrawable(getContext().getResources().getDrawable(android.R.drawable.ic_dialog_info));
        setIndeterminate(true);
        new CountDownTimer(millisToWait, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                dismiss();
            }
        }.start();
    }

    public static HiProgressDialog show(Context context, String message) {
        HiProgressDialog progressDialog = new HiProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
        return progressDialog;
    }

}
