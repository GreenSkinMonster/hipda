package net.jejer.hipda.job;

import android.graphics.Bitmap;
import android.net.Uri;

import net.jejer.hipda.async.UploadImgHelper;
import net.jejer.hipda.ui.HiApplication;

import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

/**
 * Created by GreenSkinMonster on 2016-04-01.
 */
public class ImageUploadJob extends BaseJob implements UploadImgHelper.UploadImgListener {

    private String mUid;
    private String mHash;
    private Uri[] mUris;
    private Collection<ImageUploadEvent> mHoldEvents = new ArrayList<>();

    public ImageUploadJob(String sessionId, String uid, String hash, Uri[] uris) {
        super(sessionId);
        mUid = uid;
        mHash = hash;
        mUris = uris;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        new UploadImgHelper(getApplicationContext(), this, mUid, mHash, mUris).upload();

        ImageUploadEvent event = new ImageUploadEvent();
        event.mSessionId = mSessionId;
        event.type = ImageUploadEvent.ALL_DONE;
        event.holdEvents = mHoldEvents;
        EventBus.getDefault().postSticky(event);
    }

    @Override
    protected void onCancel() {

    }

    @Override
    public void updateProgress(int total, int current, int percentage) {
        ImageUploadEvent event = new ImageUploadEvent();
        event.mSessionId = mSessionId;
        event.type = ImageUploadEvent.UPLOADING;
        event.total = total;
        event.current = current;
        event.percentage = percentage;
        EventBus.getDefault().post(event);
    }

    @Override
    public void itemComplete(Uri uri, int total, int current, String currentFileName, String message, String imgId, Bitmap thumbtail) {
        ImageUploadEvent event = new ImageUploadEvent();
        event.mSessionId = mSessionId;
        event.type = ImageUploadEvent.ITEM_DONE;
        event.uri = uri;
        event.total = total;
        event.current = current;
        event.currentFileName = currentFileName;
        event.message = message;
        event.thumbtail = thumbtail;
        event.imgId = imgId;
        if (HiApplication.isActivityVisible()) {
            EventBus.getDefault().post(event);
        } else {
            mHoldEvents.add(event);
        }
    }

}
