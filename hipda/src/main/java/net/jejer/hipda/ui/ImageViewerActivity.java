package net.jejer.hipda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.ui.adapter.ImageViewerAdapter;
import net.jejer.hipda.ui.widget.ImageViewPager;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by GreenSkinMonster on 2016-11-30.
 */

public class ImageViewerActivity extends AppCompatActivity {

    public static final String KEY_IMAGES = "images";
    public static final String KEY_IMAGE_INDEX = "imageIndex";

    private PagerAdapter mPagerAdapter;

    private boolean lastClicked = false;
    private boolean firstClicked = false;

    private String mSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        mSessionId = UUID.randomUUID().toString();

        Intent intent = getIntent();
        int imageIndex = intent.getExtras().getInt(KEY_IMAGE_INDEX);
        final ArrayList<ContentImg> images = intent.getExtras().getParcelableArrayList(KEY_IMAGES);

        final ImageViewPager viewPager = (ImageViewPager) findViewById(R.id.view_pager);
        final TextView tvImageInfo = (TextView) findViewById(R.id.tv_image_info);
        final TextView tvImageFileInfo = (TextView) findViewById(R.id.tv_image_file_info);
        final TextView tvFloorInfo = (TextView) findViewById(R.id.tv_floor_info);
        final Button btnBack = (Button) findViewById(R.id.btn_back);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (images == null || images.size() == 0) {
            finish();
        }

        mPagerAdapter = new ImageViewerAdapter(this, images, mSessionId);
        viewPager.setAdapter(mPagerAdapter);

        EventBus.getDefault().register(mPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ContentImg contentImg = images.get(position);
                tvFloorInfo.setText(contentImg.getFloor() + "# " + contentImg.getAuthor());
                tvImageInfo.setText((position + 1) + " / " + images.size());
                String url = images.get(viewPager.getCurrentItem()).getContent();
                updateImageFileInfo(tvImageFileInfo, url);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewPager.setOnSwipeOutListener(new ImageViewPager.OnSwipeOutListener() {

            @Override
            public void onSwipeOutAtStart() {
                finishRight();
            }

            @Override
            public void onSwipeOutAtEnd() {
                finishLeft();
            }
        });

        viewPager.setCurrentItem(imageIndex);
        ContentImg contentImg = images.get(imageIndex);
        tvFloorInfo.setText(contentImg.getFloor() + "# " + contentImg.getAuthor());
        tvImageInfo.setText((imageIndex + 1) + " / " + images.size());

        String url = images.get(viewPager.getCurrentItem()).getContent();
        updateImageFileInfo(tvImageFileInfo, url);

        tvImageInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (tvImageFileInfo.getVisibility() == View.GONE) {
                            tvImageFileInfo.setVisibility(View.VISIBLE);
                        } else {
                            tvImageFileInfo.setVisibility(View.GONE);
                        }
                    }
                }
        );

        ImageButton btnDownload = (ImageButton) findViewById(R.id.btn_download_image);
        btnDownload.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_file_download)
                .sizeDp(24).color(ContextCompat.getColor(this, R.color.silver)));
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
        btnShare.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_share)
                .sizeDp(24).color(ContextCompat.getColor(this, R.color.silver)));

        btnShare.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        String url = images.get(viewPager.getCurrentItem()).getContent();
                        UIUtils.shareImage(ImageViewerActivity.this, findViewById(R.id.image_viewer), url);
                    }
                }
        );

        ImageButton btnNext = (ImageButton) findViewById(R.id.btn_next_image);
//        btnNext.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_chevron_right)
//                .sizeDp(24).color(ContextCompat.getColor(this, R.color.silver)));
        btnNext.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewPager.getCurrentItem() < images.size() - 1) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                            firstClicked = false;
                        } else {
                            if (!lastClicked) {
                                Toast.makeText(ImageViewerActivity.this, "已经是最后一张，再次点击关闭.", Toast.LENGTH_SHORT).show();
                                lastClicked = true;
                            } else {
                                finish();
                            }
                        }
                    }
                }
        );

        ImageButton btnPrev = (ImageButton) findViewById(R.id.btn_previous_image);
//        btnPrev.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_chevron_left)
//                .sizeDp(24).color(ContextCompat.getColor(this, R.color.silver)));
        btnPrev.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewPager.getCurrentItem() > 0) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                            lastClicked = false;
                        } else {
                            if (!firstClicked) {
                                Toast.makeText(ImageViewerActivity.this, "已经是第一张，再次点击关闭.", Toast.LENGTH_SHORT).show();
                                firstClicked = true;
                            } else {
                                finish();
                            }
                        }
                    }
                }
        );
    }

    private void updateImageFileInfo(TextView tvImageFileInfo, String url) {
        ImageInfo imageInfo = ImageContainer.getImageInfo(url);
        if (imageInfo != null && imageInfo.isReady()) {
            String msg = imageInfo.getWidth() + "x" + imageInfo.getHeight()
                    + " / " + Utils.toSizeText(imageInfo.getFileSize());
            if (HiSettingsHelper.getInstance().isErrorReportMode()) {
                DecimalFormat df = new DecimalFormat("#.##");
                msg += " / " + df.format(imageInfo.getSpeed()) + " K/s";
            }
            tvImageFileInfo.setText(msg);
        } else {
            tvImageFileInfo.setText("?");
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }

    public void finishLeft() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_left);
    }

    public void finishRight() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    @Override
    public void onDestroy() {
        if (mPagerAdapter != null) {
            EventBus.getDefault().unregister(mPagerAdapter);
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
