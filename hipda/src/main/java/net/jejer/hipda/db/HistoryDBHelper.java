package net.jejer.hipda.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.jejer.hipda.ui.HiApplication;

/**
 * Created by GreenSkinMonster on 2016-07-23.
 */
public class HistoryDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "history.db";
    public static final String TABLE_NAME = "History";

    private static final String CONTENTS_TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "tid TEXT PRIMARY KEY, " +
                    "fid TEXT, " +
                    "title TEXT, " +
                    "uid TEXT, " +
                    "username TEXT, " +
                    "post_time TEXT, " +
                    "visit_time NUMERIC); ";

    private static HistoryDBHelper instance;

    HistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized HistoryDBHelper getHelper() {
        if (instance == null)
            instance = new HistoryDBHelper(HiApplication.getAppContext());

        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CONTENTS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
