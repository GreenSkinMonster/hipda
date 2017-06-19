package net.jejer.hipda.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.vanniktech.emoji.EmojiPopup;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Utils;

import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by GreenSkinMonster on 2017-06-14.
 */

public class BaseActivity extends AppCompatActivity {

    public String mSessionId;
    protected View mRootView;
    protected View mMainFrameContainer;
    protected Toolbar mToolbar;
    protected AppBarLayout mAppBarLayout;
    protected FloatingActionButton mMainFab;
    protected FloatingActionButton mNotiificationFab;

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
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && mAppBarLayout != null) {
            if (HiSettingsHelper.getInstance().isWhiteTheme()) {
                mAppBarLayout.setStateListAnimator(null);
                mAppBarLayout.setElevation(Utils.dpToPx(this, 2));
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (HiApplication.isFontSet())
            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        else
            super.attachBaseContext(newBase);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateAppBarScrollFlag() {
        setAppBarCollapsible(HiSettingsHelper.getInstance().isAppBarCollapsible());
    }

    protected void setAppBarCollapsible(boolean collapsible) {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        if (collapsible) {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            mAppBarLayout.setTag("1");
        } else {
            params.setScrollFlags(0);
            mAppBarLayout.setTag("0");
            if (mMainFrameContainer.getPaddingBottom() != 0) {
                mMainFrameContainer.setPadding(
                        mMainFrameContainer.getPaddingLeft(),
                        mMainFrameContainer.getPaddingTop(),
                        mMainFrameContainer.getPaddingRight(),
                        0);
                mMainFrameContainer.requestLayout();
            }
        }
    }

    public FloatingActionButton getMainFab() {
        return mMainFab;
    }

    public FloatingActionButton getNotificationFab() {
        return mNotiificationFab;
    }

    public EmojiPopup.Builder getEmojiBuilder() {
        return EmojiPopup.Builder.fromRootView(mRootView);
    }

    public View getRootView() {
        return mRootView;
    }

    public View getToolbar() {
        return mToolbar;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_frame_container);
        if (fragment instanceof BaseFragment) {
            if (!((BaseFragment) fragment).onBackPressed()) {
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    public void finishWithNoSlide() {
        super.finish();
    }

}
