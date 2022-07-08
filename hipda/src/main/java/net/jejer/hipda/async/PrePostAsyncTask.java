package net.jejer.hipda.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.okhttp.NetworkError;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Response;

public class PrePostAsyncTask extends AsyncTask<PostBean, Void, PrePostInfoBean> {

    private PrePostListener mListener;
    private Context mCtx;
    private int mMode;
    private String mMessage;

    public PrePostAsyncTask(Context ctx, PrePostListener listener, int mode) {
        mCtx = ctx;
        mListener = listener;
        mMode = mode;
    }

    @Override
    public PrePostInfoBean doInBackground(PostBean... postBeans) {

        PostBean postBean = postBeans[0];
        String tid = postBean.getTid();
        String pid = postBean.getPid();
        int fid = postBean.getFid();

        String url = HiUtils.ReplyUrl + tid;
        switch (mMode) {
            case PostHelper.MODE_REPLY_THREAD:
            case PostHelper.MODE_QUICK_REPLY:
                break;
            case PostHelper.MODE_REPLY_POST:
                url += "&reppost=" + pid;
                break;
            case PostHelper.MODE_QUOTE_POST:
                url += "&repquote=" + pid;
                break;
            case PostHelper.MODE_NEW_THREAD:
                url = HiUtils.NewThreadUrl + fid;
                break;
            case PostHelper.MODE_EDIT_POST:
                //fid is not really needed, just put a value here
                url = HiUtils.EditUrl + "&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";
                break;
        }


        try {
            Response respObj = OkHttpHelper.getInstance().getAsResponse(url);
            if (respObj.isSuccessful()) {
                if (respObj.request().url().toString().contains("memcp.php?action=bind")) {
                    mMessage = "需要通过网页完成实名验证才可以发帖";
                } else {
                    String resp = respObj.body().string();
                    Document doc = Jsoup.parse(resp);
                    return parseRsp(doc);
                }
            }
        } catch (Exception e) {
            NetworkError message = OkHttpHelper.getErrorMessage(e);
            mMessage = message.getMessage();
        }
        return null;
    }

