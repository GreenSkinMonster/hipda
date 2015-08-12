package net.jejer.hipda.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
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
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.File;
import java.util.List;

/**
 * image gallery
 * Created by GreenSkinMonster on 2015-05-20.
 */
public class PopupImageDialog extends DialogFragment {

    private final static int IMAGE_SHARE_ACTION = 1;
    private String localAbsoluteFilePath = "";

    private Context mCtx;
    private DetailListBean mDetailListBean;
    private int mImageIndex;

    private LayoutInflater mInflater;

    public PopupImageDialog() {
    }

    public void init(DetailListBean detailListBean, int imageIndex) {
        mDetailListBean = detailListBean;
        mImageIndex = imageIndex;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = getActivity();
        mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View layout = mInflater.inflate(R.layout.popup_image, null);

        final Dialog dialog = new Dialog(mCtx, android.R.style.Theme_Black_NoTitleBar);
        dialog.setContentView(layout);

        final ViewPager viewPager = (ViewPager) layout.findViewById(R.id.view_pager);
        final TextView tvImageInfo = (TextView) layout.findViewById(R.id.tv_image_info);
        final TextView tvFloorInfo = (TextView) layout.findViewById(R.id.tv_floor_info);

        final List<ContentImg> images = mDetailListBean.getContentImages();
        PagerAdapter adapter = new PopupImageAdapter(mCtx, images);
        viewPager.setAdapter(adapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ContentImg contentImg = images.get(position);
                tvFloorInfo.setText(contentImg.getFloor() + "# ");
                tvImageInfo.setText((position + 1) + " / " + images.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewPager.setCurrentItem(mImageIndex);
        ContentImg contentImg = images.get(mImageIndex);
        tvFloorInfo.setText(contentImg.getFloor() + "# ");
        tvImageInfo.setText((mImageIndex + 1) + " / " + images.size());

        ImageButton btnDownload = (ImageButton) layout.findViewById(R.id.btn_download_image);
        btnDownload.setImageDrawable(new IconicsDrawable(mCtx, GoogleMaterial.Icon.gmd_file_download).sizeDp(20).color(Color.WHITE));
        btnDownload.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View arg0) {
                                               try {
                                                   String url = images.get(viewPager.getCurrentItem()).getContent();
                                                   ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(url);
                                                   String filename = HiUtils.getImageFilename("Hi_IMG", imageReadyInfo.getMime());
                                                   HttpUtils.download(mCtx, url, filename);
                                               } catch (Exception e) {
                                                   Logger.e(e);
                                                   Toast.makeText(mCtx, "下载出现错误，请使用浏览器下载\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                               }
                                           }
                                       }

        );

        ImageButton btnShare = (ImageButton) layout.findViewById(R.id.btn_share_image);
        btnShare.setImageDrawable(new IconicsDrawable(mCtx, GoogleMaterial.Icon.gmd_share).sizeDp(20).color(Color.WHITE));

        btnShare.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            String url = images.get(viewPager.getCurrentItem()).getContent();

                                            //generate a random file name, will be deleted after share
                                            ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(url);
                                            String filename = HiUtils.getImageFilename("Hi_Share", imageReadyInfo.getMime());

                                            File destFile = new File(Environment.getExternalStoragePublicDirectory(
                                                    Environment.DIRECTORY_DOWNLOADS), filename);

                                            try {
                                                Utils.copy(new File(imageReadyInfo.getPath()), destFile);

                                                localAbsoluteFilePath = destFile.getAbsolutePath();

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
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_SHARE_ACTION) {
            if (!TextUtils.isEmpty(localAbsoluteFilePath)) {
                File file = new File(localAbsoluteFilePath);
                if (file.exists())
                    file.delete();
                localAbsoluteFilePath = "";
            }
        }

    }

}
