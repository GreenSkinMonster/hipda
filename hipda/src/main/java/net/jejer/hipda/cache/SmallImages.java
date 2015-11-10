package net.jejer.hipda.cache;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.HiUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * cache small images
 * Created by GreenSkinMonster on 2015-11-10.
 */
public class SmallImages {

    private static Map<String, Integer> IMAGES = new HashMap<>();

    static {
        IMAGES.put(HiUtils.BaseUrl + "attachments/day_140621/1406211752793e731a4fec8f7b.png", R.drawable.win);
    }

    public static boolean contains(String url) {
        return IMAGES.containsKey(url);
    }

    public static int getDrawable(String url) {
        return IMAGES.get(url);
    }

}
