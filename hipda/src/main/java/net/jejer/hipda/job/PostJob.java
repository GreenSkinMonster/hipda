package net.jejer.hipda.job;

import android.content.Context;

import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.utils.Constants;

import de.greenrobot.event.EventBus;

/**
 * Created by GreenSkinMonster on 2016-03-28.
 */
public class PostJob extends BaseJob {

    private PostBean mPostArg;
    private PrePostInfoBean mPrePostInfo;
    private int mMode;
    private Context mCtx;
    private PostEvent mEvent;

    public PostJob(String sessionId, Context ctx, int mode, PrePostInfoBean prePostInfo, PostBean postArg) {

        super(sessionId, JobMgr.PRIORITY_HIGH);

        mPostArg = postArg;
        mPrePostInfo = prePostInfo;
        mMode = mode;
        mCtx = ctx;

        mEvent = new PostEvent();
        mEvent.mMode = mMode;
        mEvent.mSessionId = mSessionId;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().post(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        PostHelper postHelper = new PostHelper(mCtx, mMode, mPrePostInfo, mPostArg);
        PostBean postResult = postHelper.post();

        mEvent.mPostResult = postResult;
        mEvent.mStatus = postResult.getStatus();
        mEvent.mMessage = postResult.getMessage();

        EventBus.getDefault().post(mEvent);
    }

    @Override
    protected void onCancel() {

    }
}
