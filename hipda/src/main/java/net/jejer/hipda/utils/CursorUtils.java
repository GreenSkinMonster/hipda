package net.jejer.hipda.utils;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * http://hmkcode.com/android-display-selected-image-and-its-real-path/
 * Created by GreenSkinMonster on 2015-04-14.
 */
public class CursorUtils {

    public static ImageFileInfo getImageFileInfo(Context context, Uri uri) {
        ImageFileInfo result;
        if (Build.VERSION.SDK_INT < 19 || uri.toString().startsWith("content://media")) {
            result = getImageInfo_API11to18(context, uri);
            if (result == null) {
                result = getImageInfo_API19(context, uri);
            }
        } else {
            result = getImageInfo_API19(context, uri);
            if (result == null) {
                result = getImageInfo_API11to18(context, uri);
            }
        }
        if (result == null)
            return new ImageFileInfo();
        return result;
    }

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
            Log.e("ImageFileInfo", e.getMessage());
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
            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    contentUri, column, null, null, null);
            cursor = cursorLoader.loadInBackground();
            int pathIndex = cursor.getColumnIndexOrThrow(column[0]);
            int orientationIndex = cursor.getColumnIndexOrThrow(column[1]);

            if (cursor.moveToFirst()) {
                if (pathIndex >= 0)
                    result.setFilePath(cursor.getString(pathIndex));
                if (orientationIndex >= 0)
                    result.setOrientation(cursor.getInt(orientationIndex));
            }

        } catch (Exception e) {
            Log.e("ImageFileInfo", e.getMessage());
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return result;
    }

}

