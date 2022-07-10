package net.jejer.hipda.utils;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;

/**
 * http://hmkcode.com/android-display-selected-image-and-its-real-path/
 * Created by GreenSkinMonster on 2015-04-14.
 */
public class CursorUtils {

    public static ImageFileInfo getImageFileInfo(File imageFile) {
        if (!imageFile.exists())
            return null;

        ImageFileInfo result = new ImageFileInfo();
        result.setFilePath(imageFile.getAbsolutePath());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        int width = options.outWidth;
        int height = options.outHeight;
        String mime = Utils.nullToText(options.outMimeType);

        if (TextUtils.isEmpty(mime) || width <= 0 || height <= 0)
            return null;

        result.setMime(mime);
        result.setFileSize(imageFile.length());
        result.setWidth(width);
        result.setHeight(height);

        if (result.getOrientation() == -1) {
            int orientation = getOrientationFromExif(result.getFilePath());
            result.setOrientation(orientation);
        }
        return result;
    }

    private static int getOrientationFromExif(String path) {
        int orientation = -1;
        try {
            ExifInterface exif = new ExifInterface(path);
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;
            }
        } catch (Exception e) {
            Logger.e(e);
        }
        return orientation;
    }

}

