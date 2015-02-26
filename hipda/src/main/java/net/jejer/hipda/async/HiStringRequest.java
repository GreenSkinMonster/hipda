package net.jejer.hipda.async;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

public class HiStringRequest extends StringRequest {
	public HiStringRequest(Context ctx, int method, String url,
			Listener<String> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
		// TODO Auto-generated constructor stub
	}

	public HiStringRequest(Context ctx, String url, Listener<String> listener,
			ErrorListener errorListener) {
		super(url, listener, errorListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = super.getHeaders();

		if (headers == null
				|| headers.equals(Collections.emptyMap())) {
			headers = new HashMap<String, String>();
		}

		headers.put("Cookie", "cdb_auth="+HiSettingsHelper.getInstance().getCookieAuth());
		headers.put("User-agent", HiUtils.UserAgent);

		return headers;
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String parsed;
		try {
			parsed = new String(response.data, HiSettingsHelper.getInstance().getEncode());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			parsed = "";
		}
		return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
	}

}
