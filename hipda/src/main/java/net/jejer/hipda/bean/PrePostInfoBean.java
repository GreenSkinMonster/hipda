package net.jejer.hipda.bean;

import java.util.ArrayList;
import java.util.List;

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
    private List<String> typeidValues = new ArrayList<>();
    private List<String> typeidNames = new ArrayList<>();
    private List<String> attachdel = new ArrayList<>();
    private List<String> unusedImages = new ArrayList<>();

    public List<String> getAttachdel() {
        return attachdel;
    }

    public void setAttachdel(List<String> attachdel) {
        this.attachdel = attachdel;
    }

    public List<String> getAttaches() {
        return attaches;
    }

    public void setAttaches(List<String> attaches) {
        this.attaches = attaches;
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

    public List<String> getTypeidNames() {
        return typeidNames;
    }

    public void setTypeidNames(List<String> typeidNames) {
        this.typeidNames = typeidNames;
    }

    public List<String> getTypeidValues() {
        return typeidValues;
    }

    public void setTypeidValues(List<String> typeidValues) {
        this.typeidValues = typeidValues;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void addTypeidValues(String value) {
        typeidValues.add(value);
    }

    public void addTypeidNames(String name) {
        typeidNames.add(name);
    }

    public void addAttach(String attach) {
        attaches.add(attach);
    }

    public void removeAttach(String attach) {
        attaches.remove(attach);
    }

    public void addAttachdel(String attach) {
        attachdel.add(attach);
    }

    public List<String> getUnusedImages() {
        return unusedImages;
    }

    public void setUnusedImages(List<String> unusedImages) {
        this.unusedImages = unusedImages;
    }

    public void addUnusedImage(String imgId) {
        unusedImages.add(imgId);
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
}
