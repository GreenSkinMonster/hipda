package net.jejer.hipda.bean;

public class ContentGoToFloor extends ContentAbs {
    private String text;
    private int floor;
    private String author;

    public ContentGoToFloor(String text, int floor, String author) {
        this.text = text;
        this.floor = floor;
        this.author = author;
    }

    public int getFloor() {
        return floor;
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
