package net.jejer.hipda.ui.setting;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;

import net.jejer.hipda.R;
import net.jejer.hipda.async.TaskHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.SettingActivity;
import net.jejer.hipda.ui.widget.HiProgressDialog;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.Date;

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

    private static final int REQUEST_CODE_ALERT_RINGTONE = 1;

    public static final String TAG_KEY = "SCREEN_KEY";
    private HiProgressDialog mProgressDialog;
    private Preference ringtonePreference;
    private Preference mBlackListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPreferenceResource();
    }

    @Override
    public void onStop() {
        super.onStop();
        UIUtils.getSaveFolder();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBlackListPreference != null) {
            Date bSyncDate = HiSettingsHelper.getInstance().getBlacklistSyncTime();
            mBlackListPreference.setSummary(
                    "黑名单用户 : " + HiSettingsHelper.getInstance().getBlacklists().size() + "，上次同步 : "
                            + Utils.shortyTime(bSyncDate));
        }
    }

    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);
        // Load the preferences from an XML resource
        switch (key) {
            case SCREEN_TAIL:
                setActionBarTitle(R.string.pref_category_forum);
                addPreferencesFromResource(R.xml.pref_forum);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FREQ_MENUS));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TAILURL));

                Preference forumsPreference = findPreference(HiSettingsHelper.PERF_FORUMS);
                forumsPreference.setOnPreferenceClickListener(new ForumSelectListener(getActivity()));
                forumsPreference.setSummary(HiUtils.getForumsSummary());

                final Preference tailTextPreference = findPreference(HiSettingsHelper.PERF_TAILTEXT);
                tailTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String text = Utils.nullToText((String) newValue);
                        if (Utils.getWordCount(text) > HiSettingsHelper.MAX_TAIL_TEXT_LENGTH) {
                            UIUtils.toast("小尾巴文字限制最长 " + HiSettingsHelper.MAX_TAIL_TEXT_LENGTH + " 字符，中文视为 2 个字符");
                            return false;
                        }
                        preference.setSummary(text);
                        return true;
                    }
                });
                tailTextPreference.setSummary(HiSettingsHelper.getInstance().getTailText());

                mBlackListPreference = findPreference(HiSettingsHelper.PERF_BLACKLIST);
                mBlackListPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getActivity(), SettingActivity.class);
                        intent.putExtra(BlacklistFragment.TAG_KEY, BlacklistFragment.TAG_KEY);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getActivity(), R.anim.slide_in_right, 0);
                        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
                        return true;
                    }
                });

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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && navBarColoredPreference != null)
                    navBarColoredPreference.setEnabled(false);

                Preference fontPreference = findPreference(HiSettingsHelper.PERF_FONT);
                fontPreference.setOnPreferenceClickListener(new FilePickerListener(getActivity(), FilePickerListener.FONT_FILE));

                Preference swipeCompatPreference = findPreference(HiSettingsHelper.PERF_SWIPE_COMPAT_MODE);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && swipeCompatPreference != null)
                    swipeCompatPreference.setEnabled(false);

                break;

            case SCREEN_NOTIFICATION:
                setActionBarTitle(R.string.pref_category_notification);
                addPreferencesFromResource(R.xml.pref_notification);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_NOTI_SILENT_BEGIN));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_NOTI_SILENT_END));

                final Preference notiEnablePreference = findPreference(HiSettingsHelper.PERF_NOTI_TASK_ENABLED);
                notiEnablePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue instanceof Boolean) {
                            enableNotiItems(preference, (Boolean) newValue);
                        }
                        return true;
                    }
                });

                ringtonePreference = findPreference(HiSettingsHelper.PERF_NOTI_SOUND);
                ringtonePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

                        String existingValue = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_NOTI_SOUND, "");
                        if (existingValue != null) {
                            if (existingValue.length() == 0) {
                                // Select "Silent"
                                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                            } else {
                                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                            }
                        } else {
                            // No ringtone has been selected, set to the default
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                        }

                        startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE);
                        return true;
                    }
                });
                ringtonePreference.setSummary(Utils.getRingtoneTitle(getActivity(), Uri.parse(HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_NOTI_SOUND, ""))));

                final Preference silentBeginPreference = findPreference(HiSettingsHelper.PERF_NOTI_SILENT_BEGIN);
                final Preference silentEndPreference = findPreference(HiSettingsHelper.PERF_NOTI_SILENT_END);

                silentBeginPreference.setOnPreferenceClickListener(
                        new TimePickerListener(HiSettingsHelper.getInstance().getSilentBegin()));
                silentBeginPreference.setSummary(HiSettingsHelper.getInstance().getSilentBegin());

                silentEndPreference.setOnPreferenceClickListener(
                        new TimePickerListener(HiSettingsHelper.getInstance().getSilentEnd()));
                silentEndPreference.setSummary(HiSettingsHelper.getInstance().getSilentEnd());

                enableNotiItems(notiEnablePreference, HiSettingsHelper.getInstance().isNotiTaskEnabled());
                break;

            case SCREEN_NETWORK:
                setActionBarTitle(R.string.pref_category_network);
                addPreferencesFromResource(R.xml.pref_network);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_IMAGE_LOAD_TYPE));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_IMAGE_AUTO_LOAD_SIZE));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_SAVE_FOLDER));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_CACHE_SIZE_IN_MB));

                Preference saveFolderPreference = findPreference(HiSettingsHelper.PERF_SAVE_FOLDER);
                saveFolderPreference.setOnPreferenceClickListener(new FilePickerListener(getActivity(), FilePickerListener.SAVE_DIR));

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
                        showClearCacheDialog();
                        return true;
                    }
                });

                Preference clearImagePreference = findPreference(HiSettingsHelper.PERF_CLEAR_IMAGE_CACHE);
                clearImagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        showClearImageCacheDialog();
                        return true;
                    }
                });
                break;
            default:
                break;
        }
    }

    private void enableNotiItems(Preference preference, boolean isNotiTaskEnabled) {
        if (isNotiTaskEnabled) {
            preference.setSummary("上次检查 : " + Utils.shortyTime(HiSettingsHelper.getInstance().getNotiJobLastRunTime()));
        } else {
            preference.setSummary("已停用");
        }
        findPreference(HiSettingsHelper.PERF_NOTI_LED_LIGHT).setEnabled(isNotiTaskEnabled);
        findPreference(HiSettingsHelper.PERF_NOTI_SILENT_MODE).setEnabled(isNotiTaskEnabled);
        findPreference(HiSettingsHelper.PERF_NOTI_SILENT_BEGIN).setEnabled(isNotiTaskEnabled);
        findPreference(HiSettingsHelper.PERF_NOTI_SILENT_END).setEnabled(isNotiTaskEnabled);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ALERT_RINGTONE && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri == null) {
                HiSettingsHelper.getInstance().setStringValue(HiSettingsHelper.PERF_NOTI_SOUND, "");
                ringtonePreference.setSummary("无");
            } else {
                HiSettingsHelper.getInstance().setStringValue(HiSettingsHelper.PERF_NOTI_SOUND, uri.toString());
                ringtonePreference.setSummary(Utils.getRingtoneTitle(getActivity(), uri));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showClearCacheDialog() {
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("清除网络缓存？")
                .setMessage("继续操作将清除网络访问相关缓存。\n\n在频繁出现网络错误情况下，可以尝试本功能看是否可以解决问题。")
                .setPositiveButton(getResources().getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Void, Void, Exception>() {
                                    @Override
                                    protected Exception doInBackground(Void... voids) {
                                        SettingMainFragment.mCacheCleared = true;
                                        try {
                                            OkHttpHelper.getInstance().clearCookies();
                                            Utils.clearOkhttpCache();
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
                                                mProgressDialog.dismiss("网络缓存已经清除");
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
    }

    private void showClearImageCacheDialog() {
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("清除图片和头像缓存？")
                .setMessage("图片和头像缓存文件较多，该操作可能需要较长时间。")
                .setPositiveButton(getResources().getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Void, Void, Exception>() {
                                    @Override
                                    protected Exception doInBackground(Void... voids) {
                                        SettingMainFragment.mCacheCleared = true;
                                        try {
                                            GlideHelper.clearAvatarFiles();
                                            Utils.clearExternalCache();
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
                                                mProgressDialog.dismiss("图片和头像缓存已经清除");
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
    }

}
