package net.jejer.hipda.job;

import android.content.Context;
import android.text.TextUtils;

import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.bean.SearchBean;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.db.History;
import net.jejer.hipda.db.HistoryDao;
import net.jejer.hipda.okhttp.NetworkError;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by GreenSkinMonster on 2016-11-16.
 */

public class SimpleListJob extends BaseJob {

    public static final int TYPE_MYREPLY = 0;
    public static final int TYPE_MYPOST = 1;
    public static final int TYPE_SEARCH = 2;
    public static final int TYPE_SMS = 3;
    public static final int TYPE_THREAD_NOTIFY = 4;
    public static final int TYPE_SMS_DETAIL = 5;
    public static final int TYPE_FAVORITES = 6;
    public static final int TYPE_SEARCH_USER_THREADS = 7;
    public static final int TYPE_ATTENTION = 8;
    public static final int TYPE_HISTORIES = 9;
    public static final int TYPE_NEW_POSTS = 10;

    private Context mCtx;
    private int mType;
    private int mPage = 1;
    private String mExtra = "";
    private SearchBean mSearchBean;

    private SimpleListEvent mEvent;

    public SimpleListJob(Context context, String sessionId, int type, int page, String extra) {
        super(sessionId);
        mCtx = context;
        mType = type;
        mPage = page;
        mExtra = extra;

        mEvent = new SimpleListEvent();
        mEvent.mSessionId = mSessionId;
        mEvent.mPage = page;
        mEvent.mType = mType;
        mEvent.mExtra = mExtra;
    }

    public SimpleListJob(Context context, String sessionId, int type, int page, SearchBean searchBean) {
        super(sessionId);
        mCtx = context;
        mType = type;
        mPage = page;
        mSearchBean = searchBean;

        mEvent = new SimpleListEvent();
        mEvent.mSessionId = mSessionId;
        mEvent.mPage = page;
        mEvent.mType = mType;
        mEvent.mExtra = mExtra;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        SimpleListBean data = null;

        int eventStatus = Constants.STATUS_SUCCESS;
        String eventMessage = "";
        String eventDetail = "";
        String formhash = "";

        if (mType == TYPE_HISTORIES) {
            data = new SimpleListBean();
            List<History> histories = HistoryDao.getHistories();
            for (History history : histories) {
                SimpleListItemBean bean = new SimpleListItemBean();
                String forumName = "";
                if (!TextUtils.isEmpty(history.getFid()) && TextUtils.isDigitsOnly(history.getFid()))
                    forumName = HiUtils.getForumNameByFid(Integer.parseInt(history.getFid()));
                bean.setTid(history.getTid());
                bean.setUid(history.getUid());
                bean.setTitle(history.getTitle());
                bean.setAuthor(history.getUsername());
                bean.setTime(history.getPostTime());
                bean.setAvatarUrl(HiUtils.getAvatarUrlByUid(history.getUid()));
                bean.setForum(forumName);
                data.add(bean);
            }
        } else {
            try {
                Document doc = Jsoup.parse(fetchSimpleList(mType));
                boolean loggedin = LoginHelper.checkLoggedin(doc);
                if (!loggedin) {
                    int status = new LoginHelper().login();
                    if (status == Constants.STATUS_FAIL_ABORT) {
                        eventStatus = Constants.STATUS_FAIL_RELOGIN;
                        eventMessage = "请重新登录";
                    } else if (status == Constants.STATUS_SUCCESS) {
                        doc = Jsoup.parse(fetchSimpleList(mType));
                        loggedin = LoginHelper.checkLoggedin(doc);
                    }
                }
                if (loggedin) {
                    data = HiParser.parseSimpleList(mCtx, mType, doc, (mType == TYPE_SEARCH && mSearchBean != null && mSearchBean.isFulltext()));
                    formhash = HiParser.parseFormhash(doc);
                    eventStatus = Constants.STATUS_SUCCESS;
                }
            } catch (Exception e) {
                NetworkError message = OkHttpHelper.getErrorMessage(e);
                eventStatus = Constants.STATUS_FAIL;
                eventMessage = message.getMessage();
                eventDetail = message.getDetail();
            }
        }

        mEvent.mData = data;
        mEvent.mStatus = eventStatus;
        mEvent.mMessage = eventMessage;
        mEvent.mDetail = eventDetail;
        mEvent.mFormhash = formhash;
        EventBus.getDefault().postSticky(mEvent);
    }

    private String fetchSimpleList(int type) throws Exception {
        String url = null;
        switch (type) {
            case TYPE_MYREPLY:
                url = HiUtils.MyReplyUrl + "&page=" + mPage;
                break;
            case TYPE_MYPOST:
                url = HiUtils.MyPostUrl + "&page=" + mPage;
                break;
            case TYPE_SMS:
                url = HiUtils.SMSUrl;
                break;
            case TYPE_THREAD_NOTIFY:
                url = HiUtils.ThreadNotifyUrl;
                break;
            case TYPE_SMS_DETAIL:
                url = HiUtils.SMSDetailUrl + mExtra;
                break;
            case TYPE_NEW_POSTS:
                //mExtra is searchid
                if (TextUtils.isEmpty(mExtra)) {
                    url = HiUtils.NewPostsUrl;
                } else {
                    url = HiUtils.SearchByIdUrl.replace("{searchid}", mExtra);
                }
                if (mPage > 1)
                    url += "&page=" + mPage;
                break;
            case TYPE_SEARCH:
                try {
                    url = HiUtils.SearchUrl
                            .replace("{srchtype}", mSearchBean.isFulltext() ? "fulltext" : "title")
                            .replace("{srchtxt}", URLEncoder.encode(mSearchBean.getQuery(), "GBK"))
                            .replace("{srchuname}", URLEncoder.encode(mSearchBean.getAuthor(), "GBK"))
                            .replace("{fid}", mSearchBean.getForum());
                    if (mPage > 1)
                        url += "&page=" + mPage;
                } catch (UnsupportedEncodingException e) {
                    Logger.e("Encoding error", e);
                }
                break;
            case TYPE_SEARCH_USER_THREADS:
                if (TextUtils.isEmpty(mSearchBean.getSearchId())) {
                    url = HiUtils.SearchUserThreads.replace("{srchuid}", mSearchBean.getUid());
                } else {
                    url = HiUtils.SearchByIdUrl.replace("{searchid}", mSearchBean.getSearchId());
                }
                if (mPage > 1)
                    url += "&page=" + mPage;
                break;
            case TYPE_FAVORITES:
                url = HiUtils.FavoritesUrl;
                url = url.replace("{item}", FavoriteHelper.TYPE_FAVORITE);
                if (mPage > 1)
                    url += "&page=" + mPage;
                break;
            case TYPE_ATTENTION:
                url = HiUtils.FavoritesUrl;
                url = url.replace("{item}", FavoriteHelper.TYPE_ATTENTION);
                if (mPage > 1)
                    url += "&page=" + mPage;
                break;
            default:
                break;
        }
        return OkHttpHelper.getInstance().get(url, mSessionId);
    }
}
