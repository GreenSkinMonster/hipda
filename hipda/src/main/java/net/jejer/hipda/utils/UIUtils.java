package net.jejer.hipda.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import net.jejer.hipda.BuildConfig;
import net.jejer.hipda.R;
import net.jejer.hipda.async.FileDownTask;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.Theme;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.MainFrameActivity;
import net.jejer.hipda.ui.PostActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

/**
 * Created by GreenSkinMonster on 2016-04-05.
 */
public class UIUtils {

    private final static String IMAGES_DIR = "HiPDA";

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
//        snackbar.setBackgroundTint(Color.DKGRAY);
//        snackbar.setActionTextColor(Color.WHITE);

        if (!TextUtils.isEmpty(detail)) {
            snackbar.setAction("详情", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtils.showMessageDialog(view.getContext(),
                            "详细信息",
                            message + "\n" + detail, true);
                    snackbar.dismiss();

                }
            });
        }
        return snackbar;
    }

    public static Snackbar makeSnackbar(View view, CharSequence sequence, int duration, int textColor) {
        Snackbar snackbar = Snackbar.make(view, sequence, duration);
        setSnackbarMessageTextColor(snackbar, textColor);
//        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.md_grey_800));
//        snackbar.setActionTextColor(Color.WHITE);
        return snackbar;
    }

    public static void setSnackbarMessageTextColor(Snackbar snackbar, int color) {
        View view = snackbar.getView();
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(color);
    }

    public static AlertDialog.Builder getMessageDialogBuilder(final Context context, String message, final String detail) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.item_simple_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final TextView tvTitle = (TextView) viewlayout.findViewById(R.id.tv_dialog_title);
        tvTitle.setText(message);

        final TextView textView = (TextView) viewlayout.findViewById(R.id.tv_dialog_content);
        textView.setText(detail);
        UIUtils.setLineSpacing(textView);

        builder.setView(viewlayout);
        return builder;
    }

    public static void showMessageDialog(final Context context, String message, final String detail, boolean copyable) {
        AlertDialog.Builder builder = getMessageDialogBuilder(context, message, detail);
        builder.setPositiveButton(context.getResources().getString(R.string.action_close), null);
        if (copyable) {
            builder.setNeutralButton(context.getResources().getString(R.string.action_copy),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("COPY FROM HiPDA", detail);
                            clipboard.setPrimaryClip(clip);
                        }
                    });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showReleaseNotesDialog(final Activity activity) {
        String releaseNotes;
        try {
            releaseNotes = Utils.readFromAssets(activity, "release-notes.txt");
        } catch (Exception e) {
            releaseNotes = e.getMessage();
        }

        String info = "*** 问题反馈请到 “设置”-“客户端发布帖”，不要另开新帖或短消息。\n*** 反馈前请阅读1楼红色字体须知，提供必需的信息和详细错误描述。\n\n";

        showMessageDialog(activity, "更新记录", info + releaseNotes, false);
    }

    public static boolean askForStoragePermission(Context ctx) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            UIUtils.toast("需要授予 \"存储空间\" 权限");
            if (ctx instanceof Activity)
                ActivityCompat.requestPermissions((Activity) ctx,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MainFrameActivity.PERMISSIONS_REQUEST_CODE_STORAGE);
            return true;
        }
        return false;
    }

    public static boolean askForBothPermissions(Activity activity) {
        boolean askCamera = !HiSettingsHelper.getInstance().isCameraPermAsked()
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED;
        boolean askStorage = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
        String[] perms = null;
        if (askCamera && askStorage) {
            HiSettingsHelper.getInstance().setCameraPermAsked(true);
            perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else if (askCamera) {
            HiSettingsHelper.getInstance().setCameraPermAsked(true);
            perms = new String[]{Manifest.permission.CAMERA};
        } else if (askStorage) {
            perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            UIUtils.toast("需要授予 \"存储空间\" 权限");
        }
        if (perms != null) {
            ActivityCompat.requestPermissions(activity, perms,
                    PostActivity.PERMISSIONS_REQUEST_CODE_BOTH);
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

    public static void shareImage(final Activity activity, final View view, String url) {
        if (activity == null || view == null)
            return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                && askForStoragePermission(activity))
            return;

        final ImageInfo imageInfo = ImageContainer.getImageInfo(url);
        if (!imageInfo.isSuccess()) {
            FileDownTask fileDownTask = new FileDownTask(activity) {
                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    if (imageInfo.isSuccess())
                        shareImage(activity, view, imageInfo);
                    else
                        errorSnack(view, "分享时发生错误", mException != null ? mException.getMessage() : "");
                }
            };
            fileDownTask.execute(url);
        } else {
            shareImage(activity, view, imageInfo);
        }
    }

    private static void shareImage(Activity activity, final View view, ImageInfo imageInfo) {
        try {
            String filename = Utils.getImageFileName(Constants.FILE_SHARE_PREFIX, imageInfo.getMime());
            File cacheDirectory = HiApplication.getAppContext().getExternalCacheDir();
            File destFile = new File(cacheDirectory, filename);
            Utils.copy(new File(imageInfo.getPath()), destFile);

            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(
                        activity,
                        BuildConfig.APPLICATION_ID + ".provider",
                        destFile);
            } else {
                uri = Uri.fromFile(destFile);
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(imageInfo.getMime());

            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(Intent.createChooser(shareIntent, "分享图片"));
        } catch (Exception e) {
            Logger.e(e);
            errorSnack(view, "分享时发生错误", e.getMessage());
        }
    }

    public static void saveImage(final Activity activity, final View view, String url) {
        if (activity == null || view == null)
            return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && askForStoragePermission(activity))
            return;

        final ImageInfo imageInfo = ImageContainer.getImageInfo(url);
        if (!imageInfo.isSuccess()) {
            FileDownTask fileDownTask = new FileDownTask(activity) {
                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    if (imageInfo.isSuccess()) {
                        saveImage(activity, view, imageInfo);
                    } else {
                        errorSnack(view, "保存时发生错误", mException != null ? mException.getMessage() : "");
                    }
                }
            };
            fileDownTask.execute(url);
        } else {
            saveImage(activity, view, imageInfo);
        }
    }

    private static void saveImage(final Activity activity, final View view, final ImageInfo imageInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageQ(activity, view, imageInfo);
            return;
        }
        try {
            String filename = Utils.getImageFileName("Hi_IMG", imageInfo.getMime());

            File imagesDir = getImagesDir();
            final File destFile = new File(imagesDir, filename);
            Utils.copy(new File(imageInfo.getPath()), destFile);
            MediaScannerConnection.scanFile(activity, new String[]{destFile.getPath()}, null, null);

            Uri destUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                destUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", destFile);
            } else {
                destUri = Uri.fromFile(destFile);
            }
            snackViewSaveImage(activity, view, destUri, imageInfo.getMime());
        } catch (Exception e) {
            Logger.e(e);
            errorSnack(view, "保存文件时发生错误", e.getMessage());
        }
    }

    private static void saveImageQ(final Activity activity, final View view, ImageInfo imageInfo) {
        try {
            String filename = Utils.getImageFileName("Hi_IMG", imageInfo.getMime());
            FileInputStream fis = new FileInputStream(imageInfo.getPath());
            final File destFile;
            final Uri destUri;

            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = activity.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, imageInfo.getMime());
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + IMAGES_DIR);
                destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(destUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                destFile = new File(imagesDir, filename);
                fos = new FileOutputStream(destFile);
                destUri = Uri.fromFile(destFile);
            }

            int i;
            do {
                byte[] buf = new byte[1024];
                i = fis.read(buf);
                fos.write(buf);
            } while (i != -1);

            fos.close();

            snackViewSaveImage(activity, view, destUri, imageInfo.getMime());
        } catch (Exception e) {
            Logger.e(e);
            errorSnack(view, "保存文件时发生错误", e.getMessage());
        }
    }

    private static void snackViewSaveImage(Activity activity, View view, Uri destUri, String mime) {
        Snackbar snackbar = makeSnackbar(view, "文件已保存至 Pictures/" + IMAGES_DIR + " 目录",
                Snackbar.LENGTH_LONG, Color.WHITE);

        snackbar.setAction("查看", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.setDataAndType(destUri, mime);
                    activity.startActivity(intent);
                } catch (Exception e) {
                    errorSnack(view, "打开文件发生错误", e.getMessage());
                }
            }
        });
        snackbar.show();
    }

    private static File getImagesDir() throws IOException {
        String imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                        + "/" + IMAGES_DIR;
        File destDir = new File(imagesDir);
        if (destDir.isFile())
            destDir.delete();
        if (!destDir.exists()) {
            if (!destDir.mkdirs())
                throw new IOException("不能创建目录 " + imagesDir);
        }
        return destDir;
    }

    public static Boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static View getSnackView(Activity activity) {
        if (activity != null)
            return activity.getWindow().getDecorView().getRootView().findViewById(R.id.main_frame_container);
        return null;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static void toast(String text) {
        Toast.makeText(HiApplication.getAppContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static int getRelativeTop(View myView, ViewGroup parentView) {
        Rect offsetViewBounds = new Rect();
        myView.getDrawingRect(offsetViewBounds);
        parentView.offsetDescendantRectToMyCoords(myView, offsetViewBounds);
        return offsetViewBounds.top;
    }

    public static void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) HiApplication.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("COPY FROM HiPDA", text);
        clipboard.setPrimaryClip(clip);
    }

    public static void trimChildMargins(@NonNull ViewGroup vg) {
        final int childCount = vg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = vg.getChildAt(i);

            if (child instanceof ViewGroup) {
                trimChildMargins((ViewGroup) child);
            }

            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) lp).leftMargin = 0;
            }
            child.setBackground(null);
            child.setPadding(0, 0, 0, 0);
        }
    }

    public static void setLightDarkThemeMode() {
        if (HiSettingsHelper.THEME_MODE_AUTO.equals(HiSettingsHelper.getInstance().getTheme())) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (HiSettingsHelper.THEME_MODE_LIGHT.equals(HiSettingsHelper.getInstance().getTheme())) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    public static void setActivityTheme(Activity activity) {
        activity.setTheme(getThemeValue(activity));

        Window window = activity.getWindow();
        View view = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isWhiteTheme(activity)) {
                view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                view.setSystemUiVisibility(view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        if (HiSettingsHelper.getInstance().isNavBarColored()) {
            window.setNavigationBarColor(ColorHelper.getColorPrimary(activity));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (UIUtils.isWhiteTheme(activity)) {
                    view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                } else {
                    view.setSystemUiVisibility(view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                }
            }
        }
    }

    public static boolean isInLightThemeMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                return true;
            case Configuration.UI_MODE_NIGHT_YES:
                return false;
        }
        return true;
    }

    public static boolean isInDarkThemeMode(Context context) {
        return !isInLightThemeMode(context);
    }

    public static int getToolbarTextColor(Context context) {
        return isWhiteTheme(context) ? Color.BLACK : Color.WHITE;
    }

    public static boolean isWhiteTheme(Context context) {
        return UIUtils.isInLightThemeMode(context)
                && HiSettingsHelper.THEME_WHITE.equals(HiSettingsHelper.getInstance().getLightTheme());
    }

    public static int getThemeValue(Context context) {
        String theme = UIUtils.isInLightThemeMode(context) ? HiSettingsHelper.THEME_MODE_LIGHT : HiSettingsHelper.THEME_MODE_DARK;
        if (HiSettingsHelper.THEME_MODE_DARK.equals(theme)) {
            if (HiSettingsHelper.THEME_BLACK.equals(HiSettingsHelper.getInstance().getDarkTheme()))
                return R.style.ThemeBlack;
            return R.style.ThemeDark;
        } else {
            String lightTheme = HiSettingsHelper.getInstance().getLightTheme();
            for (Theme t : HiSettingsHelper.LIGHT_THEMES) {
                if (t.getName().equals(lightTheme))
                    return t.getThemeId();
            }
            HiSettingsHelper.getInstance().setTheme(HiSettingsHelper.THEME_MODE_LIGHT);
            HiSettingsHelper.getInstance().setLightTheme(HiSettingsHelper.THEME_WHITE);
            return R.style.ThemeLight_White;
        }
    }

    public static void hideSystemUI(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public static void showSystemUI(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}