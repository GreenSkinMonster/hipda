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


public class ThemeSettingDialog extends BottomDialog {

    private Context context;
    private Drawable whiteCheckDrawable;
    private Drawable blackCheckDrawable;

    public ThemeSettingDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        whiteCheckDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_check)
                .color(Color.WHITE).sizeDp(20).paddingDp(4);
        blackCheckDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_check)
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
        if (UIUtils.isInLightThemeMode(context)) {
            lightThemeLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            darkThemeLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        }

        view.post(new Runnable() {
            @Override
            public void run() {
                int dialogPadding = (int) context.getResources().getDimension(R.dimen.theme_dialog_padding);
                int dialogWidth = view.getWidth() - 2 * dialogPadding;
                int columnCount = context.getResources().getInteger(R.integer.theme_grid_column_count);
                int unitWidth = dialogWidth / columnCount;

                for (Theme theme : HiSettingsHelper.DARK_THEMES) {
                    AppCompatImageButton button = new AppCompatImageButton(getContext());
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(unitWidth, Utils.dpToPx(48));
                    button.setLayoutParams(layoutParams);
                    button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), theme.getColorId()));
                    button.setTag(HiSettingsHelper.THEME_MODE_DARK + ":" + theme.getName());
                    button.setOnClickListener(themeClickListener);
                    if (theme.getName().equals(HiSettingsHelper.getInstance().getDarkTheme())) {
                        button.setImageDrawable(whiteCheckDrawable);
                    }
                    darkColorsLayout.addView(button);
                }

                for (Theme theme : HiSettingsHelper.LIGHT_THEMES) {
                    AppCompatImageButton button = new AppCompatImageButton(getContext());
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(unitWidth, Utils.dpToPx(48));
                    button.setLayoutParams(layoutParams);
                    button.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), theme.getColorId()));
                    button.setTag(HiSettingsHelper.THEME_MODE_LIGHT + ":" + theme.getName());
                    button.setOnClickListener(themeClickListener);
                    if (theme.getName().equals(HiSettingsHelper.getInstance().getLightTheme())) {
                        if (theme.getTextColorId() == R.color.white)
                            button.setImageDrawable(whiteCheckDrawable);
                        else
                            button.setImageDrawable(blackCheckDrawable);
                    }
                    lightColorsLayout.addView(button);
                }
            }
        });

        btnThemeLight.setOnClickListener(themeModeClickListener);
        btnThemeAuto.setOnClickListener(themeModeClickListener);
        btnThemeDark.setOnClickListener(themeModeClickListener);

        if (HiSettingsHelper.THEME_MODE_AUTO.equals(HiSettingsHelper.getInstance().getTheme())) {
            btnThemeAuto.setCompoundDrawables(whiteCheckDrawable, null, null, null);
        } else if (HiSettingsHelper.THEME_MODE_DARK.equals(HiSettingsHelper.getInstance().getTheme())) {
            btnThemeDark.setCompoundDrawables(whiteCheckDrawable, null, null, null);
        } else if (HiSettingsHelper.THEME_MODE_LIGHT.equals(HiSettingsHelper.getInstance().getTheme())) {
            btnThemeLight.setCompoundDrawables(blackCheckDrawable, null, null, null);
        }
    }

    private final View.OnClickListener themeModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String tag = view.getTag().toString();
            HiSettingsHelper.getInstance().setTheme(tag);
            UIUtils.setLightDarkThemeMode();
            dismiss();
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
                HiSettingsHelper.getInstance().setDarkTheme(themeName);
            } else if (HiSettingsHelper.THEME_MODE_LIGHT.equals(themeMode)) {
                HiSettingsHelper.getInstance().setLightTheme(themeName);
            }
//            UIUtils.setDayNightTheme();
//            UIUtils.setActivityTheme((MainFrameActivity)context);
            ((MainFrameActivity) context).recreateActivity();
            dismiss();
        }
    };
}
