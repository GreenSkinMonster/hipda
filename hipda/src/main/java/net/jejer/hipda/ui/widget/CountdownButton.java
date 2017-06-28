package net.jejer.hipda.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2017-06-28.
 */

public class CountdownButton extends RelativeLayout {

    private ImageButton mImageButton;
    private TextView mTextView;
    private CountDownTimer mCountDownTimer;

    public CountdownButton(Context context) {
        super(context);
        init();
    }

    public CountdownButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountdownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.vw_countdown_button, this);
        mImageButton = (ImageButton) findViewById(R.id.ib_button);
        mTextView = (TextView) findViewById(R.id.tv_countdown);
    }

    public void setCountdown(int timeToWait) {
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        if (timeToWait > 0) {
            mImageButton.setVisibility(View.GONE);
            mTextView.setAlpha(1f);
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(timeToWait + "");
            mCountDownTimer = new CountDownTimer(timeToWait * 1000, 500) {
                public void onTick(long millisUntilFinished) {
                    mTextView.setText((millisUntilFinished / 1000) + "");
                }

                public void onFinish() {
                    mTextView.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mTextView.setVisibility(View.GONE);
                                }
                            });

                    mImageButton.setAlpha(0f);
                    mImageButton.setVisibility(View.VISIBLE);
                    mImageButton.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .setListener(null);
                    mImageButton.setVisibility(View.VISIBLE);
                }
            }.start();
        } else {
            mImageButton.setAlpha(1f);
            mImageButton.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mImageButton.setOnClickListener(onClickListener);
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        mImageButton.setOnLongClickListener(onLongClickListener);
    }

    public void setImageDrawable(Drawable drawable) {
        mImageButton.setImageDrawable(drawable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
    }
}
