package net.jejer.hipda.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.jejer.hipda.R;

/**
 * @author markmjw
 * @date 2013-10-08
 */
public class XHeaderView extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;

    private LinearLayout mContainer;
    private TextView mHintTextView;

    private int mState = STATE_NORMAL;

    private boolean mIsFirst;

    public XHeaderView(Context context) {
        super(context);
        initView(context);
    }

    public XHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        // Initial set header view height 0
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.vw_header, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);

        mHintTextView = (TextView) findViewById(R.id.header_hint_text);
    }

    public void setState(int state) {
        if (state == mState && mIsFirst) {
            mIsFirst = true;
            return;
        }

        if (state == STATE_REFRESHING) {
            // show progress
        } else {
            // show arrow image
        }

        switch (state) {
            case STATE_NORMAL:
                mHintTextView.setText(R.string.header_hint_refresh_normal);
                break;

            case STATE_READY:
                if (mState != STATE_READY) {
                    mHintTextView.setText(R.string.header_hint_refresh_ready);
                }
                break;

            case STATE_REFRESHING:
                mHintTextView.setText(R.string.header_hint_refresh_loading);
                break;

            default:
                break;
        }

        mState = state;
    }

    /**
     * Set the header view visible height.
     *
     * @param height
     */
    public void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    /**
     * Get the header view visible height.
     *
     * @return
     */
    public int getVisibleHeight() {
        return mContainer.getHeight();
    }

}