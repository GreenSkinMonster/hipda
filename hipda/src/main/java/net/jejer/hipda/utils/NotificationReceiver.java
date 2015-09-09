package net.jejer.hipda.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.NotificationBean;
import net.jejer.hipda.volley.VolleyHelper;

/**
 * fetch user's message or thread notifications
 * Created by GreenSkinMonster on 2015-09-07.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Logger.i("NotificationReceiver");
        NotificationMgr.isAlarmRnning(context);

        if (!Connectivity.isConnected(context))
            return;

        if (!HiSettingsHelper.getInstance().ready())
            HiSettingsHelper.getInstance().init(context);
        if (!VolleyHelper.getInstance().ready())
            VolleyHelper.getInstance().init(context);

        if (HiSettingsHelper.getInstance().isInSilentMode())
            return;

        String uid = HiSettingsHelper.getInstance().getUid();
        if (HiUtils.isValidId(uid) && VolleyHelper.getInstance().isLoggedIn()) {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationBean bean = NotificationMgr.fetchNotification(null);
                        NotificationMgr.showNotification(context, bean);
                    }
                }).start();
            } catch (Exception e) {
                Logger.e(e);
            }
        }
    }
}

