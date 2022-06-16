package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import net.jejer.hipda.ui.adapter.BaseRvAdapter;
import net.jejer.hipda.utils.Utils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by GreenSkinMonster on 2016-11-13.
 */
public class XRecyclerView extends EinkRecyclerView {

    private final static int SCROLL_BACK_HEADER = 0;
    private final static int SCROLL_BACK_FOOTER = 1;

    private final static int SCROLL_DURATION = 400;

    protected final static int HEIGHT_IN_DP = 48;
    private final static int PULL_DELTA_IN_DP = 40;
    private final static float OFFSET_RADIO = 1.8f;

    private float mLastY = -1;
    private boolean mDispatchEvent = true;
    private int mPullDelta = 0;

    private Scroller mScroller;
    private int mScrollBack;

    private XRecyclerListener mListener;
    private LinearLayoutManager mLayoutManager;
    private BaseRvAdapter mAdapter;

    private XHeaderView mHeaderView;
    private XFooterView mFooterView;

    public XRecyclerView(Context context) {
        super(context);
        initWithContext(context);
    }

    public XRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWithContext(context);
    }

    public XRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initWithContext(context);
    }

    private void initWithContext(Context context) {
        mPullDelta = Utils.dpToPx(PULL_DELTA_IN_DP);
        mScroller = new Scroller(context, new DecelerateInterpolator());
        mHeaderView = new XHeaderView(context);
        mFooterView = new XFooterView(context);
        mHeaderView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mHeaderView.getState() == XHeaderView.STATE_ERROR) {
                    onHeaderError();
                }
            }
        });
        mFooterView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mFooterView.getState() == XFooterView.STATE_END) {
                    atEnd();
                } else if (mFooterView.getState() == XFooterView.STATE_ERROR) {
                    onFooterError();
                }
            }
        });
    }

    @Override
    public void setLayoutManager(LayoutManager layoutManager) {
        mLayoutManager = (LinearLayoutManager) layoutManager;
        super.setLayoutManager(layoutManager);
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = (BaseRvAdapter) adapter;
        super.setAdapter(mAdapter);
    }

    public void setHeaderState(final int state) {
        post(new Runnable() {
            @Override
            public void run() {
                mHeaderView.setState(state);
                if (state == XHeaderView.STATE_HIDDEN) {
                    mAdapter.removeHeaderView();
                } else {
                    mAdapter.setHeaderView(mHeaderView);
                }
            }
        });
    }

    public void setFooterState(final int state) {
        post(new Runnable() {
            @Override
            public void run() {
                mFooterView.setState(state);
                if (state == XFooterView.STATE_HIDDEN) {
                    mAdapter.removeFooterView();
                } else {
                    mAdapter.setFooterView(mFooterView);
                }
            }
        });
    }

    public void setXRecyclerListener(XRecyclerListener listener) {
        mListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
            mDispatchEvent = true;
        if (mDispatchEvent)
            return super.dispatchTouchEvent(ev);
        else
            return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScrollBack == SCROLL_BACK_HEADER) {
                mHeaderView.setTopMargin(mScroller.getCurrY());
            } else {
                mFooterView.setBottomMargin(mScroller.getCurrY());
            }
            postInvalidate();
        }
        super.computeScroll();
    }

    private void atEnd() {
        if (null != mListener) {
            mListener.atEnd();
        }
    }

    private void onHeaderError() {
        if (null != mListener) {
            mListener.onHeaderError();
        }
    }

    private void onFooterError() {
        if (null != mListener) {
            mListener.onFooterError();
        }
    }

    public void stopScroll() {
        dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    public void scrollToTop() {
        mLayoutManager.scrollToPositionWithOffset(0, 0);
    }

    public void scrollToBottom() {
        mLayoutManager.scrollToPositionWithOffset(mLayoutManager.getItemCount() - 1, 0);
    }

    public void smoothScrollToTop() {
        smoothScrollToPosition(0);
    }

    public void smoothScrollToBottom() {
        smoothScrollToPosition(mLayoutManager.getItemCount() - 1);
    }

    public boolean isNearBottom() {
        return mLayoutManager.findLastVisibleItemPosition() >= mLayoutManager.getItemCount() - 2;
    }

    @Override
    public void scrollToPosition(int position) {
        mLayoutManager.scrollToPositionWithOffset(position, 0);
    }

    public interface XRecyclerListener {
        void atEnd();

        void onHeaderError();

        void onFooterError();
    }
}
