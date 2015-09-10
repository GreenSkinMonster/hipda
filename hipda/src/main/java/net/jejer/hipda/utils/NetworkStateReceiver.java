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
        Logger.v("isConnected=" + Connectivity.isConnected(context));
        HiSettingsHelper.updateMobileNetworkStatus(context);

        if (Connectivity.isConnected(context)) {
            if (!HiSettingsHelper.getInstance().ready())
                HiSettingsHelper.getInstance().init(context);

            if (HiSettingsHelper.getInstance().isNotiTaskEnabled()) {
                if (!NotificationMgr.isAlarmRnning(context))
                    NotificationMgr.startAlarm(context);
            } else if (NotificationMgr.isAlarmRnning(context)) {
                NotificationMgr.cancelAlarm(context);
            }
        }
    }
}
