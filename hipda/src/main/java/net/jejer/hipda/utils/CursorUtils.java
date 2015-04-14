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
        if (Build.VERSION.SDK_INT < 19)
            return getImageInfo_API11to18(context, uri);
        else
            return getImageInfo_API19(context, uri);
    }

    private static ImageFileInfo getImageInfo_API19(Context context, Uri uri) {
        ImageFileInfo result = new ImageFileInfo();
        String[] column = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.ORIENTATION};

        String wholeID = DocumentsContract.getDocumentId(uri);
        String id = wholeID.split(":")[1];
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);
        try {
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
        }
        cursor.close();
        return result;
    }


    private static ImageFileInfo getImageInfo_API11to18(Context context, Uri contentUri) {
        ImageFileInfo result = new ImageFileInfo();
        String[] column = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.ORIENTATION};

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, column, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        try {
            if (cursor != null) {
                int pathIndex = cursor.getColumnIndexOrThrow(column[0]);
                int orientationIndex = cursor.getColumnIndexOrThrow(column[1]);

                if (cursor.moveToFirst()) {
                    if (pathIndex >= 0)
                        result.setFilePath(cursor.getString(pathIndex));
                    if (orientationIndex >= 0)
                        result.setOrientation(cursor.getInt(orientationIndex));
                }

            }
        } catch (Exception e) {
            Log.e("ImageFileInfo", e.getMessage());
        }
        if (cursor != null)
            cursor.close();
        return result;
    }

}

