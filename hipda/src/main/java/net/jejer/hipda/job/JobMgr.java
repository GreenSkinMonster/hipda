package net.jejer.hipda.job;

import android.util.Log;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Logger;

/**
 * Created by GreenSkinMonster on 2015-08-27.
 */
public class JobMgr {

    public final static int PRIORITY_HIGH = 9;
    public final static int PRIORITY_MIDIUM = 6;
    public final static int PRIORITY_LOW = 3;

    private JobManager jobManager;
    private JobManager glideJobManager;

    private JobMgr() {
        CustomLogger logger = new CustomLogger() {
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
        };

        jobManager = new JobManager(HiApplication.getAppContext(), new Configuration.Builder(HiApplication.getAppContext())
                .customLogger(logger)
                .minConsumerCount(1)
                .maxConsumerCount(3)
                .loadFactor(2)
                .build());
        glideJobManager = new JobManager(HiApplication.getAppContext(), new Configuration.Builder(HiApplication.getAppContext())
                .customLogger(logger)
                .maxConsumerCount(5)
                .loadFactor(1)
                .build());
    }

    private static class SingletonHolder {
        static final JobMgr INSTANCE = new JobMgr();
    }

    public static JobMgr getInstance() {
        return JobMgr.SingletonHolder.INSTANCE;
    }

    private void addJobImpl(BaseJob job) {
        if (job instanceof GlideImageJob) {
            glideJobManager.addJob(job);
        } else {
            jobManager.addJob(job);
        }
    }

    public static void addJob(BaseJob job) {
        getInstance().addJobImpl(job);
    }
}
