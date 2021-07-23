package net.jejer.hipda.utils;

import android.content.Context;
import android.text.TextUtils;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.typeface.IIcon;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.Forum;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.SmallImages;
import net.jejer.hipda.ui.HiApplication;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;

public class HiUtils {
    public static final String UserAgentPrefix = "net.jejer.hipda ";

    public static final String ForumServer = "http://www.hi-pda.com";
    public static final String ForumServerSsl = "https://www.hi-pda.com";
    public static final String ImageHost = "http://img.hi-pda.com";
    public static final String AvatarPath = "uc_server/data/avatar/";
    public static final String CookieDomain = "hi-pda.com";
    public static final String AvatarSuffix = "_avatar_middle.jpg";
    public static final String NewPMImage = "images/default/notice_newpm.gif";
    public static final String SmiliesPattern = CookieDomain + "/forum/images/smilies/";
    public static final String ForumImagePattern = CookieDomain + "/forum/images/";
    public static final String ForumUrlPattern = "." + CookieDomain + "/forum/";

    public static final int CLIENT_TID = 1579403;

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
    public static String SMSPostByUid;
    public static String SMSPostByUsername;
    public static String ThreadNotifyUrl;
    public static String CheckSMS;
    public static String NewSMS;
    public static String ClearSMS;
    public static String UploadImgUrl;
    public static String SearchUrl;
    public static String SearchUserThreads;
    public static String FavoritesUrl;
    public static String FavoriteAddUrl;
    public static String FavoriteRemoveUrl;
    public static String FavoriteDeleteUrl;
    public static String UserInfoUrl;
    public static String UserWarningUrl;
    public static String AddBlackUrl;
    public static String DelBlackUrl;
    public static String ViewBlackUrl;
    public static String NewPostsUrl;
    public static String SearchByIdUrl;

    public static String LoginSubmit;
    public static String LoginGetFormHash;

    private static String userAgent;

    public static String ImageBaseUrl;
    public static String AvatarBaseUrl;

    private final static String AVATAR_BASE = "000000000";
    // max upload file size : 8M
    public final static int DEFAULT_MAX_UPLOAD_FILE_SIZE = 8 * 1024 * 1024;

    public final static int FID_BS = 6;
    public final static int FID_DISCOVERY = 2;

    public final static Forum[] FORUMS = {
            new Forum(FID_DISCOVERY, "Discovery", FontAwesome.Icon.faw_cc_discover),
            new Forum(FID_BS, "Buy & Sell", FontAwesome.Icon.faw_shopping_cart),
            new Forum(7, "Geek Talks", FontAwesome.Icon.faw_forumbee),
            new Forum(59, "E-INK", FontAwesome.Icon.faw_book),
            new Forum(12, "PalmOS", FontAwesome.Icon.faw_mobile),
            new Forum(57, "疑似机器人", FontAwesome.Icon.faw_reddit),
            new Forum(63, "已完成交易", FontAwesome.Icon.faw_check_square),
            new Forum(62, "Joggler", FontAwesome.Icon.faw_cogs),
            new Forum(5, "站务与公告", FontAwesome.Icon.faw_bullhorn),
            new Forum(9, "Smartphone", FontAwesome.Icon.faw_phone_square),
            new Forum(56, "iPhone, iPod Touch，iPad", FontAwesome.Icon.faw_apple),
            new Forum(60, "Android, Chrome, & Google", FontAwesome.Icon.faw_android),
            new Forum(14, "Windows Mobile，PocketPC，HPC", FontAwesome.Icon.faw_windows),
            new Forum(22, "麦客爱苹果", FontAwesome.Icon.faw_desktop),
            new Forum(50, "DC,NB,MP3,Gadgets", FontAwesome.Icon.faw_camera_retro),
            new Forum(24, "意欲蔓延", FontAwesome.Icon.faw_coffee),
            new Forum(23, "随笔与个人文集", FontAwesome.Icon.faw_pencil_square),
            new Forum(25, "吃喝玩乐", FontAwesome.Icon.faw_cutlery),
            new Forum(51, "La Femme", FontAwesome.Icon.faw_female),
            new Forum(65, "改版建议", FontAwesome.Icon.faw_commenting),
            new Forum(64, "只讨论2.0", FontAwesome.Icon.faw_child)
    };

    private final static Map<Integer, Forum> FORUMS_MAP;

