package net.jejer.hipda.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Looper;

import net.jejer.hipda.ui.HiApplication;

/**
 * Created by GreenSkinMonster on 2021-07-23.
 */
public class SimpleExceptionHandler implements Thread.UncaughtExceptionHandler {

    final private Thread.UncaughtExceptionHandler mDefaultHandler;

    public SimpleExceptionHandler() {
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ClipboardManager clipboard = (ClipboardManager) HiApplication.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, Utils.getDeviceInfo() + "\n" + Utils.getStackTrace(e));
        clipboard.setPrimaryClip(clipData);
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                UIUtils.toast("抱歉，程序崩溃，相关信息已经保存到粘贴板");
                Looper.loop();
            }
        }.start();
        try {
            Thread.sleep(1500);
        } catch (Exception ignored) {
        }
        mDefaultHandler.uncaughtException(t, e);
    }
}