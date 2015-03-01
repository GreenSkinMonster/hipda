package net.jejer.hipda.async;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.AvatarUrlCache;
import net.jejer.hipda.ui.ThreadListAdapter;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Collection;

public class AvatarUrlTask extends AsyncTask<String, Void, String> {

    private final String LOG_TAG = getClass().getSimpleName();

    private Context mCtx;
    private ThreadListAdapter mThreadListAdapter;

    private Collection<String> mUids;

    public AvatarUrlTask(Context context, ThreadListAdapter threadListAdapter, Collection<String> uids) {
        mCtx = context;
        mUids = uids;
        mThreadListAdapter = threadListAdapter;
    }

    @Override
    protected String doInBackground(String... arg0) {

        CookieStore cookieStore = HttpUtils.restoreCookie(mCtx);
        HttpContext localContext = new BasicHttpContext();
        AndroidHttpClient client = AndroidHttpClient.newInstance(HiUtils.UserAgent);
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        for (String uid : mUids) {
            HttpGet req = new HttpGet(HiUtils.UserInfoUrl + uid);

            Log.e(LOG_TAG, "getting avatarUrl " + HiUtils.UserInfoUrl + uid);
            try {
                HttpResponse rsp = client.execute(req, localContext);

                HttpEntity rsp_ent = rsp.getEntity();
                String rstStr = EntityUtils.toString(rsp_ent, HiSettingsHelper.getInstance().getEncode());

                if (rstStr.contains("您无权进行当前操作")) {
                    break;
                }

                String avatarUrl = HttpUtils.getMiddleString(rstStr, "<div class=\"avatar\"><img src=\"", "\"");
                if (avatarUrl != null && !avatarUrl.contains("noavatar") && avatarUrl.startsWith("http")) {
                    AvatarUrlCache.getInstance().put(uid, avatarUrl);
                } else {
                    AvatarUrlCache.getInstance().put(uid, "");
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "network error in AvatorUrlLoader", e);
            }
        }

        //mThreadListAdapter.refreshAvatars();

        client.close();

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
