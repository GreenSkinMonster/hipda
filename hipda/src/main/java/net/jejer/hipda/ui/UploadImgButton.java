package net.jejer.hipda.ui;

import android.content.Context;
import android.widget.ImageButton;

public class UploadImgButton extends ImageButton {

    private String mImgId;

    public UploadImgButton(Context context) {
        super(context);
    }

    public void setImgId(String imgId) {
        mImgId = imgId;
    }

    public String getImgId() {
        return mImgId;
    }

}