    public final static int[] DEFAULT_FORUMS = {FID_DISCOVERY, FID_BS, 7};

    static {
        FORUMS_MAP = new LinkedHashMap<>(FORUMS.length);
        for (Forum forum : FORUMS) {
            FORUMS_MAP.put(forum.getId(), forum);
        }
    }

    public static void updateBaseUrls() {
        String imageHost = HiSettingsHelper.getInstance().getImageHost();
        String forumServer = HiSettingsHelper.getInstance().getForumServer();

        BaseUrl = forumServer + "/forum/";
        ImageBaseUrl = imageHost + "/forum/";

        AvatarBaseUrl = ImageBaseUrl + AvatarPath;

        ThreadListUrl = BaseUrl + "forumdisplay.php?fid=";
        DetailListUrl = BaseUrl + "viewthread.php?tid=";
        ReplyUrl = BaseUrl + "post.php?action=reply&tid=";
        EditUrl = BaseUrl + "post.php?action=edit";
        NewThreadUrl = BaseUrl + "post.php?action=newthread&fid=";
        MyReplyUrl = BaseUrl + "my.php?item=posts";
        MyPostUrl = BaseUrl + "my.php?item=threads";
        LastPageUrl = BaseUrl + "redirect.php?goto=lastpost&from=fastpost&tid=";
        RedirectToPostUrl = BaseUrl + "redirect.php?goto=findpost&pid={pid}&ptid={tid}";
        GotoPostUrl = BaseUrl + "gotopost.php?pid={pid}";
        SMSUrl = BaseUrl + "pm.php?filter=privatepm";
        SMSDetailUrl = BaseUrl + "pm.php?daterange=5&uid=";
        SMSPreparePostUrl = BaseUrl + "pm.php?daterange=1&uid=";
        SMSPostByUid = BaseUrl + "pm.php?action=send&pmsubmit=yes&infloat=yes&inajax=1&uid={uid}";
        SMSPostByUsername = BaseUrl + "pm.php?action=send&pmsubmit=yes&infloat=yes&inajax=1";
        ThreadNotifyUrl = BaseUrl + "notice.php";
        CheckSMS = BaseUrl + "pm.php?checknewpm";
        NewSMS = BaseUrl + "pm.php?filter=newpm";
        ClearSMS = BaseUrl + "pm.php?action=del&uid={uid}&filter=privatepm";
        UploadImgUrl = BaseUrl + "misc.php?action=swfupload&operation=upload&simple=1&type=image";
        SearchUrl = BaseUrl + "search.php?srchtype={srchtype}&srchtxt={srchtxt}&searchsubmit=true&st=on&srchuname={srchuname}&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D={fid}";
        SearchUserThreads = BaseUrl + "search.php?srchfid=all&srchfrom=0&searchsubmit=yes&srchuid={srchuid}";
        FavoritesUrl = BaseUrl + "my.php?item={item}&type=thread";
        FavoriteAddUrl = BaseUrl + "my.php?item={item}&action=add&inajax=1&ajaxtarget=favorite_msg&tid={tid}";
        FavoriteRemoveUrl = BaseUrl + "my.php?item={item}&action=remove&inajax=1&ajaxtarget=favorite_msg&tid={tid}";
        FavoriteDeleteUrl = BaseUrl + "my.php?item={item}&type=thread";
        UserInfoUrl = BaseUrl + "space.php?uid=";
        UserWarningUrl = BaseUrl + "misc.php?action=viewwarning&tid={tid}&uid={uid}";
        AddBlackUrl = BaseUrl + "pm.php?action=addblack";
        DelBlackUrl = BaseUrl + "pm.php?action=delblack";
        ViewBlackUrl = BaseUrl + "pm.php?action=viewblack";
        NewPostsUrl = BaseUrl + "search.php?srchfrom=86400&searchsubmit=yes";
        SearchByIdUrl = BaseUrl + "search.php?searchid={searchid}&orderby=lastpost&ascdesc=desc&searchsubmit=yes";

        LoginSubmit = BaseUrl + "logging.php?action=login&loginsubmit=yes&inajax=1";
        LoginGetFormHash = BaseUrl + "logging.php?action=login&referer=http%3A//www.hi-pda.com/forum/logging.php";

        SmallImages.clear();
    }

    public static String getForumNameByFid(int fid) {
        return FORUMS_MAP.containsKey(fid) ? FORUMS_MAP.get(fid).getName() : "";
    }

