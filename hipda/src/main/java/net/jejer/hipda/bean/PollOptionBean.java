package net.jejer.hipda.bean;

/**
 * Created by GreenSkinMonster on 2021-08-20.
 */
public class PollOptionBean {

    private String mOptionId = "";
    private String mText = "";
    private String mRates = "";

    public String getOptionId() {
        return mOptionId;
    }

    public void setOptionId(String optionId) {
        mOptionId = optionId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getRates() {
        return mRates;
    }

    public void setRates(String rates) {
        mRates = rates;
    }

}
