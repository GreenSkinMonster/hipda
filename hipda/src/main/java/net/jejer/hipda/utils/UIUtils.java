package net.jejer.hipda.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.MainFrameActivity;

/**
 * Created by GreenSkinMonster on 2016-04-05.
 */
public class UIUtils {

    public static void infoSnack(View view, CharSequence message) {
        if (view != null) {
            makeSnack(view, message, null, Snackbar.LENGTH_SHORT, Color.WHITE)
                    .show();
        }
    }

    public static void errorSnack(View view, CharSequence message, CharSequence detail) {
        if (view != null) {
            makeSnack(view, message, detail, Snackbar.LENGTH_LONG,
                    ContextCompat.getColor(HiApplication.getAppContext(),
                            R.color.md_yellow_500))
                    .show();
        }
    }

    private static Snackbar makeSnack(final View view, final CharSequence message, final CharSequence detail, int length, int textColor) {
        final Snackbar snackbar = Snackbar.make(view, message, length);
        setSnackbarMessageTextColor(snackbar, textColor);

        if (!TextUtils.isEmpty(detail)) {
            snackbar.setAction("详情", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtils.showMessageDialog(view.getContext(),
                            "详细信息",
                            message + "\n" + detail);
                    snackbar.dismiss();

                }
            });
        }
        return snackbar;
    }

    public static void setSnackbarMessageTextColor(Snackbar snackbar, int color) {
        View view = snackbar.getView();
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(color);
    }

    public static void showMessageDialog(Context context, String message, String detail) {

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.item_select_text, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        final TextView tvTitle = (TextView) viewlayout.findViewById(R.id.tv_select_text_title);
        tvTitle.setText(message);
        tvTitle.setTextSize(HiSettingsHelper.getInstance().getTitleTextSize());

        final EditText etText = (EditText) viewlayout.findViewById(R.id.et_select_text);
        etText.setText(detail);
        etText.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        UIUtils.setLineSpacing(etText);

        alertDialog.setView(viewlayout);
        alertDialog.setNegativeButton(context.getResources().getString(R.string.action_close), null);
        alertDialog.show();
        etText.requestFocus();
        if (detail.length() > 0)
            etText.setSelection(0);
    }

    public static boolean askForPermission(Context ctx) {
        if (ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ctx, "需要授予 \"存储空间\" 权限", Toast.LENGTH_SHORT).show();
            if (ctx instanceof Activity)
                ActivityCompat.requestPermissions((Activity) ctx,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MainFrameActivity.PERMISSIONS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    public static void setLineSpacing(TextView textView) {
        float lineSpacingExtra = 2;
        float lineSpacingMultiplier = 1.1f;
        if (HiSettingsHelper.getInstance().getPostLineSpacing() == 1) {
            lineSpacingExtra = 4;
            lineSpacingMultiplier = 1.2f;
        } else if (HiSettingsHelper.getInstance().getPostLineSpacing() == 2) {
            lineSpacingExtra = 6;
            lineSpacingMultiplier = 1.3f;
        } else if (HiSettingsHelper.getInstance().getPostLineSpacing() == 3) {
            lineSpacingExtra = 8;
            lineSpacingMultiplier = 1.4f;
        }
        textView.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier);
    }

}
