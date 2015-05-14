package net.jejer.hipda.utils;

import android.content.Context;
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

        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(attrId, typedValue, true);
        int colorId = ctx.getResources().getColor(typedValue.resourceId);
        COLOR_IDS.put(attrId, colorId);

        return colorId;
    }

    public static int getDefaultTextColor(Context ctx) {
        return getColorIdByAttr(ctx, android.R.attr.textColorPrimary);
    }

    public static int getColorAccent(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.colorAccent);
    }

    public static int getListBackgroundColor(Context ctx) {
        return getColorIdByAttr(ctx, R.attr.list_item_background);
    }

}
