package net.jejer.hipda.async;

import android.content.Context;
import android.os.AsyncTask;

import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrePostAsyncTask extends AsyncTask<PostBean, Void, Map<String, List<String>>> {
    private final String LOG_TAG = getClass().getSimpleName();

    private PrePostListener mListener;
    private Context mCtx;
    private int mMode;

    public PrePostAsyncTask(Context ctx, PrePostListener listener, int mode) {
        mCtx = ctx;
        mListener = listener;
        mMode = mode;
    }

    @Override
    protected Map<String, List<String>> doInBackground(PostBean... postBeans) {

        PostBean postBean = postBeans[0];
        String tid = postBean.getTid();
        String pid = postBean.getPid();
        String fid = postBean.getFid();

        String url = HiUtils.ReplyUrl + tid;
        switch (mMode) {
            case PostAsyncTask.MODE_REPLY_THREAD:
            case PostAsyncTask.MODE_QUICK_REPLY:
                break;
            case PostAsyncTask.MODE_REPLY_POST:
                url += "&reppost=" + pid;
                break;
            case PostAsyncTask.MODE_QUOTE_POST:
                url += "&repquote=" + pid;
                break;
            case PostAsyncTask.MODE_NEW_THREAD:
                url = HiUtils.NewThreadUrl + fid;
                break;
            case PostAsyncTask.MODE_EDIT_POST:
                //fid is not really needed, just put a value here
                url = HiUtils.EditUrl + "&fid=" + fid + "&tid=" + tid + "&pid=" + pid + "&page=1";
                break;
        }

        String rsp_str;
        Boolean rspOk = false;
        int retry = 0;
        do {
            VolleyHelper.MyErrorListener errorListener = VolleyHelper.getInstance().getErrorListener();
            rsp_str = VolleyHelper.getInstance().synchronousGet(url, errorListener);
            if (rsp_str != null) {
                if (!LoginHelper.checkLoggedin(mCtx, rsp_str)) {
                    int status = new LoginHelper(mCtx, null).login();
                    if (status > Constants.STATUS_FAIL) {
                        break;
                    }
                } else {
                    rspOk = true;
                }
            }
            retry++;
        } while (!rspOk && retry < 3);

        if (!rspOk) {
            return null;
        }

        Document doc = Jsoup.parse(rsp_str);
        return parseRsp(doc);
    }

    private Map<String, List<String>> parseRsp(Document doc) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        result.put("formhash", new ArrayList<String>());
        result.put("text", new ArrayList<String>());
        result.put("uid", new ArrayList<String>());
        result.put("hash", new ArrayList<String>());
        result.put("attaches", new ArrayList<String>());
        result.put("subject", new ArrayList<String>());
        result.put("typeid_values", new ArrayList<String>());
        result.put("typeid_names", new ArrayList<String>());
        result.put("attachdel", new ArrayList<String>());

        Elements formhashES = doc.select("input[name=formhash]");
        if (formhashES.size() < 1) {
            return result;
        } else {
            result.get("formhash").add(formhashES.first().attr("value"));
        }

        Elements addtextES = doc.select("textarea");
        if (addtextES.size() < 1) {
            return result;
        } else {
            result.get("text").add(addtextES.first().text());
        }

        Elements scriptES = doc.select("script");
        if (scriptES.size() < 1) {
            return result;
        } else {
            result.get("uid").add(HttpUtils.getMiddleString(scriptES.first().data(), "discuz_uid = ", ","));
        }

        Elements hashES = doc.select("input[name=hash]");
        if (hashES.size() < 1) {
            return result;
        } else {
            result.get("hash").add(hashES.first().attr("value"));
        }

        //for edit post
        Elements subjectES = doc.select("input[name=subject]");
        if (subjectES.size() > 0) {
            result.get("subject").add(subjectES.first().attr("value"));
        }

        Elements typeidES = doc.select("#typeid > option");
        for (int i = 0; i < typeidES.size(); i++) {
            Element typeidEl = typeidES.get(i);
            result.get("typeid_values").add(typeidEl.val());
            result.get("typeid_names").add(typeidEl.text());
        }
        return result;
    }

    @Override
    protected void onPostExecute(Map<String, List<String>> result) {
        if (result == null) {
            mListener.PrePostComplete(mMode, false, null);
            return;
        }
        mListener.PrePostComplete(mMode, !result.get("formhash").isEmpty(), result);
    }

    public interface PrePostListener {
        void PrePostComplete(int mode, boolean result, Map<String, List<String>> info);
    }

}
