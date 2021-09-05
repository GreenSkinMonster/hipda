package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.Theme;
import net.jejer.hipda.ui.MainFrameActivity;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class ThemeSettingDialog extends BottomDialog {

    private Drawable mWhiteCheckDrawable;
    private Drawable mBlackCheckDrawable;

    private String mThemeMode = HiSettingsHelper.getInstance().getTheme();
    private String mLightTheme = HiSettingsHelper.getInstance().getLightTheme();
    private String mDarkTheme = HiSettingsHelper.getInstance().getDarkTheme();
    private boolean mNavBarColored = HiSettingsHelper.getInstance().isNavBarColored();

    private MainFrameActivity mActivity;
    private final List<AppCompatButton> mThemeModeButtons = new ArrayList<>();
    private final List<AppCompatImageButton> mLightThemeButtons = new ArrayList<>();
    private final List<AppCompatImageButton> mDarkThemeButtons = new ArrayList<>();

    public ThemeSettingDialog(@NonNull Context context) {
        super(context);
        mActivity = (MainFrameActivity) context;
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        mWhiteCheckDrawable = new IconicsDrawable(getContext(), GoogleMaterial.Icon.gmd_check)
                .color(Color.WHITE).sizeDp(20).paddingDp(4);
        mBlackCheckDrawable = new IconicsDrawable(getContext(), GoogleMaterial.Icon.gmd_check)
                .color(Color.BLACK).sizeDp(20).paddingDp(4);

        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        GridLayout darkColorsLayout = view.findViewById(R.id.dark_theme_container);
        GridLayout lightColorsLayout = view.findViewById(R.id.light_theme_container);
        AppCompatButton btnThemeLight = view.findViewById(R.id.btn_theme_light);
        AppCompatButton btnThemeAuto = view.findViewById(R.id.btn_theme_auto);
        AppCompatButton btnThemeDark = view.findViewById(R.id.btn_theme_dark);
        TextView lightThemeLabel = view.findViewById(R.id.light_theme_label);
        TextView darkThemeLabel = view.findViewById(R.id.dark_theme_label);
        SwitchCompat swNavbarColored = view.findViewById(R.id.switch_navbar_colored);
        AppCompatButton btnApply = view.findViewById(R.id.btn_apply);

        if (UIUtils.isInLightThemeMode(getContext())) {
            lightThemeLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            darkThemeLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        }

        swNavbarColored.setChecked(HiSettingsHelper.getInstance().isNavBarColored());
        swNavbarColored.setOnCheckedChangeListener((buttonView, isChecked) -> mNavBarColored = isChecked);
        btnApply.setOnClickListener((v) -> {
            boolean themeModeChanged = false;
            if (!mThemeMode.equals(HiSettingsHelper.getInstance().getTheme())) {
                HiSettingsHelper.getInstance().setTheme(mThemeMode);
                themeModeChanged = true;
            }
            boolean otherChanged = !mDarkTheme.equals(HiSettingsHelper.getInstance().getDarkTheme())
                    || !mLightTheme.equals(HiSettingsHelper.getInstance().getLightTheme())
                    || mNavBarColored != HiSettingsHelper.getInstance().isNavBarColored();

            HiSettingsHelper.getInstance().setDarkTheme(mDarkTheme);
            HiSettingsHelper.getInstance().setLightTheme(mLightTheme);
            HiSettingsHelper.getInstance().setNavBarColored(mNavBarColored);

            if (otherChanged) {
                mActivity.recreateActivity();
            } else if (themeModeChanged) {
                UIUtils.setLightDarkThemeMode();
            }
            dismiss();
        });

        view.post(new Runnable() {
            @Override
            public void run() {
                int dialogPadding = (int) getContext().getResources().getDimension(R.dimen.theme_dialog_padding);
                int dialogWidth = view.getWidth() - 2 * dialogPadding;
                int columnCount = getContext().getResources().getInteger(R.integer.theme_grid_column_count);
                int unitWidth = dialogWidth / columnCount;

                for (Theme theme : HiSettingsHelper.DARK_THEMES) {
                    AppCompatImageButton button = new AppCompatImageButton(getContext());
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(unitWidth, Utils.dpToPx(48));
                    button.setLayoutParams(layoutParams);
                    button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), theme.getColorId()));
                    button.setTag(HiSettingsHelper.THEME_MODE_DARK + ":" + theme.getName());
                    button.setOnClickListener(themeClickListener);
                    darkColorsLayout.addView(button);
                    mDarkThemeButtons.add(button);
                }
                setDarkThemeCheckState();

                for (Theme theme : HiSettingsHelper.LIGHT_THEMES) {
                    AppCompatImageButton button = new AppCompatImageButton(getContext());
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(unitWidth, Utils.dpToPx(48));
                    button.setLayoutParams(layoutParams);
                    button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), theme.getColorId()));
                    button.setTag(HiSettingsHelper.THEME_MODE_LIGHT + ":" + theme.getName());
                    button.setOnClickListener(themeClickListener);
                    lightColorsLayout.addView(button);
                    mLightThemeButtons.add(button);
                }
                setLightThemeCheckState();
            }
        });

        btnThemeLight.setOnClickListener(themeModeClickListener);
        btnThemeAuto.setOnClickListener(themeModeClickListener);
        btnThemeDark.setOnClickListener(themeModeClickListener);

        mThemeModeButtons.add(btnThemeAuto);
        mThemeModeButtons.add(btnThemeLight);
        mThemeModeButtons.add(btnThemeDark);

        setThemeModeCheckState();
    }

    private void setThemeModeCheckState() {
        for (AppCompatButton button : mThemeModeButtons) {
            if (mThemeMode.equals(button.getTag())) {
                if (mThemeMode.equals(HiSettingsHelper.THEME_MODE_LIGHT)) {
                    button.setCompoundDrawables(mBlackCheckDrawable, null, null, null);
                } else {
                    button.setCompoundDrawables(mWhiteCheckDrawable, null, null, null);
                }
            } else {
                button.setCompoundDrawables(null, null, null, null);
            }
        }
    }

    private void setDarkThemeCheckState() {
        for (AppCompatImageButton button : mDarkThemeButtons) {
            if ((HiSettingsHelper.THEME_MODE_DARK + ":" + mDarkTheme).equals(button.getTag())) {
                button.setImageDrawable(mWhiteCheckDrawable);
            } else {
                button.setImageDrawable(null);
            }
        }
    }

    private void setLightThemeCheckState() {
        for (AppCompatImageButton button : mLightThemeButtons) {
            if ((HiSettingsHelper.THEME_MODE_LIGHT + ":" + mLightTheme).equals(button.getTag())) {
                if (HiSettingsHelper.THEME_WHITE.equals(mLightTheme))
                    button.setImageDrawable(mBlackCheckDrawable);
                else
                    button.setImageDrawable(mWhiteCheckDrawable);
            } else {
                button.setImageDrawable(null);
            }
        }
    }

    private final View.OnClickListener themeModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String tag = view.getTag().toString();
            mThemeMode = tag;
            setThemeModeCheckState();
        }
    };

    private final View.OnClickListener themeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String tag = view.getTag().toString();
            int index = tag.indexOf(":");
            String themeMode = tag.substring(0, index);
            String themeName = tag.substring(index + 1);
            if (HiSettingsHelper.THEME_MODE_DARK.equals(themeMode)) {
                mDarkTheme = themeName;
                setDarkThemeCheckState();
            } else if (HiSettingsHelper.THEME_MODE_LIGHT.equals(themeMode)) {
                mLightTheme = themeName;
                setLightThemeCheckState();
            }
        }
    };
}
