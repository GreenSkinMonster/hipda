package net.jejer.hipda.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.HiApplication;

/**
 * Created by GreenSkinMonster on 2016-04-05.
 */
public class UIUtils {

    public static Snackbar infoSnack(View view, CharSequence message) {
        return makeSnack(view, message, null, Snackbar.LENGTH_SHORT, Color.WHITE);
    }

    public static Snackbar errorSnack(View view, CharSequence message, CharSequence detail) {
        return makeSnack(view, message, detail, Snackbar.LENGTH_LONG,
                ContextCompat.getColor(HiApplication.getAppContext(), R.color.md_red_500));
    }

    public static Snackbar makeSnack(final View view, final CharSequence message, final CharSequence detail, int length, int textColor) {
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

    private static void setSnackbarMessageTextColor(Snackbar snackbar, int color) {
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
        etText.setLineSpacing(4, 1.1f);

        alertDialog.setView(viewlayout);
        alertDialog.setNegativeButton(context.getResources().getString(R.string.action_close), null);
        alertDialog.show();
        etText.requestFocus();
    }

}
