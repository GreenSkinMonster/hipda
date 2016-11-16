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
public class XFooterView extends RelativeLayout {

    public final static int STATE_HIDDEN = 0;
    public final static int STATE_LOADING = 1;
    public final static int STATE_READY = 2;
    public final static int STATE_END = 3;

    private View mLayout;
    private ProgressBar mProgressBar;
    private TextView mHintView;

    private int mState = STATE_HIDDEN;

    public XFooterView(Context context) {
        this(context, null);
    }

    public XFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayout = LayoutInflater.from(context).inflate(R.layout.vw_footer, null);
        mLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        addView(mLayout);

        mProgressBar = (ProgressBar) mLayout.findViewById(R.id.footer_progressbar);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(Color.LTGRAY, android.graphics.PorterDuff.Mode.SRC_IN);
        mHintView = (TextView) mLayout.findViewById(R.id.footer_text);
    }

    protected void setState(int state) {
        if (state == mState) return;
        switch (state) {
            case STATE_READY:
                mProgressBar.setVisibility(View.GONE);
                mHintView.setText(R.string.footer_hint_load_normal);
                mHintView.setVisibility(View.VISIBLE);
                break;

            case STATE_END:
                mProgressBar.setVisibility(View.GONE);
                mHintView.setText(R.string.footer_hint_end);
                mHintView.setVisibility(View.VISIBLE);
                break;

            case STATE_LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                mHintView.setVisibility(View.INVISIBLE);
                break;
        }
        mState = state;
    }

    protected int getState() {
        return mState;
    }

    protected void setBottomMargin(int margin) {
        if (margin < 0) return;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLayout.getLayoutParams();
        lp.bottomMargin = margin;
        mLayout.setLayoutParams(lp);
    }

    protected int getBottomMargin() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLayout.getLayoutParams();
        return lp.bottomMargin;
    }

}
