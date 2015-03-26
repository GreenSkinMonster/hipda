package net.jejer.hipda.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.glide.GlideHelper;

import java.util.ArrayList;

public class SmsAdapter extends ArrayAdapter<SimpleListItemBean> {
    private View.OnClickListener mAvatarListener;
    private String mId;
    private String mUid;
    private LayoutInflater mInflater;

    public SmsAdapter(Context context, String id, String Uid, int resource , View.OnClickListener avatarListener) {
        super(context, resource, new ArrayList<SimpleListItemBean>());
        mAvatarListener = avatarListener;
        mId = id;
        mUid = Uid;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleListItemBean item = getItem(position);

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.item_sms_list, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
        holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
        holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);

        holder.iv_avatar.setTag(R.id.avatar_tag_uid, mUid);
        holder.iv_avatar.setTag(R.id.avatar_tag_username, mId);
        holder.iv_avatar.setOnClickListener(mAvatarListener);

        holder.tv_author.setText(item.getAuthor());
        holder.tv_time.setText(item.getTime());
        holder.tv_content.setText(item.getInfo());

        holder.tv_author.setTextSize(HiSettingsHelper.getPostTextSize());
        holder.tv_content.setTextSize(HiSettingsHelper.getPostTextSize());

        if (HiSettingsHelper.getInstance().isShowThreadListAvatar()) {
            holder.iv_avatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(getContext(), holder.iv_avatar, item.getAvatarUrl());
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
