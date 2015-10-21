package net.jejer.hipda.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.ColorUtils;

/**
 * https://github.com/MarkMjw/PullToRefresh
 *
 * @author markmjw
 * @date 2013-10-08
 */
public class XFooterView extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_LOADING = 2;
    public final static int STATE_END = 3;

    private View mLayout;
    private ProgressBar mProgressBar;
    private TextView mHintView;

    private int mState = STATE_NORMAL;

    public XFooterView(Context context) {
        super(context);
        initView(context);
    }

    public XFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mLayout = LayoutInflater.from(context).inflate(R.layout.vw_footer, null);
        mLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        addView(mLayout);

        mProgressBar = (ProgressBar) mLayout.findViewById(R.id.footer_progressbar);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(Color.LTGRAY, android.graphics.PorterDuff.Mode.SRC_IN);
        mHintView = (TextView) mLayout.findViewById(R.id.footer_hint_text);
    }

    /**
     * Set footer view state
     */
    public void setState(int state) {
        if (state == mState) return;

        if (state == STATE_LOADING) {
            mProgressBar.setVisibility(View.VISIBLE);
            mHintView.setVisibility(View.INVISIBLE);
        } else {
            mHintView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        switch (state) {
            case STATE_NORMAL:
                mHintView.setTextColor(ColorUtils.getColorAccent(getContext()));
                mHintView.setText(R.string.footer_hint_load_normal);
                break;

            case STATE_READY:
                if (mState != STATE_READY) {
                    mHintView.setTextColor(ColorUtils.getColorAccent(getContext()));
                    mHintView.setText(R.string.footer_hint_load_ready);
                }
                break;

            case STATE_END:
                mHintView.setTextColor(Color.GRAY);
                mHintView.setText(R.string.footer_hint_end);
                break;

            case STATE_LOADING:
                break;
        }

        mState = state;
    }

    /**
     * Set footer view bottom margin.
     *
     * @param margin
     */
    public void setBottomMargin(int margin) {
        if (margin < 0) return;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        lp.bottomMargin = margin;
        mLayout.setLayoutParams(lp);
    }

    /**
     * Get footer view bottom margin.
     *
     * @return
     */
    public int getBottomMargin() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        return lp.bottomMargin;
    }

    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        if (lp.height != 0) {
            lp.height = 0;
            mLayout.setLayoutParams(lp);
        }
    }

    /**
     * show footer
     */
    public void show() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mLayout.getLayoutParams();
        if (lp.height != LayoutParams.WRAP_CONTENT) {
            lp.height = LayoutParams.WRAP_CONTENT;
            mLayout.setLayoutParams(lp);
        }
    }

}