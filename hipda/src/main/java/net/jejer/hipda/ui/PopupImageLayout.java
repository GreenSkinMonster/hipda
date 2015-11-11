package net.jejer.hipda.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.glide.GlideImageView;

/**
 * Layout contains thread image
 * Created by GreenSkinMonster on 2015-11-07.
 */
public class PopupImageLayout extends RelativeLayout {

    private final SubsamplingScaleImageView scaleImageView;
    private final GlideImageView glideImageView;
    private final ProgressBar progressBar;

    private String url;

    public PopupImageLayout(Context context) {
        this(context, null);
    }

    public PopupImageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopupImageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_popup_image, this, true);

        scaleImageView = (SubsamplingScaleImageView) findViewById(R.id.scale_image);
        glideImageView = (GlideImageView) findViewById(R.id.glide_image);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    public GlideImageView getGlideImageView() {
        return glideImageView;
    }

    public SubsamplingScaleImageView getScaleImageView() {
        return scaleImageView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void recycle() {
        scaleImageView.recycle();
        Glide.clear(glideImageView);
    }
}
