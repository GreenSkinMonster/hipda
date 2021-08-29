package net.jejer.hipda.ui.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.Preference;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.TaskHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.SettingActivity;
import net.jejer.hipda.ui.adapter.BaseRvAdapter;
import net.jejer.hipda.ui.adapter.RecyclerItemClickListener;
import net.jejer.hipda.ui.widget.HiProgressDialog;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan;
import io.github.inflationx.calligraphy3.TypefaceUtils;

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
    private ActivityResultLauncher<String> mGetFontFile;
    private Drawable mCheckDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPreferenceResource();

        mGetFontFile = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        importFontFile(uri);
                    }
                });

        mCheckDrawable = new IconicsDrawable(getContext(), GoogleMaterial.Icon.gmd_check)
                .color(UIUtils.isInLightThemeMode(getContext()) ? Color.BLACK : Color.WHITE)
                .sizeDp(20).paddingDp(4);

    }

    @Override
    public void onStop() {
        super.onStop();
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
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_TAILTEXT));
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
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_THEME_MODE));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_DARK_THEME));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FONT));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_AVATAR_LOAD_TYPE));

                Preference fontPreference = findPreference(HiSettingsHelper.PERF_FONT);
                fontPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showFontSelectDialog();
                        return true;
                    }
                });
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
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_CACHE_SIZE_IN_MB));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_WIFI_IMAGE_POLICY));
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_MOBILE_IMAGE_POLICY));

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
                                mProgressDialog = HiProgressDialog.show(getActivity(), "正在处理...");
                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                Handler handler = new Handler(Looper.getMainLooper());
                                executor.execute(() -> {
                                    SettingMainFragment.mCacheCleared = true;
                                    Exception error = null;
                                    try {
                                        OkHttpHelper.getInstance().clearCookies();
                                        Utils.clearOkhttpCache();
                                    } catch (Exception ex) {
                                        error = ex;
                                    }
                                    final Exception e = error;
                                    handler.post(() -> {
                                        if (mProgressDialog != null) {
                                            if (e == null)
                                                mProgressDialog.dismiss("网络缓存已经清除");
                                            else
                                                mProgressDialog.dismissError("发生错误 : " + e.getMessage());
                                        }
                                    });
                                });
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
                                mProgressDialog = HiProgressDialog.show(getActivity(), "正在处理...");
                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                Handler handler = new Handler(Looper.getMainLooper());
                                executor.execute(() -> {
                                    Exception ee = null;
                                    SettingMainFragment.mCacheCleared = true;
                                    try {
                                        GlideHelper.clearAvatarFiles();
                                        Utils.clearExternalCache();
                                    } catch (Exception ex) {
                                        ee = ex;
                                    }
                                    final Exception e = ee;
                                    handler.post(() -> {
                                        if (mProgressDialog != null) {
                                            if (e == null)
                                                mProgressDialog.dismiss("图片和头像缓存已经清除");
                                            else
                                                mProgressDialog.dismissError("发生错误 : " + e.getMessage());
                                        }
                                    });
                                });
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

    private void showFontSelectDialog() {
        final List<File> fonts = new ArrayList<>();
        try {
            File fontsDir = Utils.getFontsDir();
            File[] files = fontsDir.listFiles();
            Arrays.sort(files);
            for (File file : files) {
                fonts.add(file);
            }
        } catch (Exception e) {
            Logger.e(e);
        }

        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_font_selector, null);

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getActivity(),
                new RecyclerItemClickListener.SimpleOnItemClickListener() {
                });
        FontsAdapter fontsAdapter = new FontsAdapter(getActivity(), itemClickListener);
        fontsAdapter.setDatas(fonts);
        final RecyclerView recyclerView = viewlayout.findViewById(R.id.rv_fonts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(fontsAdapter);

        final AppCompatButton btnNoFont = viewlayout.findViewById(R.id.btn_no_font);
        btnNoFont.setTag("");
        if (TextUtils.isEmpty(HiSettingsHelper.getInstance().getFont()))
            btnNoFont.setCompoundDrawablesRelative(mCheckDrawable, null, null, null);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback
                = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                File font = fonts.get(position);
                try {
                    boolean result = font.delete();
                    if (result) {
                        UIUtils.toast(font.getName() + " 已经删除");
                        fonts.remove(position);
                        fontsAdapter.notifyItemRemoved(position);
                        if (font.getName().equals(HiSettingsHelper.getInstance().getFont())) {
                            HiSettingsHelper.getInstance().setFont("");
                            bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FONT));
                            btnNoFont.setCompoundDrawablesRelative(mCheckDrawable, null, null, null);
                        }
                    } else {
                        UIUtils.toast(font.getName() + " 删除失败");
                    }
                } catch (Exception exception) {
                    UIUtils.errorSnack(getView(), font.getName() + " 删除失败", exception.getMessage());
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(viewlayout);

        builder.setTitle("自定义字体")
                .setPositiveButton(getResources().getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                .setNeutralButton("导入字体",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mGetFontFile.launch("*/*");
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.show();

        View.OnClickListener btnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fontName = view.getTag().toString();
                HiSettingsHelper.getInstance().setFont(fontName);
                bindPreferenceSummaryToValue(findPreference(HiSettingsHelper.PERF_FONT));
                dialog.dismiss();
            }
        };
        fontsAdapter.setButtonClickListener(btnClickListener);
        btnNoFont.setOnClickListener(btnClickListener);
    }

    private void importFontFile(Uri uri) {
        if (uri == null)
            return;
        try {
            DocumentFile documentFile = DocumentFile.fromSingleUri(getContext(), uri);

            if (documentFile == null || documentFile.getName() == null) {
                UIUtils.errorSnack(getView(), "无法读取字体文件", "Uri : " + uri.toString());
                return;
            }
            if (!documentFile.getName().toLowerCase().endsWith(".ttf")
                    && !documentFile.getName().toLowerCase().endsWith(".otf")) {
                UIUtils.errorSnack(getView(),
                        documentFile.getName() + " 不是字体文件 (支持后缀 ttf或otf)",
                        "Uri : " + uri.toString());
                return;
            }
            Utils.copy(uri, new File(Utils.getFontsDir(), documentFile.getName()));
            showFontSelectDialog();
        } catch (Exception e) {
            UIUtils.errorSnack(getView(), "无法导入字体", e.getMessage());
            Logger.e(e);
        }
    }

    private class FontsAdapter extends BaseRvAdapter<File> {

        private final LayoutInflater mInflater;
        private final Drawable mCheckDrawable;
        private View.OnClickListener mButtonClickListener;

        FontsAdapter(Context context, RecyclerItemClickListener itemClickListener) {
            mInflater = LayoutInflater.from(context);
            mItemClickListener = itemClickListener;
            mCheckDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_check)
                    .color(UIUtils.isInLightThemeMode(context) ? Color.BLACK : Color.WHITE)
                    .sizeDp(20).paddingDp(4);
        }

        public void setButtonClickListener(View.OnClickListener listener) {
            mButtonClickListener = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType) {
            return new FontsAdapter.ViewHolderImpl(mInflater.inflate(R.layout.item_font, parent, false));
        }

        @Override
        public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, final int position) {
            final FontsAdapter.ViewHolderImpl holder = (FontsAdapter.ViewHolderImpl) viewHolder;

            File font = getItem(position);
            SpannableStringBuilder sBuilder = new SpannableStringBuilder();
            sBuilder.append(font.getName());
            CalligraphyTypefaceSpan typefaceSpan
                    = new CalligraphyTypefaceSpan(
                    TypefaceUtils.load(getContext().getAssets(), font.getAbsolutePath()));
            sBuilder.setSpan(typefaceSpan, 0, font.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.btnFont.setText(sBuilder, TextView.BufferType.SPANNABLE);
            if (font.getName().equals(HiSettingsHelper.getInstance().getFont())) {
                holder.btnFont.setCompoundDrawables(mCheckDrawable, null, null, null);
            } else {
                holder.btnFont.setCompoundDrawables(null, null, null, null);
            }
            holder.btnFont.setTag(font.getName());
            holder.btnFont.setOnClickListener(mButtonClickListener);
        }

        private class ViewHolderImpl extends RecyclerView.ViewHolder {
            AppCompatButton btnFont;

            ViewHolderImpl(View itemView) {
                super(itemView);
                btnFont = itemView.findViewById(R.id.btn_font);
            }
        }
    }

}
