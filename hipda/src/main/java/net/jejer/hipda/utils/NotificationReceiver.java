package net.jejer.hipda.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiApplication;

/**
 * fetch user's message or thread notifications
 * Created by GreenSkinMonster on 2015-09-07.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Logger.v("NotificationReceiver");
        if (!Connectivity.isConnected(context)) {
            Logger.v("Netork is offline, do nothing.");
            return;
        }

        if (HiSettingsHelper.getInstance().isInSilentMode()) {
            Logger.v("Notification is in silent mode, do nothing.");
            return;
        }

        if (HiApplication.isActivityVisible()) {
            Logger.v("Activity is visible, do nothing.");
            return;
        }

        String uid = HiSettingsHelper.getInstance().getUid();
        if (HiUtils.isValidId(uid) && OkHttpHelper.getInstance().isLoggedIn()) {
            Logger.v("Notification start checking....");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        NotificationMgr.fetchNotification(null);
                        NotificationMgr.showNotification(context);
                    } catch (Exception e) {
                        Logger.e(e);
                    } finally {
                        Logger.v(NotificationMgr.getCurrentNotification().toString());
                    }
                }
            }).start();
        } else {
            Logger.v("User is not logged in, cancel alarm");
            NotificationMgr.cancelAlarm(context);
        }
    }
}

