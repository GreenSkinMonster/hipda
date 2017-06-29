package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by GreenSkinMonster on 2017-06-29.
 */

public class ImageViewPager extends ViewPager {
    public ImageViewPager(Context context) {
        super(context);
    }

    public ImageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //to avoid following error
        //Fatal Exception: java.lang.IllegalArgumentException: pointerIndex out of range
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            return true;
        }
    }
}
