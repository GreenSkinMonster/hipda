package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.RequestManager;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

/**
 * Created by GreenSkinMonster on 2016-04-21.
 */
public class ThreadItemLayout extends ConstraintLayout {

    private ImageView mAvatar;
    private TextView mTvAuthor;
    private TextView mTvThreadType;
    private TextView mTvTitle;
    private TextView mTvReplycounter;
    private TextView mTvCreateTime;
    private ImageView mIvIndicator;

    private RequestManager mGlide;

    public ThreadItemLayout(Context context, RequestManager glide) {
        super(context, null, 0);
        inflate(context, R.layout.item_thread_list, this);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        setPadding(Utils.dpToPx(8), Utils.dpToPx(4), Utils.dpToPx(8), Utils.dpToPx(4));

        mAvatar = findViewById(R.id.iv_avatar);
        mTvAuthor = findViewById(R.id.tv_username);
        mTvThreadType = findViewById(R.id.tv_thread_type);
        mTvTitle = findViewById(R.id.tv_title);
        mTvReplycounter = findViewById(R.id.tv_replycounter);
        mTvCreateTime = findViewById(R.id.tv_create_time);
        mIvIndicator = findViewById(R.id.iv_indicator);
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

        if(HiSettingsHelper.getInstance().isEinkMode())
            UIUtils.setEinkDisplayOptimize(mTvTitle);

        if (!TextUtils.isEmpty(thread.getType())) {
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

        if (thread.isPoll()) {
            mIvIndicator.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.outline_poll_white_24));
            mIvIndicator.setVisibility(View.VISIBLE);
        } else if (thread.isWithPic()) {
            mIvIndicator.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.outline_image_white_24));
            mIvIndicator.setVisibility(View.VISIBLE);
        } else {
            mIvIndicator.setVisibility(View.GONE);
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
