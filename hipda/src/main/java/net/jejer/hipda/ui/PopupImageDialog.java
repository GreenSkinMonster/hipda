package net.jejer.hipda.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Logger;

import java.util.List;

/**
 * image gallery
 * Created by GreenSkinMonster on 2015-05-20.
 */
public class PopupImageDialog extends Dialog {

    private Context mCtx;
    private DetailListBean mDetailListBean;
    private int mImageIndex;

    private LayoutInflater mInflater;

    public PopupImageDialog(Context context, DetailListBean detailListBean, int imageIndex) {
        super(context, android.R.style.Theme_Black_NoTitleBar);
        mCtx = context;
        mDetailListBean = detailListBean;
        mImageIndex = imageIndex;

        mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View layout = mInflater.inflate(R.layout.popup_image, null);

        setContentView(layout);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
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
                                                   String filename = url.substring(url.lastIndexOf("/") + 1);
                                                   HttpUtils.download(mCtx, url, filename);
                                               } catch (SecurityException e) {
                                                   Logger.e(e);
                                                   Toast.makeText(mCtx, "下载出现错误，请使用浏览器下载\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                               }
                                           }
                                       }

        );

    }
}
