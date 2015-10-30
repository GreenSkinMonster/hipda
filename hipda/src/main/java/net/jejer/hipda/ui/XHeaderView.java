package net.jejer.hipda.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.Utils;

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
    private ProgressBar mProgressBar;

    private int mState = STATE_NORMAL;

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
        mProgressBar = (ProgressBar) findViewById(R.id.header_progressbar);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(Color.LTGRAY, android.graphics.PorterDuff.Mode.SRC_IN);
    }

    public void setState(int state) {
        switch (state) {
            case STATE_NORMAL:
                if (mProgressBar.getVisibility() == VISIBLE)
                    mProgressBar.setVisibility(GONE);
                mHintTextView.setText(R.string.header_hint_refresh_normal);
                break;

            case STATE_READY:
                if (mProgressBar.getVisibility() == VISIBLE)
                    mProgressBar.setVisibility(GONE);
                if (mState != STATE_READY) {
                    mHintTextView.setText(R.string.header_hint_refresh_ready);
                }
                break;

            case STATE_REFRESHING:
                mProgressBar.setVisibility(VISIBLE);
                setVisibleHeight(Utils.dpToPx(getContext(), 56));
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