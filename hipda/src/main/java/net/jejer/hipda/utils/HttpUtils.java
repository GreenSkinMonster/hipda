package net.jejer.hipda.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import net.jejer.hipda.bean.HiSettingsHelper;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.util.Log;

public class HttpUtils {
	//private final static String LOG_TAG = "HttpUtils";

	public static String buildHttpString(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		try {
			for (String key : params.keySet()) {
				if (key == "base") {
					sb.insert(0, params.get(key)+"?");
				}
				else {
					sb.append(URLEncoder.encode(key, "GBK"));
					sb.append("=");
					sb.append(URLEncoder.encode(params.get(key), "GBK"));
					sb.append("&");
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Remove the tail &
		return sb.toString();
	}

	public static String getMiddleString(String source, String start, String end) {
		int start_idx = source.indexOf(start) + start.length();
		int end_idx = 0;
		if (end.isEmpty()) {
			end_idx = source.length();
		} else {
			end_idx = source.indexOf(end, start_idx);
			if (end_idx <= 0) {
				end_idx = source.length();
			}
		}

		if (start_idx <= 0 || end_idx <= 0 || end_idx <= start_idx) {
			Log.e(HttpUtils.class.getName(), "getValue fail");
			return null;
		}

		return source.substring(start_idx, end_idx);
	}

	public static void saveAuth(Context ctx, CookieStore cookieStore) {
		String authStr = "";
		List<Cookie> cookies = cookieStore.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("cdb_auth")) {
				authStr = cookie.getValue();
				HiSettingsHelper.getInstance().setCookieAuth(authStr);
			}
		}
	}

	public static CookieStore restoreCookie(Context ctx) {
		CookieStore cookieStore = new BasicCookieStore();

		String authStr = HiSettingsHelper.getInstance().getCookieAuth();
		if (authStr.equals("")) {
			return cookieStore;
		}

		BasicClientCookie cookie = new BasicClientCookie("cdb_auth", authStr);
		cookie.setDomain("www.hi-pda.com");
		cookie.setPath("/");
		cookie.setVersion(0);
		cookieStore.addCookie(cookie);

		return cookieStore;
	}

	public static int getIntFromString(String s) {
		String tmp = s.replaceAll( "[^\\d]", "" );
		if (!tmp.isEmpty()) {
			return Integer.parseInt(tmp);
		} else {
			return 0;
		}
	}
}
