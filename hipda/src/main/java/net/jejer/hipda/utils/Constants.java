package net.jejer.hipda.utils;

import net.jejer.hipda.async.SimpleListLoader;

/**
 * some constants
 * Created by GreenSkinMonster on 2015-03-13.
 */
public class Constants {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAIL = 1;
    public static final int STATUS_IN_PROGRESS = 5;
    public static final int STATUS_FAIL_ABORT = 9;

    public static final int DRAWER_SEARCH = 1000 + SimpleListLoader.TYPE_SEARCH;
    public static final int DRAWER_MYPOST = 1000 + SimpleListLoader.TYPE_MYPOST;
    public static final int DRAWER_MYREPLY = 1000 + SimpleListLoader.TYPE_MYREPLY;
    public static final int DRAWER_FAVORITES = 1000 + SimpleListLoader.TYPE_FAVORITES;
    public static final int DRAWER_SMS = 1000 + SimpleListLoader.TYPE_SMS;
    public static final int DRAWER_THREADNOTIFY = 1000 + SimpleListLoader.TYPE_THREAD_NOTIFY;
    public static final int DRAWER_SETTINGS = 10000;
    public static final int DRAWER_NIGHT_MODE = 10001;

    public static final String FONT_ROBOTO_SLAB = "RobotoSlab";

    public static final String INTENT_NOTIFICATION = "notification";

    public static final String EXTRA_SMS_COUNT = "sms_count";
    public static final String EXTRA_THREAD_COUNT = "thread_count";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_UID = "uid";

    public static final String LOAD_TYPE_ALWAYS = "0";
    public static final String LOAD_TYPE_MAUNAL = "1";
    public static final String LOAD_TYPE_ONLY_WIFI = "2";

    public static final int MIN_CLICK_INTERVAL = 600;

    public static final String FILE_SHARE_PREFIX = "Hi_Share_Temp_";
}
