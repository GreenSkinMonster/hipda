package net.jejer.hipda.service;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by GreenSkinMonster on 2017-07-19.
 */

public class NotiJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case NotiJob.TAG:
                return new NotiJob();
            default:
                return null;
        }
    }
}
