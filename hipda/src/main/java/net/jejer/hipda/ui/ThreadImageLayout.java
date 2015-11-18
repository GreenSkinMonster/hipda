package net.jejer.hipda.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.glide.GlideImageView;

/**
 * Layout contains thread image
 * Created by GreenSkinMonster on 2015-11-07.
 */
public class ThreadImageLayout extends RelativeLayout {

    private GlideImageView giv;
    private ProgressBar progressBar;
    private TextView imageInfo;

    public ThreadImageLayout(Context context) {
        this(context, null);
    }

    public ThreadImageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThreadImageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_thread_image, this, true);

        giv = (GlideImageView) findViewById(R.id.thread_image);
        progressBar = (ProgressBar) findViewById(R.id.thread_image_progress);
        imageInfo = (TextView) findViewById(R.id.thread_image_info);
    }

    public GlideImageView getImageView() {
        return giv;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getImageInfoTextView() {
        return imageInfo;
    }

}
