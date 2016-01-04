package net.jejer.hipda.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.NotificationBean;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.MainFrameActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;

/**
 * parse and fetch notifications
 * Created by GreenSkinMonster on 2015-09-08.
 */
public class NotificationMgr {

    private final static int REQUEST_CODE = 0;
    public final static int MIN_REPEAT_MINUTTES = 10;
    public final static String DEFAUL_SLIENT_BEGIN = "22:00";
    public final static String DEFAUL_SLIENT_END = "08:00";

    private final static NotificationBean mCurrentBean = new NotificationBean();

    public static NotificationBean getCurrentNotification() {
        return mCurrentBean;
    }

    public static void startAlarm(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int repeat = HiSettingsHelper.getInstance().getNotiRepeatMinutes();
        if (repeat < MIN_REPEAT_MINUTTES) {
            repeat = MIN_REPEAT_MINUTTES;
            HiSettingsHelper.getInstance().setNotiRepeatMinutes(repeat);
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 30000,
                repeat * 60 * 1000, sender);
        Logger.v("NotificationAlarm started.");
        isAlarmRuning(context);
    }

    public static void cancelAlarm(Context context) {
        cancelNotification(context);
        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(context,
                    REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender);
            sender.cancel();
        } catch (Exception e) {
            Logger.e(e);
        }
        Logger.v("NotificationAlarm cancelled.");
        isAlarmRuning(context);
    }

    public static void fetchNotification(Document doc) {
        int smsCount = -1;
        int threadCount = -1;
        SimpleListItemBean smsBean = null;

        //check new sms
        if (doc == null || HiSettingsHelper.getInstance().isCheckSms()) {
            HiSettingsHelper.getInstance().setLastCheckSmsTime(System.currentTimeMillis());
            try {
                String response = OkHttpHelper.getInstance().get(HiUtils.NewSMS);
                if (!TextUtils.isEmpty(response)) {
                    doc = Jsoup.parse(response);
                    SimpleListBean listBean = HiParser.parseSMS(doc);
                    if (listBean != null) {
                        smsCount = listBean.getCount();
                        if (smsCount == 1) {
                            smsBean = listBean.getAll().get(0);
                        }
                    }
                }
            } catch (Exception e) {
                Logger.e(e);
            }
        }
        //check new thread notify
        if (doc != null) {
            Elements promptThreadES = doc.select("div.promptcontent a#prompt_threads");
            if (promptThreadES.size() > 0) {
                String notifyStr = promptThreadES.first().text();
                String tcount = HttpUtils.getMiddleString(notifyStr, "(", ")");
                if (!TextUtils.isEmpty(tcount) && TextUtils.isDigitsOnly(tcount)) {
                    threadCount = Integer.parseInt(tcount);
                }
            }
        }

        if (threadCount >= 0)
            mCurrentBean.setThreadCount(threadCount);

        if (smsCount >= 0) {
            mCurrentBean.setSmsCount(smsCount);
            if (smsCount == 1 && smsBean != null) {
                mCurrentBean.setUsername(smsBean.getAuthor());
                mCurrentBean.setUid(smsBean.getUid());
                mCurrentBean.setContent(smsBean.getTitle());
            } else {
                mCurrentBean.setUsername("");
                mCurrentBean.setUid("");
                mCurrentBean.setContent("");
            }
        }
    }

    public static void showNotification(Context context) {
        if (!HiApplication.isActivityVisible()) {
            if (mCurrentBean.hasNew()) {
                Intent intent = new Intent(context, MainFrameActivity.class);
                intent.setAction(Constants.INTENT_NOTIFICATION);
                intent.putExtra(Constants.EXTRA_SMS_COUNT, mCurrentBean.getSmsCount());
                intent.putExtra(Constants.EXTRA_THREAD_COUNT, mCurrentBean.getThreadCount());
                if (!TextUtils.isEmpty(mCurrentBean.getUsername()))
                    intent.putExtra(Constants.EXTRA_USERNAME, mCurrentBean.getUsername());
                if (HiUtils.isValidId(mCurrentBean.getUid()))
                    intent.putExtra(Constants.EXTRA_UID, mCurrentBean.getUid());
                PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                String title = "HiPDA论坛提醒";
                String content = getContentText(mCurrentBean);
                Bitmap icon = null;

                int color = ContextCompat.getColor(context, R.color.icon_blue);

                if (mCurrentBean.getSmsCount() == 1 && mCurrentBean.getThreadCount() == 0) {
                    title = mCurrentBean.getUsername() + " 的短消息";
                    content = mCurrentBean.getContent();
                    if (!GlideHelper.ready())
                        GlideHelper.init(context);
                    File avatarFile = GlideHelper.getAvatarFile(context, HiUtils.getAvatarUrlByUid(mCurrentBean.getUid()));
                    if (avatarFile != null && avatarFile.exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        icon = BitmapFactory.decodeFile(avatarFile.getPath(), options);
                    }
                }

                if (icon == null)
                    icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

                final NotificationCompat.Builder notif = new NotificationCompat.Builder(context)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(R.drawable.ic_stat_hi)
                        .setLargeIcon(icon)
                        .setColor(color);

                String sound = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_NOTI_SOUND, "");
                if (!TextUtils.isEmpty(sound))
                    notif.setSound(Uri.parse(sound));
                if (HiSettingsHelper.getInstance().isNotiLedLight())
                    notif.setLights(color, 1000, 3000);
                if (Build.VERSION.SDK_INT >= 16)
                    notif.setPriority(Notification.PRIORITY_HIGH)
                            .setVibrate(new long[0]);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, notif.build());
                HiApplication.setNotified(true);

                //clean count to avoid notification button on start up
                mCurrentBean.setSmsCount(0);
                mCurrentBean.setThreadCount(0);

            } else {
                cancelNotification(context);
            }
        }
    }

    public static void cancelNotification(Context context) {
        if (HiApplication.isNotified()) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
            HiApplication.setNotified(false);
        }
    }

    private static String getContentText(NotificationBean bean) {
        StringBuilder sb = new StringBuilder();
        sb.append("您有 ");
        sb.append(bean.getSmsCount() > 0 ? bean.getSmsCount() + " 条新的短消息" : "");
        if (bean.getSmsCount() > 0 && bean.getThreadCount() > 0)
            sb.append("， ");
        sb.append(bean.getThreadCount() > 0 ? bean.getThreadCount() + " 条新的帖子通知" : "");
        return sb.toString();
    }

    public static boolean isAlarmRuning(Context context) {
        return (PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(context, NotificationReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
    }

}
