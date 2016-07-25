package net.jejer.hipda.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.TextUtils;

import net.jejer.hipda.utils.HiUtils;
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

        if (!HiUtils.isValidId(tid))
            return;

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = HistoryDBHelper.getHelper().getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("tid", tid);

            if (!TextUtils.isEmpty(title))
                contentValues.put("title", title);
            if (!TextUtils.isEmpty(fid))
                contentValues.put("fid", fid);
            if (!TextUtils.isEmpty(uid))
                contentValues.put("uid", uid);
            if (!TextUtils.isEmpty(username))
                contentValues.put("username", username);
            if (!TextUtils.isEmpty(postTime))
                contentValues.put("post_time", postTime);

            contentValues.put("visit_time", System.currentTimeMillis());

            cursor = db.rawQuery(
                    "select tid from " + HistoryDBHelper.TABLE_NAME +
                            " where tid=?", new String[]{tid});

            if (cursor.getCount() == 0) {
                db.insert(HistoryDBHelper.TABLE_NAME, null, contentValues);
            } else {
                contentValues.remove("tid");
                db.update(HistoryDBHelper.TABLE_NAME, contentValues, "tid=?", new String[]{tid});
            }
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
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
                            " where title is not null and title<>''" +
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

                //remove time info, only keep date
                if (post_time.contains("-") && post_time.contains(" "))
                    post_time = post_time.substring(0, post_time.indexOf(" "));

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
