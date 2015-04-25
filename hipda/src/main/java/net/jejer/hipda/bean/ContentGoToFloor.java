package net.jejer.hipda.bean;

public class ContentGoToFloor extends ContentAbs {
    private String text;
    private int floor;
    private String author;
    private String postId;
    private String tid;

    public ContentGoToFloor(String text, String tid, String postId, int floor, String author) {
        this.text = text;
        this.floor = floor;
        this.author = author;
        this.postId = postId;
        this.tid = tid;
    }

    public int getFloor() {
        return floor;
    }

    public String getPostId() {
        return postId;
    }

    public String getTid() {
        return tid;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String getContent() {
        return text;
    }

    @Override
    public String getCopyText() {
        return text;
    }
}
