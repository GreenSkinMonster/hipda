package net.jejer.hipda.utils;

import android.content.Context;

import org.acra.ACRA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * send report with log
 * Created by GreenSkinMonster on 2015-04-21.
 */
public class ACRAUtils {

    private static String LOG_FILE = "";

    public static String getLogFile(Context ctx) {
        return ctx.getFilesDir().getPath() + "/hipda.log";
    }

    public static void init(Context ctx) {
        LOG_FILE = getLogFile(ctx);
        ACRA.getConfig().setApplicationLogFile(LOG_FILE);

        //write a blank file to avoid ACRA file not found error
        if (!(new File(LOG_FILE).exists()))
            writeContentToFile("");
    }

    public static boolean writeContentToFile(String content) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(LOG_FILE), "UTF-8"));
            out.write(content);
            out.close();
            return true;
        } catch (Exception ignored) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ignored) {

                }
            }
        }
        return false;
    }

    public static void acraReport(String title, String content) {
        //write content need to be reported
        writeContentToFile(content);
        ACRA.getErrorReporter().handleException(new Exception(title));

        //clear file content
        writeContentToFile("");
    }

    public static void acraReport(Exception e, String content) {
        //write content need to be reported
        writeContentToFile(content);
        ACRA.getErrorReporter().handleException(e);

        //clear file content
        writeContentToFile("");
    }

}
