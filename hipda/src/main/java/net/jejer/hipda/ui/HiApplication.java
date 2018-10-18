package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;

import net.jejer.hipda.BuildConfig;
import net.jejer.hipda.R;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.service.NotiJobCreator;
import net.jejer.hipda.utils.HiUtils;

import java.io.File;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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

        JobManager.create(this).addJobCreator(new NotiJobCreator());

        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());

        updated = UpdateHelper.updateApp();

        String font = HiSettingsHelper.getInstance().getFont();
        if (new File(font).exists()) {
            fontSet = true;
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath(font)
                    .setFontAttrId(R.attr.fontPath)
                    .build()
            );
        } else {
            HiSettingsHelper.getInstance().setFont("");
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
