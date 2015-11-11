package net.jejer.hipda.ui;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.Utils;

public class SmsAdapter extends HiAdapter<SimpleListItemBean> {
    private View.OnClickListener mAvatarListener;
    private LayoutInflater mInflater;
    private SmsFragment mFragment;

    public SmsAdapter(SmsFragment fragment, View.OnClickListener avatarListener) {
        mAvatarListener = avatarListener;
        mInflater = LayoutInflater.from(fragment.getActivity());
        mFragment = fragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleListItemBean item = getItem(position);

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.item_sms_list, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_content = (TextViewWithEmoticon) convertView.findViewById(R.id.tv_content);
        holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        holder.tv_isnew = (TextView) convertView.findViewById(R.id.tv_isnew);
        holder.iv_my_avatar = (ImageView) convertView.findViewById(R.id.iv_my_avatar);
        holder.iv_friend_avatar = (ImageView) convertView.findViewById(R.id.iv_friend_avatar);
        holder.info_layout = (LinearLayout) convertView.findViewById(R.id.sms_info_layout);

        if (item.getUid().equals(HiSettingsHelper.getInstance().getUid())) {
            holder.iv_my_avatar.setTag(R.id.avatar_tag_uid, item.getUid());
            holder.iv_my_avatar.setTag(R.id.avatar_tag_username, item.getAuthor());
        } else {
            holder.iv_friend_avatar.setTag(R.id.avatar_tag_uid, item.getUid());
            holder.iv_friend_avatar.setTag(R.id.avatar_tag_username, item.getAuthor());
        }

        if (item.getUid().equals(HiSettingsHelper.getInstance().getUid())) {
            holder.tv_content.setGravity(Gravity.RIGHT);
            holder.info_layout.setGravity(Gravity.RIGHT);
            if (HiSettingsHelper.getInstance().isLoadAvatar()) {
                holder.iv_my_avatar.setVisibility(View.VISIBLE);
                holder.iv_friend_avatar.setVisibility(View.INVISIBLE);
                if (!TextUtils.isEmpty(item.getUid())) {
                    holder.iv_my_avatar.setOnClickListener(mAvatarListener);
                }
                GlideHelper.loadAvatar(mFragment, holder.iv_my_avatar, item.getAvatarUrl());
            }
        } else {
            holder.tv_content.setGravity(Gravity.LEFT);
            holder.info_layout.setGravity(Gravity.LEFT);
            if (HiSettingsHelper.getInstance().isLoadAvatar()) {
                holder.iv_friend_avatar.setVisibility(View.VISIBLE);
                holder.iv_my_avatar.setVisibility(View.INVISIBLE);
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

        if (item.isNew())
            holder.tv_isnew.setVisibility(View.VISIBLE);
        else
            holder.tv_isnew.setVisibility(View.GONE);

        return convertView;
    }

    private static class ViewHolder {
        TextViewWithEmoticon tv_content;
        TextView tv_time;
        TextView tv_isnew;
        ImageView iv_my_avatar;
        ImageView iv_friend_avatar;
        LinearLayout info_layout;
    }
}
