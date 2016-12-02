package net.jejer.hipda.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;

public class HiUtils {
    public static final String UserAgent = "net.jejer.hipda " + HiSettingsHelper.getInstance().getAppVersion();
    public static final String ForumServer = "http://www.hi-pda.com";
    public static final String ForumServerSsl = "https://www.hi-pda.com";
    public static final String ImageHost = "http://img.hi-pda.com";

    public static String BaseUrl;
    public static String ThreadListUrl;
    public static String DetailListUrl;
    public static String ReplyUrl;
    public static String EditUrl;
    public static String NewThreadUrl;
    public static String MyReplyUrl;
    public static String MyPostUrl;
    public static String LastPageUrl;
    public static String RedirectToPostUrl;
    public static String GotoPostUrl;
    public static String SMSUrl;
    public static String SMSDetailUrl;
    public static String SMSPreparePostUrl;
    public static String SMSPostUrl;
    public static String ThreadNotifyUrl;
    public static String CheckSMS;
    public static String UploadImgUrl;
    public static String SearchTitle;
    public static String SearchFullText;
    public static String SearchUserThreads;
    public static String FavoritesUrl;
    public static String FavoriteAddUrl;
    public static String FavoriteRemoveUrl;
    public static String UserInfoUrl;

    public static String LoginStep3;
    public static String LoginStep2;

    public static String ImageBaseUrl;
    public static String AvatarBaseUrl;
    public static String SmiliesBaseUrl;
    public static boolean ImageHostUpdated = false;

    public static final String CookieDomain = "hi-pda.com";
    public static final String SmiliesPattern = CookieDomain + "/forum/images/smilies/";
    public static final String ForumImagePattern = CookieDomain + "/forum/images/";

    private static String AVATAR_BASE = "000000000";
    public static int MAX_THREADS_IN_PAGE = 50;

    public static int FID_BS = 6;
    public static int FID_DISCOVERY = 2;
    public static String[] FORUMS = {"Discovery", "Buy & Sell", "Geek Talks", "E-INK", "疑似机器人"};
    public static int[] FORUM_IDS = {FID_DISCOVERY, FID_BS, 7, 59, 57};

    public static int getForumID(int idx) {
        return FORUM_IDS[idx];
    }

    public static int getForumIndexByFid(String fid) {
        for (int i = 0; i < FORUM_IDS.length; i++) {
            if (fid.equals(FORUM_IDS[i] + "")) {
                return i;
            }
        }
        return -1;
    }

    public static String getFullUrl(String particalUrl) {
        return BaseUrl + particalUrl;
    }

    public static Boolean isAutoLoadImg(Context ctx) {
        if (HiSettingsHelper.getInstance().isLoadImgOnMobileNwk()) {
            return true;
        } else {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null || !info.isConnected() || info.getType() != ConnectivityManager.TYPE_WIFI) {
                //Mobile Network
                return false;
            } else {
                return true;
            }
        }
    }

    public static String getAvatarUrlByUid(String uid) {
        if (TextUtils.isEmpty(uid)
                || uid.length() > AVATAR_BASE.length()
                || !TextUtils.isDigitsOnly(uid))
            return "";

        String fullUid = AVATAR_BASE.substring(0, AVATAR_BASE.length() - uid.length()) + uid;
        String url = AvatarBaseUrl
                + fullUid.substring(0, 3) + "/"
                + fullUid.substring(3, 5) + "/"
                + fullUid.substring(5, 7) + "/"
                + fullUid.substring(7, 9) + "_avatar_middle.jpg";
        return url;
    }

    public static void updateImageHost(String host) {
        ImageBaseUrl = "http://" + host + "/forum/";
        AvatarBaseUrl = ImageBaseUrl + "uc_server/data/avatar/";
        SmiliesBaseUrl = ImageBaseUrl + "images/smilies/";
    }

    public static void updateBaseUrls() {
        String imageHost = HiSettingsHelper.getInstance().getImageHost();
        String forumServer = HiSettingsHelper.getInstance().getForumServer();

        BaseUrl = forumServer + "/forum/";
        ImageBaseUrl = imageHost + "/forum/";
        AvatarBaseUrl = ImageBaseUrl + "uc_server/data/avatar/";
        SmiliesBaseUrl = ImageBaseUrl + "images/smilies/";

        ThreadListUrl = BaseUrl + "forumdisplay.php?fid=";
        DetailListUrl = BaseUrl + "viewthread.php?tid=";
        ReplyUrl = BaseUrl + "post.php?action=reply&tid=";
        EditUrl = BaseUrl + "post.php?action=edit";
        NewThreadUrl = BaseUrl + "post.php?action=newthread&fid=";
        MyReplyUrl = BaseUrl + "my.php?item=posts";
        MyPostUrl = BaseUrl + "my.php?item=threads";
        LastPageUrl = BaseUrl + "/redirect.php?goto=lastpost&from=fastpost&tid=";
        RedirectToPostUrl = BaseUrl + "/redirect.php?goto=findpost&pid={pid}&ptid={tid}";
        GotoPostUrl = BaseUrl + "/gotopost.php?pid={pid}";
        SMSUrl = BaseUrl + "pm.php?filter=privatepm";
        SMSDetailUrl = BaseUrl + "pm.php?daterange=5&uid=";
        SMSPreparePostUrl = BaseUrl + "pm.php?daterange=1&uid=";
        SMSPostUrl = BaseUrl + "pm.php?action=send&pmsubmit=yes&infloat=yes&inajax=1&uid=";
        ThreadNotifyUrl = BaseUrl + "notice.php?filter=threads";
        CheckSMS = BaseUrl + "pm.php?checknewpm";
        UploadImgUrl = BaseUrl + "misc.php?action=swfupload&operation=upload&simple=1&type=image";
        SearchTitle = BaseUrl + "search.php?srchtype=title&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
        SearchFullText = BaseUrl + "search.php?srchtype=fulltext&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
        SearchUserThreads = BaseUrl + "search.php?srchfid=all&srchfrom=0&searchsubmit=yes&srchuid=";
        FavoritesUrl = BaseUrl + "my.php?item=favorites&type=thread";
        FavoriteAddUrl = BaseUrl + "my.php?item=favorites&inajax=1&ajaxtarget=favorite_msg&tid=";
        FavoriteRemoveUrl = BaseUrl + "my.php?item=favorites&action=remove&inajax=1&ajaxtarget=favorite_msg&tid=";
        UserInfoUrl = BaseUrl + "space.php?uid=";

        LoginStep3 = BaseUrl + "logging.php?action=login&loginsubmit=yes&inajax=1";
        LoginStep2 = BaseUrl + "logging.php?action=login&referer=http%3A//www.hi-pda.com/forum/logging.php";
    }

}
