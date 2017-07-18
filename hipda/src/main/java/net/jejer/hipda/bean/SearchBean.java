package net.jejer.hipda.bean;

import android.text.TextUtils;

import net.jejer.hipda.utils.Utils;

/**
 * Created by GreenSkinMonster on 2017-07-17.
 */

public class SearchBean {

    private String mQuery = "";
    private String mAuthor = "";
    private String mUid = "";
    private String mForum = "all";
    private String mSearchId = "";
    private boolean mFulltext;

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String query) {
        mQuery = Utils.nullToText(query);
    }

    public String getAuthor() {
        return Utils.nullToText(mAuthor);
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getForum() {
        return mForum;
    }

    public void setForum(String forum) {
        mForum = Utils.nullToText(forum);
    }

    public boolean isFulltext() {
        return mFulltext;
    }

    public void setFulltext(boolean fulltext) {
        mFulltext = fulltext;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = Utils.nullToText(uid);
    }

    public String getSearchId() {
        return mSearchId;
    }

    public void setSearchId(String searchId) {
        mSearchId = Utils.nullToText(searchId);
    }

    public SearchBean newCopy() {
        SearchBean bean = new SearchBean();
        bean.setAuthor(mAuthor);
        bean.setForum(mForum);
        bean.setFulltext(mFulltext);
        bean.setQuery(mQuery);
        bean.setUid(mUid);
        bean.setSearchId(mSearchId);
        return bean;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (isFulltext() && !TextUtils.isEmpty(getQuery())) {
            sb.append("全文：").append(getQuery());
        } else if (isFulltext()) {
            sb.append("全文");
        } else {
            sb.append(getQuery());
        }
        if (!TextUtils.isEmpty(getAuthor())) {
            if (sb.length() > 0)
                sb.append("，");
            sb.append("作者：").append(getAuthor());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchBean bean = (SearchBean) o;

        if (mFulltext != bean.mFulltext) return false;
        if (mQuery != null ? !mQuery.equals(bean.mQuery) : bean.mQuery != null) return false;
        if (mAuthor != null ? !mAuthor.equals(bean.mAuthor) : bean.mAuthor != null) return false;
        if (mUid != null ? !mUid.equals(bean.mUid) : bean.mUid != null) return false;
        if (mForum != null ? !mForum.equals(bean.mForum) : bean.mForum != null) return false;
        return mSearchId != null ? mSearchId.equals(bean.mSearchId) : bean.mSearchId == null;

    }

    @Override
    public int hashCode() {
        int result = mQuery != null ? mQuery.hashCode() : 0;
        result = 31 * result + (mAuthor != null ? mAuthor.hashCode() : 0);
        result = 31 * result + (mUid != null ? mUid.hashCode() : 0);
        result = 31 * result + (mForum != null ? mForum.hashCode() : 0);
        result = 31 * result + (mSearchId != null ? mSearchId.hashCode() : 0);
        result = 31 * result + (mFulltext ? 1 : 0);
        return result;
    }
}
