package net.jejer.hipda.utils;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class HiUtils {
	public static final String UserAgent = "net.jejer.hipda";
	public static final String ThreadListUrl = "http://www.hi-pda.com/forum/forumdisplay.php?fid=";
	public static final String DetailListUrl = "http://www.hi-pda.com/forum/viewthread.php?tid=";
	public static final String BaseUrl = "http://www.hi-pda.com/forum/";
	public static final String ReplyUrl = BaseUrl+"post.php?action=reply&tid=";
	public static final String NewThreadUrl = BaseUrl+"post.php?action=newthread&fid=";
	public static final String MyReplyUrl = BaseUrl+"my.php?item=posts";
	public static final String SMSUrl = BaseUrl+"pm.php?filter=privatepm";
	public static final String SMSDetailUrl = BaseUrl+"pm.php?daterange=5&uid=";
	public static final String SMSPreparePostUrl = BaseUrl+"pm.php?daterange=1&uid=";
	public static final String SMSPostUrl = BaseUrl+"pm.php?action=send&pmsubmit=yes&infloat=yes&inajax=1&uid=";
	public static final String ThreadNotifyUrl = BaseUrl+"notice.php?filter=threads";
	public static final String CheckSMS = BaseUrl + "pm.php?checknewpm";
	public static final String UploadImgUrl = BaseUrl + "misc.php?action=swfupload&operation=upload&simple=1&type=image";
	public static final String SearchTitle = BaseUrl + "search.php?srchtype=title&searchsubmit=true&st=on&srchuname=&srchfilter=all&srchfrom=0&before=&orderby=lastpost&ascdesc=desc&srchfid%5B0%5D=all&srchtxt=";
	public static final String FavoritesUrl = BaseUrl + "my.php?item=favorites&type=thread";
	public static final String FavoriteAddUrl = BaseUrl + "my.php?item=favorites&inajax=1&ajaxtarget=favorite_msg&tid=";
	public static final String FavoriteRemoveUrl = BaseUrl + "my.php?item=favorites&action=remove&inajax=1&ajaxtarget=favorite_msg&tid=";
	public static final String UserInfoUrl = BaseUrl + "space.php?uid=";
	//public static final String LoginStep1 = BaseUrl + "index.php";
	//public static final String LoginStep2 = BaseUrl + "logging.php?action=login&referer=http:///www.hi-pda.com/forum/index.php&sid=";
	public static final String LoginStep3 = BaseUrl + "logging.php?action=login&loginsubmit=yes&inajax=1";
	public static final String LoginStep2 = BaseUrl + "logging.php?action=login&referer=http%3A//www.hi-pda.com/forum/logging.php";

	public static int getForumID(Context ctx, long idx) {
		final int[] forumsID = ctx.getResources().getIntArray(R.array.forums_id);
		return forumsID[(int) idx];
	}

	public static String getFullUrl(String particalUrl) {
		return BaseUrl + particalUrl;
	}

	public static Boolean isAutoLoadImg(Context ctx) {
		if (HiSettingsHelper.getInstance().isLoadImgOnMobileNwk()) {
			return true;
		} else {
			ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info == null || !info.isConnected() || info.getType() != ConnectivityManager.TYPE_WIFI) {
				//Mobile Network
				return false;
			} else {
				return true;
			}
		}
	}
}
