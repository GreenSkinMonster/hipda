package net.jejer.hipda.async;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class VolleyHelper {

    public final static int REQUEST_TIMEOUT = 15;
    private static String LOG_TAG = VolleyHelper.class.getSimpleName();

    private Context mCtx;
    private RequestQueue mRequestQueue;

    public void init(Context ctx) {
        mCtx = ctx;
        if (mRequestQueue == null) {

            CookieManager cookieManager = new CookieManager(new HiCookieStore(), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
            CookieHandler.setDefault(cookieManager);

            mRequestQueue = Volley.newRequestQueue(mCtx);
        }
    }

    public <T> void add(Request<T> req) {
        mRequestQueue.add(req);
    }

    public void cancelAll() {
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    private VolleyHelper() {
    }

    private static class SingletonHolder {
        public static final VolleyHelper INSTANCE = new VolleyHelper();
    }

    public static VolleyHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static String getErrorReason(VolleyError error) {
        String reason = "未知错误";
        if (error == null) {
            return reason;
        }
        if (error instanceof TimeoutError) {
            reason = "连接超时";
        } else if (error instanceof NoConnectionError) {
            reason = "请检查网络连接";
        } else if (error instanceof AuthFailureError) {
            reason = "认证失败";
        } else if (error instanceof ServerError) {
            reason = "服务器错误";
        } else if (error instanceof NetworkError) {
            reason = "网络错误";
        } else if (error instanceof ParseError) {
            reason = "解析失败";
        } else {
            reason = error.getMessage();
        }
        return reason;
    }

    public String synchronousGet(String url, Response.ErrorListener errorListener) {
        RequestFuture<String> future = RequestFuture.newFuture();
        HiStringRequest request = new HiStringRequest(Request.Method.GET, url, future, future);
        mRequestQueue.add(request);
        try {
            return future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Log.e(LOG_TAG, "Error when synchronousGet : " + url, e);
            if (errorListener != null)
                errorListener.onErrorResponse(new VolleyError(e));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error when synchronousGet : " + url, e);
            if (errorListener != null)
                errorListener.onErrorResponse(new VolleyError(e));
        }
        return null;
    }

    public String synchronousPost(String url, Map<String, String> params, Response.ErrorListener errorListener) {
        RequestFuture<String> future = RequestFuture.newFuture();
        HiStringRequest request = new HiStringRequest(Request.Method.POST, url, params, future, future);
        mRequestQueue.add(request);
        try {
            return future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Log.e(LOG_TAG, "Error when synchronousPost : " + url, e);
            if (errorListener != null)
                errorListener.onErrorResponse(new VolleyError(e));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error when synchronousPost : " + url, e);
            if (errorListener != null)
                errorListener.onErrorResponse(new VolleyError(e));
        }
        return null;
    }

    class MyErrorListener implements Response.ErrorListener {
        private VolleyError mError;

        @Override
        public void onErrorResponse(VolleyError error) {
            mError = error;
        }

        public VolleyError getError() {
            return mError;
        }

        public String getErrorText() {
            return getErrorReason(mError);
        }
    }

    public MyErrorListener getErrorListener() {
        return new MyErrorListener();
    }


}
