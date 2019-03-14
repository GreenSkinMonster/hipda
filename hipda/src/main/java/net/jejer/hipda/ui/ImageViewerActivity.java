package net.jejer.hipda.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.ui.adapter.ImageViewerAdapter;
import net.jejer.hipda.ui.widget.swipeback.SwipeBackLayout;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by GreenSkinMonster on 2016-11-30.
 */
public class ImageViewerActivity extends SwipeBackActivity {

    public static final String KEY_IMAGES = "images";
    public static final String KEY_IMAGE_INDEX = "imageIndex";

    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
        }

        int imageIndex = intent.getExtras().getInt(KEY_IMAGE_INDEX);
        final ArrayList<ContentImg> images = intent.getExtras().getParcelableArrayList(KEY_IMAGES);

        if (images == null || images.size() == 0) {
            finish();
        } else {
            final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
            final TextView tvImageInfo = (TextView) findViewById(R.id.tv_image_info);
            final TextView tvFloorInfo = (TextView) findViewById(R.id.tv_floor_info);

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
                    updateSwipeEdges(images.size(), position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            viewPager.setCurrentItem(imageIndex);
            ContentImg contentImg = images.get(imageIndex);
            tvFloorInfo.setText(contentImg.getFloor() + "# " + contentImg.getAuthor());
            tvImageInfo.setText((imageIndex + 1) + " / " + images.size());

            updateSwipeEdges(images.size(), imageIndex);
            getSwipeBackLayout().setEdgeSize(Utils.dpToPx(50));

            ImageButton btnDownload = (ImageButton) findViewById(R.id.btn_download_image);
            btnDownload.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            String url = images.get(viewPager.getCurrentItem()).getContent();
                            UIUtils.saveImage(ImageViewerActivity.this, findViewById(R.id.image_viewer), url);
                        }
                    }

            );

            ImageButton btnShare = (ImageButton) findViewById(R.id.btn_share_image);

            btnShare.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            String url = images.get(viewPager.getCurrentItem()).getContent();
                            UIUtils.shareImage(ImageViewerActivity.this, findViewById(R.id.image_viewer), url);
                        }
                    }
            );
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (HiApplication.isFontSet())
            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        else
            super.attachBaseContext(newBase);
    }

    private void updateSwipeEdges(int total, int position) {
        if (total == 1) {
            getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_ALL);
        } else if (position == 0) {
            getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        } else if (position == total - 1) {
            getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_RIGHT);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }

    @Override
    public void onDestroy() {
        if (mPagerAdapter != null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Utils.cleanShareTempFiles();
                }
            });
        }
        super.onDestroy();
    }

}
