package net.jejer.hipda.job;

import net.jejer.hipda.utils.Constants;

/**
 * Created by GreenSkinMonster on 2016-11-17.
 */

public abstract class EventCallback<T extends BaseEvent> {

    public abstract void onSuccess(T event);

    public abstract void onFail(T event);

    public void onFailAbort(T event) {
        onFail(event);
    }

    public void onFailRelogin(T event) {
        onFail(event);
    }

    public void process(T event) {
        switch (event.mStatus) {
            case Constants.STATUS_SUCCESS:
                onSuccess(event);
                break;
            case Constants.STATUS_FAIL:
                onFail(event);
                break;
            case Constants.STATUS_FAIL_ABORT:
                onFailAbort(event);
                break;
            case Constants.STATUS_FAIL_RELOGIN:
                onFailRelogin(event);
                break;
        }
    }

}
