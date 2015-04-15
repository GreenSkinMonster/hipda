package net.jejer.hipda.async;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class PostAsyncTask extends AsyncTask<PostBean, Void, Void> {
    private final String LOG_TAG = getClass().getSimpleName();

    public static final int MODE_REPLY_THREAD = 0;
    public static final int MODE_REPLY_POST = 1;
    public static final int MODE_QUOTE_POST = 2;
    public static final int MODE_NEW_THREAD = 3;
    public static final int MODE_QUICK_REPLY = 4;
    public static final int MODE_EDIT_POST = 5;

    private int mMode;
    private String mResult;
    private int mStatus = Constants.STATUS_FAIL;
    private Context mCtx;
    private PrePostInfoBean mInfo;

    private PostListener mPostListenerCallback;
    private String mContent;
    private String mTid;
    private String mTitle;
    private String mFloor;

    public PostAsyncTask(Context ctx, int mode, PrePostInfoBean info, PostListener postListenerCallback) {
        mCtx = ctx;
        mMode = mode;
        mInfo = info;
        mPostListenerCallback = postListenerCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mPostListenerCallback != null)
            mPostListenerCallback.onPrePost();
    }

    @Override
    protected Void doInBackground(PostBean... postBeans) {

        PostBean postBean = postBeans[0];
        String replyText = postBean.getContent();
        String tid = postBean.getTid();
        String pid = postBean.getPid();
        String fid = postBean.getFid();
        String floor = postBean.getFloor();
        String subject = postBean.getSubject();
        String typeid = postBean.getTypeid();

        if (mInfo == null) {
            mInfo = new PrePostAsyncTask(mCtx, null, mMode).doInBackground(postBean);
        }
        if (!TextUtils.isEmpty(floor) && TextUtils.isDigitsOnly(floor))
            mFloor = floor;

        mContent = replyText;

        if (mMode != MODE_EDIT_POST) {
            String tail_text = HiSettingsHelper.getInstance().getTailText();
            if (!tail_text.isEmpty() && HiSettingsHelper.getInstance().isAddTail()) {
                String tail_url = HiSettingsHelper.getInstance().getTailUrl();
                if (!tail_url.isEmpty()) {
                    if ((!tail_url.startsWith("http")) && (!tail_url.startsWith("https"))) {
                        tail_url = "http://" + tail_url;
                    }
                    replyText = replyText + "  [url=" + tail_url + "][size=1]" + tail_text + "[/size][/url]";
                } else {
                    replyText = replyText + "  [size=1]" + tail_text + "[/size]";
                }
            }
        }

        String url = HiUtils.ReplyUrl + tid + "&replysubmit=yes";
        // do send
        switch (mMode) {
            case MODE_REPLY_THREAD:
            case MODE_QUICK_REPLY:
                doPost(url, replyText, null, null);
                break;
            case MODE_REPLY_POST:
            case MODE_QUOTE_POST:
                doPost(url, mInfo.getText() + "\n\n    " + replyText, null, null);
                break;
            case MODE_NEW_THREAD:
                url = HiUtils.NewThreadUrl + fid + "&typeid=" + typeid + "&topicsubmit=yes";
                doPost(url, replyText, subject, null);
                break;
            case MODE_EDIT_POST:
                url = HiUtils.EditUrl + "&extra=&editsubmit=yes&mod=&editsubmit=yes" + "&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";
                doPost(url, replyText, subject, typeid);
                break;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void avoid) {
        if (mStatus != Constants.STATUS_SUCCESS && !TextUtils.isEmpty(mContent)) {
            ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("AUTO SAVE FROM HiPDA", mContent);
            clipboard.setPrimaryClip(clip);
            mResult += "\n请注意：发表失败的内容已经复制到粘贴板";
        }
        PostBean postBean = new PostBean();
        postBean.setSubject(mTitle);
        postBean.setFloor(mFloor);
        postBean.setTid(mTid);
        if (mPostListenerCallback != null)
            mPostListenerCallback.onPostDone(mMode, mStatus, mResult, postBean);
    }

    private void doPost(String url, String replyText, String subject, String typeid) {

        String formhash = mInfo.getFormhash();

        if (TextUtils.isEmpty(formhash)) {
            mResult = "发表失败!";
            mStatus = Constants.STATUS_FAIL;
            return;
        }

        Map<String, String> post_param = new HashMap<String, String>();
        post_param.put("formhash", formhash);
        post_param.put("posttime", String.valueOf(System.currentTimeMillis()));
        post_param.put("wysiwyg", "0");
        post_param.put("checkbox", "0");
        post_param.put("message", replyText);
        for (String attach : mInfo.getAttaches()) {
            post_param.put("attachnew[" + attach + "][description]", attach);
        }
        for (String attach : mInfo.getAttachdel()) {
            post_param.put("attachdel[" + attach + "]", attach);
        }
        if (mMode == MODE_NEW_THREAD) {
            post_param.put("subject", subject);
            mTitle = subject;
        } else if (mMode == MODE_EDIT_POST) {
            if (!TextUtils.isEmpty(subject)) {
                post_param.put("subject", subject);
                mTitle = subject;
                if (!TextUtils.isEmpty(typeid)) {
                    post_param.put("typeid", typeid);
                }
            }
        }

        VolleyHelper.MyErrorListener errorListener = VolleyHelper.getInstance().getErrorListener();
        String rsp_str = VolleyHelper.getInstance().synchronousPost(url, post_param,
                errorListener);

        //when success, volley will follow 302 redirect get the page content
        if (!TextUtils.isEmpty(rsp_str)) {
            if (rsp_str.contains("tid = parseInt('")) {
                mTid = HttpUtils.getMiddleString(rsp_str, "tid = parseInt('", "'");
                mResult = "发表成功!";
                mStatus = Constants.STATUS_SUCCESS;
            } else {
                mResult = "发表失败!";
                Document doc = Jsoup.parse(rsp_str);
                Elements error = doc.select("div.alert_info");
                if (!error.isEmpty()) {
                    mResult += error.text();
                }
                mStatus = Constants.STATUS_FAIL;
            }
        } else {
            mResult = "发表失败! " + errorListener.getErrorText();
            mStatus = Constants.STATUS_FAIL;
        }

    }

    public interface PostListener {
        void onPrePost();

        void onPostDone(int mode, int status, String message, PostBean postBean);
    }
}
