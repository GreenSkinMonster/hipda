package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.ui.ContentLoadingProgressBar;
import net.jejer.hipda.utils.ColorHelper;

/**
 * Created by GreenSkinMonster on 2016-11-18.
 */

public class ContentLoadingView extends LinearLayout {

    public static final int LOADING = 0;
    public static final int LOAD_NOW = 1;
    public static final int ERROR = 2;
    public static final int NO_DATA = 3;
    public static final int CONTENT = 4;

    private ContentLoadingProgressBar mProgressBar;
    private TextView mContentInfo;

    public ContentLoadingView(Context context) {
        super(context);
        init();
    }

    public ContentLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContentLoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.vw_content_loading, this);
        mProgressBar = (ContentLoadingProgressBar) findViewById(R.id.content_progressbar);
        mContentInfo = (TextView) findViewById(R.id.content_info);
    }

    public void setState(int state) {
        if (state == LOADING) {
            mProgressBar.show();
            mContentInfo.setVisibility(View.GONE);
        } else if (state == LOAD_NOW) {
            mProgressBar.showNow();
            mContentInfo.setVisibility(View.GONE);
        } else if (state == ERROR) {
            mProgressBar.hide();
            mContentInfo.setTextColor(ContextCompat.getColor(getContext(), R.color.md_orange_800));
            mContentInfo.setText(R.string.content_hint_error);
            mContentInfo.setVisibility(View.VISIBLE);
        } else if (state == NO_DATA) {
            mProgressBar.hide();
            mContentInfo.setTextColor(ColorHelper.getTextColorSecondary(getContext()));
            mContentInfo.setText(R.string.content_hint_nodata);
            mContentInfo.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.hide();
            mContentInfo.setVisibility(View.GONE);
        }
    }

}
