package net.jejer.hipda.async;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;


public class VolleyHelper {

    private Context mCtx;
    private RequestQueue mRequestQueue;

    public void init(Context ctx) {
        mCtx = ctx;
        if (mRequestQueue == null) {
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
        if (error instanceof TimeoutError) {
            reason = "连接超时";
        } else if (error instanceof NoConnectionError) {
            if (error.toString().contains("UnknownHostException")) {
                reason = "域名解析失败";
            } else {
                reason = "无法连接";
            }
        } else if (error instanceof AuthFailureError) {
            reason = "认证失败";
        } else if (error instanceof ServerError) {
            reason = "服务器错误";
        } else if (error instanceof NetworkError) {
            reason = "网络错误";
        } else if (error instanceof ParseError) {
            reason = "解析失败";
        }
        return reason;
    }
}
