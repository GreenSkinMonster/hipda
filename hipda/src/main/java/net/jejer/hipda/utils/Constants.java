package net.jejer.hipda.utils;

import net.jejer.hipda.async.SimpleListLoader;

/**
 * some constants
 * Created by GreenSkinMonster on 2015-03-13.
 */
public class Constants {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAIL = 1;
    public static final int STATUS_FAIL_ABORT = 9;

    public static final int DRAWER_SEARCH = 1000 + SimpleListLoader.TYPE_SEARCH;
    public static final int DRAWER_MYPOST = 1000 + SimpleListLoader.TYPE_MYPOST;
    public static final int DRAWER_MYREPLY = 1000 + SimpleListLoader.TYPE_MYREPLY;
    public static final int DRAWER_FAVORITES = 1000 + SimpleListLoader.TYPE_FAVORITES;
    public static final int DRAWER_SMS = 1000 + SimpleListLoader.TYPE_SMS;
    public static final int DRAWER_THREADNOTIFY = 1000 + SimpleListLoader.TYPE_THREADNOTIFY;
    public static final int DRAWER_SETTINGS = 10000;

}
