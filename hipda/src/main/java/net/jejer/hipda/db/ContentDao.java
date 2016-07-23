package net.jejer.hipda.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import net.jejer.hipda.utils.Logger;

import java.util.LinkedHashSet;

/**
 * Created by GreenSkinMonster on 2016-07-23.
 */
public class ContentDao {

    private final static int MAX_SIZE = 10;

    public synchronized static void saveContent(String sessionId, String content) {
        SQLiteDatabase db = null;
        try {
            db = ContentDBHelper.getHelper().getWritableDatabase();
            if (!TextUtils.isEmpty(content)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("session_id", sessionId);
                contentValues.put("time", System.currentTimeMillis());
                contentValues.put("content", content);
                db.replace(ContentDBHelper.TABLE_NAME, null, contentValues);
            } else {
                db.delete(ContentDBHelper.TABLE_NAME, "session_id=?", new String[]{sessionId});
            }
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public synchronized static void cleanup() {
        SQLiteDatabase db = null;
        try {
            db = ContentDBHelper.getHelper().getWritableDatabase();
            db.execSQL("delete from " + ContentDBHelper.TABLE_NAME
                    + " where session_id not in " +
                    "(select session_id from " + ContentDBHelper.TABLE_NAME +
                    " order by time desc limit " + MAX_SIZE + ")" +
                    " or session_id is null" +
                    " or session_id=''");
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static Content[] getSavedContents(String currentSessionId) {
        Content[] contents = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = ContentDBHelper.getHelper().getReadableDatabase();

            String[] projection = {
                    "session_id",
                    "time",
                    "content"
            };

            cursor = db.query(
                    ContentDBHelper.TABLE_NAME,
                    projection,
                    "content is not null and content<>'' and session_id<>?",
                    new String[]{currentSessionId},
                    null,
                    null,
                    "time desc",
                    "" + MAX_SIZE
            );

            LinkedHashSet<Content> cnts = new LinkedHashSet<>();
            while (cursor.moveToNext()) {
                long time = cursor.getLong(cursor.getColumnIndex("time"));
                String sessionId = cursor.getString(cursor.getColumnIndex("session_id"));
                String content = cursor.getString(cursor.getColumnIndex("content"));

                cnts.add(new Content(sessionId, content, time));
            }

            contents = cnts.toArray(new Content[cnts.size()]);
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.close();
        }
        return contents;
    }

}
