package net.jejer.hipda.ui;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.glide.ImageReadyInfo;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * image gallery
 * Created by GreenSkinMonster on 2015-05-20.
 */
public class PopupImageDialog extends DialogFragment {

    private final static int IMAGE_SHARE_ACTION = 1;

    private Context mCtx;
    private DetailListBean mDetailListBean;
    private int mImageIndex;

    private LayoutInflater mInflater;
    private PagerAdapter mPagerAdapter;

    private boolean lastClicked = false;
    private boolean firstClicked = false;

    private String mSessionId;

    public PopupImageDialog() {
    }

    public void init(DetailListBean detailListBean, int imageIndex, String sessionId) {
        mDetailListBean = detailListBean;
        mImageIndex = imageIndex;
        mSessionId = sessionId;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mCtx = getActivity();
        mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View layout = mInflater.inflate(R.layout.popup_image, null);

        final Dialog dialog = new Dialog(mCtx, android.R.style.Theme_Black_NoTitleBar);
        dialog.setContentView(layout);

        final ImageViewPager viewPager = (ImageViewPager) layout.findViewById(R.id.view_pager);
        final TextView tvImageInfo = (TextView) layout.findViewById(R.id.tv_image_info);
        final TextView tvFloorInfo = (TextView) layout.findViewById(R.id.tv_floor_info);
        final Button btnBack = (Button) layout.findViewById(R.id.btn_back);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final List<ContentImg> images = mDetailListBean.getContentImages();
        mPagerAdapter = new PopupImageAdapter(this, images, mSessionId);
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
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewPager.setOnSwipeOutListener(new ImageViewPager.OnSwipeOutListener() {

            @Override
            public void onSwipeOutAtStart() {
                dismiss();
            }

            @Override
            public void onSwipeOutAtEnd() {
                dismiss();
            }
        });

        viewPager.setCurrentItem(mImageIndex);
        ContentImg contentImg = images.get(mImageIndex);
        tvFloorInfo.setText(contentImg.getFloor() + "# " + contentImg.getAuthor());
        tvImageInfo.setText((mImageIndex + 1) + " / " + images.size());

        tvImageInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        String url = images.get(viewPager.getCurrentItem()).getContent();
                        ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(url);
                        if (imageReadyInfo == null || !imageReadyInfo.isReady()) {
                            Toast.makeText(mCtx, "文件还未下载完成", Toast.LENGTH_SHORT).show();
                        } else {
                            File f = new File(imageReadyInfo.getPath());

                            String msg = "格式　: " + imageReadyInfo.getMime()
                                    + "\n分辨率: " + imageReadyInfo.getWidth() + "x" + imageReadyInfo.getHeight()
                                    + "\n大小　: " + Utils.toSizeText(f.length());

                            Toast.makeText(mCtx, msg, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        ImageButton btnDownload = (ImageButton) layout.findViewById(R.id.btn_download_image);
        btnDownload.setImageDrawable(new IconicsDrawable(mCtx, GoogleMaterial.Icon.gmd_download)
                .sizeDp(24).color(ContextCompat.getColor(mCtx, R.color.silver)));
        btnDownload.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        try {
                            if (ContextCompat.checkSelfPermission(getActivity(),
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getActivity(), "需要在权限管理中授权存储空间权限", Toast.LENGTH_SHORT).show();
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MainFrameActivity.PERMISSIONS_REQUEST_CODE);
                                return;
                            }

                            String url = images.get(viewPager.getCurrentItem()).getContent();
                            ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(url);
                            if (imageReadyInfo == null || !imageReadyInfo.isReady()) {
                                Toast.makeText(mCtx, "文件还未下载完成", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String filename = Utils.getImageFileName("Hi_IMG", imageReadyInfo.getMime());
                            File destFile = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS), filename);
                            Utils.copy(new File(imageReadyInfo.getPath()), destFile);
                            Toast.makeText(mCtx, "图片已经保存至下载目录 <" + filename + ">", Toast.LENGTH_SHORT).show();
                            //HttpUtils.download(mCtx, url, filename);

                            MediaScannerConnection.scanFile(mCtx, new String[]{destFile.getPath()}, null, null);
                        } catch (Exception e) {
                            Logger.e(e);
                            Toast.makeText(mCtx, "保存图片文件时发生错误，请使用浏览器下载\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }

        );

        ImageButton btnShare = (ImageButton) layout.findViewById(R.id.btn_share_image);
        btnShare.setImageDrawable(new IconicsDrawable(mCtx, GoogleMaterial.Icon.gmd_share)
                .sizeDp(24).color(ContextCompat.getColor(mCtx, R.color.silver)));

        btnShare.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getActivity(), "需要在权限管理中授权存储空间权限", Toast.LENGTH_SHORT).show();
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MainFrameActivity.PERMISSIONS_REQUEST_CODE);
                            return;
                        }

                        String url = images.get(viewPager.getCurrentItem()).getContent();

                        //generate a random file name, will be deleted after share
                        ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(url);
                        if (imageReadyInfo == null || !imageReadyInfo.isReady()) {
                            Toast.makeText(mCtx, "文件还未下载完成", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String filename = Utils.getImageFileName(Constants.FILE_SHARE_PREFIX, imageReadyInfo.getMime());

                        File destFile = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS), filename);

                        try {
                            Utils.copy(new File(imageReadyInfo.getPath()), destFile);

                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType(imageReadyInfo.getMime());
                            Uri uri = Uri.fromFile(destFile);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            startActivity(Intent.createChooser(shareIntent, "分享图片"));
                        } catch (Exception e) {
                            Logger.e(e);
                            Toast.makeText(mCtx, "分享时发生错误", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        ImageButton btnNext = (ImageButton) layout.findViewById(R.id.btn_next_image);
        btnNext.setImageDrawable(new IconicsDrawable(mCtx, GoogleMaterial.Icon.gmd_chevron_right)
                .sizeDp(24).color(ContextCompat.getColor(mCtx, R.color.silver)));
        btnNext.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewPager.getCurrentItem() < images.size() - 1) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                            firstClicked = false;
                        } else {
                            if (!lastClicked) {
                                Toast.makeText(mCtx, "已经是最后一张，再次点击关闭.", Toast.LENGTH_SHORT).show();
                                lastClicked = true;
                            } else {
                                dismiss();
                            }
                        }
                    }
                }

        );

        ImageButton btnPrev = (ImageButton) layout.findViewById(R.id.btn_previous_image);
        btnPrev.setImageDrawable(new IconicsDrawable(mCtx, GoogleMaterial.Icon.gmd_chevron_left)
                .sizeDp(24).color(ContextCompat.getColor(mCtx, R.color.silver)));
        btnPrev.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewPager.getCurrentItem() > 0) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                            lastClicked = false;
                        } else {
                            if (!firstClicked) {
                                Toast.makeText(mCtx, "已经是第一张，再次点击关闭.", Toast.LENGTH_SHORT).show();
                                firstClicked = true;
                            } else {
                                dismiss();
                            }
                        }
                    }
                }

        );
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_SHARE_ACTION) {
        }

    }

    @Override
    public void onDestroy() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Utils.cleanShareTempFiles();
            }
        });
        EventBus.getDefault().unregister(mPagerAdapter);
        super.onDestroy();
    }
}
