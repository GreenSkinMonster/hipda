package net.jejer.hipda.cookie;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.HttpCookie;

/**
 * http://codereview.stackexchange.com/questions/61494/persistent-cookie-support-using-volley-and-httpurlconnection
 */
public class HttpCookieParcelable implements Parcelable {
    private HttpCookie cookie;

    public HttpCookieParcelable(HttpCookie cookie) {
        this.cookie = cookie;
    }

    public HttpCookieParcelable(Parcel source) {
        String name = source.readString();
        String value = source.readString();
        cookie = new HttpCookie(name, value);
        cookie.setComment(source.readString());
        cookie.setCommentURL(source.readString());
        cookie.setDiscard(source.readByte() != 0);
        cookie.setDomain(source.readString());
        cookie.setMaxAge(source.readLong());
        cookie.setPath(source.readString());
        cookie.setPortlist(source.readString());
        cookie.setSecure(source.readByte() != 0);
        cookie.setVersion(source.readInt());
    }

    public HttpCookie getCookie() {
        return cookie;
    }

    public void setCookie(HttpCookie cookie) {
        this.cookie = cookie;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cookie.getName());
        dest.writeString(cookie.getValue());
        dest.writeString(cookie.getComment());
        dest.writeString(cookie.getCommentURL());
        dest.writeByte((byte) (cookie.getDiscard() ? 1 : 0));
        dest.writeString(cookie.getDomain());
        dest.writeLong(cookie.getMaxAge());
        dest.writeString(cookie.getPath());
        dest.writeString(cookie.getPortlist());
        dest.writeByte((byte) (cookie.getSecure() ? 1 : 0));
        dest.writeInt(cookie.getVersion());
    }

    public static final Parcelable.Creator<HttpCookieParcelable> CREATOR =
            new Creator<HttpCookieParcelable>() {

                @Override
                public HttpCookieParcelable[] newArray(int size) {
                    return new HttpCookieParcelable[size];
                }

                @Override
                public HttpCookieParcelable createFromParcel(Parcel source) {
                    return new HttpCookieParcelable(source);
                }
            };
}