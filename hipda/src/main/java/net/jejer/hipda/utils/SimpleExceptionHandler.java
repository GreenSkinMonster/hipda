package net.jejer.hipda.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

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
                Toast.makeText(HiApplication.getAppContext(),
                        "抱歉，程序崩溃，信息已保存到粘贴板",
                        Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        try {
            Thread.sleep(2000);
        } catch (Exception ignored) {
        }
        mDefaultHandler.uncaughtException(t, e);
    }
}