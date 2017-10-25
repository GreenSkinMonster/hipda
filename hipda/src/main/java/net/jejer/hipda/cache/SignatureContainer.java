package net.jejer.hipda.cache;

import net.jejer.hipda.utils.Utils;

/**
 * Created by GreenSkinMonster on 2017-10-25.
 */

public class SignatureContainer {

    private static LRUCache<String, String> SIGS = new LRUCache<>(128);

    public static void putSignature(String uid, String signature) {
        SIGS.put(uid, signature);
    }

    public static String getSignature(String uid) {
        return Utils.nullToText(SIGS.get(uid));
    }


}
