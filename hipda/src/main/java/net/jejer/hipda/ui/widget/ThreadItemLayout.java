package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.Utils;

/**
 * Created by GreenSkinMonster on 2016-04-21.
 */
public class ThreadItemLayout extends LinearLayout {

    private ImageView mAvatar;
    private TextView mTvAuthor;
    private TextView mTvThreadType;
    private TextView mTvTitle;
    private TextView mTvReplycounter;
    private TextView mTvCreateTime;
    private TextView mTvImageIndicator;

    private RequestManager mGlide;

    public ThreadItemLayout(Context context, RequestManager glide) {
        super(context, null, 0);
        inflate(context, R.layout.item_thread_list, this);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        setOrientation(VERTICAL);
        setPadding(Utils.dpToPx(context, 8), Utils.dpToPx(context, 4), Utils.dpToPx(context, 8), Utils.dpToPx(context, 4));

        mAvatar = (ImageView) findViewById(R.id.iv_avatar);
        mTvAuthor = (TextView) findViewById(R.id.tv_author);
        mTvThreadType = (TextView) findViewById(R.id.tv_thread_type);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvReplycounter = (TextView) findViewById(R.id.tv_replycounter);
        mTvCreateTime = (TextView) findViewById(R.id.tv_create_time);
        mTvImageIndicator = (TextView) findViewById(R.id.tv_image_indicator);
        mGlide = glide;
    }

    public void setData(final ThreadBean thread) {
        mTvAuthor.setText(thread.getAuthor());

        mTvTitle.setTextSize(HiSettingsHelper.getInstance().getTitleTextSize());
        mTvTitle.setText(thread.getTitle());

        String titleColor = Utils.nullToText(thread.getTitleColor()).trim();

        if (titleColor.startsWith("#")) {
            try {
                mTvTitle.setTextColor(Color.parseColor(titleColor));
            } catch (Exception ignored) {
                mTvTitle.setTextColor(ColorHelper.getTextColorPrimary(getContext()));
            }
        } else
            mTvTitle.setTextColor(ColorHelper.getTextColorPrimary(getContext()));

        if (HiSettingsHelper.getInstance().isShowPostType() &&
                !TextUtils.isEmpty(thread.getType())) {
            mTvThreadType.setText(thread.getType());
            mTvThreadType.setVisibility(View.VISIBLE);
        } else {
            mTvThreadType.setVisibility(View.GONE);
        }

        mTvReplycounter.setText(
                Utils.toCountText(thread.getCountCmts())
                        + "/"
                        + Utils.toCountText(thread.getCountViews()));

        mTvCreateTime.setText(Utils.shortyTime(thread.getTimeCreate()));

        if (thread.getHavePic()) {
            mTvImageIndicator.setVisibility(View.VISIBLE);
        } else {
            mTvImageIndicator.setVisibility(View.GONE);
        }

        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            mAvatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(mGlide, mAvatar, thread.getAvatarUrl());
        } else {
            mAvatar.setVisibility(View.GONE);
        }
        mAvatar.setTag(R.id.avatar_tag_uid, thread.getAuthorId());
        mAvatar.setTag(R.id.avatar_tag_username, thread.getAuthor());
    }

}
