package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2017-06-06.
 */

public class ValueChagerView extends RelativeLayout {

    private Button mBtnPlus;
    private Button mBtnMinus;
    private TextView mTvValue;
    private TextView mTvTitle;

    private int mMinValue;
    private int mMaxValue;
    private int mCurrentValue;
    private String mTitle;

    private OnChangeListener mOnChangeListener;

    public ValueChagerView(Context context) {
        super(context);
        init();
    }

    public ValueChagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ValueChagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ValueChagerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.vw_value_changer, this);
        mBtnPlus = (Button) findViewById(R.id.btn_plus);
        mBtnMinus = (Button) findViewById(R.id.btn_minus);
        mTvValue = (TextView) findViewById(R.id.tv_value);
        mTvTitle = (TextView) findViewById(R.id.tv_title);

        updateViews();

        mBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentValue < mMaxValue) {
                    mCurrentValue++;
                    updateViews();

                    if (mOnChangeListener != null)
                        mOnChangeListener.onChange(mCurrentValue);
                }
            }
        });

        mBtnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentValue > mMinValue) {
                    mCurrentValue--;
                    updateViews();

                    if (mOnChangeListener != null)
                        mOnChangeListener.onChange(mCurrentValue);
                }
            }
        });

    }

    private void updateViews() {
        mTvValue.setText(String.valueOf(mCurrentValue));
        mBtnPlus.setEnabled(mCurrentValue < mMaxValue);
        mBtnMinus.setEnabled(mCurrentValue > mMinValue);
    }

    public void setTitle(int resId) {
        mTvTitle.setText(resId);
    }

    public void setValues(int currentValue, int minValue, int maxValue) {
        mCurrentValue = currentValue;
        mMaxValue = maxValue;
        mMinValue = minValue;
        updateViews();
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListener = onChangeListener;
    }

    public interface OnChangeListener {
        void onChange(int currentValue);
    }

}
