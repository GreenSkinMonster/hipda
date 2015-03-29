package net.jejer.hipda.ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import net.jejer.hipda.R;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;

import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment {
    private final String LOG_TAG = getClass().getSimpleName();

    private boolean mEinkUi;
    private int mScreenOrietation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);

        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_USERNAME));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_SECQUESTION));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TAILTEXT));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TAILURL));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_BLANKLIST_USERNAMES));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TEXTSIZE_POST_ADJ));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TEXTSIZE_TITLE_ADJ));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_SCREEN_ORIENTATION));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_EINK_MODE));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TITLE_BOLD));
        bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_POST_LINE_SPACING));

        Preference dialogPref = findPreference(HiSettingsHelper.PERF_ABOUT);
        dialogPref.setSummary(HiSettingsHelper.getInstance().getAppVersion());
        dialogPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                final Dialog dialog = new AboutDialog(getActivity(), android.R.style.Theme_Black_NoTitleBar);
                dialog.getWindow().setWindowAnimations(android.R.anim.fade_in);
                dialog.show();
                return true;
            }
        });

        Preference checkPreference = findPreference(HiSettingsHelper.PERF_LAST_UPDATE_CHECK);
        checkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new UpdateHelper(getView().getContext(), false).check();
                return true;
            }
        });

        mEinkUi = HiSettingsHelper.getInstance().isEinkModeUIEnabled();
        mScreenOrietation = HiSettingsHelper.getInstance().getScreenOrietation();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setTitle(R.string.title_fragment_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStop() {
        Log.v(LOG_TAG, "onStop, reload settings");
        super.onStop();
        HiSettingsHelper.getInstance().reload();

        if (HiSettingsHelper.getInstance().isEinkModeUIEnabled() != mEinkUi
                || HiSettingsHelper.getInstance().getScreenOrietation() != mScreenOrietation) {
            Intent intent = getActivity().getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            getActivity().finish();
            startActivity(intent);
        }

    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            Log.v("onPreferenceChange", "onPreferenceChange");
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
