package net.jejer.hipda.ui;

import android.app.Application;
import android.content.Context;

import net.jejer.hipda.R;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.ACRAUtils;
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

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        HiApplication.context = getApplicationContext();

        ACRA.init(this);
        ACRAUtils.init(context);

        UpdateHelper.updateApp(context);
        GlideHelper.init(context);

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

}
