package net.jejer.hipda.ui.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.TextUtils;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.AboutFragment;
import net.jejer.hipda.ui.FragmentUtils;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.MainFrameActivity;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.Utils;

import java.util.Set;

/**
 * main setting fragment
 * Created by GreenSkinMonster on 2015-09-11.
 */
public class SettingMainFragment extends BaseSettingFragment {

    private int mScreenOrietation;
    private String mTheme;
    private int mPrimaryColor;
    private Set<String> mForums;
    private Set<String> mFreqMenus;
    private boolean mNavBarColored;
    private String mFont;
    static boolean mCacheCleared;
    private boolean mNightSwitchEnabled;
    private String mIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

        bindPreferenceSummaryToValue();

        // "nested_#" is the <Preference android:key="nested" android:persistent="false"/>
        for (int i = 1; i <= 5; i++) {
            final int screenKey = i;
            findPreference("nested_" + i).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                private long mLastClickTime;

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    //avoid double click
                    long currentClickTime = System.currentTimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;
                    if (elapsedTime <= Constants.MIN_CLICK_INTERVAL)
                        return true;

                    Fragment fragment = new SettingNestedFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(SettingNestedFragment.TAG_KEY, screenKey);
                    fragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .add(R.id.main_frame_container, fragment)
                            .addToBackStack(fragment.getClass().getName())
                            .commit();
                    return true;
                }
            });
        }

        mScreenOrietation = HiSettingsHelper.getInstance().getScreenOrietation();
        mTheme = HiSettingsHelper.getInstance().getActiveTheme();
        mPrimaryColor = HiSettingsHelper.getInstance().getPrimaryColor();
        mForums = HiSettingsHelper.getInstance().getForums();
        mFreqMenus = HiSettingsHelper.getInstance().getFreqMenus();
        mNavBarColored = HiSettingsHelper.getInstance().isNavBarColored();
        mNightSwitchEnabled = !TextUtils.isEmpty(HiSettingsHelper.getInstance().getNightTheme());
        mFont = HiSettingsHelper.getInstance().getFont();
        mIcon = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_ICON, "0");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setActionBarTitle(R.string.title_fragment_settings);
    }

    @Override
    public void onStop() {
        Logger.v("onStop, reload settings");
        super.onStop();

        HiSettingsHelper.getInstance().reload();

        if (HiSettingsHelper.getInstance().isNotiTaskEnabled()) {
            if (NotificationMgr.isAlarmRuning(getActivity()))
                NotificationMgr.cancelAlarm(getActivity());
            NotificationMgr.startAlarm(getActivity());
        } else {
            NotificationMgr.cancelAlarm(getActivity());
        }

        if (!HiSettingsHelper.getInstance().isGestureBack() && getActivity() != null)
            ((MainFrameActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        String newIcon = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_ICON, "0");
        if (TextUtils.isDigitsOnly(newIcon) && !mIcon.equals(newIcon))
            setIcon(Integer.parseInt(newIcon));

        if (mCacheCleared
                || HiSettingsHelper.getInstance().getScreenOrietation() != mScreenOrietation
                || !HiSettingsHelper.getInstance().getActiveTheme().equals(mTheme)
                || HiSettingsHelper.getInstance().getPrimaryColor() != mPrimaryColor
                || !HiSettingsHelper.getInstance().getForums().equals(mForums)
                || !HiSettingsHelper.getInstance().getFreqMenus().equals(mFreqMenus)
                || HiSettingsHelper.getInstance().isNavBarColored() != mNavBarColored
                || TextUtils.isEmpty(HiSettingsHelper.getInstance().getNightTheme()) == mNightSwitchEnabled
                || !HiSettingsHelper.getInstance().getFont().equals(mFont)
                || !mIcon.equals(newIcon)) {
            mCacheCleared = false;
            Utils.restartActivity(getActivity());
        }

    }

    private void bindPreferenceSummaryToValue() {

        final Preference userPreference = findPreference(HiSettingsHelper.PERF_USERNAME);
        if (LoginHelper.isLoggedIn())
            userPreference.setSummary(Html.fromHtml(HiSettingsHelper.getInstance().getUsername() + "    <font color=grey>(已登录)</font>"));
        else
            userPreference.setSummary(Html.fromHtml("<font color=grey>(未登录)</font>"));

        userPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (LoginHelper.isLoggedIn()) {
                    Dialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle("退出登录？")
                            .setMessage("\n确认清除当前用户的登录信息？\n")
                            .setPositiveButton(getResources().getString(android.R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            HiSettingsHelper.getInstance().setUsername("");
                                            HiSettingsHelper.getInstance().setPassword("");
                                            HiSettingsHelper.getInstance().setSecQuestion("");
                                            HiSettingsHelper.getInstance().setSecAnswer("");
                                            HiSettingsHelper.getInstance().setUid("");
                                            LoginHelper.logout();
                                            userPreference.setSummary(Html.fromHtml("<font color=grey>(未登录)</font>"));
                                            ((MainFrameActivity) getActivity()).updateAccountHeader();
                                        }
                                    })
                            .setNegativeButton(getResources().getString(android.R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).create();
                    dialog.show();
                } else {
                    Toast.makeText(getActivity(), "已经退出登录，返回可以重新登录", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        Preference dialogPref = findPreference(HiSettingsHelper.PERF_ABOUT);

        dialogPref.setSummary(HiApplication.getAppVersion()
                + (Utils.isFromGooglePlay(getActivity()) ? " (Google Play)" : ""));
        dialogPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                FragmentUtils.showFragment(getFragmentManager(), new AboutFragment());
                return true;
            }
        });

        Preference checkPreference = findPreference(HiSettingsHelper.PERF_LAST_UPDATE_CHECK);
        checkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new UpdateHelper(getActivity(), false).check();
                return true;
            }
        });

    }

    private void setIcon(int icon) {
        Context ctx = getActivity();
        PackageManager pm = getActivity().getPackageManager();

        pm.setComponentEnabledSetting(
                new ComponentName(ctx, "net.jejer.hipda.ng.MainActivity-Original"),
                icon == Constants.ICON_ORIGINAL ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        pm.setComponentEnabledSetting(
                new ComponentName(ctx, "net.jejer.hipda.ng.MainActivity-Circle"),
                icon == Constants.ICON_ROUND ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }

}
