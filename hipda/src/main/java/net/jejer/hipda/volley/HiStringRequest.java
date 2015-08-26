package net.jejer.hipda.volley;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HiStringRequest extends StringRequest {


    private Map<String, String> mParams;

    public HiStringRequest(String url, Listener<String> listener,
                           ErrorListener errorListener) {
        super(url, listener, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(
                VolleyHelper.SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public HiStringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(
                VolleyHelper.SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public HiStringRequest(int method, String url, Map<String, String> params, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(
                VolleyHelper.SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mParams = params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }

        headers.put("User-agent", HiUtils.UserAgent);
        if (getMethod() == Method.POST)
            headers.put("Content-Type", "application/x-www-form-urlencoded");

        return headers;
    }

    @Override
    public Map<String, String> getParams() {
        return mParams;
    }

    @Override
    protected String getParamsEncoding() {
        return HiSettingsHelper.getInstance().getEncode();
    }


    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        String encoding = HiSettingsHelper.getInstance().getEncode();
        String contextType = response.headers.get("Content-Type");
        if (!TextUtils.isEmpty(contextType)) {
            if (contextType.toUpperCase().contains("UTF")) {
                encoding = "UTF-8";
            } else if (contextType.toUpperCase().contains("GBK")) {
                encoding = "GBK";
            }
        }
        try {
            parsed = new String(response.data, encoding);
        } catch (UnsupportedEncodingException e) {
            Logger.e("encoding error", e);
            parsed = "";
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

}
