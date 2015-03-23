package net.jejer.hipda.bean;

public class ContentGoToFloor extends ContentAbs {
    private String text;
    private int floor;
    private String author;
    private String postId;

    public ContentGoToFloor(String text, String postId, int floor, String author) {
        this.text = text;
        this.floor = floor;
        this.author = author;
        this.postId = postId;
    }

    public int getFloor() {
        return floor;
    }

    public String getPostId() {
        return postId;
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
