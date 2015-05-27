package net.jejer.hipda.volley;

import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * a default error listener
 * Created by GreenSkinMonster on 2015-05-27.
 */
public class SimpleErrorListener implements Response.ErrorListener {
    private VolleyError mError;

    @Override
    public void onErrorResponse(VolleyError error) {
        mError = error;
    }

    public VolleyError getError() {
        return mError;
    }

    public String getErrorText() {
        return VolleyHelper.getErrorReason(mError);
    }
}
