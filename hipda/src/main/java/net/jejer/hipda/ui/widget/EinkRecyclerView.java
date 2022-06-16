package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import net.jejer.hipda.bean.HiSettingsHelper;

public class EinkRecyclerView extends RecyclerView {
    float mDownX;
    float mDownY;
    boolean mMultiTouchDown;

    public EinkRecyclerView(@NonNull Context context) {
        super(context);
    }

    public EinkRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EinkRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void previPage(){
        int[] location = new int[2];
        this.getLocationInWindow(location);
        this.scrollBy(0,-this.getHeight()+location[1]);
    }

    public void nextPage(){
        int[] location = new int[2];
        this.getLocationInWindow(location);
        this.scrollBy(0,this.getHeight()-location[1]);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

//        if(!HiSettingsHelper.getInstance().isEinkSwipePageModeEnabled())
//            return super.dispatchTouchEvent(ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mMultiTouchDown = false;
                mDownX = ev.getRawX();
                mDownY = ev.getRawY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mMultiTouchDown = true;
//                setFooterOffset(0);
                break;

            case MotionEvent.ACTION_MOVE:
                if(!mMultiTouchDown)
                    return true;
                break;
            case MotionEvent.ACTION_UP:
//                if(listener!=null)
                {
                    float deltaYEink = ev.getRawY() - mDownY;
                    float deltaXEink = ev.getRawX() - mDownX;
                    if((!mMultiTouchDown)&&Math.abs(deltaYEink)>Math.abs(deltaXEink)&&Math.abs(deltaYEink)>50) {
                        if (deltaYEink > 50) previPage();
                        else if (deltaYEink < -50) nextPage();
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }
                break;
            default:
                break;
        }
//        if(performSuperFunction)
//            return super.dispatchTouchEvent(ev);
//        else
//            return true;
        return super.dispatchTouchEvent(ev);
    }
}
