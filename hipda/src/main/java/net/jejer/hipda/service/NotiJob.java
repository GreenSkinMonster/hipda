package net.jejer.hipda.service;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Logger;

/**
 * Created by GreenSkinMonster on 2017-07-19.
 */

public class NotiJob extends Job {

    public static final String TAG = "noti_job_tag";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        if (!OkHttpHelper.getInstance().isLoggedIn()) {
            NotiHelper.cancelJob();
        } else {
            HiSettingsHelper.getInstance().setNotiJobLastRunTime();
            if (!HiApplication.isAppVisible()
                    && !HiSettingsHelper.getInstance().isInSilentMode()) {
                checkNotifications();
            }
        }
        return Result.SUCCESS;
    }

    private void checkNotifications() {
        try {
            NotiHelper.fetchNotification(null);
            NotiHelper.showNotification(HiApplication.getAppContext());
        } catch (Exception e) {
            Logger.e(e);
        }
    }

}
