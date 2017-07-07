package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2016-11-18.
 */

public class ContentLoadingView extends RelativeLayout {

    public static final int LOADING = 0;
    public static final int LOAD_NOW = 1;
    public static final int ERROR = 2;
    public static final int NO_DATA = 3;
    public static final int CONTENT = 4;
    public static final int NOT_LOGIN = 5;

    private ContentLoadingProgressBar mProgressBar;
    private TextView mContentInfo;
    private OnClickListener mOnErrorListener;
    private OnClickListener mOnNotLoginListener;
    private int mState = -1;

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
        mContentInfo.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (mState == ERROR && mOnErrorListener != null) {
                    mOnErrorListener.onClick(view);
                } else if (mState == NOT_LOGIN && mOnNotLoginListener != null) {
                    mOnNotLoginListener.onClick(view);
                }
            }
        });
    }

    public void setState(int state) {
        if (mState == state ||
                (mState == LOAD_NOW && state == LOADING))
            return;
        mState = state;
        if (state == LOADING) {
            mContentInfo.setVisibility(View.GONE);
            mProgressBar.show();
        } else if (state == LOAD_NOW) {
            mContentInfo.setVisibility(View.GONE);
            mProgressBar.showNow();
        } else if (state == ERROR) {
            mProgressBar.hide();
            mContentInfo.setText(R.string.content_hint_error);
            mContentInfo.setVisibility(View.VISIBLE);
        } else if (state == NOT_LOGIN) {
            mProgressBar.hide();
            mContentInfo.setText(R.string.content_not_login);
            mContentInfo.setVisibility(View.VISIBLE);
        } else if (state == NO_DATA) {
            mProgressBar.hide();
            mContentInfo.setText(R.string.content_hint_nodata);
            mContentInfo.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.hide();
            mContentInfo.setVisibility(View.GONE);
        }
    }

    public void setErrorStateListener(OnClickListener listener) {
        mOnErrorListener = listener;
    }

    public void setNotLoginStateListener(OnClickListener listener) {
        mOnNotLoginListener = listener;
    }
}
