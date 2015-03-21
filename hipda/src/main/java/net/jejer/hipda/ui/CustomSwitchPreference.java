package net.jejer.hipda.ui;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

/**
 * avoid a bug about SwitchPreference, os 4.x.x
 * https://code.google.com/p/android/issues/detail?id=26194
 * Created by GreenSkinMonster on 2015-03-21.
 */
public class CustomSwitchPreference extends SwitchPreference {
    public CustomSwitchPreference(Context context) {
        this(context, null);
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.switchPreferenceStyle);
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
