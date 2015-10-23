package net.jejer.hipda.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.jejer.hipda.bean.HiSettingsHelper;

/**
 * set mobile status when network changed
 * Created by GreenSkinMonster on 2015-08-06.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        HiSettingsHelper.updateMobileNetworkStatus(context);

        if (Connectivity.isConnected(context)) {
            if (HiSettingsHelper.getInstance().isNotiTaskEnabled()) {
                if (!NotificationMgr.isAlarmRuning(context))
                    NotificationMgr.startAlarm(context);
            } else if (NotificationMgr.isAlarmRuning(context)) {
                NotificationMgr.cancelAlarm(context);
            }
        }
    }
}