    public static boolean isForumValid(int fid) {
        return FORUMS_MAP.containsKey(fid);
    }

    public static Forum getForumByFid(int fid) {
        return FORUMS_MAP.get(fid);
    }

    public final static String[] BS_TYPES = {"全部", "手机", "掌上电脑", "笔记本电脑", "无线产品", "数码相机、摄像机", "MP3随身听", "各类配件", "其他好玩的"};
    public final static String[] BS_TYPE_IDS = {"", "1", "2", "3", "4", "5", "6", "7", "8"};
    public final static IIcon[] BS_TYPE_ICONS = {
            FontAwesome.Icon.faw_tags,
            FontAwesome.Icon.faw_mobile,
            FontAwesome.Icon.faw_tablet,
            FontAwesome.Icon.faw_laptop,
            FontAwesome.Icon.faw_wifi,
            FontAwesome.Icon.faw_camera_retro,
            FontAwesome.Icon.faw_music,
            FontAwesome.Icon.faw_desktop,
            FontAwesome.Icon.faw_gamepad
    };

    public static int getBSTypeIndexByFid(String typeId) {
        for (int i = 0; i < BS_TYPE_IDS.length; i++) {
            if (BS_TYPE_IDS[i].equals(typeId)) {
                return i;
            }
        }
        return -1;
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
                + fullUid.substring(7, 9) + HiUtils.AvatarSuffix;
        return url;
    }

    public static int getThemeValue(Context context, String theme, int primaryColor) {
        if (HiSettingsHelper.THEME_DARK.equals(theme)) {
            return R.style.ThemeDark;
        } else if (HiSettingsHelper.THEME_BLACK.equals(theme)) {
            return R.style.ThemeBlack;
        } else if (HiSettingsHelper.THEME_LIGHT.equals(theme)) {
            if (primaryColor == ContextCompat.getColor(context, R.color.md_red_700))
                return R.style.ThemeLight_Red;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_pink_700))
                return R.style.ThemeLight_Pink;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_purple_700))
                return R.style.ThemeLight_Purple;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_deep_purple_700))
                return R.style.ThemeLight_DeepPurple;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_indigo_700))
                return R.style.ThemeLight_Indigo;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_blue_700))
                return R.style.ThemeLight_Blue;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_light_blue_700))
                return R.style.ThemeLight_LightBlue;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_cyan_700))
                return R.style.ThemeLight_Cyan;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_teal_700))
                return R.style.ThemeLight_Teal;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_green_700))
                return R.style.ThemeLight_Green;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_light_green_700))
                return R.style.ThemeLight_LightGreen;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_lime_700))
                return R.style.ThemeLight_Lime;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_yellow_700))
                return R.style.ThemeLight_Yellow;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_amber_700))
                return R.style.ThemeLight_Amber;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_orange_700))
                return R.style.ThemeLight_Orange;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_deep_orange_700))
                return R.style.ThemeLight_DeepOrange;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_brown_700))
                return R.style.ThemeLight_Brown;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_grey_700))
                return R.style.ThemeLight_Grey;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_blue_grey_700))
                return R.style.ThemeLight_BlueGrey;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_grey_200))
                return R.style.ThemeLight_White;
            if (primaryColor == ContextCompat.getColor(context, R.color.md_black_1000))
                return R.style.ThemeLight_Black;
        }
        HiSettingsHelper.getInstance().setTheme(HiSettingsHelper.THEME_LIGHT);
        HiSettingsHelper.getInstance().setPrimaryColor(ContextCompat.getColor(context, R.color.md_blue_grey_700));
        return R.style.ThemeLight_BlueGrey;
    }

    public static boolean isValidId(String id) {
        return !TextUtils.isEmpty(id) && Utils.parseInt(id) > 0;
    }

    public static String getUserAgent() {
        if (userAgent == null)
            userAgent = UserAgentPrefix + " " + HiApplication.getAppVersion();
        return userAgent;
    }

    public static String getForumsSummary() {
        List<Integer> fids = HiSettingsHelper.getInstance().getForums();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fids.size(); i++) {
            int fid = fids.get(i);
            Forum forum = getForumByFid(fid);
            if (forum != null) {
                sb.append(forum.getName());
                if (i != fids.size() - 1)
                    sb.append(", ");
            }
        }
        return sb.toString();
    }
}
