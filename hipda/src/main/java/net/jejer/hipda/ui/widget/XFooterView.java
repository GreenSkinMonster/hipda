package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.Utils;

/**
 * Created by GreenSkinMonster on 2016-11-08.
 */
public class XFooterView extends RelativeLayout {

    public final static int STATE_HIDDEN = 0;
    public final static int STATE_LOADING = 1;
    public final static int STATE_READY = 2;
    public final static int STATE_END = 3;
    public final static int STATE_ERROR = 4;

    private View mLayout;
    private ProgressBar mProgressBar;
    private TextView mHintView;

    private int mState = STATE_HIDDEN;

    public XFooterView(Context context) {
        this(context, null);
    }

    public XFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayout = inflate(getContext(), R.layout.vw_footer, null);
        mLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                Utils.dpToPx(context, XRecyclerView.HEIGHT_IN_DP)));
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
                mHintView.setTextColor(ColorHelper.getTextColorSecondary(getContext()));
                mHintView.setText(R.string.footer_hint_load_next);
                mHintView.setVisibility(View.VISIBLE);
                break;

            case STATE_END:
                mProgressBar.setVisibility(View.GONE);
                mHintView.setTextColor(ColorHelper.getTextColorSecondary(getContext()));
                mHintView.setText(R.string.footer_hint_end);
                mHintView.setVisibility(View.VISIBLE);
                break;

            case STATE_LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                mHintView.setVisibility(View.INVISIBLE);
                break;

            case STATE_ERROR:
                mProgressBar.setVisibility(View.GONE);
                mHintView.setVisibility(View.VISIBLE);
                mHintView.setTextColor(ContextCompat.getColor(getContext(), R.color.md_orange_800));
                mHintView.setText(R.string.footer_hint_error);
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
