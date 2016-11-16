package net.jejer.hipda.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.okhttp.NetworkError;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

public class PrePostAsyncTask extends AsyncTask<PostBean, Void, PrePostInfoBean> {

    private PrePostListener mListener;
    private Context mCtx;
    private int mMode;
    private String mMessage;

    private String mUrl;

    public PrePostAsyncTask(Context ctx, PrePostListener listener, int mode) {
        mCtx = ctx;
        mListener = listener;
        mMode = mode;
    }

    @Override
    protected PrePostInfoBean doInBackground(PostBean... postBeans) {

        PostBean postBean = postBeans[0];
        String tid = postBean.getTid();
        String pid = postBean.getPid();
        String fid = postBean.getFid();

        mUrl = HiUtils.ReplyUrl + tid;
        switch (mMode) {
            case PostHelper.MODE_REPLY_THREAD:
            case PostHelper.MODE_QUICK_REPLY:
                break;
            case PostHelper.MODE_REPLY_POST:
                mUrl += "&reppost=" + pid;
                break;
            case PostHelper.MODE_QUOTE_POST:
                mUrl += "&repquote=" + pid;
                break;
            case PostHelper.MODE_NEW_THREAD:
                mUrl = HiUtils.NewThreadUrl + fid;
                break;
            case PostHelper.MODE_EDIT_POST:
                //fid is not really needed, just put a value here
                mUrl = HiUtils.EditUrl + "&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";
                break;
        }


        for (int i = 0; i < OkHttpHelper.MAX_RETRY_TIMES; i++) {
            try {
                String resp = OkHttpHelper.getInstance().get(mUrl);
                if (resp != null) {
                    if (!LoginHelper.checkLoggedin(mCtx, resp)) {
                        int status = new LoginHelper(mCtx).login();
                        if (status == Constants.STATUS_FAIL_ABORT) {
                            break;
                        }
                    } else {
                        Document doc = Jsoup.parse(resp);
                        return parseRsp(doc);
                    }
                }
            } catch (Exception e) {
                NetworkError message = OkHttpHelper.getErrorMessage(e);
                mMessage = message.getMessage();
            }
        }

        return null;
    }

    private PrePostInfoBean parseRsp(Document doc) {
        PrePostInfoBean result = new PrePostInfoBean();

        Elements formhashES = doc.select("input[name=formhash]");
        if (formhashES.size() < 1) {
            mMessage = "页面解析错误";
            return result;
        } else {
            result.setFormhash(formhashES.first().attr("value"));
        }

        Elements addtextES = doc.select("textarea");
        if (addtextES.size() < 1) {
            return result;
        } else {
            result.setText(addtextES.first().text());
        }

        Elements scriptES = doc.select("script");
        if (scriptES.size() < 1) {
            return result;
        } else {
            result.setUid(HttpUtils.getMiddleString(scriptES.first().data(), "discuz_uid = ", ","));
        }

        Elements hashES = doc.select("input[name=hash]");
        if (hashES.size() < 1) {
            return result;
        } else {
            result.setHash(hashES.first().attr("value"));
        }

        //for edit post
        Elements subjectES = doc.select("input[name=subject]");
        if (subjectES.size() > 0) {
            result.setSubject(subjectES.first().attr("value"));
        }

        Elements deleteCheckBox = doc.select("input#delete");
        if (deleteCheckBox.size() > 0) {
            result.setDeleteable(true);
        }

        //for replay or quote notify
        if (mMode == PostHelper.MODE_REPLY_POST
                || mMode == PostHelper.MODE_QUOTE_POST) {
            Elements authorES = doc.select("input[name=noticeauthor]");
            if (authorES.size() > 0)
                result.setNoticeauthor(authorES.first().attr("value"));
            Elements authorMsgES = doc.select("input[name=noticeauthormsg]");
            if (authorMsgES.size() > 0)
                result.setNoticeauthormsg(authorMsgES.first().attr("value"));
            Elements noticeTrimES = doc.select("input[name=noticetrimstr]");
            if (noticeTrimES.size() > 0)
                result.setNoticetrimstr(noticeTrimES.first().attr("value"));
        }

        Elements unusedImagesES = doc.select("div#unusedimgattachlist table.imglist img");
        for (int i = 0; i < unusedImagesES.size(); i++) {
            Element imgE = unusedImagesES.get(i);
            String href = Utils.nullToText(imgE.attr("src"));
            String imgId = Utils.nullToText(imgE.attr("id"));
            if (href.startsWith("attachments/") && imgId.contains("_")) {
                imgId = imgId.substring(imgId.lastIndexOf("_") + 1);
                if (imgId.length() > 0 && TextUtils.isDigitsOnly(imgId)) {
                    result.addUnusedImage(imgId);
                }
            }
        }

        Elements typeidES = doc.select("#typeid > option");
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < typeidES.size(); i++) {
            Element typeidEl = typeidES.get(i);
            values.put(typeidEl.val(), typeidEl.text());
            if (i == 0 || "selected".equals(typeidEl.attr("selected")))
                result.setTypeid(typeidEl.val());
        }
        result.setTypeValues(values);
        return result;
    }

    @Override
    protected void onPostExecute(PrePostInfoBean info) {
        if (info != null && !TextUtils.isEmpty(info.getFormhash()))
            mListener.PrePostComplete(mMode, true, null, info);
        else
            mListener.PrePostComplete(mMode, false, mMessage, null);
    }

    public interface PrePostListener {
        void PrePostComplete(int mode, boolean result, String message, PrePostInfoBean info);
    }

}
