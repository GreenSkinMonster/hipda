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
        HiSettingsHelper.getInstance().updateMobileNetworkStatus();
        Logger.v("isConnected=" + Connectivity.isConnected(context)
                + ",  isConnectedFast=" + Connectivity.isConnectedFast(context)
                + ", isConnectedWifi=" + Connectivity.isConnectedWifi(context)
                + ", isConnectedMobile=" + Connectivity.isConnectedMobile(context)
                + ", networkClass=" + Connectivity.getNetworkClass(context));
    }
}
