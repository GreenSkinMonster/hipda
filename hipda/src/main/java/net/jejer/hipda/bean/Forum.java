package net.jejer.hipda.bean;

import com.mikepenz.iconics.typeface.IIcon;

/**
 * Created by GreenSkinMonster on 2016-07-21.
 */
public class Forum {
    private String mName;
    private int mId;
    private IIcon mIcon;

    public Forum(int id, String name, IIcon icon) {
        mIcon = icon;
        mId = id;
        mName = name;
    }

    public IIcon getIcon() {
        return mIcon;
    }

    public void setIcon(IIcon icon) {
        mIcon = icon;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

}
