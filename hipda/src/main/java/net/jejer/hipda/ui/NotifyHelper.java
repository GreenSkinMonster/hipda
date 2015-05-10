package net.jejer.hipda.ui;

import android.app.Activity;


public class NotifyHelper {
    private Activity mActivity;
    private int mCntSMS = 0;
    private int mCntThread = 0;

    public void init(Activity activity) {
        mActivity = activity;
    }

    private NotifyHelper() {
    }

    private static class SingletonHolder {
        public static final NotifyHelper INSTANCE = new NotifyHelper();
    }

    public static NotifyHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public int getCntSMS() {
        return mCntSMS;
    }

    public void setCntSMS(int cntSMS) {
        mCntSMS = cntSMS;
    }

    public int getCntThread() {
        return mCntThread;
    }

    public void setCntThread(int cntThread) {
        mCntThread = cntThread;
    }

    // Call in the thread which created the drawer
    public void updateDrawer() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainFrameActivity) mActivity).onNotification();
            }
        });
    }
}
