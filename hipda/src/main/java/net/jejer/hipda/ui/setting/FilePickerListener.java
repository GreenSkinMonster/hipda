package net.jejer.hipda.ui.setting;

import android.app.Activity;
import android.os.Environment;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import net.jejer.hipda.bean.HiSettingsHelper;

import java.io.File;

/**
 * Created by GreenSkinMonster on 2017-06-03.
 */

public class FilePickerListener extends OnPreferenceClickListener implements DialogSelectionListener {

    public final static int FONT_FILE = 0;
    public final static int SAVE_DIR = 1;

    private Preference mPreference;
    private Activity mActivity;
    private int mType;

    FilePickerListener(Activity activity, int type) {
        mType = type;
        mActivity = activity;
    }

    @Override
    public boolean onPreferenceSingleClick(Preference preference) {
        mPreference = preference;

        File offset = null;
        String current = HiSettingsHelper.getInstance().getStringValue(preference.getKey(), "");
        if (!TextUtils.isEmpty(current)) {
            File currentFile = new File(current);
            if (currentFile.exists()) {
                offset = currentFile.getParentFile();
            }
        }

        DialogProperties properties = new DialogProperties();
        properties.root = Environment.getExternalStorageDirectory();
        properties.error_dir = Environment.getExternalStorageDirectory();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        if (offset != null)
            properties.offset = offset;
        if (mType == FONT_FILE) {
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.extensions = "ttf:otf".split(":");
            properties.enable_clear_button = true;
        } else if (mType == SAVE_DIR) {
            properties.selection_type = DialogConfigs.DIR_SELECT;
        }

        FilePickerDialog mDialog = new FilePickerDialog(mActivity);
        mDialog.setProperties(properties);
        mDialog.setDialogSelectionListener(this);
        mDialog.setTitle(preference.getTitle());
        mDialog.show();
        return false;
    }

    @Override
    public void onSelectedFilePaths(String[] files) {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            buff.append(files[i]);
            if (i != files.length - 1)
                buff.append(":");
        }
        String dFiles = buff.toString();
        if (mPreference != null) {
            HiSettingsHelper.getInstance().setStringValue(mPreference.getKey(), dFiles);
            mPreference.setSummary(dFiles);
        }
    }
}
