package net.jejer.hipda.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import net.jejer.hipda.R;

import java.util.HashMap;

/**
 * get color id from theme attr id
 * Created by GreenSkinMonster on 2015-05-09.
 */
public class ColorUtils {

    private static HashMap<Integer, Integer> COLOR_IDS = new HashMap<>();

    public static void clear() {
        COLOR_IDS.clear();
    }

    public static int getColorIdByAttr(Context ctx, int attrId) {
        if (COLOR_IDS.containsKey(attrId))
            return COLOR_IDS.get(attrId);

        int colorId = getColor2(ctx, attrId);

        COLOR_IDS.put(attrId, colorId);
        return colorId;
    }

    public static int getDefaultTextColor(Context ctx) {
        return getColorIdByAttr(ctx, android.R.attr.textColorPrimary);
    }

    public static int getColorAccent(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.colorAccent);
    }

    public static int getColorPrimary(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.colorPrimary);
    }

    public static int getListBackgroundColor(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.list_item_background);
    }

    private static int getColor1(Context ctx, int attrId) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = ctx.obtainStyledAttributes(typedValue.data, new int[]{attrId});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    private static int getColor2(Context ctx, int attrId) {
        final TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }

    private static int getColor3(Context ctx, int attrId) {
        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(attrId, typedValue, true);
        return ContextCompat.getColor(ctx, typedValue.resourceId);
    }

}
