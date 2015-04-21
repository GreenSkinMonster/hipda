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

    public static String getLogFile(Context context) {
        return context.getFilesDir().getPath() + "/hipda.log";
    }

    public static boolean writeContentToFile(String file, String content) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF-8"));
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

    private static boolean delete(String file) {
        File logFile = new File(file);
        if (logFile.exists() && logFile.canWrite())
            return logFile.delete();
        return false;
    }

    public static void acraReport(Context context, String title, String content) {
        String logFile = getLogFile(context);
        writeContentToFile(logFile, content);
        ACRA.getConfig().setApplicationLogFile(logFile);
        ACRA.getErrorReporter().handleException(new Exception(title));
        delete(logFile);
        ACRA.getConfig().setApplicationLogFile("");
    }

}
