package net.jejer.hipda.ui;

import android.app.Activity;
import android.widget.TextView;


public class NotifyHelper {
    private static final int NOTIFICATION_ID = 0;
    private Activity mActivity;
    private int mCntSMS = 0;
    private int mCntThread = 0;

    private TextView mViewSmsItemTextView;
    private TextView mViewThreadItemTextView;

    public void initSmsItemTextView(TextView smsItemTextView) {
        mViewSmsItemTextView = smsItemTextView;
    }

    public void initThreadItemTextView(TextView threadItemTextView) {
        mViewThreadItemTextView = threadItemTextView;
    }

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

                if (mCntSMS > 0) {
                    mViewSmsItemTextView.setText("短消息 (" + mCntSMS + ")");
                } else {
                    mViewSmsItemTextView.setText("短消息");
                }

                if (mCntThread > 0) {
                    mViewThreadItemTextView.setText("帖子消息 (" + mCntThread + ")");
                } else {
                    mViewThreadItemTextView.setText("帖子消息");
                }
            }
        });


//        if (mCntSMS + mCntThread > 0) {
//            Notification.Builder mBuilder =
//                    new Notification.Builder(mActivity)
//                            .setSmallIcon(R.drawable.ic_stat_hi)
//                            .setContentTitle("您有新短消息或帖子通知")
//                            .setContentText("短消息(" + String.valueOf(mCntSMS) + "), 帖子通知(" + String.valueOf(mCntThread) + ") 请打开侧栏查阅")
//                            .setAutoCancel(true);
//
//            NotificationManager mNotifyMgr =
//                    (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
//            if (Build.VERSION.SDK_INT < 16) {
//                mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.getNotification());
//            } else {
//                mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
//            }
//        } else {
//            NotificationManager mNotifyMgr =
//                    (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotifyMgr.cancel(NOTIFICATION_ID);
//        }
    }
}
