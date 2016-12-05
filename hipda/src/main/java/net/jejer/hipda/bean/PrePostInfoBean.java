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
    private String formhash;
    private String uid;
    private String hash;
    private String subject;
    private String text;
    private String typeid;
    private String noticeauthor;
    private String noticeauthormsg;
    private String noticetrimstr;
    private List<String> attaches = new ArrayList<>();
    private List<String> attachdel = new ArrayList<>();
    private List<String> allImages = new ArrayList<>();
    private Map<String, String> typeValues = new LinkedHashMap<>();
    private boolean deleteable;

    public List<String> getAttachdel() {
        return attachdel;
    }

    public void addAttachdel(String attach) {
        if (!attachdel.contains(attach))
            attachdel.add(attach);
    }

    public List<String> getAttaches() {
        return attaches;
    }

    public String getFormhash() {
        return formhash;
    }

    public void setFormhash(String formhash) {
        this.formhash = formhash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTypeid() {
        return typeid;
    }

    public void setTypeid(String typeid) {
        this.typeid = typeid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void addAttach(String attach) {
        if (!attaches.contains(attach))
            attaches.add(attach);
    }

    public List<String> getAllImages() {
        return allImages;
    }

    public void addImage(String imgId) {
        if (!allImages.contains(imgId))
            allImages.add(imgId);
    }

    public String getNoticeauthor() {
        return noticeauthor;
    }

    public void setNoticeauthor(String noticeauthor) {
        this.noticeauthor = noticeauthor;
    }

    public String getNoticeauthormsg() {
        return noticeauthormsg;
    }

    public void setNoticeauthormsg(String noticeauthormsg) {
        this.noticeauthormsg = noticeauthormsg;
    }

    public String getNoticetrimstr() {
        return noticetrimstr;
    }

    public void setNoticetrimstr(String noticetrimstr) {
        this.noticetrimstr = noticetrimstr;
    }

    public Map<String, String> getTypeValues() {
        return typeValues;
    }

    public void setTypeValues(Map<String, String> typeValues) {
        this.typeValues = typeValues;
    }

    public boolean isDeleteable() {
        return deleteable;
    }

    public void setDeleteable(boolean deleteable) {
        this.deleteable = deleteable;
    }
}
