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
    private String tid;
    private String postId;
    private int page;
    private int floor;
    private String uid;
    private String username;
    private boolean directOpen;

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isDirectOpen() {
        return directOpen;
    }

    public void setDirectOpen(boolean directOpen) {
        this.directOpen = directOpen;
    }
}
