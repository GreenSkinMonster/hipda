package net.jejer.hipda.async;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class PostSmsAsyncTask extends AsyncTask<String, Void, Void> {

    private String LOG_TAG = getClass().getSimpleName();

    private Context mCtx;
    private String mUid;

    private String mFormhash;
    private int mStatus = Constants.STATUS_FAIL;
    private String mResult = "";
    private String mText = "";
    private PostListener mPostListenerCallback;

    public PostSmsAsyncTask(Context ctx, String uid, PostListener postListener) {
        mCtx = ctx;
        mUid = uid;
        mPostListenerCallback = postListener;
    }

    @Override
    protected Void doInBackground(String... arg0) {
        String content = arg0[0];

        // fetch a new page and parse formhash
        String rsp_str;
        Boolean done = false;
        int retry = 0;
        do {
            VolleyHelper.MyErrorListener errorListener = VolleyHelper.getInstance().getErrorListener();
            rsp_str = VolleyHelper.getInstance().synchronousGet(HiUtils.SMSPreparePostUrl + mUid,
                    errorListener);
            if (!TextUtils.isEmpty(rsp_str)) {
                if (!LoginHelper.checkLoggedin(mCtx, rsp_str)) {
                    int status = new LoginHelper(mCtx, null).login();
                    if (status > Constants.STATUS_FAIL) {
                        break;
                    }
                } else {
                    done = true;
                }
            } else {
                mResult = errorListener.getErrorText();
            }
            retry++;
        } while (!done && retry < 3);

        if (done) {
            Document doc = Jsoup.parse(rsp_str);
            Elements formhashES = doc.select("input#formhash");
            if (formhashES.size() == 0) {
                mResult = "SMS send fail, can not get formhash.";
                return null;
            } else {
                mFormhash = formhashES.first().attr("value");
            }
            // do post
            doPost(content);
        }

        return null;
    }

    private String doPost(String content) {
        String url = HiUtils.SMSPostUrl + mUid;

        Map<String, String> post_param = new HashMap<String, String>();
        post_param.put("formhash", mFormhash);
        post_param.put("lastdaterange", String.valueOf(System.currentTimeMillis()));
        post_param.put("handlekey", "pmreply");
        post_param.put("message", content);

        mText = content;

        VolleyHelper.MyErrorListener errorListener = VolleyHelper.getInstance().getErrorListener();
        String rsp_str = VolleyHelper.getInstance().synchronousPost(url, post_param,
                errorListener);

        if (TextUtils.isEmpty(rsp_str)) {
            mResult = "短消息发送失败! " + errorListener.getErrorText();
        } else if (!rsp_str.contains("class=\"summary\"")) {
            mResult = "短消息发送失败.";
        } else {
            mResult = "短消息发送成功.";
            mStatus = Constants.STATUS_SUCCESS;
        }
        return rsp_str;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mPostListenerCallback != null)
            mPostListenerCallback.onPrePost();
        else
            Toast.makeText(mCtx, "正在发送...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(Void avoid) {
        super.onPostExecute(avoid);
        if (mStatus != Constants.STATUS_SUCCESS && !TextUtils.isEmpty(mText)) {
            ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("AUTO SAVE FROM HiPDA", mText);
            clipboard.setPrimaryClip(clip);
            mResult += "\n请注意：发表失败的短消息已经复制到粘贴板";
        }
        if (mPostListenerCallback != null) {
            mPostListenerCallback.onPostDone(mStatus, mResult);
        } else {
            Toast.makeText(mCtx, mResult, Toast.LENGTH_LONG).show();
        }
    }

    public interface PostListener {
        public void onPrePost();

        public void onPostDone(int status, String message);
    }

}
