package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
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

    private int mMinValue;
    private int mMaxValue;
    private int mCurrentValue;

    private OnChangeListener mOnChangeListener;

    public ValueChagerView(Context context) {
        super(context);
        init(context, null);
    }

    public ValueChagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ValueChagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ValueChagerView);
        String title = a.getString(R.styleable.ValueChagerView_title);
        mMinValue = a.getInt(R.styleable.ValueChagerView_minValue, 0);
        mMaxValue = a.getInt(R.styleable.ValueChagerView_maxValue, 0);
        a.recycle();

        inflate(getContext(), R.layout.vw_value_changer, this);
        mBtnPlus = (Button) findViewById(R.id.btn_plus);
        mBtnMinus = (Button) findViewById(R.id.btn_minus);
        mTvValue = (TextView) findViewById(R.id.tv_value);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);

        tvTitle.setText(title);
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

    public void setCurrentValue(int currentValue) {
        mCurrentValue = currentValue;
        updateViews();
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListener = onChangeListener;
    }

    public interface OnChangeListener {
        void onChange(int currentValue);
    }

}
