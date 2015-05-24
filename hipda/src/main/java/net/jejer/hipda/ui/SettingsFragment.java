package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.ColorUtils;
import net.jejer.hipda.utils.Logger;

import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment {

    private int mScreenOrietation;
    private String mTheme;
    private Set<String> mForums;
    private boolean mNavBarColored;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);

        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TAILTEXT));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TAILURL));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_BLANKLIST_USERNAMES));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TEXTSIZE_POST_ADJ));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TEXTSIZE_TITLE_ADJ));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_SCREEN_ORIENTATION));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_POST_LINE_SPACING));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_THEME));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FORUMS));

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
        dialogPref.setSummary(HiSettingsHelper.getInstance().getAppVersion());
        dialogPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(R.id.main_frame_container, new AboutFragment(), AboutFragment.class.getName())
                        .addToBackStack(AboutFragment.class.getName())
                        .commit();
                return true;
            }
        });

        Preference checkPreference = findPreference(HiSettingsHelper.PERF_LAST_UPDATE_CHECK);
        checkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (LoginHelper.isLoggedIn()) {
                    new UpdateHelper(getActivity(), false).check();
                } else {
                    Toast.makeText(getActivity(), "登录后才可以检查更新", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        mScreenOrietation = HiSettingsHelper.getInstance().getScreenOrietation();
        mTheme = HiSettingsHelper.getInstance().getTheme();
        mForums = HiSettingsHelper.getInstance().getForums();
        mNavBarColored = HiSettingsHelper.getInstance().isNavBarColored();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(ColorUtils.getListBackgroundColor(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_fragment_settings);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStop() {
        Logger.v("onStop, reload settings");
        super.onStop();
        HiSettingsHelper.getInstance().reload();

        if (!HiSettingsHelper.getInstance().isGestureBack()
                && getActivity() != null)
            ((MainFrameActivity) getActivity()).drawerResult.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (HiSettingsHelper.getInstance().getScreenOrietation() != mScreenOrietation
                || !HiSettingsHelper.getInstance().getTheme().equals(mTheme)
                || !HiSettingsHelper.getInstance().getForums().equals(mForums)
                || HiSettingsHelper.getInstance().isNavBarColored() != mNavBarColored) {
            ColorUtils.clear();
            Intent intent = getActivity().getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            getActivity().finish();
            startActivity(intent);
        }

    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();
            if (preference instanceof MultiSelectListPreference) {
                MultiSelectListPreference listPreference = (MultiSelectListPreference) preference;
                Set<String> selectedValues = (Set<String>) value;
                CharSequence[] entries = listPreference.getEntries();
                CharSequence[] entryValues = listPreference.getEntryValues();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < entryValues.length; i++) {
                    String v = entryValues[i].toString();
                    if (selectedValues.contains(v)) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        int index = listPreference.findIndexOfValue(v);
                        sb.append(entries[index]);
                    }
                }
                preference.setSummary(sb.toString());
            } else if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference
                        .setSummary(index >= 0 ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference
                .setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof CheckBoxPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                    preference,
                    PreferenceManager.getDefaultSharedPreferences(
                            preference.getContext()).getBoolean(preference.getKey(),
                            false));
        } else if (preference instanceof SwitchPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                    preference,
                    PreferenceManager.getDefaultSharedPreferences(
                            preference.getContext()).getBoolean(preference.getKey(),
                            false));
        } else if (preference instanceof MultiSelectListPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                    preference,
                    PreferenceManager.getDefaultSharedPreferences(
                            preference.getContext()).getStringSet(preference.getKey(),
                            new HashSet<String>()));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                    preference,
                    PreferenceManager.getDefaultSharedPreferences(
                            preference.getContext()).getString(preference.getKey(),
                            ""));
        }
    }

}
