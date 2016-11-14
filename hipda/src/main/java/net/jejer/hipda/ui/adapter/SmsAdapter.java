package net.jejer.hipda.ui.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.ui.SmsFragment;
import net.jejer.hipda.ui.TextViewWithEmoticon;
import net.jejer.hipda.utils.Utils;

/**
 * Created by GreenSkinMonster on 2016-11-14.
 */

public class SmsAdapter extends BaseRvAdapter<SimpleListItemBean> {

    private View.OnClickListener mAvatarListener;
    private LayoutInflater mInflater;
    private SmsFragment mFragment;

    public SmsAdapter(SmsFragment fragment, View.OnClickListener avatarListener, RecyclerItemClickListener itemClickListener) {
        mAvatarListener = avatarListener;
        mInflater = LayoutInflater.from(fragment.getActivity());
        mFragment = fragment;
        mListener = itemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType) {
        return new ViewHolderImpl(mInflater.inflate(R.layout.item_sms_list, parent, false));
    }

    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolderImpl holder = (ViewHolderImpl) viewHolder;

        SimpleListItemBean item = getItem(position);

        holder.iv_my_avatar.setVisibility(View.INVISIBLE);
        holder.iv_friend_avatar.setVisibility(View.INVISIBLE);

        if (item.getUid().equals(HiSettingsHelper.getInstance().getUid())) {
            holder.iv_my_avatar.setTag(R.id.avatar_tag_uid, item.getUid());
            holder.iv_my_avatar.setTag(R.id.avatar_tag_username, item.getAuthor());
        } else {
            holder.iv_friend_avatar.setTag(R.id.avatar_tag_uid, item.getUid());
            holder.iv_friend_avatar.setTag(R.id.avatar_tag_username, item.getAuthor());
        }

        if (item.getUid().equals(HiSettingsHelper.getInstance().getUid())) {
            holder.info_layout.setGravity(Gravity.RIGHT);
            holder.bubble_layout.setArrowDirection(ArrowDirection.RIGHT);
            holder.bubble_layout.setBubbleColor(ContextCompat.getColor(mFragment.getActivity(), R.color.md_yellow_400));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.bubble_layout.getLayoutParams();
            params.gravity = Gravity.RIGHT;
            holder.bubble_layout.setLayoutParams(params);

            if (HiSettingsHelper.getInstance().isLoadAvatar()) {
                holder.iv_my_avatar.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(item.getUid())) {
                    holder.iv_my_avatar.setOnClickListener(mAvatarListener);
                }
                GlideHelper.loadAvatar(mFragment, holder.iv_my_avatar, item.getAvatarUrl());
            }
        } else {
            holder.info_layout.setGravity(Gravity.LEFT);
            holder.bubble_layout.setArrowDirection(ArrowDirection.LEFT);
            holder.bubble_layout.setForegroundGravity(Gravity.LEFT);
            holder.bubble_layout.setBubbleColor(ContextCompat.getColor(mFragment.getActivity(), R.color.md_grey_300));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.bubble_layout.getLayoutParams();
            params.gravity = Gravity.LEFT;
            holder.bubble_layout.setLayoutParams(params);

            if (HiSettingsHelper.getInstance().isLoadAvatar()) {
                holder.iv_friend_avatar.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(item.getUid())) {
                    holder.iv_friend_avatar.setOnClickListener(mAvatarListener);
                }
                GlideHelper.loadAvatar(mFragment, holder.iv_friend_avatar, item.getAvatarUrl());
            }
        }

        if (!HiSettingsHelper.getInstance().isLoadAvatar()) {
            if (item.getUid().equals(HiSettingsHelper.getInstance().getUid())) {
                holder.iv_my_avatar.setVisibility(View.GONE);
                holder.iv_friend_avatar.setVisibility(View.INVISIBLE);
            } else {
                holder.iv_my_avatar.setVisibility(View.INVISIBLE);
                holder.iv_friend_avatar.setVisibility(View.GONE);
            }
        }

        holder.tv_time.setText(Utils.shortyTime(item.getTime()));
        holder.tv_content.setFragment(mFragment);
        //hack, replace url to link, only parse plain text
        String text = Utils.nullToText(item.getInfo());
        if (!text.contains("<a") && text.contains("http"))
            text = Utils.textToHtmlConvertingURLsToLinks(text);
        holder.tv_content.setText(text);
        holder.tv_content.setFocusable(false);

        holder.tv_content.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        holder.tv_content.setTextColor(ContextCompat.getColor(mFragment.getActivity(), R.color.black));

        if (item.isNew())
            holder.tv_isnew.setVisibility(View.VISIBLE);
        else
            holder.tv_isnew.setVisibility(View.GONE);
    }

    private static class ViewHolderImpl extends RecyclerView.ViewHolder {
        TextViewWithEmoticon tv_content;
        TextView tv_time;
        TextView tv_isnew;
        ImageView iv_my_avatar;
        ImageView iv_friend_avatar;
        LinearLayout info_layout;
        BubbleLayout bubble_layout;

        ViewHolderImpl(View itemView) {
            super(itemView);

            tv_content = (TextViewWithEmoticon) itemView.findViewById(R.id.tv_content);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_isnew = (TextView) itemView.findViewById(R.id.tv_isnew);
            iv_my_avatar = (ImageView) itemView.findViewById(R.id.iv_my_avatar);
            iv_friend_avatar = (ImageView) itemView.findViewById(R.id.iv_friend_avatar);
            info_layout = (LinearLayout) itemView.findViewById(R.id.sms_info_layout);
            bubble_layout = (BubbleLayout) itemView.findViewById(R.id.bl_bubble);

        }
    }

}
