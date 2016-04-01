package net.jejer.hipda.job;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Collection;

/**
 * Created by GreenSkinMonster on 2016-04-01.
 */
public class ImageUploadEvent extends BaseEvent {

    public final static int UPLOADING = 0;
    public final static int ITEM_DONE = 1;
    public final static int ALL_DONE = 2;

    public Collection<ImageUploadEvent> holdEvents;

    public int type;
    public Uri uri;
    public int total;
    public int current;
    public String currentFileName;
    public int percentage;
    public String message;
    public String imgId;
    public Bitmap thumbtail;

}
