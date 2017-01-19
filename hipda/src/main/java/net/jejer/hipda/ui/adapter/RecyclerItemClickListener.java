package net.jejer.hipda.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import net.jejer.hipda.bean.HiSettingsHelper;

/**
 * Created by GreenSkinMonster on 2016-11-10.
 */

public class RecyclerItemClickListener implements View.OnTouchListener {

    private static final long MIN_CLICK_INTERVAL = 600;

    private OnItemClickListener mListener;
    private View mChildView;
    private long mLastClickTime;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onLongItemClick(View view, int position);

        void onDoubleTap(View view, int position);
    }

    private GestureDetectorCompat mGestureDetector;

    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mChildView != null && mListener != null) {
                    long currentClickTime = System.currentTimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;

                    if (elapsedTime <= MIN_CLICK_INTERVAL)
                        return true;

                    mListener.onItemClick(mChildView, (int) mChildView.getTag());
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (mChildView != null && mListener != null) {
                    mListener.onLongItemClick(mChildView, (int) mChildView.getTag());
                }
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mChildView != null && mListener != null) {
                    mListener.onDoubleTap(mChildView, (int) mChildView.getTag());
                }
                return super.onDoubleTap(e);
            }

        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mChildView = view;
        mGestureDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        if (HiSettingsHelper.getInstance().getBooleanValue(HiSettingsHelper.PERF_CLICK_EFFECT, true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.drawableHotspotChanged(x, y);
            }

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    break;
            }
        }
        return true;
    }
}
