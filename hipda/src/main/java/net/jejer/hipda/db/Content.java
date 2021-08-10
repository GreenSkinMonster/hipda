package net.jejer.hipda.db;

import android.text.TextUtils;

import net.jejer.hipda.utils.Utils;

import java.util.Date;

/**
 * Created by GreenSkinMonster on 2016-07-23.
 */
public class Content {

    private String mSessionId;
    private String mUsername;
    private long mTime;
    private String mContent;

    public Content(String sessionId, String username, String content, long time) {
        mContent = content;
        mSessionId = sessionId;
        mTime = time;
        mUsername = username;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void setSessionId(String sessionId) {
        mSessionId = sessionId;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getDesc() {
        return (TextUtils.isEmpty(mUsername) ? "" : "<" + mUsername + "> ") + "输入于 "
                + Utils.shortyTime(new Date(mTime))
                + "，共 " + Utils.getWordCount(mContent) + " 字";
    }

}
