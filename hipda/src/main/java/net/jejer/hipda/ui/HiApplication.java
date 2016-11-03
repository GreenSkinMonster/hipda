package net.jejer.hipda.ui;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import net.jejer.hipda.R;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.Constants;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by GreenSkinMonster on 2015-03-28.
 */
public class HiApplication extends Application {

    private static Context context;
    private static boolean notified;

    @Override
    public void onCreate() {
        super.onCreate();
        HiApplication.context = getApplicationContext();

        Fabric.with(this, new Crashlytics());

        UpdateHelper.updateApp(context);

        if (Constants.FONT_ROBOTO_SLAB.equals(HiSettingsHelper.getInstance().getFont())) {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/RobotoSlab-Regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()
            );
        }
    }

    public static Context getAppContext() {
        return HiApplication.context;
    }

    private static boolean activityVisible;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
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

}
