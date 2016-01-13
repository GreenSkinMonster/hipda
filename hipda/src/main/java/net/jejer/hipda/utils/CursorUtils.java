package net.jejer.hipda.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

/**
 * http://hmkcode.com/android-display-selected-image-and-its-real-path/
 * Created by GreenSkinMonster on 2015-04-14.
 */
public class CursorUtils {

    public static ImageFileInfo getImageFileInfo(Context context, Uri uri) {
        ImageFileInfo result;
        if (Build.VERSION.SDK_INT < 19 || uri.toString().startsWith("content://media")) {
            result = getImageInfo_API11to18(context, uri);
            if (result == null && Build.VERSION.SDK_INT >= 19) {
                result = getImageInfo_API19(context, uri);
            }
        } else {
            result = getImageInfo_API19(context, uri);
            if (result == null) {
                result = getImageInfo_API11to18(context, uri);
            }
        }
        if (result == null || TextUtils.isEmpty(result.getFilePath()))
            return new ImageFileInfo();

        File imageFile = new File(result.getFilePath());
        if (!imageFile.exists())
            return new ImageFileInfo();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(result.getFilePath(), options);
        int width = options.outWidth;
        int height = options.outHeight;
        String mime = Utils.nullToText(options.outMimeType);

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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static ImageFileInfo getImageInfo_API19(Context context, Uri uri) {
        ImageFileInfo result = new ImageFileInfo();
        String[] column = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.ORIENTATION};

        Cursor cursor = null;
        try {
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
            int pathIndex = cursor.getColumnIndex(column[0]);
            int orientationIndex = cursor.getColumnIndex(column[1]);

            if (cursor.moveToFirst()) {
                if (pathIndex >= 0)
                    result.setFilePath(cursor.getString(pathIndex));
                if (orientationIndex >= 0)
                    result.setOrientation(cursor.getInt(orientationIndex));
            }
        } catch (Exception e) {
            Logger.e(e);
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return result;
    }


    private static ImageFileInfo getImageInfo_API11to18(Context context, Uri contentUri) {
        ImageFileInfo result = new ImageFileInfo();
        String[] column = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.ORIENTATION};

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentUri, null, null, null, null);

            int pathIndex = cursor.getColumnIndexOrThrow(column[0]);
            int orientationIndex = cursor.getColumnIndexOrThrow(column[1]);

            if (cursor.moveToFirst()) {
                if (pathIndex >= 0)
                    result.setFilePath(cursor.getString(pathIndex));
                if (orientationIndex >= 0)
                    result.setOrientation(cursor.getInt(orientationIndex));
            }
        } catch (Exception e) {
            Logger.e(e);
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return result;
    }

}

