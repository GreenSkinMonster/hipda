package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * bean for getting pre post info
 * Created by GreenSkinMonster on 2015-04-15.
 */
public class PrePostInfoBean {
    private String mFormhash;
    private String mUid;
    private String mHash;
    private String mSubject;
    private String mText = "";
    private String mQuoteText = "";
    private String mTypeId;
    private String mNoticeAuthor;
    private String mNoticeAuthorMsg;
    private String mNoticeTrimStr;
    private List<String> mAttaches = new ArrayList<>(0);
    private List<String> mNewAttaches = new ArrayList<>(0);
    private List<String> mDeleteAttaches = new ArrayList<>(0);
    private List<String> mAllImages = new ArrayList<>(0);
    private Map<String, String> mTypeValues = new LinkedHashMap<>();
    private boolean mDeleteable;

    public List<String> getDeleteAttaches() {
        return mDeleteAttaches;
    }

    public void addDeleteAttach(String attach) {
        if (!mDeleteAttaches.contains(attach))
            mDeleteAttaches.add(attach);
    }

    public List<String> getNewAttaches() {
        return mNewAttaches;
    }

    public void addNewAttach(String attach) {
        if (!mNewAttaches.contains(attach))
            mNewAttaches.add(attach);
    }

    public List<String> getAttaches() {
        return mAttaches;
    }

    public void addAttach(String attach) {
        if (!mAttaches.contains(attach))
            mAttaches.add(attach);
    }

    public String getFormhash() {
        return mFormhash;
    }

    public void setFormhash(String formhash) {
        this.mFormhash = formhash;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        this.mHash = hash;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public String getQuoteText() {
        return mQuoteText;
    }

    public void setQuoteText(String quoteText) {
        mQuoteText = quoteText;
    }

    public String getTypeId() {
        return mTypeId;
    }

    public void setTypeId(String typeId) {
        this.mTypeId = typeId;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public List<String> getAllImages() {
        return mAllImages;
    }

    public void addImage(String imgId) {
        if (!mAllImages.contains(imgId))
            mAllImages.add(imgId);
    }

    public String getNoticeAuthor() {
        return mNoticeAuthor;
    }

    public void setNoticeAuthor(String noticeAuthor) {
        this.mNoticeAuthor = noticeAuthor;
    }

    public String getNoticeAuthorMsg() {
        return mNoticeAuthorMsg;
    }

    public void setNoticeAuthorMsg(String noticeAuthorMsg) {
        this.mNoticeAuthorMsg = noticeAuthorMsg;
    }

    public String getNoticeTrimStr() {
        return mNoticeTrimStr;
    }

    public void setNoticeTrimStr(String noticeTrimStr) {
        this.mNoticeTrimStr = noticeTrimStr;
    }

    public Map<String, String> getTypeValues() {
        return mTypeValues;
    }

    public void setTypeValues(Map<String, String> typeValues) {
        this.mTypeValues = typeValues;
    }

    public boolean isDeleteable() {
        return mDeleteable;
    }

    public void setDeleteable(boolean deleteable) {
        this.mDeleteable = deleteable;
    }
}
