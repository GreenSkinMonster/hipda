package net.jejer.hipda.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class PostAsyncTask extends AsyncTask<PostBean, Void, Void> {

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

        int count = 0;
        while (mInfo == null && count < 3) {
            count++;
            mInfo = new PrePostAsyncTask(mCtx, null, mMode).doInBackground(postBean);
        }

        if (!TextUtils.isEmpty(floor) && TextUtils.isDigitsOnly(floor))
            mFloor = floor;

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
        PostBean postBean = new PostBean();
        postBean.setSubject(mTitle);
        postBean.setFloor(mFloor);
        postBean.setTid(mTid);
        if (mPostListenerCallback != null)
            mPostListenerCallback.onPostDone(mMode, mStatus, mResult, postBean);
    }

    private void doPost(String url, String replyText, String subject, String typeid) {

        String formhash = mInfo != null ? mInfo.getFormhash() : null;

        if (TextUtils.isEmpty(formhash)) {
            mResult = "发表失败，无法获取必要信息 ！";
            mStatus = Constants.STATUS_FAIL;
            return;
        }

        Map<String, String> post_param = new HashMap<>();
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
        for (String attach : mInfo.getUnusedImages()) {
            post_param.put("attachdel[" + attach + "]", attach);
        }
        if (mMode == MODE_NEW_THREAD) {
            post_param.put("subject", subject);
            post_param.put("attention_add", "1");
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

        if (mMode == MODE_QUOTE_POST
                || mMode == MODE_REPLY_POST) {
            String noticeauthor = mInfo.getNoticeauthor();
            String noticeauthormsg = mInfo.getNoticeauthormsg();
            String noticetrimstr = mInfo.getNoticetrimstr();
            if (!TextUtils.isEmpty(noticeauthor)) {
                post_param.put("noticeauthor", noticeauthor);
                post_param.put("noticeauthormsg", Utils.nullToText(noticeauthormsg));
                post_param.put("noticetrimstr", Utils.nullToText(noticetrimstr));
            }
        }

        String rsp_str;
        try {
            rsp_str = OkHttpHelper.getInstance().post(url, post_param);

            //when success, okhttp will follow 302 redirect get the page content
            if (!TextUtils.isEmpty(rsp_str)) {
                String tid = "";
                if (rsp_str.contains("tid = parseInt('")) {
                    tid = HttpUtils.getMiddleString(rsp_str, "tid = parseInt('", "'");
                }
                if (!TextUtils.isEmpty(tid)
                        && TextUtils.isDigitsOnly(tid)
                        && Integer.parseInt(tid) > 0
                        && !rsp_str.contains("alert_info")) {
                    mTid = tid;
                    mResult = "发表成功!";
                    mStatus = Constants.STATUS_SUCCESS;
                } else {
                    Logger.e(rsp_str);
                    mResult = "发表失败! ";
                    mStatus = Constants.STATUS_FAIL;

                    Document doc = Jsoup.parse(rsp_str);
                    Elements error = doc.select("div.alert_info");
                    if (error != null && error.size() > 0) {
                        mResult += "\n" + error.text();
                    }
                }
            } else {
                mResult = "发表失败，无返回结果! ";
                mStatus = Constants.STATUS_FAIL;
            }

        } catch (Exception e) {
            Logger.e(e);
            mResult = "发表失败 : " + OkHttpHelper.getErrorMessage(e);
            mStatus = Constants.STATUS_FAIL;
        }

    }

    public interface PostListener {
        void onPrePost();

        void onPostDone(int mode, int status, String message, PostBean postBean);
    }
}
