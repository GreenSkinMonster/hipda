package net.jejer.hipda.cache;

import net.jejer.hipda.glide.ImageReadyInfo;

/**
 * downloaded image cache
 * Created by GreenSkinMonster on 2015-05-21.
 */
public class ImageContainer {

    private static LRUCache<String, ImageReadyInfo> IMAGES = new LRUCache<>(1024);

    public static void markImageReady(String url, ImageReadyInfo imageReadyInfo) {
        IMAGES.put(url, imageReadyInfo);
    }

    public static ImageReadyInfo getImageInfo(String url) {
        return IMAGES.get(url);
    }


}
