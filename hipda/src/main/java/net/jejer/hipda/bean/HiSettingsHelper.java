package net.jejer.hipda.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.jejer.hipda.R;

public class HiSettingsHelper {
    /*
     *
     * NOTE! PLEASE LINE-UP WITH PREFERENCE.XML
     *
     * */
    public static final String PERF_USERNAME = "PERF_USERNAME";
    public static final String PERF_PASSWORD = "PERF_PASSWORD";
    public static final String PERF_SECQUESTION = "PERF_SECQUESTION";
    public static final String PERF_SECANSWER = "PERF_SECANSWER";
    public static final String PERF_COOKIEAUTH = "PERF_COOKIEAUTH";
    public static final String PERF_SHOWSTICKTHREADS = "PERF_SHOWSTICKTHREADS";
    public static final String PERF_LOADIMGONMOBILENWK = "PERF_LOADIMGONMOBILENWK";
    public static final String PERF_THREADLISTAVATAR = "PERF_THREADLISTAVATAR";
    public static final String PERF_PREFETCH = "PERF_PREFETCH";
    public static final String PERF_SORTBYPOSTTIME = "PERF_SORTBYPOSTTIME";
    public static final String PERF_ADDTAIL = "PERF_ADDTAIL";
    public static final String PERF_TAILTEXT = "PERF_TAILTEXT";
    public static final String PERF_TAILURL = "PERF_TAILURL";
    public static final String PERF_NIGHTTHEME = "PERF_NIGHTTHEME";
    public static final String PERF_ENCODEUTF8 = "PERF_ENCODEUTF8";
    public static final String PERF_EINK_OPTIMIZATION = "PERF_EINK_OPTIMIZATION";
    public static final String PERF_BLANKLIST_USERNAMES = "PERF_BLANKLIST_USERNAMES";
    public static final String PERF_TEXTSIZE_POST_ADJ = "PERF_TEXTSIZE_POST_ADJ";
	public static final String PERF_SCREEN_ORIENTATION = "PERF_SCREEN_ORIENTATION";


    private Context mCtx;
    private SharedPreferences mSharedPref;

    private String mUsername = "";
    private String mPassword = "";
    private String mSecQuestion = "";
    private String mSecAnswer = "";

    private String mCookieAuth = "";

    private boolean mShowStickThreads = false;
    private boolean mLoadImgOnMobileNwk = true;
    private boolean mPreFetch = true;
    private boolean mSortByPostTime = false;
    private boolean mShowThreadListAvatar = true;

    private boolean mAddTail = true;
    private String mTailText = "";
    private String mTailUrl = "";

    private boolean mNightTheme = false;

    private boolean mEncodeUtf8 = false;
    private boolean mEinkOptimization = false;

    private String[] mBlanklistUsernames = null;

    private String mPostTextSizeAdj = "";
	private String mScreenOrientation = "";

    // --------------- THIS IS NOT IN PERF -----------
    private boolean mIsLandscape = false;

    public void setIsLandscape(boolean landscape) {
        mIsLandscape = landscape;
    }

    public boolean getIsLandscape() {
        return mIsLandscape;
    }
    // --------------- THIS IS NOT IN PERF -----------


    private HiSettingsHelper() {
    }

    private static class SingletonHolder {
        public static final HiSettingsHelper INSTANCE = new HiSettingsHelper();
    }

