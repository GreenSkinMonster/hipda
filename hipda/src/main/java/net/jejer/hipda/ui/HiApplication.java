package net.jejer.hipda.ui;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.Constants;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * for error report
 * Created by GreenSkinMonster on 2015-03-28.
 */
@ReportsCrashes(
        mailTo = "chartreuse.orc@gmail.com",
        customReportContent = {ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.APPLICATION_LOG,
                ReportField.LOGCAT
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class HiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (Constants.FONT_ROBOTO_SLAB.equals(prefs.getString(HiSettingsHelper.PERF_FONT, ""))) {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/RobotoSlab-Regular.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
            );
        }
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


}
