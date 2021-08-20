package net.jejer.hipda.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.ui.HiApplication;

import java.util.concurrent.ThreadLocalRandom;

/**
 * get color id from theme attr id
 * Created by GreenSkinMonster on 2015-05-09.
 */
public class ColorHelper {

    private final static int DAY_REF_COLOR = Color.parseColor("#ffffff");
    private final static int NIGHT_REF_COLOR = Color.parseColor("#000000");

    public static int getColorIdByAttr(Context ctx, int attrId) {
        return getColor(ctx, attrId);
    }

    public static int getTextColorPrimary(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.textColorPrimary);
    }

    public static int getTextColorSecondary(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.textColorSecondary);
    }

    public static int getColorAccent(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.colorAccent);
    }

    public static int getColorPrimary(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.colorPrimary);
    }

    public static int getWindowBackgroundColor(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.window_background);
    }

    public static int getSwipeBackgroundColor(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.swipe_background);
    }

    public static int getSwipeColor(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.swipe_color);
    }

    private static int getColor(Context ctx, int attrId) {
        final TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }

    public static boolean isTextColorReadable(String color) {
        float delta = UIUtils.isInDarkThemeMode(HiApplication.getAppContext()) ? 0.35f : 0.1f;
        float[] textHslColor = new float[3], refHslColor = new float[3];
        ColorUtils.colorToHSL(Color.parseColor(color), textHslColor);
        ColorUtils.colorToHSL(UIUtils.isInDarkThemeMode(HiApplication.getAppContext()) ? NIGHT_REF_COLOR : DAY_REF_COLOR, refHslColor);
        return Math.abs(textHslColor[2] - refHslColor[2]) >= delta;
    }

    public static int getRandomColor() {
        int[] colors = {
                R.color.md_red_700
                , R.color.md_pink_700
                , R.color.md_purple_700
                , R.color.md_deep_purple_700
                , R.color.md_indigo_700
                , R.color.md_blue_700
                , R.color.md_light_blue_700
                , R.color.md_cyan_700
                , R.color.md_teal_700
                , R.color.md_green_700
                , R.color.md_light_green_700
                , R.color.md_lime_700
                , R.color.md_yellow_700
                , R.color.md_amber_700
                , R.color.md_orange_700
                , R.color.md_deep_orange_700
                , R.color.md_brown_700
        };
        return colors[ThreadLocalRandom.current().nextInt(0, colors.length)];
    }

}