    public static HiSettingsHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Context ctx) {
        mCtx = ctx;
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        reload();
    }

    public void reload() {
        getUsernameFromPref();
        getPasswordFromPref();
        getSecQuestionFromPref();
        getSecAnswerFromPref();
        getCookieAuthFromPref();
        isShowStickThreadsFromPref();
        isLoadImgOnMobileNwkFromPref();
        isShowThreadListAvatarFromPref();
        isPreFetchFromPref();
        isSortByPostTimeFromPref();
        isAddTailFromPref();
        getTailTextFromPref();
        getTailUrlFromPref();
        isNightThemeFromPref();
        isEncodeUtf8FromPref();
        isEinkOptimizationFromPref();
        getBlanklistUsernamesFromPref();
        getPostTextsizeAdjFromPref();
		getScreenOrietationFromPref();
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
        editor.putString(PERF_USERNAME, username).commit();
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
        editor.putString(PERF_PASSWORD, password).commit();
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
        editor.putString(PERF_SECQUESTION, secQuestion).commit();
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
        editor.putString(PERF_SECANSWER, secAnswer).commit();
    }

    public String getCookieAuth() {
        return mCookieAuth;
    }

    public String getCookieAuthFromPref() {
        mCookieAuth = mSharedPref.getString(PERF_COOKIEAUTH, "");
        return mCookieAuth;
    }

    public void setCookieAuth(String cookieAuth) {
        mCookieAuth = cookieAuth;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_COOKIEAUTH, cookieAuth).commit();
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
        editor.putBoolean(PERF_SHOWSTICKTHREADS, showStickThreads).commit();
    }

    public boolean isShowThreadListAvatar() {
        return mShowThreadListAvatar;
    }

    public boolean isShowThreadListAvatarFromPref() {
        mShowThreadListAvatar = mSharedPref.getBoolean(PERF_THREADLISTAVATAR, true);
        return mShowThreadListAvatar;
    }

    public void setShowThreadListAvatar(boolean showThreadListAvatar) {
        mShowThreadListAvatar = showThreadListAvatar;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_THREADLISTAVATAR, showThreadListAvatar).commit();
    }

    public boolean isLoadImgOnMobileNwk() {
        return mLoadImgOnMobileNwk;
    }

    public boolean isLoadImgOnMobileNwkFromPref() {
        mLoadImgOnMobileNwk = mSharedPref.getBoolean(PERF_LOADIMGONMOBILENWK, true);
        return mLoadImgOnMobileNwk;
    }

    public void setLoadImgOnMobileNwk(boolean loadImgOnMobileNwk) {
        mLoadImgOnMobileNwk = loadImgOnMobileNwk;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_LOADIMGONMOBILENWK, loadImgOnMobileNwk).commit();
    }

    public boolean isPreFetch() {
        return mPreFetch;
    }

    public boolean isPreFetchFromPref() {
        mPreFetch = mSharedPref.getBoolean(PERF_PREFETCH, true);
        return mPreFetch;
    }

    public void setPreFetch(boolean preFetch) {
        mPreFetch = preFetch;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_PREFETCH, preFetch).commit();
    }

    public boolean isSortByPostTime() {
        return mSortByPostTime;
    }

    public boolean isSortByPostTimeFromPref() {
        mSortByPostTime = mSharedPref.getBoolean(PERF_SORTBYPOSTTIME, false);
        return mSortByPostTime;
    }

    public void setSortByPostTime(boolean sortByPostTime) {
        mSortByPostTime = sortByPostTime;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_SORTBYPOSTTIME, sortByPostTime).commit();
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
        editor.putBoolean(PERF_ADDTAIL, addTail).commit();
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
        editor.putString(PERF_TAILTEXT, tailText).commit();
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
        editor.putString(PERF_TAILURL, tailUrl).commit();
    }

    public boolean isNightTheme() {
        return mNightTheme;
    }

    public boolean isNightThemeFromPref() {
        mNightTheme = mSharedPref.getBoolean(PERF_NIGHTTHEME, false);
        return mNightTheme;
    }

    public void setNightTheme(boolean nightTheme) {
        mNightTheme = nightTheme;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_NIGHTTHEME, nightTheme).commit();
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
        editor.putBoolean(PERF_ENCODEUTF8, encodeUtf8).commit();
    }

    public String getEncode() {
        if (mEncodeUtf8) {
            return "UTF8";
        } else {
            return "GBK";
        }
    }

    public boolean isEinkOptimization() {
        return mEinkOptimization;
    }

    public boolean isEinkOptimizationFromPref() {
        mEinkOptimization = mSharedPref.getBoolean(PERF_EINK_OPTIMIZATION, false);
        return mEinkOptimization;
    }

    public void setEinkOptimization(boolean einkOptimization) {
        mEinkOptimization = einkOptimization;
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(PERF_EINK_OPTIMIZATION, einkOptimization).commit();
    }

    public String[] getBlanklistUsernames() {
        return mBlanklistUsernames;
    }

    public String[] getBlanklistUsernamesFromPref() {
        mBlanklistUsernames = mSharedPref.getString(PERF_BLANKLIST_USERNAMES, "").split(" ");
        return mBlanklistUsernames;
    }

    public void setBlanklistUsernames(String blanklistUsernames) {
        mBlanklistUsernames = blanklistUsernames.split(" ");
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(PERF_BLANKLIST_USERNAMES, blanklistUsernames).commit();
    }

    public boolean isUserBlack(String username) {
        for (String s : mBlanklistUsernames) {
            if (s.equals(username)) {
                return true;
            }
        }
        return false;
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
        editor.putString(PERF_TEXTSIZE_POST_ADJ, adj).commit();
    }

	public String getScreenOrietation() {
		return mScreenOrientation;
	}

	public String getScreenOrietationFromPref() {
		mScreenOrientation = mSharedPref.getString(PERF_SCREEN_ORIENTATION, mCtx.getResources().getString(R.string.screen_portrait));
		return mScreenOrientation;
	}

	public void setScreenOrietation(String screenOrientation) {
		mScreenOrientation = screenOrientation;
		SharedPreferences.Editor editor = mSharedPref.edit();
		editor.putString(PERF_SCREEN_ORIENTATION, mScreenOrientation + "").commit();
	}

}
