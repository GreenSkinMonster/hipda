package net.jejer.hipda.utils;

/**
 * calculate image display size base on argments
 * Created by GreenSkinMonster on 2015-05-22.
 */
public class ImageSizeUtils {

    private static long MAX_MEMORY = -1;

    public final static int NORMAL_IMAGE_DECODE_WIDTH = 450;
    public final static int SMALLER_IMAGE_DECODE_WIDTH = 420;
    public final static int SMALLEST_IMAGE_DECODE_WIDTH = 380;

    public final static int GIF_DECODE_WIDTH = 320;

    public static int getDecodeSize(int imageCount) {
        int decodeSize = NORMAL_IMAGE_DECODE_WIDTH;
        if (imageCount > 25) {
            decodeSize = SMALLEST_IMAGE_DECODE_WIDTH;
        } else if (imageCount > 10) {
            decodeSize = SMALLER_IMAGE_DECODE_WIDTH;
        }

        if (MAX_MEMORY == -1) {
            MAX_MEMORY = Runtime.getRuntime().maxMemory();
        }
        //smaller size for less than 64M app memory device
        if (MAX_MEMORY < 64 * 1024 * 1024) {
            decodeSize -= 40;
        }
        return decodeSize;
    }
}
