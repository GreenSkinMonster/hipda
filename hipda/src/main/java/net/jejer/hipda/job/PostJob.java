package net.jejer.hipda.job;

import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.async.PrePostAsyncTask;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;

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

    public PostJob(String sessionId, int mode, PrePostInfoBean prePostInfo, PostBean postArg, boolean fromQuickReply) {

        super(sessionId, JobMgr.PRIORITY_HIGH);

        mPostArg = postArg;
        mPrePostInfo = prePostInfo;
        mMode = mode;

        mEvent = new PostEvent();
        mEvent.mMode = mMode;
        mEvent.mSessionId = mSessionId;
        mEvent.fromQuickReply = fromQuickReply;
    }

    @Override
    public void onAdded() {
        mEvent.mStatus = Constants.STATUS_IN_PROGRESS;
        EventBus.getDefault().postSticky(mEvent);
    }

    @Override
    public void onRun() throws Throwable {
        long start = System.currentTimeMillis();

        if (mMode == PostHelper.MODE_QUICK_DELETE) {
            PrePostAsyncTask prePostAsyncTask = new PrePostAsyncTask(HiApplication.getAppContext(), null, PostHelper.MODE_EDIT_POST);
            PostBean postBean = new PostBean();
            postBean.setTid(mPostArg.getTid());
            postBean.setPid(mPostArg.getPid());
            postBean.setFid(mPostArg.getFid());
            mPrePostInfo = prePostAsyncTask.doInBackground(postBean);
            if (mPrePostInfo == null) {
                mEvent.mStatus = Constants.STATUS_FAIL;
                mEvent.mMessage = prePostAsyncTask.getMessage();
                EventBus.getDefault().postSticky(mEvent);
                return;
            }
            if (mPrePostInfo.isDeleteable()) {
                mMode = PostHelper.MODE_EDIT_POST;
                mPostArg.setDelete(true);
            } else {
                mMode = PostHelper.MODE_EDIT_POST;
                mPostArg.setDelete(false);
                mPostArg.setContent(".....");
                for (String attach : mPrePostInfo.getAllImages()) {
                    mPrePostInfo.addDeleteAttach(attach);
                }
                for (String attach : mPrePostInfo.getAttaches()) {
                    mPrePostInfo.addDeleteAttach(attach);
                }
            }
        }

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

        DetailListBean data = postResult.getDetailListBean();
        if (data != null && data.getPage() == data.getLastPage()
                && HiUtils.isForumValid(data.getFid())) {
            ThreadUpdatedEvent tuEvent = new ThreadUpdatedEvent();
            tuEvent.mFid = data.getFid();
            tuEvent.mTid = data.getTid();
            tuEvent.mTitle = data.getTitle();
            tuEvent.mReplyCount = data.getAll().get(data.getCount() - 1).getFloor() - 1;
            EventBus.getDefault().postSticky(tuEvent);
        }
    }

}
