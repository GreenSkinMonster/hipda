package net.jejer.hipda.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;

/**
 * fetch user's message or thread notifications
 * Created by GreenSkinMonster on 2015-09-07.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Logger.i("NotificationReceiver");
        if (!Connectivity.isConnected(context)) {
            Logger.i("Netork is offline, do nothing.");
            return;
        }

        if (HiSettingsHelper.getInstance().isInSilentMode()) {
            Logger.i("Notification is in silent mode, do nothing.");
            return;
        }

        String uid = HiSettingsHelper.getInstance().getUid();
        if (HiUtils.isValidId(uid) && OkHttpHelper.getInstance().isLoggedIn()) {
            Logger.i("Notification start checking....");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        NotificationMgr.fetchNotification(null);
                        NotificationMgr.showNotification(context);
                    } catch (Exception e) {
                        Logger.e(e);
                    } finally {
                        Logger.i(NotificationMgr.getCurrentNotification().toString());
                    }
                }
            }).start();
        } else {
            Logger.i("User is not logged in, cancel alarm");
            NotificationMgr.cancelAlarm(context);
        }
    }
}

