package net.jejer.hipda.glide;

import android.util.Log;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Logger;

/**
 * Image loading manager
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageManager {

    private static JobManager jobManager;

    private GlideImageManager() {
    }

    public static JobManager getJobManager() {
        if (jobManager == null) {
            synchronized (GlideImageManager.class) {
                if (jobManager == null) {
                    Configuration configuration = new Configuration.Builder(HiApplication.getAppContext())
                            .customLogger(new CustomLogger() {
                                private static final String TAG = "JOBS";

                                @Override
                                public boolean isDebugEnabled() {
                                    return Logger.isDebug();
                                }

                                @Override
                                public void d(String text, Object... args) {
                                    Log.d(TAG, String.format(text, args));
                                }

                                @Override
                                public void e(Throwable t, String text, Object... args) {
                                    Log.e(TAG, String.format(text, args), t);
                                }

                                @Override
                                public void e(String text, Object... args) {
                                    Log.e(TAG, String.format(text, args));
                                }
                            })
                            .minConsumerCount(1)//always keep at least one consumer alive
                            .maxConsumerCount(3)//up to 3 consumers at a time
                            .loadFactor(3)//3 jobs per consumer
                            .consumerKeepAlive(120)//wait 2 minute
                            .build();
                    jobManager = new JobManager(HiApplication.getAppContext(), configuration);
                }
            }
        }
        return jobManager;
    }

}
