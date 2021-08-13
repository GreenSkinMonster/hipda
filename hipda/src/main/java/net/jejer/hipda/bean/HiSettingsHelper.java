package net.jejer.hipda.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.jejer.hipda.R;
import net.jejer.hipda.service.NotiHelper;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Connectivity;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HiSettingsHelper {

    /* begin of encrypted user info */
    private static final String PERF_USERNAME = "PERF_USERNAME";
    private static final String PERF_PASSWORD = "PERF_PASSWORD";
    private static final String PERF_UID = "PERF_UID";
    private static final String PERF_SECQUESTION = "PERF_SECQUESTION";
    private static final String PERF_SECANSWER = "PERF_SECANSWER";
    private static final String PERF_PROFILES = "PERF_PROFILES";
    /* end of encrypted user info */

    public static final String PERF_SHOWSTICKTHREADS = "PERF_SHOWSTICKTHREADS";
    public static final String PERF_WIFI_IMAGE_POLICY = "PERF_WIFI_IMAGE_POLICY";
    public static final String PERF_MOBILE_IMAGE_POLICY = "PERF_MOBILE_IMAGE_POLICY";
    public static final String PERF_AVATAR_LOAD_TYPE = "PERF_AVATAR_LOAD_TYPE";
    public static final String PERF_SORTBYPOSTTIME_BY_FORUM = "PERF_SORTBYPOSTTIME_BY_FORUM";
    public static final String PERF_ADDTAIL = "PERF_ADDTAIL";
    public static final String PERF_TAILTEXT = "PERF_TAILTEXT";
    public static final String PERF_TAILURL = "PERF_TAILURL";
    public static final String PERF_THEME_MODE = "PERF_THEME";
    public static final String PERF_DARK_THEME = "PERF_NIGHT_THEME";
    public static final String PERF_LIGHT_THEME = "PERF_DAY_THEME";
    public static final String PERF_NAVBAR_COLORED = "PERF_NAVBAR_COLORED";
    public static final String PERF_FONT = "PERF_FONT";
    public static final String PERF_FORUMS = "PERF_FORUMS2";
    public static final String PERF_FREQ_MENUS = "PERF_FREQ_MENUS";
    public static final String PERF_ENCODEUTF8 = "PERF_ENCODEUTF8";
    public static final String PERF_OLD_BLACKLIST = "PERF_BLANKLIST_USERNAMES";
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
    public static final String PERF_SUPPORT = "PERF_SUPPORT";
    public static final String PERF_MAX_POSTS_IN_PAGE = "PERF_MAX_POSTS_IN_PAGE";
    public static final String PERF_POST_LINE_SPACING = "PERF_POST_LINE_SPACING";
    public static final String PERF_LAST_FORUM_ID = "PERF_LAST_FORUM_ID";
    public static final String PERF_ERROR_REPORT_MODE = "PERF_ERROR_REPORT_MODE";
    public static final String PERF_INSTALLED_VERSION = "PERF_INSTALLED_VERSION";
    public static final String PERF_CLEAR_CACHE = "PERF_CLEAR_CACHE";
    public static final String PERF_CLEAR_IMAGE_CACHE = "PERF_CLEAR_IMAGE_CACHE";
    public static final String PERF_NOTI_TASK_ENABLED = "PERF_NOTI_TASK_ENABLED";
    public static final String PERF_NOTI_JOB_LAST_TIME = "PERF_NOTI_JOB_LAST_TIME";
    public static final String PERF_NOTI_SOUND = "PERF_NOTI_SOUND";
    public static final String PERF_NOTI_SILENT_MODE = "PERF_NOTI_SILENT_MODE";
    public static final String PERF_NOTI_SILENT_BEGIN = "PERF_NOTI_SILENT_BEGIN";
    public static final String PERF_NOTI_SILENT_END = "PERF_NOTI_SILENT_END";
    public static final String PERF_BS_TYPE_ID = "PERF_BS_TYPE_ID";
    public static final String PERF_CIRCLE_AVATAR = "PERF_CIRCLE_AVATAR";
    public static final String PERF_LAST_TASK_TIME = "PERF_LAST_TASK_TIME";
    public static final String PERF_CACHE_SIZE_IN_MB = "PERF_CACHE_SIZE_IN_MB";
    public static final String PERF_FORUM_SERVER = "PERF_FORUM_SERVER";
    public static final String PERF_IMAGE_HOST = "PERF_IMAGE_HOST";
    public static final String PERF_IMAGE_HOST_UPDATE_TIME = "PERF_IMAGE_HOST_UPDATE_TIME";
    public static final String PERF_MAX_UPLOAD_FILE_SIZE = "PERF_MAX_UPLOAD_FILE_SIZE";
    public static final String PERF_SHOW_TAIL = "PERF_SHOW_TAIL";
    public static final String PERF_CAMERA_PERM_ASKED = "PERF_CAMERA_PERM_ASKED";
    public static final String PERF_BLACKLIST = "PERF_BLACKLIST";
    public static final String PERF_BLACKLIST_SYNC_TIME = "PERF_BLACKLIST_SYNC_TIME";

    public static final String THEME_MODE_AUTO = "auto";
    public static final String THEME_MODE_LIGHT = "light";
    public static final String THEME_MODE_DARK = "dark";
    public static final String THEME_BLACK = "black";
    public static final String THEME_WHITE = "white";
    public static final int MAX_TAIL_TEXT_LENGTH = 12;

    public static final int SMALL_IMAGE_SIZE = 500 * 1024; //500K
    public static final String IMAGE_POLICY_NONE = "none";
    public static final String IMAGE_POLICY_SMALL = "small_images";
    public static final String IMAGE_POLICY_THUMB = "prefer_thumb";
    public static final String IMAGE_POLICY_ORIGINAL = "prefer_original";

    private Context mCtx;
    private SharedPreferences mSharedPref;
    private SharedPreferences mEncrytedSharedPref;

    private String mUsername = "";
    private String mPassword = "";
    private String mSecQuestion = "";
    private String mSecAnswer = "";
    private String mUid = "";

    private boolean mShowStickThreads = false;
    private String mAvatarLoadType = "0";
    private Set<String> mSortByPostTimeByForum;

    private boolean mAddTail = true;
    private String mTailText = "";
    private String mTailUrl = "";

    private String mTheme = "";
    private String mDarkTheme = "";
    private String mLightTheme = "";
    private boolean mNavBarColor = false;
    private String mFont = "";
    private List<Integer> mForums = new ArrayList<>();
    private Set<String> mFreqMenus = new HashSet<>();

    private boolean mEncodeUtf8 = false;

    private List<String> mOldBlacklists;
    private List<String> mBlacklists;

    private int mPostTextSizeAdj = 0;
    private int mPostLineSpacing = 0;
    private int mTitleTextSizeAdj = 0;
    private int mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;
    private boolean mGestureBack = true;
    private int mMaxPostsInPage;
    private int mLastForumId = 0;
    private boolean mErrorReportMode;

    private boolean mNotiTaskEnabled;
    private String mBSTypeId;

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
        String policy = getCurrectImagePolicy();
        return IMAGE_POLICY_ORIGINAL.equals(policy)
                || (IMAGE_POLICY_THUMB.equals(policy))
                || (IMAGE_POLICY_SMALL.equals(policy) && (isThumb || (imageSize > 0 && imageSize <= SMALL_IMAGE_SIZE)));
    }

    public String getCurrectImagePolicy() {
        if (mMobileNetwork) {
            return getStringValue(PERF_MOBILE_IMAGE_POLICY, IMAGE_POLICY_NONE);
        } else {
            return getStringValue(PERF_WIFI_IMAGE_POLICY, IMAGE_POLICY_THUMB);
        }
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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                mEncrytedSharedPref = EncryptedSharedPreferences.create(
                        "encrypted_shared_prefs",
                        masterKey,
                        mCtx,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } else {
                mEncrytedSharedPref = mSharedPref;
            }
        } catch (NoClassDefFoundError | Exception e) {
            Logger.e(e.getClass().getName() + " : " + e.getMessage() + ", use default shared preference ");
            mEncrytedSharedPref = mSharedPref;
        }
        reload();
    }

    public final static Theme[] DARK_THEMES = {
            new Theme("dark", R.style.ThemeDark, R.color.theme_dark, R.color.white),
            new Theme("black", R.style.ThemeBlack, R.color.theme_black, R.color.white)
    };

    public final static Theme[] LIGHT_THEMES = {
            new Theme("white", R.style.ThemeLight_White, R.color.md_grey_200, R.color.black),
            new Theme("red", R.style.ThemeLight_Red, R.color.md_red_700, R.color.white),
            new Theme("pink", R.style.ThemeLight_Pink, R.color.md_pink_700, R.color.white),
            new Theme("purple", R.style.ThemeLight_Purple, R.color.md_purple_700, R.color.white),
            new Theme("deep_purple", R.style.ThemeLight_DeepPurple, R.color.md_deep_purple_700, R.color.white),
            new Theme("indigo", R.style.ThemeLight_Orange, R.color.md_indigo_700, R.color.white),
            new Theme("blue", R.style.ThemeLight_Blue, R.color.md_blue_700, R.color.white),
            new Theme("light_blue", R.style.ThemeLight_LightBlue, R.color.md_light_blue_700, R.color.white),
            new Theme("cyan", R.style.ThemeLight_Cyan, R.color.md_cyan_700, R.color.white),
            new Theme("teal", R.style.ThemeLight_Teal, R.color.md_teal_700, R.color.white),
            new Theme("green", R.style.ThemeLight_Green, R.color.md_green_700, R.color.white),
            new Theme("light_green", R.style.ThemeLight_LightGreen, R.color.md_light_green_700, R.color.white),
            new Theme("lime", R.style.ThemeLight_Lime, R.color.md_lime_700, R.color.white),
            new Theme("yellow", R.style.ThemeLight_Yellow, R.color.md_yellow_700, R.color.white),
            new Theme("amber", R.style.ThemeLight_Amber, R.color.md_amber_700, R.color.white),
            new Theme("orange", R.style.ThemeLight_Orange, R.color.md_orange_700, R.color.white),
            new Theme("deep_orange", R.style.ThemeLight_DeepOrange, R.color.md_deep_orange_700, R.color.white),
            new Theme("brown", R.style.ThemeLight_Brown, R.color.md_brown_700, R.color.white),
            new Theme("grey", R.style.ThemeLight_Grey, R.color.md_grey_700, R.color.white),
            new Theme("blue_grey", R.style.ThemeLight_BlueGrey, R.color.md_blue_grey_700, R.color.white),
            new Theme("black", R.style.ThemeLight_Black, R.color.md_black_1000, R.color.white),
    };

    public SharedPreferences getSharedPref() {
        return mSharedPref;
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
        isSortByPostTimeByForumFromPref();
        isAddTailFromPref();
        getTailTextFromPref();
        getTailUrlFromPref();
        getThemeFromPref();
        getDarkThemeFromPref();
        getLightThemeFromPref();
        isNavBarColoredFromPref();
        getFontFromPref();
        isEncodeUtf8FromPref();
        getPostTextSizeAdjFromPref();
        getTitleTextSizeAdjFromPref();
        getScreenOrietationFromPref();
        isGestureBackFromPref();
        getPostLineSpacingFromPref();
        getLastForumIdFromPerf();
        isErrorReportModeFromPref();
        getForumsFromPref();
        getFreqMenusFromPref();
        isNotiTaskEnabledFromPref();
        getBSTypeIdFromPref();
        getForumServerFromPref();
        getImageHostFromPref();
        getOldBlacklistsFromPref();
        getBlacklistsFromPref();

        updateMobileNetworkStatus(mCtx);
    }

    public boolean isLoginInfoValid() {
        return (!mUsername.isEmpty() && !mPassword.isEmpty());
    }

    public String getUsername() {
        return mUsername;
    }

    private String getUsernameFromPref() {
        mUsername = mEncrytedSharedPref.getString(PERF_USERNAME, "");
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
        SharedPreferences.Editor editor = mEncrytedSharedPref.edit();
        editor.putString(PERF_USERNAME, username).apply();
    }

    public String getPassword() {
        return mPassword;
    }

    private String getPasswordFromPref() {
        mPassword = mEncrytedSharedPref.getString(PERF_PASSWORD, "");
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
        SharedPreferences.Editor editor = mEncrytedSharedPref.edit();
        editor.putString(PERF_PASSWORD, password).apply();
    }

    public String getUid() {
        return mUid;
    }

    private String getUidFromPref() {
        mUid = mEncrytedSharedPref.getString(PERF_UID, "");
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
        SharedPreferences.Editor editor = mEncrytedSharedPref.edit();
        editor.putString(PERF_UID, uid).apply();
    }

    public String getSecQuestion() {
        return mSecQuestion;
    }

    private String getSecQuestionFromPref() {
        mSecQuestion = mEncrytedSharedPref.getString(PERF_SECQUESTION, "");
        return mSecQuestion;
    }

    public void setSecQuestion(String secQuestion) {
        mSecQuestion = secQuestion;
        SharedPreferences.Editor editor = mEncrytedSharedPref.edit();
        editor.putString(PERF_SECQUESTION, secQuestion).apply();
    }

    public String getSecAnswer() {
        return mSecAnswer;
    }

    private String getSecAnswerFromPref() {
        mSecAnswer = mEncrytedSharedPref.getString(PERF_SECANSWER, "");
        return mSecAnswer;
    }

    public void setSecAnswer(String secAnswer) {
        mSecAnswer = secAnswer;
        SharedPreferences.Editor editor = mEncrytedSharedPref.edit();
        editor.putString(PERF_SECANSWER, secAnswer).apply();
    }

    public Map<String, Profile> getProfiles() {
        String profilesValue = mEncrytedSharedPref.getString(PERF_PROFILES, "{}");
        Gson gson = new Gson();
        Type profilesType = new TypeToken<Map<String, Profile>>() {
        }.getType();
        Map<String, Profile> profiles;
        try {
            profiles = gson.fromJson(profilesValue, profilesType);
        } catch (Exception e) {
            profiles = new HashMap<>();
            Logger.e(e);
        }
        return profiles;
    }

    public Profile getProfile(String username) {
        return getProfiles().get(username.toUpperCase());
    }

    public void saveCurrentProfile() {
        if (TextUtils.isEmpty(getUsername()))
            return;
        Profile profile = new Profile(getUsername(),
                getPassword(),
                getUid(),
                getSecQuestion(),
                getSecAnswer());

        Gson gson = new Gson();
        Type profilesType = new TypeToken<Map<String, Profile>>() {
        }.getType();
        Map<String, Profile> profiles = getProfiles();
        profiles.put(profile.getUsername().toUpperCase(), profile);
        SharedPreferences.Editor editor = mEncrytedSharedPref.edit();
        String v = gson.toJson(profiles, profilesType);
        editor.putString(PERF_PROFILES, v).apply();
    }

    public void removeProfile(String username) {
        Map<String, Profile> profiles = getProfiles();
        profiles.remove(username.toUpperCase());

        Gson gson = new Gson();
        Type profilesType = new TypeToken<Map<String, Profile>>() {
        }.getType();
        SharedPreferences.Editor editor = mEncrytedSharedPref.edit();
        String v = gson.toJson(profiles, profilesType);
        editor.putString(PERF_PROFILES, v).apply();
    }

    public void migrateEncrytSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mEncrytedSharedPref instanceof EncryptedSharedPreferences
                    && !TextUtils.isEmpty(mSharedPref.getString(PERF_USERNAME, ""))) {
                setUsername(mSharedPref.getString(PERF_USERNAME, ""));
                setPassword(mSharedPref.getString(PERF_PASSWORD, ""));
                setUid(mSharedPref.getString(PERF_UID, ""));
                setSecQuestion(mSharedPref.getString(PERF_SECQUESTION, ""));
                setSecAnswer(mSharedPref.getString(PERF_SECANSWER, ""));

                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.remove(PERF_USERNAME);
                editor.remove(PERF_PASSWORD);
                editor.remove(PERF_UID);
                editor.remove(PERF_SECQUESTION);
                editor.remove(PERF_SECANSWER);
                editor.apply();
            }
        }
    }

    public boolean isShowStickThreads() {
        return mShowStickThreads;
    }

    private boolean isShowStickThreadsFromPref() {
        mShowStickThreads = mSharedPref.getBoolean(PERF_SHOWSTICKTHREADS, false);
        return mShowStickThreads;
    }

    public void setShowStickThreads(boolean showStickThreads) {
        mShowStickThreads = showStickThreads;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_SHOWSTICKTHREADS, showStickThreads).apply();
    }

    private String getAvatarLoadTypeFromPref() {
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

    private Set<String> isSortByPostTimeByForumFromPref() {
        mSortByPostTimeByForum = mSharedPref.getStringSet(PERF_SORTBYPOSTTIME_BY_FORUM, new HashSet<String>());
        return mSortByPostTimeByForum;
    }

    public boolean isAddTail() {
        return mAddTail && Utils.getWordCount(mTailText) <= MAX_TAIL_TEXT_LENGTH;
    }

    public boolean isAddTailFromPref() {
        mAddTail = mSharedPref.getBoolean(PERF_ADDTAIL, false);
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

    private String getTailTextFromPref() {
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

    private String getTailUrlFromPref() {
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
                    tailUrl = "https://" + tailUrl;
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

    private String getThemeFromPref() {
        mTheme = mSharedPref.getString(PERF_THEME_MODE, THEME_MODE_LIGHT);
        return mTheme;
    }

    public void setTheme(String theme) {
        mTheme = theme;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_THEME_MODE, theme).apply();
    }

    public String getDarkTheme() {
        return mDarkTheme;
    }

    private String getDarkThemeFromPref() {
        mDarkTheme = mSharedPref.getString(PERF_DARK_THEME, THEME_MODE_DARK);
        return mDarkTheme;
    }

    public void setDarkTheme(String theme) {
        mDarkTheme = theme;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_DARK_THEME, theme).apply();
    }

    public String getLightTheme() {
        return mLightTheme;
    }

    private String getLightThemeFromPref() {
        mLightTheme = mSharedPref.getString(PERF_LIGHT_THEME, THEME_WHITE);
        return mLightTheme;
    }

    public void setLightTheme(String theme) {
        mLightTheme = theme;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_LIGHT_THEME, theme).apply();
    }

    public String getFont() {
        return mFont;
    }

    private String getFontFromPref() {
        mFont = mSharedPref.getString(PERF_FONT, "");
        return mFont;
    }

    public void setFont(String font) {
        mFont = font;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_FONT, font).apply();
    }

    public boolean isNavBarColored() {
        return mNavBarColor;
    }

    private boolean isNavBarColoredFromPref() {
        mNavBarColor = mSharedPref.getBoolean(PERF_NAVBAR_COLORED, true);
        return mNavBarColor;
    }

    public void setNavBarColored(boolean navBarColored) {
        mNavBarColor = navBarColored;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_NAVBAR_COLORED, navBarColored).apply();
    }

    public List<Integer> getForums() {
        return mForums;
    }

    private List<Integer> getForumsFromPref() {
        List<Integer> forums = new ArrayList<>();
        String fidsAsString = mSharedPref.getString(PERF_FORUMS, "");
        String[] fids = fidsAsString.split(",");
        for (String fid : fids) {
            if (HiUtils.isValidId(fid) && HiUtils.getForumByFid(Integer.valueOf(fid)) != null)
                forums.add(Integer.valueOf(fid));
        }
        if (forums.size() == 0) {
            for (int fid : HiUtils.DEFAULT_FORUMS) {
                forums.add(fid);
            }
        }
        mForums = forums;
        return mForums;
    }

    public void setForums(List<Integer> forums) {
        if (forums.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int fid : forums) {
                sb.append(fid).append(",");
            }
            mForums = forums;
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.remove(PERF_FORUMS).apply();
            editor.putString(PERF_FORUMS, sb.toString()).apply();
        }
    }

    public Set<String> getFreqMenus() {
        return mFreqMenus;
    }

    private Set<String> getFreqMenusFromPref() {
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

    private boolean isEncodeUtf8FromPref() {
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

    private boolean isErrorReportModeFromPref() {
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

    private boolean isNotiTaskEnabledFromPref() {
        mNotiTaskEnabled = mSharedPref.getBoolean(PERF_NOTI_TASK_ENABLED, false);
        return mNotiTaskEnabled;
    }

    public List<String> getOldBlacklists() {
        return mOldBlacklists;
    }

    public void setOldBlacklists(List<String> blacklists) {
        mOldBlacklists = blacklists;
        StringBuilder sb = new StringBuilder();
        for (String username : blacklists) {
            if (!TextUtils.isEmpty(username)) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(username);
            }
        }
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_OLD_BLACKLIST, sb.toString()).apply();
    }

    private List<String> getOldBlacklistsFromPref() {
        String[] usernames = mSharedPref.getString(PERF_OLD_BLACKLIST, "").split("\n");
        mOldBlacklists = new ArrayList<>();
        for (String username : usernames) {
            if (!TextUtils.isEmpty(username) && !mOldBlacklists.contains(username))
                mOldBlacklists.add(username);
        }
        return mOldBlacklists;
    }

    public List<String> getBlacklists() {
        if (mBlacklists == null)
            mBlacklists = new ArrayList<>();
        return mBlacklists;
    }

    private List<String> getBlacklistsFromPref() {
        String[] usernames = mSharedPref.getString(PERF_BLACKLIST, "").split("\n");
        mBlacklists = new ArrayList<>();
        for (String username : usernames) {
            if (!TextUtils.isEmpty(username) && !mBlacklists.contains(username))
                mBlacklists.add(username);
        }
        return mBlacklists;
    }

    public void setBlacklists(List<String> blacklists) {
        mBlacklists = blacklists;
        StringBuilder sb = new StringBuilder();
        for (String username : blacklists) {
            if (!TextUtils.isEmpty(username)) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(username);
            }
        }
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_BLACKLIST, sb.toString()).apply();
    }

    public boolean isInBlacklist(String username) {
        return mBlacklists.contains(username) || mOldBlacklists.contains(username);
    }

    public void addToBlacklist(String username) {
        if (!TextUtils.isEmpty(username) && !mBlacklists.contains(username)) {
            mBlacklists.add(username);
            setBlacklists(mBlacklists);
        }
    }

    public void removeFromBlacklist(String username) {
        if (!TextUtils.isEmpty(username)) {
            mBlacklists.remove(username);
            setBlacklists(mBlacklists);
        }
    }

    public int getPostTextSizeAdj() {
        return mPostTextSizeAdj;
    }

    private int getPostTextSizeAdjFromPref() {
        mPostTextSizeAdj = Utils.parseInt(mSharedPref.getString(PERF_TEXTSIZE_POST_ADJ, "0"));
        return mPostTextSizeAdj;
    }

    public void setPostTextSizeAdj(int adj) {
        mPostTextSizeAdj = adj;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_TEXTSIZE_POST_ADJ, String.valueOf(adj)).apply();
    }


    public void setPostLineSpacing(int lineSpacing) {
        mPostLineSpacing = lineSpacing;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_POST_LINE_SPACING, lineSpacing + "").apply();
    }

    public int getPostLineSpacing() {
        return mPostLineSpacing;
    }

    private int getPostLineSpacingFromPref() {
        String value = mSharedPref.getString(PERF_POST_LINE_SPACING, "0");
        if (TextUtils.isDigitsOnly(value)) {
            mPostLineSpacing = Integer.parseInt(value);
        }
        return mPostLineSpacing;
    }

    public int getTitleTextSizeAdj() {
        return mTitleTextSizeAdj;
    }

    private int getTitleTextSizeAdjFromPref() {
        mTitleTextSizeAdj = Utils.parseInt(mSharedPref.getString(PERF_TEXTSIZE_TITLE_ADJ, "0"));
        return mTitleTextSizeAdj;
    }

    public void setTitleTextSizeAdj(int adj) {
        mTitleTextSizeAdj = adj;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_TEXTSIZE_TITLE_ADJ, String.valueOf(adj)).apply();
    }

    public int getScreenOrietation() {
        return mScreenOrientation;
    }

    private int getScreenOrietationFromPref() {
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

    private boolean isGestureBackFromPref() {
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

    private int getLastForumIdFromPerf() {
        mLastForumId = mSharedPref.getInt(PERF_LAST_FORUM_ID, 0);
        return mLastForumId;
    }

    public String getBSTypeId() {
        return mBSTypeId;
    }

    private String getBSTypeIdFromPref() {
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
        return mBasePostTextSize + getInstance().getPostTextSizeAdj();
    }

    public int getTitleTextSize() {
        if (mBaseTitleTextSize <= 0)
            mBaseTitleTextSize = mCtx.getResources().getInteger(R.integer.title_text_size);
        return mBaseTitleTextSize + getInstance().getTitleTextSizeAdj();
    }


    public String getForumServer() {
        return mForumServer;
    }

    private String getForumServerFromPref() {
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

    private String getImageHostFromPref() {
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
                && Utils.isInTimeRange(getSilentBegin(), getSilentEnd());
    }

    public boolean isAppBarCollapsible() {
        return getBooleanValue(PERF_APP_BAR_COLLAPSIBLE, true);
    }

    public boolean isFabLeftSide() {
        return getBooleanValue(PERF_FAB_LEFT_SIDE, false);
    }

    public boolean isFabAutoHide() {
        return getBooleanValue(PERF_FAB_AUTO_HIDE, true);
    }

    public boolean isClickEffect() {
        return getBooleanValue(PERF_CLICK_EFFECT, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public boolean isShowTail() {
        return getBooleanValue(PERF_SHOW_TAIL, false);
    }

    public int getMaxUploadFileSize() {
        return getIntValue(PERF_MAX_UPLOAD_FILE_SIZE, HiUtils.DEFAULT_MAX_UPLOAD_FILE_SIZE);
    }

    public void setMaxUploadFileSize(int fileSize) {
        setIntValue(PERF_MAX_UPLOAD_FILE_SIZE, fileSize);
    }

    public boolean isCircleAvatar() {
        return getBooleanValue(PERF_CIRCLE_AVATAR, true);
    }

    public boolean isCameraPermAsked() {
        return getBooleanValue(PERF_CAMERA_PERM_ASKED, false);
    }

    public void setCameraPermAsked(boolean asked) {
        setBooleanValue(PERF_CAMERA_PERM_ASKED, asked);
    }

    public String getSilentBegin() {
        return getStringValue(
                HiSettingsHelper.PERF_NOTI_SILENT_BEGIN,
                NotiHelper.DEFAUL_SLIENT_BEGIN);
    }

    public String getSilentEnd() {
        return getStringValue(
                HiSettingsHelper.PERF_NOTI_SILENT_END,
                NotiHelper.DEFAUL_SLIENT_END);
    }

    public Date getBlacklistSyncTime() {
        long millis = getLongValue(PERF_BLACKLIST_SYNC_TIME, 0);
        if (millis > 0) {
            try {
                return new Date(millis);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public void setBlacklistSyncTime() {
        setLongValue(PERF_BLACKLIST_SYNC_TIME, System.currentTimeMillis());
    }

    public Date getNotiJobLastRunTime() {
        long millis = getLongValue(PERF_NOTI_JOB_LAST_TIME, 0);
        if (millis > 0) {
            try {
                return new Date(millis);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public void setNotiJobLastRunTime() {
        setLongValue(PERF_NOTI_JOB_LAST_TIME, System.currentTimeMillis());
    }

}
