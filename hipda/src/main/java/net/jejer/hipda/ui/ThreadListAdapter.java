package net.jejer.hipda.ui;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.Utils;

public class ThreadListAdapter extends HiAdapter<ThreadBean> {

    private LayoutInflater mInflater;
    private ThreadListFragment mFragment;

    public ThreadListAdapter(ThreadListFragment fragment) {
        mInflater = LayoutInflater.from(fragment.getActivity());
        mFragment = fragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThreadBean thread = getItem(position);

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.item_thread_list, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
        holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
        holder.tv_thread_type = (TextView) convertView.findViewById(R.id.tv_thread_type);
        holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        holder.tv_replycounter = (TextView) convertView.findViewById(R.id.tv_replycounter);
        holder.tv_create_time = (TextView) convertView.findViewById(R.id.tv_create_time);
        holder.iv_image_indicator = (TextView) convertView.findViewById(R.id.iv_image_indicator);

        holder.tv_author.setText(thread.getAuthor());
        holder.tv_title.setText(thread.getTitle());

        if (HiSettingsHelper.getInstance().isShowPostType() &&
                !TextUtils.isEmpty(thread.getType())) {
            holder.tv_thread_type.setText(thread.getType());
            holder.tv_thread_type.setVisibility(View.VISIBLE);
        } else {
            holder.tv_thread_type.setVisibility(View.GONE);
        }

        holder.tv_title.setTextSize(HiSettingsHelper.getInstance().getTitleTextSize());

        holder.tv_replycounter.setText(thread.getCountCmts() + "/" + thread.getCountViews());

        holder.tv_create_time.setText(Utils.shortyTime(thread.getTimeCreate()));

        if (thread.getHavePic()) {
            holder.iv_image_indicator.setVisibility(View.VISIBLE);
        } else {
            holder.iv_image_indicator.setVisibility(View.GONE);
        }

        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            holder.avatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(mFragment, holder.avatar, thread.getAvatarUrl());
        } else {
            holder.avatar.setVisibility(View.GONE);
        }
        holder.avatar.setTag(R.id.avatar_tag_uid, thread.getAuthorId());
        holder.avatar.setTag(R.id.avatar_tag_username, thread.getAuthor());

        return convertView;
    }

    private static class ViewHolder {
        ImageView avatar;
        TextView tv_title;
        TextView tv_author;
        TextView tv_thread_type;
        TextView tv_replycounter;
        TextView tv_create_time;
        TextView iv_image_indicator;
    }
}
