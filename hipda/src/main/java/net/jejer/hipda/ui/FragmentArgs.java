package net.jejer.hipda.ui;

/**
 * arguments for start fragments
 * Created by GreenSkinMonster on 2015-09-01.
 */
public class FragmentArgs {

    public final static int TYPE_FORUM = 0;
    public final static int TYPE_THREAD = 1;
    public final static int TYPE_SPACE = 2;
    public final static int TYPE_SMS = 3;
    public final static int TYPE_THREAD_NOTIFY = 4;
    public final static int TYPE_SMS_DETAIL = 5;

    private int type;
    private int fid;
    private int tid;
    private int page;
    private int floor;
    private int postId;
    private String uid;
    private String author;

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
