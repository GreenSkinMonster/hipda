package net.jejer.hipda.bean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Connectivity;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HiSettingsHelper {
    /*
     *
     * NOTE! PLEASE LINE-UP WITH PREFERENCE.XML
     *
     * */
    public static final String PERF_USERNAME = "PERF_USERNAME";
    public static final String PERF_PASSWORD = "PERF_PASSWORD";
    public static final String PERF_UID = "PERF_UID";
    public static final String PERF_SECQUESTION = "PERF_SECQUESTION";
    public static final String PERF_SECANSWER = "PERF_SECANSWER";
    public static final String PERF_SHOWSTICKTHREADS = "PERF_SHOWSTICKTHREADS";
    public static final String PERF_SHOW_POST_TYPE = "PERF_SHOW_POST_TYPE";
    public static final String PERF_IMAGE_LOAD_TYPE = "PERF_IMAGE_LOAD_TYPE";
    public static final String PERF_IMAGE_AUTO_LOAD_SIZE = "PERF_IMAGE_AUTO_LOAD_SIZE";
    public static final String PERF_AUTO_LOAD_THUMB = "PERF_AUTO_LOAD_THUMB";
    public static final String PERF_AVATAR_LOAD_TYPE = "PERF_AVATAR_LOAD_TYPE";
    public static final String PERF_SORTBYPOSTTIME_BY_FORUM = "PERF_SORTBYPOSTTIME_BY_FORUM";
    public static final String PERF_ADDTAIL = "PERF_ADDTAIL";
    public static final String PERF_TAILTEXT = "PERF_TAILTEXT";
    public static final String PERF_TAILURL = "PERF_TAILURL";
    public static final String PERF_THEME = "PERF_THEME";
    public static final String PERF_PRIMARY_COLOR = "PERF_PRIMARY_COLOR";
    public static final String PERF_NIGHT_THEME = "PERF_NIGHT_THEME";
    public static final String PERF_NIGHT_MODE = "PERF_NIGHT_MODE";
    public static final String PERF_NAVBAR_COLORED = "PERF_NAVBAR_COLORED";
    public static final String PERF_FONT = "PERF_FONT";
    public static final String PERF_FORUMS = "PERF_FORUMS";
    public static final String PERF_FREQ_MENUS = "PERF_FREQ_MENUS";
    public static final String PERF_ENCODEUTF8 = "PERF_ENCODEUTF8";
    public static final String PERF_BLANKLIST_USERNAMES = "PERF_BLANKLIST_USERNAMES";
    public static final String PERF_TEXTSIZE_POST_ADJ = "PERF_TEXTSIZE_POST_ADJ";
    public static final String PERF_TEXTSIZE_TITLE_ADJ = "PERF_TEXTSIZE_TITLE_ADJ";
    public static final String PERF_SCREEN_ORIENTATION = "PERF_SCREEN_ORIENTATION";
    public static final String PERF_GESTURE_BACK = "PERF_GESTURE_BACK";
    public static final String PERF_APP_BAR_COLLAPSIBLE = "PERF_APP_BAR_COLLAPSIBLE";
    public static final String PERF_FAB_LEFT_SIDE = "PERF_FAB_LEFT_SIDE";
    public static final String PERF_FAB_AUTO_HIDE = "PERF_FAB_AUTO_HIDE";
    public static final String PERF_CLICK_EFFECT = "PERF_CLICK_EFFECT";
    public static final String PERF_LAST_UPDATE_CHECK = "PERF_LAST_UPDATE_CHECK";
    public static final String PERF_AUTO_UPDATE_CHECK = "PERF_AUTO_UPDATE_CHECK";
    public static final String PERF_ABOUT = "PERF_ABOUT";
    public static final String PERF_MAX_POSTS_IN_PAGE = "PERF_MAX_POSTS_IN_PAGE";
    public static final String PERF_POST_LINE_SPACING = "PERF_POST_LINE_SPACING";
    public static final String PERF_LAST_FORUM_ID = "PERF_LAST_FORUM_ID";
    public static final String PERF_ERROR_REPORT_MODE = "PERF_ERROR_REPORT_MODE";
    public static final String PERF_INSTALLED_VERSION = "PERF_INSTALLED_VERSION";
    public static final String PERF_CLEAR_CACHE = "PERF_CLEAR_CACHE";
    public static final String PERF_NOTI_TASK_ENABLED = "PERF_NOTI_TASK_ENABLED";
    public static final String PERF_NOTI_REPEAT_MINUETS = "PERF_NOTI_REPEAT_MINUETS";
    public static final String PERF_NOTI_LED_LIGHT = "PERF_NOTI_LED_LIGHT";
    public static final String PERF_NOTI_SOUND = "PERF_NOTI_SOUND";
    public static final String PERF_NOTI_SILENT_MODE = "PERF_NOTI_SILENT_MODE";
    public static final String PERF_NOTI_SILENT_BEGIN = "PERF_NOTI_SILENT_BEGIN";
    public static final String PERF_NOTI_SILENT_END = "PERF_NOTI_SILENT_END";
    public static final String PERF_BS_TYPE_ID = "PERF_BS_TYPE_ID";
    public static final String PERF_ANIMATION_TYPE = "PERF_ANIMATION_TYPE";
    public static final String PERF_ICON = "PERF_ICON";
    public static final String PERF_SAVE_FOLDER = "PERF_SAVE_FOLDER";
    public static final String PERF_CIRCLE_AVATAR = "PERF_CIRCLE_AVATAR";
    public static final String PERF_LAST_TASK_TIME = "PERF_LAST_TASK_TIME";
    public static final String PERF_CACHE_SIZE_IN_MB = "PERF_CACHE_SIZE_IN_MB";
    public static final String PERF_FORUM_SERVER = "PERF_FORUM_SERVER";
    public static final String PERF_IMAGE_HOST = "PERF_IMAGE_HOST";
    public static final String PERF_IMAGE_HOST_UPDATE_TIME = "PERF_IMAGE_HOST_UPDATE_TIME";
    public static final String PERF_TRUST_ALL_CERTS = "PERF_TRUST_ALL_CERTS";
    public static final String PERF_MAX_UPLOAD_FILE_SIZE = "PERF_MAX_UPLOAD_FILE_SIZE";

    private Context mCtx;
    private SharedPreferences mSharedPref;

    private String mUsername = "";
    private String mPassword = "";
    private String mSecQuestion = "";
    private String mSecAnswer = "";
    private String mUid = "";

    private boolean mShowStickThreads = false;
    private boolean mShowPostType = false;
    private String mImageLoadType = "0";
    private long mImageAutoLoadSize = -1;
    private boolean mAutoLoadThumb = false;
    private String mAvatarLoadType = "0";
    private Set<String> mSortByPostTimeByForum;

    private boolean mAddTail = true;
    private String mTailText = "";
    private String mTailUrl = "";

    private String mTheme = "";
    private int mPrimaryColor = 0;
    private String mNightTheme = "";
    private boolean mNightMode = false;
    private boolean mNavBarColor = false;
    private String mFont = "";
    private Set<String> mForums = new HashSet<>();
    private Set<String> mFreqMenus = new HashSet<>();

    private boolean mEncodeUtf8 = false;

    private ArrayList<String> mBlanklistUsernames;

    private String mPostTextSizeAdj = "";
    private int mPostLineSpacing = 0;
    private String mTitleTextSizeAdj = "";
    private int mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;
    private boolean mGestureBack = true;
    private int mMaxPostsInPage;
    private int mLastForumId = 0;
    private boolean mErrorReportMode;

    private boolean mNotiTaskEnabled;
    private int mNotiRepeatMinutes;
    private boolean mNotiLedLight;
    private String mBSTypeId;
    private String mAnimationType;

    private String mForumServer;
    private String mImageHost;

    // --------------- THIS IS NOT IN PERF -----------
    private int mBasePostTextSize = -1;
    private int mBaseTitleTextSize = -1;
    private boolean mIsLandscape = false;

    public void setIsLandscape(boolean landscape) {
        mIsLandscape = landscape;
    }

    public boolean getIsLandscape() {
        return mIsLandscape;
    }

    private static boolean mMobileNetwork;

    public static void setMobileNetwork(boolean mobileNetwork) {
        mMobileNetwork = mobileNetwork;
    }

    public static boolean isMobileNetwork() {
        return mMobileNetwork;
    }

    public boolean isImageLoadable(long imageSize, boolean isThumb) {
        return Constants.LOAD_TYPE_ALWAYS.equals(mImageLoadType)
                || (!isMobileNetwork() && Constants.LOAD_TYPE_ONLY_WIFI.equals(mImageLoadType))
                || (imageSize > 0 && imageSize <= getImageAutoLoadSize())
                || (mAutoLoadThumb && isThumb);
    }

    public long getImageAutoLoadSize() {
        if (mImageAutoLoadSize == -1) {
            try {
                String value = getStringValue(PERF_IMAGE_AUTO_LOAD_SIZE, "0");
                if (TextUtils.isEmpty(value) || !TextUtils.isDigitsOnly(value))
                    value = "0";
                mImageAutoLoadSize = Integer.parseInt(value) * 1024;
            } catch (Exception ignored) {
                mImageAutoLoadSize = 0;
            }
        }
        return mImageAutoLoadSize;
    }

    public void resetImageAutoLoadSize() {
        mImageAutoLoadSize = -1;
    }

    public boolean isLoadAvatar() {
        return Constants.LOAD_TYPE_ALWAYS.equals(mAvatarLoadType)
                || (!isMobileNetwork() && Constants.LOAD_TYPE_ONLY_WIFI.equals(mAvatarLoadType));
    }

    public static void updateMobileNetworkStatus(Context context) {
        if (context != null && Connectivity.isConnected(context))
            setMobileNetwork(!Connectivity.isConnectedWifi(context));
    }

    private long mLastCheckSmsTime;

    public long getLastCheckSmsTime() {
        return mLastCheckSmsTime;
    }

    public void setLastCheckSmsTime(long lastCheckSmsTime) {
        mLastCheckSmsTime = lastCheckSmsTime;
    }

    public boolean isCheckSms() {
        return System.currentTimeMillis() > mLastCheckSmsTime + 30 * 1000;
    }

    // --------------- THIS IS NOT IN PERF -----------

    private HiSettingsHelper() {
        mCtx = HiApplication.getAppContext();
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        reload();
    }

    private static class SingletonHolder {
        public static final HiSettingsHelper INSTANCE = new HiSettingsHelper();
    }

    public static HiSettingsHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void reload() {
        getUsernameFromPref();
        getPasswordFromPref();
        getUidFromPref();
        getSecQuestionFromPref();
        getSecAnswerFromPref();
        isShowStickThreadsFromPref();
        getAvatarLoadTypeFromPref();
        getImageLoadTypeFromPref();
        isAutoLoadThumbFromPref();
        isSortByPostTimeByForumFromPref();
        isAddTailFromPref();
        getTailTextFromPref();
        getTailUrlFromPref();
        getThemeFromPref();
        getPrimaryColorFromPref();
        getNightThemeFromPref();
        isNightModeFromPref();
        isNavBarColoredFromPref();
        getFontFromPref();
        isEncodeUtf8FromPref();
        getBlanklistUsernamesFromPref();
        getPostTextsizeAdjFromPref();
        getTitleTextsizeAdjFromPref();
        getScreenOrietationFromPref();
        isGestureBackFromPref();
        getPostLineSpacingFromPref();
        getLastForumIdFromPerf();
        isShowPostTypeFromPref();
        isErrorReportModeFromPref();
        getForumsFromPref();
        getFreqMenusFromPref();
        isNotiLedLightFromPref();
        isNotiTaskEnabledFromPref();
        getNotiRepeatMinutesFromPref();
        getBSTypeIdFromPref();
        getAnimationTypeFromPref();
        getForumServerFromPref();
        getImageHostFromPref();

        updateMobileNetworkStatus(mCtx);
    }

    public boolean isLoginInfoValid() {
        return (!mUsername.isEmpty() && !mPassword.isEmpty());
    }

    public String getUsername() {
        return mUsername;
    }

    public String getUsernameFromPref() {
        mUsername = mSharedPref.getString(PERF_USERNAME, "");
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_USERNAME, username).apply();
    }

    public String getPassword() {
        return mPassword;
    }

    public String getPasswordFromPref() {
        mPassword = mSharedPref.getString(PERF_PASSWORD, "");
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_PASSWORD, password).apply();
    }

    public String getUid() {
        return mUid;
    }

    public String getUidFromPref() {
        mUid = mSharedPref.getString(PERF_UID, "");
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_UID, uid).apply();
    }

    public String getSecQuestion() {
        return mSecQuestion;
    }

    public String getSecQuestionFromPref() {
        mSecQuestion = mSharedPref.getString(PERF_SECQUESTION, "");
        return mSecQuestion;
    }

    public void setSecQuestion(String secQuestion) {
        mSecQuestion = secQuestion;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_SECQUESTION, secQuestion).apply();
    }

    public String getSecAnswer() {
        return mSecAnswer;
    }

    public String getSecAnswerFromPref() {
        mSecAnswer = mSharedPref.getString(PERF_SECANSWER, "");
        return mSecAnswer;
    }

    public void setSecAnswer(String secAnswer) {
        mSecAnswer = secAnswer;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_SECANSWER, secAnswer).apply();
    }

    public boolean isShowPostType() {
        return mShowPostType;
    }

    public boolean isShowPostTypeFromPref() {
        mShowPostType = mSharedPref.getBoolean(PERF_SHOW_POST_TYPE, true);
        return mShowPostType;
    }

    public void setShowPostType(boolean showPostType) {
        mShowPostType = showPostType;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_SHOW_POST_TYPE, showPostType).apply();
    }

    public boolean isShowStickThreads() {
        return mShowStickThreads;
    }

    public boolean isShowStickThreadsFromPref() {
        mShowStickThreads = mSharedPref.getBoolean(PERF_SHOWSTICKTHREADS, false);
        return mShowStickThreads;
    }

    public void setShowStickThreads(boolean showStickThreads) {
        mShowStickThreads = showStickThreads;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_SHOWSTICKTHREADS, showStickThreads).apply();
    }

    public String getAvatarLoadTypeFromPref() {
        mAvatarLoadType = mSharedPref.getString(PERF_AVATAR_LOAD_TYPE, Constants.LOAD_TYPE_ALWAYS);
        return mAvatarLoadType;
    }

    public String getAvatarLoadType() {
        return mAvatarLoadType;
    }

    public void setAvatarLoadType(String avatarLoadType) {
        this.mAvatarLoadType = avatarLoadType;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_AVATAR_LOAD_TYPE, avatarLoadType).apply();
    }

    public String getImageLoadTypeFromPref() {
        mImageLoadType = mSharedPref.getString(PERF_IMAGE_LOAD_TYPE, Constants.LOAD_TYPE_ONLY_WIFI);
        return mImageLoadType;
    }

    public String getImageLoadType() {
        return mImageLoadType;
    }

    public void setImageLoadType(String imageLoadType) {
        this.mImageLoadType = imageLoadType;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_IMAGE_LOAD_TYPE, imageLoadType).apply();
    }

    public boolean isAutoLoadThumbFromPref() {
        mAutoLoadThumb = mSharedPref.getBoolean(PERF_AUTO_LOAD_THUMB, false);
        return mAutoLoadThumb;
    }

    public boolean isAutoLoadThumb() {
        return mAutoLoadThumb;
    }

    public void setAutoLoadThumb(boolean autoLoadThumb) {
        mAutoLoadThumb = autoLoadThumb;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_AUTO_LOAD_THUMB, mAutoLoadThumb).apply();
    }

    public boolean isSortByPostTime(int fid) {
        return mSortByPostTimeByForum.contains(fid + "");
    }

    public void setSortByPostTime(int fid, boolean sortByPostTime) {
        if (sortByPostTime) {
            if (!mSortByPostTimeByForum.contains(fid + ""))
                mSortByPostTimeByForum.add(fid + "");
        } else {
            mSortByPostTimeByForum.remove(fid + "");
        }
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(PERF_SORTBYPOSTTIME_BY_FORUM).apply();
        editor.putStringSet(PERF_SORTBYPOSTTIME_BY_FORUM, mSortByPostTimeByForum).apply();
    }

    public Set<String> isSortByPostTimeByForumFromPref() {
        mSortByPostTimeByForum = mSharedPref.getStringSet(PERF_SORTBYPOSTTIME_BY_FORUM, new HashSet<String>());
        return mSortByPostTimeByForum;
    }

    public boolean isAddTail() {
        return mAddTail;
    }

    public boolean isAddTailFromPref() {
        mAddTail = mSharedPref.getBoolean(PERF_ADDTAIL, true);
        return mAddTail;
    }

    public void setAddTail(boolean addTail) {
        mAddTail = addTail;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_ADDTAIL, addTail).apply();
    }

    public String getTailText() {
        return mTailText;
    }

    public String getTailTextFromPref() {
        mTailText = mSharedPref.getString(PERF_TAILTEXT, "");
        return mTailText;
    }

    public void setTailText(String tailText) {
        mTailText = tailText;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_TAILTEXT, tailText).apply();
    }

    public String getTailUrl() {
        return mTailUrl;
    }

    public String getTailUrlFromPref() {
        mTailUrl = mSharedPref.getString(PERF_TAILURL, "");
        return mTailUrl;
    }

    public void setTailUrl(String tailUrl) {
        mTailUrl = tailUrl;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_TAILURL, tailUrl).apply();
    }

    public String getTailStr() {
        String tailStr = "";
        String tailText = getTailText().trim();
        if (!TextUtils.isEmpty(tailText)) {
            String tailUrl = getTailUrl();
            if (!TextUtils.isEmpty(tailUrl)) {
                if ((!tailUrl.startsWith("http")) && (!tailUrl.startsWith("https"))) {
                    tailUrl = "http://" + tailUrl;
                }
                tailStr = "[url=" + tailUrl + "][size=1]" + tailText + "[/size][/url]";
            } else {
                tailStr = "[size=1]" + tailText + "[/size]";
            }
        }
        return tailStr;
    }

    public String getTheme() {
        return mTheme;
    }

    public String getThemeFromPref() {
        mTheme = mSharedPref.getString(PERF_THEME, "light");
        return mTheme;
    }

    public void setTheme(String theme) {
        mTheme = theme;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_THEME, theme).apply();
    }

    public int getPrimaryColor() {
        return mPrimaryColor;
    }

    public int getPrimaryColorFromPref() {
        mPrimaryColor = mSharedPref.getInt(PERF_PRIMARY_COLOR, 0);
        return mPrimaryColor;
    }

    public void setPrimaryColor(int primaryColor) {
        mPrimaryColor = primaryColor;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(PERF_PRIMARY_COLOR, primaryColor).apply();
    }

    public String getNightTheme() {
        return mNightTheme;
    }

    public String getNightThemeFromPref() {
        mNightTheme = mSharedPref.getString(PERF_NIGHT_THEME, "");
        return mNightTheme;
    }

    public void setNightTheme(String theme) {
        mNightTheme = theme;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_NIGHT_THEME, theme).apply();
    }

    public boolean isNightMode() {
        return mNightMode;
    }

    public boolean isNightModeFromPref() {
        mNightMode = mSharedPref.getBoolean(PERF_NIGHT_MODE, false);
        return mNightMode;
    }

    @SuppressLint("CommitPrefEdits")
    public void setNightMode(boolean nightMode) {
        mNightMode = nightMode;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_NIGHT_MODE, nightMode).commit();
    }

    public String getFont() {
        return mFont;
    }

    public String getFontFromPref() {
        mFont = mSharedPref.getString(PERF_FONT, "");
        return mFont;
    }

    public void setFont(String font) {
        mFont = font;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(mFont, font).apply();
    }

    public boolean isNavBarColored() {
        return mNavBarColor;
    }

    public boolean isNavBarColoredFromPref() {
        mNavBarColor = mSharedPref.getBoolean(PERF_NAVBAR_COLORED, false);
        return mNavBarColor;
    }

    public void setNavBarColored(boolean navBarColored) {
        mNavBarColor = navBarColored;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_NAVBAR_COLORED, navBarColored).apply();
    }

    public Set<String> getForums() {
        return mForums;
    }

    public Set<String> getForumsFromPref() {
        mForums = mSharedPref.getStringSet(PERF_FORUMS, new HashSet<String>());
        return mForums;
    }

    public void setForums(Set<String> forums) {
        mForums = forums;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(PERF_FORUMS).apply();
        editor.putStringSet(PERF_FORUMS, forums).apply();
    }

    public Set<String> getFreqMenus() {
        return mFreqMenus;
    }

    public Set<String> getFreqMenusFromPref() {
        mFreqMenus = mSharedPref.getStringSet(PERF_FREQ_MENUS, new HashSet<String>());
        return mFreqMenus;
    }

    public void setFreqMenus(Set<String> menus) {
        mFreqMenus = menus;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(PERF_FREQ_MENUS).apply();
        editor.putStringSet(PERF_FREQ_MENUS, menus).apply();
    }

    public boolean isEncodeUtf8() {
        return mEncodeUtf8;
    }

    public boolean isEncodeUtf8FromPref() {
        mEncodeUtf8 = mSharedPref.getBoolean(PERF_ENCODEUTF8, false);
        return mEncodeUtf8;
    }

    public void setEncodeUtf8(boolean encodeUtf8) {
        mEncodeUtf8 = encodeUtf8;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_ENCODEUTF8, encodeUtf8).apply();
    }

    public String getEncode() {
        if (mEncodeUtf8) {
            return "UTF-8";
        } else {
            return "GBK";
        }
    }

    public boolean isErrorReportMode() {
        return mErrorReportMode;
    }

    public void setErrorReportMode(boolean errorReportMode) {
        mErrorReportMode = errorReportMode;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_ERROR_REPORT_MODE, errorReportMode).apply();
    }

    public boolean isErrorReportModeFromPref() {
        mErrorReportMode = mSharedPref.getBoolean(PERF_ERROR_REPORT_MODE, false);
        return mErrorReportMode;
    }

    public boolean isNotiTaskEnabled() {
        return mNotiTaskEnabled;
    }

    public void setNotiTaskEnabled(boolean notiTaskEnabled) {
        mNotiTaskEnabled = notiTaskEnabled;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_NOTI_TASK_ENABLED, mNotiTaskEnabled).apply();
    }

    public boolean isNotiTaskEnabledFromPref() {
        mNotiTaskEnabled = mSharedPref.getBoolean(PERF_NOTI_TASK_ENABLED, false);
        return mNotiTaskEnabled;
    }

    public boolean isNotiLedLight() {
        return mNotiLedLight;
    }

    public void setNotiLedLight(boolean notiLedLight) {
        mNotiLedLight = notiLedLight;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_NOTI_LED_LIGHT, mNotiLedLight).apply();
    }

    public boolean isNotiLedLightFromPref() {
        mNotiLedLight = mSharedPref.getBoolean(PERF_NOTI_LED_LIGHT, true);
        return mNotiLedLight;
    }

    public int getNotiRepeatMinutes() {
        return mNotiRepeatMinutes;
    }

    public void setNotiRepeatMinutes(int notiTaskEnable) {
        mNotiRepeatMinutes = notiTaskEnable;

        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_NOTI_REPEAT_MINUETS, mNotiRepeatMinutes + "").apply();
    }

    public int getNotiRepeatMinutesFromPref() {
        try {
            mNotiRepeatMinutes = Integer.parseInt(mSharedPref.getString(PERF_NOTI_REPEAT_MINUETS, NotificationMgr.MIN_REPEAT_MINUTTES + ""));
        } catch (Exception ignored) {
        }
        if (mNotiRepeatMinutes < NotificationMgr.MIN_REPEAT_MINUTTES) {
            mNotiRepeatMinutes = NotificationMgr.MIN_REPEAT_MINUTTES;
            setNotiRepeatMinutes(mNotiRepeatMinutes);
        }
        return mNotiRepeatMinutes;
    }

    public ArrayList<String> getBlanklistUsernames() {
        if (mBlanklistUsernames != null)
            mBlanklistUsernames = new ArrayList<>();
        return mBlanklistUsernames;
    }

    public List<String> getBlanklistUsernamesFromPref() {
        String[] usernames = mSharedPref.getString(PERF_BLANKLIST_USERNAMES, "").split("\n");
        mBlanklistUsernames = new ArrayList<>();
        for (String username : usernames) {
            if (!TextUtils.isEmpty(username) && !mBlanklistUsernames.contains(username))
                mBlanklistUsernames.add(username);
        }
        return mBlanklistUsernames;
    }

    public void setBlanklistUsernames(ArrayList<String> blanklistUsernames) {
        mBlanklistUsernames = blanklistUsernames;
        StringBuilder sb = new StringBuilder();
        for (String username : blanklistUsernames) {
            username = Utils.nullToText(username);
            if (username.length() > 0) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(username);
            }
        }
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_BLANKLIST_USERNAMES, sb.toString()).apply();
    }

    public boolean isUserBlack(String username) {
        return mBlanklistUsernames.contains(username);
    }

    public void addToBlacklist(String username) {
        if (!TextUtils.isEmpty(username) && !mBlanklistUsernames.contains(username))
            mBlanklistUsernames.add(username);
        setBlanklistUsernames(mBlanklistUsernames);
    }

    public int getPostTextsizeAdj() {
        return Integer.parseInt(mPostTextSizeAdj);
    }

    public String getPostTextsizeAdjFromPref() {
        mPostTextSizeAdj = mSharedPref.getString(PERF_TEXTSIZE_POST_ADJ, "0");
        return mPostTextSizeAdj;
    }

    public void setPostTextsizeAdj(String adj) {
        mPostTextSizeAdj = adj;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_TEXTSIZE_POST_ADJ, adj).apply();
    }


    public void setPostLineSpacing(int lineSpacing) {
        mPostLineSpacing = lineSpacing;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_POST_LINE_SPACING, lineSpacing + "").apply();
    }

    public int getPostLineSpacing() {
        return mPostLineSpacing;
    }

    public int getPostLineSpacingFromPref() {
        String value = mSharedPref.getString(PERF_POST_LINE_SPACING, "0");
        if (TextUtils.isDigitsOnly(value)) {
            mPostLineSpacing = Integer.parseInt(value);
        }
        return mPostLineSpacing;
    }

    public int getTitleTextsizeAdj() {
        return Integer.parseInt(mTitleTextSizeAdj);
    }

    public String getTitleTextsizeAdjFromPref() {
        mTitleTextSizeAdj = mSharedPref.getString(PERF_TEXTSIZE_TITLE_ADJ, "0");
        return mTitleTextSizeAdj;
    }

    public void setTitleTextsizeAdj(String adj) {
        mPostTextSizeAdj = adj;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_TEXTSIZE_TITLE_ADJ, adj).apply();
    }

    public int getScreenOrietation() {
        return mScreenOrientation;
    }

    public int getScreenOrietationFromPref() {
        try {
            mScreenOrientation = Integer.parseInt(mSharedPref.getString(PERF_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_USER + ""));
        } catch (Exception e) {
            mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;
        }
        return mScreenOrientation;
    }

    public void setScreenOrietation(int screenOrientation) {
        mScreenOrientation = screenOrientation;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_SCREEN_ORIENTATION, mScreenOrientation + "").apply();
    }

    public boolean isGestureBack() {
        return mGestureBack;
    }

    public boolean isGestureBackFromPref() {
        mGestureBack = mSharedPref.getBoolean(PERF_GESTURE_BACK, true);
        return mGestureBack;
    }

    public void setGestureBack(boolean gestureBack) {
        mGestureBack = gestureBack;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_GESTURE_BACK, gestureBack).apply();
    }

    public Date getLastUpdateCheckTime() {
        String millis = mSharedPref.getString(PERF_LAST_UPDATE_CHECK, "");
        if (!TextUtils.isEmpty(millis) && TextUtils.isDigitsOnly(millis)) {
            try {
                return new Date(Long.parseLong(millis));
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public void setLastUpdateCheckTime(Date d) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(HiSettingsHelper.PERF_LAST_UPDATE_CHECK, d.getTime() + "").apply();
    }

    public void setAutoUpdateCheck(boolean b) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(HiSettingsHelper.PERF_AUTO_UPDATE_CHECK, b).apply();
    }

    public boolean isAutoUpdateCheck() {
        return mSharedPref.getBoolean(PERF_AUTO_UPDATE_CHECK, true);
    }

    public int getMaxPostsInPage() {
        if (mMaxPostsInPage <= 0) {
            mMaxPostsInPage = mSharedPref.getInt(PERF_MAX_POSTS_IN_PAGE, 50);
        }
        return mMaxPostsInPage;
    }

    public void setMaxPostsInPage(int maxPostsInPage) {
        //could be 5,10,15 default is 50
        if (maxPostsInPage > 0 && maxPostsInPage % 5 == 0 && maxPostsInPage != mMaxPostsInPage) {
            mMaxPostsInPage = maxPostsInPage;
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt(HiSettingsHelper.PERF_MAX_POSTS_IN_PAGE, mMaxPostsInPage).apply();
        }
    }

    public void setLastForumId(int fid) {
        mLastForumId = fid;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(PERF_LAST_FORUM_ID, fid).apply();
    }

    public int getLastForumId() {
        return mLastForumId;
    }

    public int getLastForumIdFromPerf() {
        mLastForumId = mSharedPref.getInt(PERF_LAST_FORUM_ID, 0);
        return mLastForumId;
    }

    public String getBSTypeId() {
        return mBSTypeId;
    }

    public String getBSTypeIdFromPref() {
        mBSTypeId = mSharedPref.getString(PERF_BS_TYPE_ID, "");
        return mBSTypeId;
    }

    public void setBSTypeId(String typeId) {
        mBSTypeId = typeId;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_BS_TYPE_ID, typeId).apply();
    }

    public boolean isAutoUpdateCheckable() {
        if (!isAutoUpdateCheck() || Utils.isFromGooglePlay(mCtx))
            return false;
        Date lastCheck = HiSettingsHelper.getInstance().getLastUpdateCheckTime();
        //check update if last check is older than 12 hours
        return lastCheck == null
                || System.currentTimeMillis() > lastCheck.getTime() + 12 * 60 * 60 * 1000;
    }

    public void setInstalledVersion(String version) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(HiSettingsHelper.PERF_INSTALLED_VERSION, version).apply();
    }

    public String getInstalledVersion() {
        return mSharedPref.getString(PERF_INSTALLED_VERSION, "");
    }

    public int getPostTextSize() {
        if (mBasePostTextSize <= 0)
            mBasePostTextSize = mCtx.getResources().getInteger(R.integer.post_text_size);
        return mBasePostTextSize + getInstance().getPostTextsizeAdj();
    }

    public int getTitleTextSize() {
        if (mBaseTitleTextSize <= 0)
            mBaseTitleTextSize = mCtx.getResources().getInteger(R.integer.title_text_size);
        return mBaseTitleTextSize + getInstance().getTitleTextsizeAdj();
    }


    public String getAnimationType() {
        return mAnimationType;
    }

    public String getAnimationTypeFromPref() {
        mAnimationType = mSharedPref.getString(PERF_ANIMATION_TYPE, "0");
        return mAnimationType;
    }

    public void setAnimationType(String animationType) {
        mAnimationType = animationType;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_ANIMATION_TYPE, animationType).apply();
    }

    public boolean isNewAnimationType() {
        return "1".equals(getAnimationType());
    }

    public String getForumServer() {
        return mForumServer;
    }

    public String getForumServerFromPref() {
        mForumServer = mSharedPref.getString(PERF_FORUM_SERVER, HiUtils.ForumServer);
        return mForumServer;
    }

    public void setForumServer(String forumServer) {
        mForumServer = forumServer;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_FORUM_SERVER, mForumServer).apply();
    }

    public String getImageHost() {
        return mImageHost;
    }

    public String getImageHostFromPref() {
        mImageHost = mSharedPref.getString(PERF_IMAGE_HOST, HiUtils.ImageHost);
        return mImageHost;
    }

    public void setImageHost(String imageHost) {
        mImageHost = imageHost;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_IMAGE_HOST, mImageHost).apply();
    }

    public String getStringValue(String key, String defaultValue) {
        return mSharedPref.getString(key, defaultValue);
    }

    public void setStringValue(String key, String value) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(key, value).apply();
    }

    public long getLongValue(String key, long defaultValue) {
        return mSharedPref.getLong(key, defaultValue);
    }

    public void setLongValue(String key, long value) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putLong(key, value).apply();
    }

    public int getIntValue(String key, int defaultValue) {
        return mSharedPref.getInt(key, defaultValue);
    }

    public void setIntValue(String key, int value) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(key, value).apply();
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        return mSharedPref.getBoolean(key, defaultValue);
    }

    public void setBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(key, value).apply();
    }

    public boolean isInSilentMode() {
        return mSharedPref.getBoolean(PERF_NOTI_SILENT_MODE, false)
                && Utils.isInTimeRange(
                getStringValue(PERF_NOTI_SILENT_BEGIN, NotificationMgr.DEFAUL_SLIENT_BEGIN),
                getStringValue(PERF_NOTI_SILENT_END, NotificationMgr.DEFAUL_SLIENT_END));
    }

    public String getActiveTheme() {
        if (isNightMode() && !TextUtils.isEmpty(getNightTheme()))
            return getNightTheme();
        else
            return getTheme();
    }

    public boolean isAppBarCollapsible() {
        return getBooleanValue(HiSettingsHelper.PERF_APP_BAR_COLLAPSIBLE, true);
    }

    public boolean isFabLeftSide() {
        return getBooleanValue(HiSettingsHelper.PERF_FAB_LEFT_SIDE, false);
    }

    public boolean isFabAutoHide() {
        return getBooleanValue(HiSettingsHelper.PERF_FAB_AUTO_HIDE, true);
    }

    public boolean isTrustAllCerts() {
        return getBooleanValue(HiSettingsHelper.PERF_TRUST_ALL_CERTS, false);
    }

    public int getMaxUploadFileSize() {
        return getIntValue(PERF_MAX_UPLOAD_FILE_SIZE, HiUtils.DEFAULT_MAX_UPLOAD_FILE_SIZE);
    }

    public void setMaxUploadFileSize(int fileSize) {
        setIntValue(PERF_MAX_UPLOAD_FILE_SIZE, fileSize);
    }

}
