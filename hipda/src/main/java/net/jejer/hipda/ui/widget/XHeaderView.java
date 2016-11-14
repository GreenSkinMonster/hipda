package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2016-11-08.
 */
public class XHeaderView extends RelativeLayout {

    public final static int STATE_HIDDEN = 0;
    public final static int STATE_LOADING = 1;
    public final static int STATE_READY = 2;

    private int mState = STATE_HIDDEN;
    private View mLayout;
    private ProgressBar mProgressBar;
    private TextView mTitle;

    public XHeaderView(Context context) {
        this(context, null);
    }

    public XHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayout = LayoutInflater.from(context).inflate(R.layout.vw_header, null);
        mLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        addView(mLayout);

        mProgressBar = (ProgressBar) findViewById(R.id.header_progressbar);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(Color.LTGRAY, android.graphics.PorterDuff.Mode.SRC_IN);
        mTitle = (TextView) findViewById(R.id.header_text);
    }

    public void setState(int state) {
        mState = state;
        switch (state) {
            case STATE_READY:
                mProgressBar.setVisibility(GONE);
                mTitle.setVisibility(VISIBLE);
                break;

            case STATE_LOADING:
                mTitle.setVisibility(GONE);
                mProgressBar.setVisibility(VISIBLE);
                break;

            default:
                break;
        }
    }

    public int getState() {
        return mState;
    }

    public void setTopMargin(int margin) {
        if (margin < 0) return;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLayout.getLayoutParams();
        lp.topMargin = margin;
        mLayout.setLayoutParams(lp);
    }

    public int getTopMargin() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLayout.getLayoutParams();
        return lp.topMargin;
    }

}
