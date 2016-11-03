package net.jejer.hipda.okhttp;

/**
 * Created by GreenSkinMonster on 2016-11-03.
 */

public interface ProgressListener {
    void update(String url, long bytesRead, long contentLength, boolean done);
}
