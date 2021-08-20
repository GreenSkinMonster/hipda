package net.jejer.hipda.bean;

import java.util.List;

/**
 * Created by GreenSkinMonster on 2021-08-20.
 */
public class PollBean {

    private String mTitle;
    private String mFooter;
    private List<PollOptionBean> mPollOptions;
    private int mMaxAnswer = 1;
    private String mFormhash;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getFooter() {
        return mFooter;
    }

    public void setFooter(String footer) {
        mFooter = footer;
    }

    public List<PollOptionBean> getPollOptions() {
        return mPollOptions;
    }

    public void setPollOptions(List<PollOptionBean> pollOptions) {
        mPollOptions = pollOptions;
    }

    public int getMaxAnswer() {
        return mMaxAnswer;
    }

    public void setMaxAnswer(int maxAnswer) {
        mMaxAnswer = maxAnswer;
    }

    public String getFormhash() {
        return mFormhash;
    }

    public void setFormhash(String formhash) {
        this.mFormhash = formhash;
    }

}
