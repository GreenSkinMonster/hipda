package net.jejer.hipda.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.TextUtils;

import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GreenSkinMonster on 2016-07-23.
 */
public class HistoryDao {

    private final static int MAX_SIZE = 100;

    public synchronized static void saveHistoryInBackground(
            final String tid, final String fid, final String title,
            final String uid, final String username, final String postTime) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                saveHistory(tid, fid, title, uid, username, postTime);
            }
        });
    }

    private synchronized static void saveHistory(
            String tid, String fid, String title,
            String uid, String username, String postTime) {

        if (TextUtils.isEmpty(tid) || TextUtils.isEmpty(title))
            return;

        SQLiteDatabase db = null;
        try {
            db = HistoryDBHelper.getHelper().getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("tid", tid);
            contentValues.put("title", title);

            contentValues.put("fid", fid);
            contentValues.put("uid", uid);
            contentValues.put("username", username);
            contentValues.put("post_time", postTime);

            contentValues.put("visit_time", System.currentTimeMillis());

            db.replace(HistoryDBHelper.TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public synchronized static void updateHistoryInBackground(
            final String tid, final String fid, final String title) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                updateHistory(tid, fid, title);
            }
        });
    }

    private synchronized static void updateHistory(
            String tid, String fid, String title) {

        if (TextUtils.isEmpty(tid) || TextUtils.isEmpty(title))
            return;

        SQLiteDatabase db = null;
        try {
            db = HistoryDBHelper.getHelper().getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("title", title);
            contentValues.put("fid", fid);
            contentValues.put("visit_time", System.currentTimeMillis());

            db.update(HistoryDBHelper.TABLE_NAME, contentValues, "tid=?", new String[]{tid});
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
            db = HistoryDBHelper.getHelper().getWritableDatabase();
            db.execSQL("delete from " + HistoryDBHelper.TABLE_NAME
                    + " where tid not in " +
                    "(select tid from " + HistoryDBHelper.TABLE_NAME
                    + " order by visit_time desc limit " + MAX_SIZE + ")");
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public static List<History> getHistories() {
        List<History> histories = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = HistoryDBHelper.getHelper().getReadableDatabase();
            cursor = db.rawQuery(
                    "select * from " + HistoryDBHelper.TABLE_NAME +
                            " order by visit_time desc limit " + MAX_SIZE,
                    null);

            while (cursor.moveToNext()) {
                String tid = Utils.nullToText(cursor.getString(cursor.getColumnIndex("tid")));
                String fid = Utils.nullToText(cursor.getString(cursor.getColumnIndex("fid")));
                String title = Utils.nullToText(cursor.getString(cursor.getColumnIndex("title")));
                String uid = Utils.nullToText(cursor.getString(cursor.getColumnIndex("uid")));
                String username = Utils.nullToText(cursor.getString(cursor.getColumnIndex("username")));
                String post_time = Utils.nullToText(cursor.getString(cursor.getColumnIndex("post_time")));
                long visit_time = cursor.getLong(cursor.getColumnIndex("visit_time"));

                History history = new History();
                history.setTid(tid);
                history.setFid(fid);
                history.setTitle(title);
                history.setUid(uid);
                history.setUsername(username);
                history.setPostTime(post_time);
                history.setVisitTime(visit_time);

                histories.add(history);
            }
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.close();
        }

        return histories;
    }

}
