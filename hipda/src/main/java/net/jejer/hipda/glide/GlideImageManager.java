package net.jejer.hipda.glide;

import android.util.Log;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.TagConstraint;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Logger;

/**
 * Image loading manager
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class GlideImageManager {

    public final static int PRIORITY_HIGH = 9;
    public final static int PRIORITY_MIDIUM = 6;
    public final static int PRIORITY_LOW = 3;

    private static JobManager jobManager;

    private GlideImageManager() {
    }

    private static JobManager getJobManager() {
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
                                    if (isDebugEnabled())
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
                            .consumerKeepAlive(90)//wait 2 minute
                            .build();
                    jobManager = new JobManager(HiApplication.getAppContext(), configuration);
                }
            }
        }
        return jobManager;
    }

    public static void addJob(GlideImageJob job) {
        getJobManager().addJob(job);
    }

    public static void cancelJobs(String tag) {
        getJobManager().cancelJobsInBackground(null, TagConstraint.ANY, tag);
    }

}
