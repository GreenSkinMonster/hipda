package net.jejer.hipda.job;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import androidx.annotation.Nullable;

/**
 * Created by GreenSkinMonster on 2016-03-28.
 */
public abstract class BaseJob extends Job {

    protected String mSessionId;

    public BaseJob(String sessionId) {
        this(sessionId, JobMgr.PRIORITY_MIDIUM);
    }

    public BaseJob(String sessionId, int priority) {
        super(new Params(priority)
                .setPersistent(false)
                .setRequiresNetwork(false)
                .addTags(sessionId));
        mSessionId = sessionId;
    }

    protected BaseJob(Params params) {
        super(params);
    }

    @Override
    protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable,
                                                     int runCount,
                                                     int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    @Override
    protected int getRetryLimit() {
        return 0;
    }
}