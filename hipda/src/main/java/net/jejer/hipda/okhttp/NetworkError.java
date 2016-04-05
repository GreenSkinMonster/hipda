package net.jejer.hipda.okhttp;

/**
 * Created by GreenSkinMonster on 2016-04-05.
 */
public class NetworkError {
    private String message;
    private String detail;

    NetworkError(String message, String detail) {
        this.message = message;
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
