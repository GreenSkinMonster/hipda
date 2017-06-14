package net.jejer.hipda.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.TypedValue;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;

import java.util.HashMap;

/**
 * get color id from theme attr id
 * Created by GreenSkinMonster on 2015-05-09.
 */
public class ColorHelper {

    private static HashMap<Integer, Integer> COLOR_IDS = new HashMap<>();
    private final static int DAY_REF_COLOR = Color.parseColor("#ffffff");
    private final static int NIGHT_REF_COLOR = Color.parseColor("#000000");

    public static void clear() {
        COLOR_IDS.clear();
    }

    public static int getColorIdByAttr(Context ctx, int attrId) {
        if (COLOR_IDS.containsKey(attrId))
            return COLOR_IDS.get(attrId);

        int colorId = getColor(ctx, attrId);

        COLOR_IDS.put(attrId, colorId);
        return colorId;
    }

    public static int getTextColorPrimary(Context ctx) {
        return getColorIdByAttr(ctx, android.R.attr.textColorPrimary);
    }

    public static int getTextColorSecondary(Context ctx) {
        return getColorIdByAttr(ctx, android.R.attr.textColorSecondary);
    }

    public static int getColorAccent(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.colorAccent);
    }

    public static int getColorPrimary(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.colorPrimary);
    }

    public static int getColorPrimaryDark(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.colorPrimaryDark);
    }

    public static int getListBackgroundColor(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.list_item_background);
    }

    public static int getSwipeBackgroundColor(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.swipe_background);
    }

    public static int getSwipeColor(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.swipe_color);
    }

    private static int getColor(Context ctx, int attrId) {
        final TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }

    public static boolean isTextColorReadable(String color) {
        float delta = HiSettingsHelper.getInstance().isNightMode() ? 0.35f : 0.1f;
        float[] textHslColor = new float[3], refHslColor = new float[3];
        ColorUtils.colorToHSL(Color.parseColor(color), textHslColor);
        ColorUtils.colorToHSL(HiSettingsHelper.getInstance().isNightMode() ? NIGHT_REF_COLOR : DAY_REF_COLOR, refHslColor);
        return Math.abs(textHslColor[2] - refHslColor[2]) >= delta;
    }

}
