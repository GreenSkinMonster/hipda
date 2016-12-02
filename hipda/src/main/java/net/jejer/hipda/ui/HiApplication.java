package net.jejer.hipda.ui;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import net.jejer.hipda.BuildConfig;

import io.fabric.sdk.android.Fabric;

/**
 * for error report
 * Created by GreenSkinMonster on 2015-03-28.
 */
public class HiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());
    }

}
