package net.jejer.hipda.bean;

public class ContentGoToFloor extends ContentAbs {
    private String text;
    private int floor;

    public ContentGoToFloor(String text, int floor) {
        this.text = text;
        this.floor = floor;
    }

    public int getFloor() {
        return floor;
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
