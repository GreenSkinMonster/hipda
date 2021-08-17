package net.jejer.hipda.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.ui.adapter.ImageViewerAdapter;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Created by GreenSkinMonster on 2016-11-30.
 */
public class ImageViewerActivity extends AppCompatActivity {

    public static final String KEY_IMAGES = "images";
    public static final String KEY_IMAGE_INDEX = "imageIndex";

    private PagerAdapter mPagerAdapter;
    private View mImageViewerControls;
    private static boolean mSystemUIHided = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        int imageIndex = -1;
        ArrayList<ContentImg> images = new ArrayList<>();

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
        } else {
            imageIndex = intent.getExtras().getInt(KEY_IMAGE_INDEX);
            images.addAll(intent.getExtras().getParcelableArrayList(KEY_IMAGES));
        }

        if (images.size() == 0) {
            finish();
        } else {
            final ViewPager viewPager = findViewById(R.id.view_pager);
            final TextView tvImageInfo = findViewById(R.id.tv_image_info);
            final TextView tvFloorInfo = findViewById(R.id.tv_floor_info);

            mImageViewerControls = findViewById(R.id.image_viewer_controls);

            mPagerAdapter = new ImageViewerAdapter(this, images);
            viewPager.setAdapter(mPagerAdapter);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    ContentImg contentImg = images.get(position);
                    tvFloorInfo.setText(contentImg.getFloor() + "# " + contentImg.getAuthor());
                    tvImageInfo.setText((position + 1) + " / " + images.size());
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            viewPager.setCurrentItem(imageIndex);
            ContentImg contentImg = images.get(imageIndex);
            tvFloorInfo.setText(contentImg.getFloor() + "# " + contentImg.getAuthor());
            tvImageInfo.setText((imageIndex + 1) + " / " + images.size());

            ImageView btnDownload = findViewById(R.id.btn_download_image);
            btnDownload.setOnClickListener(
                    view -> {
                        String url = images.get(viewPager.getCurrentItem()).getContent();
                        UIUtils.saveImage(ImageViewerActivity.this, findViewById(R.id.image_viewer), url);
                    }
            );

            ImageView btnShare = findViewById(R.id.btn_share_image);
            btnShare.setOnClickListener(
                    view -> {
                        String url = images.get(viewPager.getCurrentItem()).getContent();
                        UIUtils.shareImage(ImageViewerActivity.this, findViewById(R.id.image_viewer), url);
                    }
            );

            animBackground(viewPager);
        }
    }

    private void animBackground(ViewPager viewPager) {
        int colorFrom = getResources().getColor(R.color.transparent);
        int colorTo = getResources().getColor(R.color.black);
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(colorFrom, colorTo);

        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                viewPager.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (HiApplication.isFontSet())
            super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
        else
            super.attachBaseContext(newBase);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }

    @Override
    public void onDestroy() {
        if (mPagerAdapter != null) {
            new Handler().post(Utils::cleanShareTempFiles);
        }
        super.onDestroy();
    }

    public void toggleFullscreen() {
        if (mSystemUIHided) {
            mSystemUIHided = false;
            UIUtils.showSystemUI(this);
            if (mImageViewerControls != null) {
                if (mImageViewerControls.getVisibility() != View.VISIBLE) {
                    Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                    anim.reset();
                    mImageViewerControls.clearAnimation();
                    mImageViewerControls.startAnimation(anim);
                    mImageViewerControls.setVisibility(View.VISIBLE);
                }
            }
        } else {
            mSystemUIHided = true;
            UIUtils.hideSystemUI(this);
            if (mImageViewerControls != null) {
                if (mImageViewerControls.getVisibility() != View.INVISIBLE) {
                    Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                    anim.reset();
                    mImageViewerControls.clearAnimation();
                    mImageViewerControls.startAnimation(anim);
                    mImageViewerControls.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

}
