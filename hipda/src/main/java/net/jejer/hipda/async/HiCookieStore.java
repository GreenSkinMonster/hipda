package net.jejer.hipda.async;

import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * a simple cookie store
 * Created by GreenSkinMonster on 2015-04-04.
 */
public class HiCookieStore implements CookieStore {

    private List<HttpCookie> cookies = new ArrayList<HttpCookie>();
    private static List<HttpCookie> emptyCookies = new ArrayList<HttpCookie>();

    public void add(URI uri, HttpCookie httpCookie) {
        if (uri.toString().startsWith(HiUtils.BaseUrl)
                && "cdb_auth".equals(httpCookie.getName())) {
            HiSettingsHelper.getInstance().setCookieAuth(httpCookie.getValue());

            HttpCookie cookie = new HttpCookie("cdb_auth", httpCookie.getValue());
            cookie.setDomain("www.hi-pda.com");
            cookie.setPath("/");
            cookie.setVersion(0);

            cookies.clear();
            cookies.add(cookie);
        }
    }

    public HiCookieStore() {
        String authStr = HiSettingsHelper.getInstance().getCookieAuth();
        if (!TextUtils.isEmpty(authStr)) {
            HttpCookie cookie = new HttpCookie("cdb_auth", authStr);
            cookie.setDomain("www.hi-pda.com");
            cookie.setPath("/");
            cookie.setVersion(0);

            cookies.add(cookie);
        }
    }

    public List<HttpCookie> get(URI uri) {
        if (uri.toString().startsWith(HiUtils.BaseUrl)) {
            return cookies;
        }
        emptyCookies.clear();
        return emptyCookies;
    }

    public boolean removeAll() {
        cookies.clear();
        return true;
    }

    public List<HttpCookie> getCookies() {
        return cookies;
    }

    public List<URI> getURIs() {
        return new ArrayList<URI>();
    }

    public boolean remove(URI uri, HttpCookie cookie) {
        return false;
    }

}