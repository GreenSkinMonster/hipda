package net.jejer.hipda.utils;

import android.text.TextUtils;

import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.iconics.typeface.IIcon;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;

public class HiUtils {
    public static final String UserAgent = "net.jejer.hipda";
    public static final String BaseUrl = "http://www.hi-pda.com/forum/";
    public static final String ThreadListUrl = BaseUrl + "forumdisplay.php?fid=";
    public static final String DetailListUrl = BaseUrl + "viewthread.php?tid=";
    public static final String ReplyUrl = BaseUrl + "post.php?action=reply&tid=";
    public static final String EditUrl = BaseUrl + "post.php?action=edit";
    public static final String NewThreadUrl = BaseUrl + "post.php?action=newthread&fid=";
    public static final String MyReplyUrl = BaseUrl + "my.php?item=posts";
    public static final String MyPostUrl = BaseUrl + "my.php?item=threads";
    public static final String LastPageUrl = BaseUrl + "/redirect.php?goto=lastpost&from=fastpost&tid=";
    public static final String RedirectToPostUrl = BaseUrl + "/redirect.php?goto=findpost&pid={pid}&ptid={tid}";
    public static final String GotoPostUrl = BaseUrl + "/gotopost.php?pid={pid}";
    public static final String SMSUrl = BaseUrl + "pm.php?filter=privatepm";
    public static final String SMSDetailUrl = BaseUrl + "pm.php?daterange=5&uid=";
    public static final String SMSPreparePostUrl = BaseUrl + "pm.php?daterange=1&uid=";
    public static final String SMSPostUrl = BaseUrl + "pm.php?action=send&pmsubmit=yes&infloat=yes&inajax=1&uid=";
    public static final String ThreadNotifyUrl = BaseUrl + "notice.php?filter=threads";
    public static final String CheckSMS = BaseUrl + "pm.php?checknewpm";
    public static final String UploadImgUrl = BaseUrl + "misc.php?action=swfupload&operation=upload&simple=1&type=image";
    public static final String SearchTitle = BaseUrl + "search.php?srchtype=title&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
    public static final String SearchFullText = BaseUrl + "search.php?srchtype=fulltext&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
    public static final String SearchUserThreads = BaseUrl + "search.php?srchfid=all&srchfrom=0&searchsubmit=yes&srchuid=";
    public static final String FavoritesUrl = BaseUrl + "my.php?item=favorites&type=thread";
    public static final String FavoriteAddUrl = BaseUrl + "my.php?item=favorites&inajax=1&ajaxtarget=favorite_msg&tid=";
    public static final String FavoriteRemoveUrl = BaseUrl + "my.php?item=favorites&action=remove&inajax=1&ajaxtarget=favorite_msg&tid=";
    public static final String UserInfoUrl = BaseUrl + "space.php?uid=";
    public static final String AvatarBaseUrl = BaseUrl + "uc_server/data/avatar/";

    public static final String LoginStep3 = BaseUrl + "logging.php?action=login&loginsubmit=yes&inajax=1";
    public static final String LoginStep2 = BaseUrl + "logging.php?action=login&referer=http%3A//www.hi-pda.com/forum/logging.php";

    public final static String SMILE_PATH = "images/smilies/";

    private final static String AVATAR_BASE = "000000000";
    public static int MAX_THREADS_IN_PAGE = 50;

    public final static int FID_BS = 6;
    public final static int FID_DISCOVERY = 2;

    public static String[] FORUMS = {"Discovery", "Buy & Sell", "Geek Talks", "E-INK", "PalmOS", "疑似机器人"};
    public static int[] FORUM_IDS = {FID_DISCOVERY, FID_BS, 7, 59, 12, 57};
    public static IIcon[] FORUM_ICONS = {
            FontAwesome.Icon.faw_cc_discover,
            FontAwesome.Icon.faw_shopping_cart,
            FontAwesome.Icon.faw_forumbee,
            FontAwesome.Icon.faw_book,
            FontAwesome.Icon.faw_mobile_phone,
            FontAwesome.Icon.faw_reddit
    };

    public static int getForumID(int idx) {
        return FORUM_IDS[idx];
    }

    public static int getForumIndexByFid(int fid) {
        for (int i = 0; i < FORUM_IDS.length; i++) {
            if (fid == FORUM_IDS[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isForumEnabled(int fid) {
        if (fid == FID_DISCOVERY) {
            return true;
        }
        if (getForumIndexByFid(fid) >= 0) {
            if (HiSettingsHelper.getInstance().getForums().contains(fid + ""))
                return true;
        }
        return false;
    }

    public static String getFullUrl(String particalUrl) {
        return BaseUrl + particalUrl;
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

    public static int getThemeValue(String theme) {
        if ("light".equals(theme)) {
            return R.style.ThemeLight;
        } else if ("dark".equals(theme)) {
            return R.style.ThemeDark;
        } else if ("black".equals(theme)) {
            return R.style.ThemeBlack;
        } else if ("light-deep_orange".equals(theme)) {
            return R.style.ThemeLight_DeepOrange;
        } else if ("light-blue_grey".equals(theme)) {
            return R.style.ThemeLight_BlueGrey;
        } else if ("light-teal".equals(theme)) {
            return R.style.ThemeLight_Teal;
        } else if ("light-indigo".equals(theme)) {
            return R.style.ThemeLight_Indigo;
        }
        HiSettingsHelper.getInstance().setTheme("light");
        return R.style.ThemeLight;
    }

}
