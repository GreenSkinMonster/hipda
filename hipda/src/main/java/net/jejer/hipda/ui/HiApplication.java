package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.SimpleExceptionHandler;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.io.File;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

/**
 * Created by GreenSkinMonster on 2015-03-28.
 */
public class HiApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public final static int IDLE = 0;
    public final static int RELOAD = 1;
    public final static int RECREATE = 2;
    public final static int RESTART = 3;

    private static Context context;
    private static boolean notified;
    private static boolean updated;
    private static boolean fontSet;
    private static int settingStatus;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        registerActivityLifecycleCallbacks(this);

        if (HiSettingsHelper.getInstance().isErrorReportMode()) {
            Thread.setDefaultUncaughtExceptionHandler(new SimpleExceptionHandler());
        }

        UIUtils.setLightDarkThemeMode();
        updated = UpdateHelper.updateApp();

        if (!HiSettingsHelper.getInstance().isLoginInfoValid()) {
            OkHttpHelper.getInstance().clearCookies();
        }

        try {
            String font = HiSettingsHelper.getInstance().getFont();
            if (!TextUtils.isEmpty(font)) {
                File fontFile = new File(Utils.getFontsDir(), font);
                if (fontFile.exists()) {
                    fontSet = true;
                    ViewPump.init(ViewPump.builder()
                            .addInterceptor(new CalligraphyInterceptor(
                                    new CalligraphyConfig.Builder()
                                            .setDefaultFontPath(fontFile.getAbsolutePath())
                                            .setFontAttrId(R.attr.fontPath)
                                            .build()))
                            .build());
                } else {
                    HiSettingsHelper.getInstance().setFont("");
                }
            }
        } catch (Exception e) {
            HiSettingsHelper.getInstance().setFont("");
            Logger.e(e);
        }
        HiUtils.updateBaseUrls();
    }

    public static Context getAppContext() {
        return HiApplication.context;
    }

    public static String getAppVersion() {
        String version = "0.0.00";
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        return version;
    }

    public static boolean isNotified() {
        return notified;
    }

    public static void setNotified(boolean b) {
        notified = b;
    }

    public static boolean isUpdated() {
        return updated;
    }

    public static void setUpdated(boolean updated) {
        HiApplication.updated = updated;
    }

    public static boolean isFontSet() {
        return fontSet;
    }

    public static int getSettingStatus() {
        return settingStatus;
    }

    public static void setSettingStatus(int settingStatus) {
        HiApplication.settingStatus = settingStatus;
    }

    private static int visibleActivityCount = 0;
    private static int foregroundActivityCount = 0;
    private static int mainActivityCount = 0;

    public static boolean isAppInForeground() {
        return foregroundActivityCount > 0;
    }

    public static boolean isAppVisible() {
        return visibleActivityCount > 0;
    }

    public static int getMainActivityCount() {
        return mainActivityCount;
    }

    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof MainFrameActivity) {
            mainActivityCount++;
        }
    }

    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof MainFrameActivity) {
            mainActivityCount--;
        }
    }

    public void onActivityResumed(Activity activity) {
        foregroundActivityCount++;
    }

    public void onActivityPaused(Activity activity) {
        foregroundActivityCount--;
    }


    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityStarted(Activity activity) {
        visibleActivityCount++;
    }

    public void onActivityStopped(Activity activity) {
        visibleActivityCount--;
    }

}