    private PrePostInfoBean parseRsp(Document doc) {
        PrePostInfoBean prePostInfo = new PrePostInfoBean();

        Elements formhashES = doc.select("input[name=formhash]");
        if (formhashES.size() < 1) {
            mMessage = HiParser.parseErrorMessage(doc);
            if (TextUtils.isEmpty(mMessage))
                mMessage = "页面解析错误";
            return prePostInfo;
        } else {
            prePostInfo.setFormhash(formhashES.first().attr("value"));
        }

        Element addtextEl = doc.select("textarea").first();
        if (addtextEl == null) {
            return prePostInfo;
        }
        if (mMode == PostHelper.MODE_REPLY_POST
                || mMode == PostHelper.MODE_QUOTE_POST) {
            prePostInfo.setQuoteText(addtextEl.text());
        } else {
            prePostInfo.setText(addtextEl.text());
        }

        Elements scriptES = doc.select("script");
        if (scriptES.size() < 1) {
            return prePostInfo;
        } else {
            prePostInfo.setUid(Utils.getMiddleString(scriptES.first().data(), "discuz_uid = ", ","));
        }

        Elements hashES = doc.select("input[name=hash]");
        if (hashES.size() < 1) {
            return prePostInfo;
        } else {
            prePostInfo.setHash(hashES.first().attr("value"));
        }

        //for edit post
        Elements subjectES = doc.select("input[name=subject]");
        if (subjectES.size() > 0) {
            prePostInfo.setSubject(subjectES.first().attr("value"));
        }

        Elements deleteCheckBox = doc.select("input#delete");
        if (deleteCheckBox.size() > 0) {
            prePostInfo.setDeleteable(true);
        }

        Elements uploadInfoES = doc.select("div.uploadinfo");
        if (uploadInfoES.size() > 0) {
            String uploadInfo = uploadInfoES.first().text();
            if (uploadInfo.contains("文件尺寸")) {
                String sizeText = Utils.getMiddleString(uploadInfo.toUpperCase(), "小于", "B").trim();
                //sizeText : 100KB 8MB
                try {
                    float size = Float.parseFloat(sizeText.substring(0, sizeText.length() - 1));
                    String unit = sizeText.substring(sizeText.length() - 1);
                    if (size > 0) {
                        int maxFileSize = 0;
                        if ("K".equals(unit)) {
                            maxFileSize = (int) (size * 1024);
                        } else if ("M".equals(unit)) {
                            maxFileSize = (int) (size * 1024 * 1024);
                        }
                        if (maxFileSize > 1024)
                            HiSettingsHelper.getInstance().setMaxUploadFileSize(maxFileSize);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        //for replay or quote notify
        if (mMode == PostHelper.MODE_REPLY_POST
                || mMode == PostHelper.MODE_QUOTE_POST) {
            Elements authorES = doc.select("input[name=noticeauthor]");
            if (authorES.size() > 0)
                prePostInfo.setNoticeAuthor(authorES.first().attr("value"));
            Elements authorMsgES = doc.select("input[name=noticeauthormsg]");
            if (authorMsgES.size() > 0)
                prePostInfo.setNoticeAuthorMsg(authorMsgES.first().attr("value"));
            Elements noticeTrimES = doc.select("input[name=noticetrimstr]");
            if (noticeTrimES.size() > 0)
                prePostInfo.setNoticeTrimStr(noticeTrimES.first().attr("value"));
        }

        Elements unusedImagesES = doc.select("div#unusedimgattachlist table.imglist img");
        for (int i = 0; i < unusedImagesES.size(); i++) {
            Element imgE = unusedImagesES.get(i);
            String href = Utils.nullToText(imgE.attr("src"));
            String imgId = Utils.nullToText(imgE.attr("id"));
            if (href.contains("attachments/") && imgId.contains("_")) {
                imgId = imgId.substring(imgId.lastIndexOf("_") + 1);
                if (imgId.length() > 0 && TextUtils.isDigitsOnly(imgId)) {
                    prePostInfo.addImage(imgId);
                }
            }
        }

        //uploaded image list
        Elements uploadedImagesES = doc.select("div.upfilelist img[id^=image_]");
        for (int i = 0; i < uploadedImagesES.size(); i++) {
            Element imgE = uploadedImagesES.get(i);
            String imgId = Utils.nullToText(imgE.attr("id"));
            imgId = imgId.substring("image_".length());
            if (imgId.length() > 0 && TextUtils.isDigitsOnly(imgId)) {
                prePostInfo.addImage(imgId);
            }
        }

        //image as attachments
        Elements attachmentImages = doc.select("div.upfilelist span a");
        for (int i = 0; i < attachmentImages.size(); i++) {
            Element aTag = attachmentImages.get(i);
            String href = Utils.nullToText(aTag.attr("href"));
            String onclick = Utils.nullToText(aTag.attr("onclick"));
            if (href.startsWith("javascript")) {
                if (onclick.startsWith("insertAttachimgTag")) {
                    //<a href="javascript:;" class="lighttxt" onclick="insertAttachimgTag('2810014')" title="...">Hi_160723_2240.jpg</a>
                    String imgId = Utils.getMiddleString(onclick, "insertAttachimgTag('", "'");
                    if (!TextUtils.isEmpty(imgId) && TextUtils.isDigitsOnly(imgId)) {
                        prePostInfo.addImage(imgId);
                    }
                } else if (onclick.startsWith("insertAttachTag")) {
                    String attachId = Utils.getMiddleString(onclick, "insertAttachTag('", "'");
                    if (!TextUtils.isEmpty(attachId) && TextUtils.isDigitsOnly(attachId)) {
                        prePostInfo.addAttach(attachId);
                    }
                }
            }
        }

        Elements typeidES = doc.select("#typeid > option");
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < typeidES.size(); i++) {
            Element typeidEl = typeidES.get(i);
            values.put(typeidEl.val(), typeidEl.text());
            if (i == 0 || "selected".equals(typeidEl.attr("selected")))
                prePostInfo.setTypeId(typeidEl.val());
        }
        prePostInfo.setTypeValues(values);
        return prePostInfo;
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

    public String getMessage() {
        return mMessage;
    }

}
