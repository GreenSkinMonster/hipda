package net.jejer.hipda.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.jejer.hipda.ui.HiApplication;

/**
 * Created by GreenSkinMonster on 2016-07-23.
 */
public class ContentDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "content.db";
    public static final String TABLE_NAME = "Contents";

    private static final String CONTENTS_TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "session_id TEXT PRIMARY KEY, " +
                    "time NUMERIC, " +
                    "content TEXT);";

    private static ContentDBHelper instance;

    ContentDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized ContentDBHelper getHelper() {
        if (instance == null)
            instance = new ContentDBHelper(HiApplication.getAppContext());

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
