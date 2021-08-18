package net.jejer.hipda.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.ui.adapter.ImageViewerAdapter;
import net.jejer.hipda.ui.widget.ImageViewerLayout;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Created by GreenSkinMonster on 2016-11-30.
 */
public class ImageViewerActivity extends AppCompatActivity {

    public static final String KEY_IMAGES = "images";
    public static final String KEY_IMAGE_INDEX = "imageIndex";
    public static final String KEY_ORI_IMAGE_RECT = "imageRect";

    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private View mImageViewerControls;

    private Rect mSourceRect;
    private String mSourceUrl;

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
            mSourceRect = intent.getExtras().getParcelable(KEY_ORI_IMAGE_RECT);
        }

        if (images.size() == 0) {
            finish();
        } else {
            mViewPager = findViewById(R.id.view_pager);
            final TextView tvImageInfo = findViewById(R.id.tv_image_info);
            final TextView tvFloorInfo = findViewById(R.id.tv_floor_info);

            mImageViewerControls = findViewById(R.id.image_viewer_controls);

            mPagerAdapter = new ImageViewerAdapter(this, images);
            mViewPager.setAdapter(mPagerAdapter);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

            mViewPager.setCurrentItem(imageIndex);
            ContentImg contentImg = images.get(imageIndex);
            mSourceUrl = contentImg.getContent();
            tvFloorInfo.setText(contentImg.getFloor() + "# " + contentImg.getAuthor());
            tvImageInfo.setText((imageIndex + 1) + " / " + images.size());

            ImageView btnDownload = findViewById(R.id.btn_download_image);
            btnDownload.setOnClickListener(
                    view -> {
                        String url = images.get(mViewPager.getCurrentItem()).getContent();
                        UIUtils.saveImage(ImageViewerActivity.this, findViewById(R.id.image_viewer), url);
                    }
            );

            ImageView btnShare = findViewById(R.id.btn_share_image);
            btnShare.setOnClickListener(
                    view -> {
                        String url = images.get(mViewPager.getCurrentItem()).getContent();
                        UIUtils.shareImage(ImageViewerActivity.this, findViewById(R.id.image_viewer), url);
                    }
            );

            animBackground(mViewPager);
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

    public Rect getSourceRect(String url) {
        if (url.equals(mSourceUrl)) {
            return mSourceRect;
        }
        return null;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (HiApplication.isFontSet())
            super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
        else
            super.attachBaseContext(newBase);
    }

    public void animImageThenFinish(ImageViewerLayout view) {
        mViewPager.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        mImageViewerControls.setVisibility(View.INVISIBLE);
        Rect rect = getSourceRect(view.getUrl());
        int x = 0;
        int y = 0;
        float scale = 0.2f;

        if (rect != null) {
            x = rect.centerX() - view.getMeasuredWidth() / 2;
            y = rect.centerY() - view.getMeasuredHeight() / 2;
            scale = Math.max((float) rect.height() / view.getMeasuredHeight(),
                    (float) rect.width() / view.getMeasuredWidth());
        }

        int delay = 0;
        if (view.resetScaleAndCenter(150)) {
            delay = 100;
        }

        final float fScale = scale;
        final int fX = x;
        final int fY = y;
        new Handler().postDelayed(
                () -> {
                    ViewPropertyAnimator animator = view.animate();
                    animator.setDuration(150)
                            .scaleX(fScale)
                            .scaleY(fScale)
                            .translationX(fX)
                            .translationY(fY)
                            .setUpdateListener(null)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    finish();
                                }
                            });
                    if (rect == null)
                        animator.alpha(0f);
                    animator.start();
                }, delay);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onDestroy() {
        if (mPagerAdapter != null) {
            new Handler().post(Utils::cleanShareTempFiles);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        View view = mViewPager.findViewWithTag(mViewPager.getCurrentItem());
        if (view instanceof ImageViewerLayout) {
            animImageThenFinish((ImageViewerLayout) view);
        } else {
            super.finish();
            overridePendingTransition(0, R.anim.fade_out);
        }
    }

    public void toggleFullscreen() {
        if (mImageViewerControls.getVisibility() != View.VISIBLE) {
            mImageViewerControls
                    .animate()
                    .alpha(1)
                    .setDuration(200)
                    .withEndAction(() -> mImageViewerControls.setVisibility(View.VISIBLE))
                    .start();
            UIUtils.showSystemUI(this);
        } else {
            mImageViewerControls
                    .animate()
                    .alpha(0)
                    .setDuration(200)
                    .withEndAction(() -> mImageViewerControls.setVisibility(View.INVISIBLE))
                    .start();
            UIUtils.hideSystemUI(this);
        }
    }

}
