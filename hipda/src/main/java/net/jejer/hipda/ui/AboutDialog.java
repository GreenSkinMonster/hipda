package net.jejer.hipda.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;

import net.frakbot.creditsroll.CreditsRollView;
import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;

public class AboutDialog extends Dialog implements SeekBar.OnSeekBarChangeListener {

    private static final float SCROLL_ANIM_DURATION = 45000;    // [ms] = 45 s

    private CreditsRollView mCreditsRollView;
    private boolean mScrolling;
    private SeekBar mSeekBar;
    private ValueAnimator mScrollAnimator;

    public AboutDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_about, null);

        setContentView(view);

        String version = HiSettingsHelper.getInstance().getAppVersion();

        TextView versionView = (TextView) view.findViewById(R.id.version);
        versionView.setText("HiPDA怪兽版 " + version);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mCreditsRollView = (CreditsRollView) view.findViewById(R.id.creditsroll);
        mCreditsRollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mScrolling) {
                    if (mSeekBar.getProgress() == mSeekBar.getMax()) {
                        mSeekBar.setProgress(0);
                    }
                    animateScroll();
                } else {
                    stopScrollAnimation();
                }
            }
        });
        animateScroll();
    }


    private void animateScroll() {
        mScrolling = true;
        mScrollAnimator = ObjectAnimator.ofInt(mSeekBar, "progress", mSeekBar.getProgress(), mSeekBar.getMax());
        mScrollAnimator.setDuration(
                (long) (SCROLL_ANIM_DURATION * (1 - (float) mSeekBar.getProgress() / mSeekBar.getMax())));
        mScrollAnimator.setInterpolator(new LinearInterpolator());
        mScrollAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Don't care
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mScrolling = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Don't care
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Don't care
            }
        });
        mScrollAnimator.start();
    }

    private void stopScrollAnimation() {
        if (mScrollAnimator != null) {
            mScrollAnimator.cancel();
            mScrollAnimator = null;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mCreditsRollView.setScrollPosition(progress / 100000f); // We have increments of 1/100000 %
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mScrolling) {
            stopScrollAnimation();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Don't care
    }

}
