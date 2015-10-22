package net.jejer.hipda.okhttp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cookie.PersistentCookieStore;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * helper class for okhttp
 * Created by GreenSkinMonster on 2015-10-22.
 */
public class OkHttpHelper {

    public final static int NETWORK_TIMEOUT_SECS = 10;

    private OkHttpClient client;
    private PersistentCookieStore cookieStore;
    private Handler handler;

    private OkHttpHelper() {
    }

    private static class SingletonHolder {
        public static final OkHttpHelper INSTANCE = new OkHttpHelper();
    }

    public static OkHttpHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Context ctx) {
        client = new OkHttpClient();
        client.setConnectTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS);
        client.setReadTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS);
        client.setWriteTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS);

        cookieStore = new PersistentCookieStore(ctx);
        CookieManager cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        client.setCookieHandler(cookieManager);

        if (Logger.isDebug())
            client.interceptors().add(new LoggingInterceptor());

        handler = new Handler(Looper.getMainLooper());
    }

    public boolean ready() {
        return client != null && cookieStore != null;
    }

    private Request buildGetRequest(String url, Object tag) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("User-Agent", HiUtils.UserAgent);

        if (tag != null)
            builder.tag(tag);

        return builder.build();
    }

    private Request buildPostFormRequest(String url, Map<String, String> params, Object tag)
            throws UnsupportedEncodingException {

        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addEncoded(entry.getKey(),
                        URLEncoder.encode(entry.getValue(), HiSettingsHelper.getInstance().getEncode()));
            }
        }

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        reqBuilder.url(url)
                .header("User-Agent", HiUtils.UserAgent)
                .post(requestBody);

        if (tag != null)
            reqBuilder.tag(tag);

        return reqBuilder.build();
    }

    public String get(String url) throws IOException {
        Request request = buildGetRequest(url, null);

        Call call = client.newCall(request);
        Response response = call.execute();

        return getResponseBody(response);
    }

    public void asyncGet(String url, ResultCallback callback) {
        asyncGet(url, callback, null);
    }

    public void asyncGet(String url, ResultCallback callback, Object tag) {
        if (callback == null) callback = DEFAULT_CALLBACK;
        final ResultCallback rspCallBack = callback;

        Request request = buildGetRequest(url, tag);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handleFailureCallback(request, e, rspCallBack);
            }

            @Override
            public void onResponse(Response response) {
                try {
                    String body = getResponseBody(response);
                    handleSuccessCallback(body, rspCallBack);
                } catch (IOException e) {
                    handleFailureCallback(response.request(), e, rspCallBack);
                }
            }
        });
    }

    public String post(String url, Map<String, String> params) throws IOException {
        Request request = buildPostFormRequest(url, params, null);
        Response response = client.newCall(request).execute();
        return getResponseBody(response);
    }


    private String getResponseBody(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response.code() + ", " + response.message());
        }

        String encoding = HiSettingsHelper.getInstance().getEncode();
        String contextType = response.headers().get("Content-Type");
        if (!TextUtils.isEmpty(contextType)) {
            if (contextType.toUpperCase().contains("UTF")) {
                encoding = "UTF-8";
            } else if (contextType.toUpperCase().contains("GBK")) {
                encoding = "GBK";
            }
        }
        return new String(response.body().bytes(), encoding);
    }

    private void handleFailureCallback(final Request request, final Exception e, final ResultCallback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(request, e);
            }
        });
    }

    private void handleSuccessCallback(final String response, final ResultCallback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(response);
            }
        });
    }

    public interface ResultCallback {

        void onError(Request request, Exception e);

        void onResponse(String response);
    }

    private final ResultCallback DEFAULT_CALLBACK = new ResultCallback() {
        @Override
        public void onError(Request request, Exception e) {
        }

        @Override
        public void onResponse(String response) {
        }
    };

    public static String getErrorMessage(Exception e) {
        String msg = e.getClass().getSimpleName();
        if (e instanceof UnknownHostException) {
            msg = "请检查网络连接";
        } else if (e instanceof SocketTimeoutException) {
            msg = "请求超时";
        } else if (e instanceof IOException) {
            String emsg = e.getMessage();
            if (emsg != null && emsg.startsWith("Unexpected code ") && emsg.contains(",")) {
                msg = "错误代码 (" + emsg.substring("Unexpected code ".length(), emsg.indexOf(",")) + ")";
            }
        }
        if (HiSettingsHelper.getInstance().isErrorReportMode())
            msg += "\n>>> " + e.getMessage();
        return msg;
    }

    public void clearCookies() {
        if (cookieStore != null)
            cookieStore.removeAll();
    }

    public boolean isLoggedIn() {
        List<HttpCookie> cookies = cookieStore.getCookies();
        for (HttpCookie cookie : cookies) {
            if ("cdb_auth".equals(cookie.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getAuthCookie() {
        List<HttpCookie> cookies = cookieStore.getCookies();
        for (HttpCookie cookie : cookies) {
            if ("cdb_auth".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
