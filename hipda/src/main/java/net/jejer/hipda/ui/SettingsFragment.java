package net.jejer.hipda.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;

public class SettingsFragment extends PreferenceFragment {
	private final String LOG_TAG = getClass().getSimpleName();

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
		bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_SCREEN_ORIENTATION));
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
	}

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {

			Log.v("onPreferenceChange", "onPreferenceChange");
			String stringValue = value.toString();
			if (preference instanceof ListPreference) {
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
		} else {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getString(preference.getKey(),
							""));
		}
	}

}
