package net.jejer.hipda.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiPopup;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.HiUtils;

import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by GreenSkinMonster on 2017-06-14.
 */

public class BaseActivity extends AppCompatActivity {

    public String mSessionId;
    protected View rootView;
    protected EmojiPopup mEmojiPopup;
    protected IconicsDrawable mKeyboardDrawable;
    protected IconicsDrawable mFaceDrawable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionId = UUID.randomUUID().toString();

        if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == HiSettingsHelper.getInstance().getScreenOrietation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == HiSettingsHelper.getInstance().getScreenOrietation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }

        setTheme(HiUtils.getThemeValue(this,
                HiSettingsHelper.getInstance().getActiveTheme(),
                HiSettingsHelper.getInstance().getPrimaryColor()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && HiSettingsHelper.getInstance().isNavBarColored()) {
            getWindow().setNavigationBarColor(ColorHelper.getColorPrimary(this));
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (HiApplication.isFontSet())
            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        else
            super.attachBaseContext(newBase);
    }

    public EmojiPopup.Builder getEmojiBuilder() {
        return EmojiPopup.Builder.fromRootView(rootView);
    }

}
