package net.jejer.hipda.ui.setting;

import android.support.v7.preference.Preference;

/**
 * Created by GreenSkinMonster on 2017-06-08.
 */

abstract class OnPreferenceClickListener implements Preference.OnPreferenceClickListener {

    private static final long MIN_CLICK_INTERVAL = 600;
    private long mLastClickTime;

    @Override
    public boolean onPreferenceClick(Preference preference) {
        long currentClickTime = System.currentTimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        mLastClickTime = currentClickTime;

        if (elapsedTime <= MIN_CLICK_INTERVAL)
            return true;

        return onPreferenceSingleClick(preference);
    }

    public abstract boolean onPreferenceSingleClick(Preference preference);

}
