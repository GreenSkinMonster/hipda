package net.jejer.hipda.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.NotificationBean;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.ui.IntentActivity;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * parse and fetch notifications
 * Created by GreenSkinMonster on 2015-09-08.
 */
public class NotiHelper {

    public final static int NOTI_REPEAT_MINUTTE = 15;
    public final static String DEFAUL_SLIENT_BEGIN = "22:00";
    public final static String DEFAUL_SLIENT_END = "08:00";

    private final static NotificationBean mCurrentBean = new NotificationBean();

    public static NotificationBean getCurrentNotification() {
        return mCurrentBean;
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

        if (doc != null) {
            threadCount = 0;
            String[] prompts = {"prompt_threads", "prompt_systempm", "prompt_friend"};
            for (String prompt : prompts) {
                Elements promptES = doc.select("div.promptcontent a#" + prompt);
                if (promptES.size() > 0) {
                    threadCount += Utils.parseInt(Utils.getMiddleString(promptES.first().text(), "(", ")"));
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
        if (!HiApplication.isAppVisible()) {
            if (mCurrentBean.hasNew()) {
                sendNotification(context,
                        mCurrentBean.getThreadCount(),
                        mCurrentBean.getSmsCount(),
                        mCurrentBean.getUsername(),
                        mCurrentBean.getUid(),
                        mCurrentBean.getContent());
                HiApplication.setNotified(true);

                //clean count to avoid notification button on start up
                mCurrentBean.setSmsCount(0);
                mCurrentBean.setThreadCount(0);

            } else {
                cancelNotification(context);
            }
        }
    }

    private static void cancelNotification(Context context) {
        if (HiApplication.isNotified()) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
            HiApplication.setNotified(false);
        }
    }

    private static String getContentText(int threadCount, int smsCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("您有 ");
        sb.append(smsCount > 0 ? smsCount + " 条新的短消息" : "");
        if (smsCount > 0 && threadCount > 0)
            sb.append("， ");
        sb.append(threadCount > 0 ? threadCount + " 条新的帖子通知" : "");
        return sb.toString();
    }


    public static void sendNotification(Context context, int threadCount, int smsCount, String username, String uid, String smsContent) {
        Intent intent = new Intent(context, IntentActivity.class);
        intent.setAction(Constants.INTENT_NOTIFICATION);
        intent.putExtra(Constants.EXTRA_SMS_COUNT, smsCount);
        intent.putExtra(Constants.EXTRA_THREAD_COUNT, threadCount);
        if (!TextUtils.isEmpty(username))
            intent.putExtra(Constants.EXTRA_USERNAME, username);
        if (HiUtils.isValidId(uid))
            intent.putExtra(Constants.EXTRA_UID, uid);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = "HiPDA论坛提醒";
        String content = getContentText(threadCount, smsCount);
        Bitmap icon = null;

        int color = ContextCompat.getColor(context, R.color.icon_blue);

        if (smsCount == 1 && threadCount == 0) {
            title = username + " 的短消息";
            content = smsContent;
            File avatarFile = GlideHelper.getAvatarFile(context, HiUtils.getAvatarUrlByUid(uid));
            if (avatarFile != null && avatarFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                icon = BitmapFactory.decodeFile(avatarFile.getPath(), options);
            }
        }

        if (icon == null)
            icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_stat_hi)
                .setLargeIcon(icon)
                .setColor(color);

        String sound = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_NOTI_SOUND, "");
        if (!TextUtils.isEmpty(sound))
            builder.setSound(Uri.parse(sound));
        if (HiSettingsHelper.getInstance().isNotiLedLight())
            builder.setLights(color, 1000, 3000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            builder.setPriority(Notification.PRIORITY_HIGH)
                    .setVibrate(new long[0]);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    public static void scheduleJob() {
        new JobRequest.Builder(NotiJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(NOTI_REPEAT_MINUTTE), TimeUnit.MINUTES.toMillis(5))
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule();
    }

    public static void cancelJob() {
        JobManager.instance().cancelAll();
    }
}
