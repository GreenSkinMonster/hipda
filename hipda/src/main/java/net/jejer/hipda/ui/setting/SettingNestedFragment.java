package net.jejer.hipda.ui.setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;

import net.jejer.hipda.R;
import net.jejer.hipda.async.TaskHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiProgressDialog;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

/**
 * nested setting fragment
 * Created by GreenSkinMonster on 2015-09-11.
 */
public class SettingNestedFragment extends BaseSettingFragment {

    public static final int SCREEN_TAIL = 1;
    public static final int SCREEN_UI = 2;
    public static final int SCREEN_NOTIFICATION = 3;
    public static final int SCREEN_NETWORK = 4;
    public static final int SCREEN_OTHER = 5;

    public static final String TAG_KEY = "SCREEN_KEY";
    private HiProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPreferenceResource();
    }

    @Override
    public void onStop() {
        super.onStop();
        setActionBarTitle("设置");
        UIUtils.getSaveFolder();
    }

    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);
        // Load the preferences from an XML resource
        switch (key) {
            case SCREEN_TAIL:
                setActionBarTitle(R.string.pref_category_forum);
                addPreferencesFromResource(R.xml.pref_forum);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FORUMS));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FREQ_MENUS));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TAILTEXT));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TAILURL));
                break;

            case SCREEN_UI:
                setActionBarTitle(R.string.pref_category_ui);
                addPreferencesFromResource(R.xml.pref_ui);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TEXTSIZE_POST_ADJ));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TEXTSIZE_TITLE_ADJ));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_SCREEN_ORIENTATION));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_POST_LINE_SPACING));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_THEME));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_NIGHT_THEME));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FONT));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_AVATAR_LOAD_TYPE));
                Preference navBarColoredPreference = findPreference(HiSettingsHelper.PERF_NAVBAR_COLORED);
                if (Build.VERSION.SDK_INT < 21 && navBarColoredPreference != null)
                    navBarColoredPreference.setEnabled(false);
                break;

            case SCREEN_NOTIFICATION:
                setActionBarTitle(R.string.pref_category_notification);
                addPreferencesFromResource(R.xml.pref_notification);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_NOTI_REPEAT_MINUETS));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_NOTI_SILENT_BEGIN));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_NOTI_SILENT_END));

                final Preference notiEnablePreference = findPreference(HiSettingsHelper.PERF_NOTI_TASK_ENABLED);
                if (NotificationMgr.isAlarmRuning(getActivity()))
                    notiEnablePreference.setTitle(notiEnablePreference.getTitle() + "*");
                notiEnablePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue instanceof Boolean) {
                            enableNotiItems((Boolean) newValue);
                        }
                        return true;
                    }
                });

                enableNotiItems(HiSettingsHelper.getInstance().isNotiTaskEnabled());
                break;

            case SCREEN_NETWORK:
                setActionBarTitle(R.string.pref_category_network);
                addPreferencesFromResource(R.xml.pref_network);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_IMAGE_LOAD_TYPE));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_IMAGE_AUTO_LOAD_SIZE));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_SAVE_FOLDER));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_CACHE_SIZE_IN_MB));
                break;

            case SCREEN_OTHER:
                setActionBarTitle(R.string.pref_category_other);
                addPreferencesFromResource(R.xml.pref_other);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FORUM_SERVER));

                final Preference forumServerPreference = findPreference(HiSettingsHelper.PERF_FORUM_SERVER);
                forumServerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        TaskHelper.updateImageHost(getActivity(), preference);
                        return true;
                    }
                });

                Preference clearPreference = findPreference(HiSettingsHelper.PERF_CLEAR_CACHE);
                clearPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Dialog dialog = new AlertDialog.Builder(getActivity())
                                .setTitle("清除缓存？")
                                .setMessage("继续操作将清除相关缓存资源。\n\n在频繁出现网络错误情况下，可以尝试本功能看是否可以解决问题。")
                                .setPositiveButton(getResources().getString(android.R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new AsyncTask<Void, Void, Exception>() {
                                                    @Override
                                                    protected Exception doInBackground(Void... voids) {
                                                        try {
                                                            OkHttpHelper.getInstance().clearCookies();
                                                            Utils.clearOkhttpCache();
                                                            SettingMainFragment.mCacheCleared = true;
                                                        } catch (Exception e) {
                                                            return e;
                                                        }
                                                        return null;
                                                    }

                                                    @Override
                                                    protected void onPreExecute() {
                                                        super.onPreExecute();
                                                        mProgressDialog = HiProgressDialog.show(getActivity(), "正在处理...");
                                                    }

                                                    @Override
                                                    protected void onPostExecute(Exception e) {
                                                        super.onPostExecute(e);
                                                        if (mProgressDialog != null) {
                                                            if (e == null)
                                                                mProgressDialog.dismiss("缓存已经清除");
                                                            else
                                                                mProgressDialog.dismissError("发生错误 : " + e.getMessage());
                                                        }
                                                    }
                                                }.execute();
                                            }
                                        })
                                .setNegativeButton(getResources().getString(android.R.string.cancel),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).create();
                        dialog.show();
                        return true;
                    }
                });
                break;
            default:
                break;
        }
    }

    private void enableNotiItems(boolean isNotiTaskEnabled) {
        findPreference(HiSettingsHelper.PERF_NOTI_REPEAT_MINUETS).setEnabled(isNotiTaskEnabled);
        findPreference(HiSettingsHelper.PERF_NOTI_LED_LIGHT).setEnabled(isNotiTaskEnabled);
        findPreference(HiSettingsHelper.PERF_NOTI_SOUND).setEnabled(isNotiTaskEnabled);
        findPreference(HiSettingsHelper.PERF_NOTI_SILENT_MODE).setEnabled(isNotiTaskEnabled);
        findPreference(HiSettingsHelper.PERF_NOTI_SILENT_BEGIN).setEnabled(isNotiTaskEnabled);
        findPreference(HiSettingsHelper.PERF_NOTI_SILENT_END).setEnabled(isNotiTaskEnabled);
    }

}
