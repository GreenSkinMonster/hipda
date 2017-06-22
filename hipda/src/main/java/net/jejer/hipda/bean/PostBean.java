package net.jejer.hipda.bean;

/**
 * Used for post arguments
 * Created by GreenSkinMonster on 2015-03-14.
 */
public class PostBean {

    private String tid;
    private String pid;
    private int fid;
    private int floor;
    private String subject;
    private String content;
    private String typeid;
    private int status;
    private String message;
    private boolean delete;
    private DetailListBean detailListBean;

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTypeid() {
        return typeid;
    }

    public void setTypeid(String typeid) {
        this.typeid = typeid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public DetailListBean getDetailListBean() {
        return detailListBean;
    }

    public void setDetailListBean(DetailListBean detailListBean) {
        this.detailListBean = detailListBean;
    }
}
