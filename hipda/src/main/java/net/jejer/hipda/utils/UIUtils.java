package net.jejer.hipda.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.MainFrameActivity;

import java.io.File;

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

        final TextView textView = (TextView) viewlayout.findViewById(R.id.tv_select_text);
        textView.setText(detail);
        textView.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        UIUtils.setLineSpacing(textView);

        alertDialog.setView(viewlayout);
        alertDialog.setNegativeButton(context.getResources().getString(R.string.action_close), null);
        alertDialog.show();
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

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void shareImage(Context context, String url) {
        if (UIUtils.askForPermission(context)) {
            return;
        }

        ImageInfo imageInfo = ImageContainer.getImageInfo(url);
        if (imageInfo == null || !imageInfo.isReady()) {
            Toast.makeText(context, "文件还未下载完成", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String filename = Utils.getImageFileName(Constants.FILE_SHARE_PREFIX, imageInfo.getMime());
            File cacheDirectory = HiApplication.getAppContext().getExternalCacheDir();
            File destFile = new File(cacheDirectory, filename);
            Utils.copy(new File(imageInfo.getPath()), destFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(imageInfo.getMime());
            Uri uri = Uri.fromFile(destFile);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(shareIntent, "分享图片"));
        } catch (Exception e) {
            Logger.e(e);
            Toast.makeText(context, "分享时发生错误", Toast.LENGTH_LONG).show();
        }
    }

    public static Boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
