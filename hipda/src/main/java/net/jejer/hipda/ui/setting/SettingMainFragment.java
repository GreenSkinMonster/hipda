package net.jejer.hipda.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.service.NotiHelper;
import net.jejer.hipda.ui.FragmentUtils;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.SettingActivity;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * main setting fragment
 * Created by GreenSkinMonster on 2015-09-11.
 */
public class SettingMainFragment extends BaseSettingFragment {

    private int mScreenOrietation;
    private String mTheme;
    private int mPrimaryColor;
    private List<Integer> mForums;
    private Set<String> mFreqMenus;
    private boolean mNavBarColored;
    private String mFont;
    static boolean mCacheCleared;
    private boolean mNightSwitchEnabled;
    private String mIcon;
    private String mForumServer;
    private boolean mTrustAllCerts;
    private boolean mCircleAvatar;

    private boolean mFirstResume = true;

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

                    Intent intent = new Intent(getActivity(), SettingActivity.class);
                    intent.putExtra(SettingNestedFragment.TAG_KEY, screenKey);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.slide_in_right, 0);
                    ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
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
        mForumServer = HiSettingsHelper.getInstance().getForumServer();
        mTrustAllCerts = HiSettingsHelper.getInstance().isTrustAllCerts();
        mCircleAvatar = HiSettingsHelper.getInstance().isCircleAvatar();

        setActionBarTitle(R.string.title_fragment_settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mFirstResume) {
            updateSettingStatus();
        } else {
            mFirstResume = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (HiSettingsHelper.getInstance().isNotiTaskEnabled()) {
            NotiHelper.scheduleJob();
        } else {
            NotiHelper.cancelJob();
        }
    }

    private void updateSettingStatus() {
        HiSettingsHelper.getInstance().reload();

        if (HiSettingsHelper.getInstance().isCircleAvatar() != mCircleAvatar)
            GlideHelper.initDefaultFiles();

        if (HiSettingsHelper.getInstance().getPrimaryColor() != mPrimaryColor)
            HiSettingsHelper.getInstance().setNightMode(false);

        String newIcon = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_ICON, "0");
        boolean iconChanged = !mIcon.equals(String.valueOf(Utils.parseInt(newIcon)));
        HiApplication.setIconChanged(iconChanged);

        if (mCacheCleared
                || !HiSettingsHelper.getInstance().getFont().equals(mFont)
                || iconChanged) {
            HiApplication.setSettingStatus(HiApplication.RESTART);
        } else if (HiSettingsHelper.getInstance().getScreenOrietation() != mScreenOrietation
                || !HiSettingsHelper.getInstance().getActiveTheme().equals(mTheme)
                || (HiSettingsHelper.getInstance().isUsingLightTheme() && HiSettingsHelper.getInstance().getPrimaryColor() != mPrimaryColor)
                || !HiSettingsHelper.getInstance().getForums().equals(mForums)
                || !HiSettingsHelper.getInstance().getFreqMenus().equals(mFreqMenus)
                || HiSettingsHelper.getInstance().isNavBarColored() != mNavBarColored
                || TextUtils.isEmpty(HiSettingsHelper.getInstance().getNightTheme()) == mNightSwitchEnabled
                || !HiSettingsHelper.getInstance().getForumServer().equals(mForumServer)
                || HiSettingsHelper.getInstance().isTrustAllCerts() != mTrustAllCerts) {
            HiApplication.setSettingStatus(HiApplication.RECREATE);
        } else {
            HiApplication.setSettingStatus(HiApplication.RELOAD);
        }
    }

    private void bindPreferenceSummaryToValue() {
        Preference dialogPref = findPreference(HiSettingsHelper.PERF_ABOUT);

        dialogPref.setSummary(HiApplication.getAppVersion()
                + (Utils.isFromGooglePlay(getActivity()) ? " (Google Play)" : ""));
        dialogPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(getActivity(), SettingActivity.class);
                intent.putExtra(AboutFragment.TAG_KEY, AboutFragment.TAG_KEY);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.slide_in_right, 0);
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
                return true;
            }
        });

        final Preference checkPreference = findPreference(HiSettingsHelper.PERF_LAST_UPDATE_CHECK);
        checkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                checkPreference.setSummary("上次检查 ：" + Utils.shortyTime(new Date()));
                new UpdateHelper(getActivity(), false).check();
                return true;
            }
        });
        Date lastCheckTime = HiSettingsHelper.getInstance().getLastUpdateCheckTime();
        if (lastCheckTime != null) {
            checkPreference.setSummary("上次检查 ：" + Utils.shortyTime(lastCheckTime));
        } else {
            checkPreference.setSummary("上次检查 ：- ");
        }

        Preference supportPreference = findPreference(HiSettingsHelper.PERF_SUPPORT);
        supportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                FragmentUtils.show(getActivity(),
                        FragmentUtils.parseUrl(HiUtils.BaseUrl + "viewthread.php?tid=" + HiUtils.CLIENT_TID));
                return true;
            }
        });

    }

}
