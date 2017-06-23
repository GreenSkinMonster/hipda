package net.jejer.hipda.job;

import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Constants;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by GreenSkinMonster on 2016-03-28.
 */
public class PostJob extends BaseJob {

    private final static int MIN_JOB_TIME_MS = 500;

    private PostBean mPostArg;
    private PrePostInfoBean mPrePostInfo;
    private int mMode;
    private PostEvent mEvent;

    public PostJob(String sessionId, int mode, PrePostInfoBean prePostInfo, PostBean postArg) {

        super(sessionId, JobMgr.PRIORITY_HIGH);

        mPostArg = postArg;
        mPrePostInfo = prePostInfo;
        mMode = mode;

        mEvent = new PostEvent();
        mEvent.mMode = mMode;
        mEvent.mSessionId = mSessionId;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        long start = System.currentTimeMillis();
        PostHelper postHelper = new PostHelper(HiApplication.getAppContext(), mMode, mPrePostInfo, mPostArg);
        PostBean postResult = postHelper.post();

        mEvent.mPostResult = postResult;
        mEvent.mStatus = postResult.getStatus();
        mEvent.mMessage = postResult.getMessage();

        long delta = System.currentTimeMillis() - start;
        if (delta < MIN_JOB_TIME_MS) {
            Thread.sleep(MIN_JOB_TIME_MS - delta);
        }

        EventBus.getDefault().postSticky(mEvent);
    }

}
