package net.jejer.hipda.job;

import java.util.Collection;

/**
 * Created by GreenSkinMonster on 2016-04-01.
 */
public class ImageUploadEvent extends BaseEvent {

    public final static int UPLOADING = 0;
    public final static int ITEM_DONE = 1;
    public final static int ALL_DONE = 2;

    public Collection<ImageUploadEvent> holdEvents;

    public int mType;
    public int mTotal;
    public int mCurrent;
    public int mPercentage;
    public UploadImage mImage;

}
