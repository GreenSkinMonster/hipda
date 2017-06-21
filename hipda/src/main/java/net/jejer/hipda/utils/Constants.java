package net.jejer.hipda.utils;

/**
 * some constants
 * Created by GreenSkinMonster on 2015-03-13.
 */
public class Constants {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_FAIL = 7;
    public static final int STATUS_FAIL_ABORT = 8;
    public static final int STATUS_FAIL_RELOGIN = 9;

    public static final int DRAWER_SEARCH = 1000;
    public static final int DRAWER_MYPOST = 1001;
    public static final int DRAWER_MYREPLY = 1002;
    public static final int DRAWER_FAVORITES = 1003;
    public static final int DRAWER_SMS = 1004;
    public static final int DRAWER_THREADNOTIFY = 1005;
    public static final int DRAWER_HISTORIES = 1006;
    public static final int DRAWER_SETTINGS = 10000;
    public static final int DRAWER_NO_ACTION = 10002;

    public static final String INTENT_NOTIFICATION = "notification";
    public static final String INTENT_SMS = "sms";
    public static final String INTENT_SEARCH = "search";
    public static final String INTENT_NEW_THREAD = "new_thread";
    public static final String INTENT_FAVORITE = "favorite";

    public static final String EXTRA_SMS_COUNT = "sms_count";
    public static final String EXTRA_THREAD_COUNT = "thread_count";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_UID = "uid";

    public static final String LOAD_TYPE_ALWAYS = "0";
    public static final String LOAD_TYPE_MAUNAL = "1";
    public static final String LOAD_TYPE_ONLY_WIFI = "2";

    public static final int MIN_CLICK_INTERVAL = 600;

    public static final String FILE_SHARE_PREFIX = "Hi_Share_";

    public static final int ICON_ORIGINAL = 0;
    public static final int ICON_ROUND = 1;
    public static final int ICON_BIGGER = 2;
}
