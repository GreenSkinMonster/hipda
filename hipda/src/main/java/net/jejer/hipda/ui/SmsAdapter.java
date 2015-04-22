package net.jejer.hipda.ui;

import android.content.Context;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.glide.GlideHelper;

public class SmsAdapter extends HiAdapter<SimpleListItemBean> {
    private View.OnClickListener mAvatarListener;
    private LayoutInflater mInflater;
    private Context mCtx;

    public SmsAdapter(Context context, View.OnClickListener avatarListener) {
        mAvatarListener = avatarListener;
        mInflater = LayoutInflater.from(context);
        mCtx = context;
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

        holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
        holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
        holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);

        holder.iv_avatar.setTag(R.id.avatar_tag_uid, item.getUid());
        holder.iv_avatar.setTag(R.id.avatar_tag_username, item.getAuthor());
        if (!TextUtils.isEmpty(item.getUid())) {
            holder.iv_avatar.setOnClickListener(mAvatarListener);
        }

        holder.tv_author.setText(item.getAuthor());
        holder.tv_time.setText(item.getTime());
        holder.tv_content.setText(item.getInfo());
        holder.tv_content.setFocusable(false);
        holder.tv_content.setAutoLinkMask(Linkify.WEB_URLS);

        holder.tv_author.setTextSize(HiSettingsHelper.getPostTextSize());
        holder.tv_content.setTextSize(HiSettingsHelper.getPostTextSize());

        if (HiSettingsHelper.getInstance().isShowThreadListAvatar()) {
            holder.iv_avatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(mCtx, holder.iv_avatar, item.getAvatarUrl());
        } else {
            holder.iv_avatar.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tv_author;
        TextView tv_content;
        TextView tv_time;
        ImageView iv_avatar;
    }
}
