package net.jejer.hipda.ui;

import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.Utils;

public class SimpleListAdapter extends HiAdapter<SimpleListItemBean> {
    private LayoutInflater mInflater;
    private BaseFragment mFragment;
    private int mType;

    public SimpleListAdapter(BaseFragment fragment, int type) {
        mInflater = LayoutInflater.from(fragment.getActivity());
        mFragment = fragment;
        mType = type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleListItemBean item = getItem(position);

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.item_simple_list, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        holder.tv_info = (TextView) convertView.findViewById(R.id.tv_info);
        holder.tv_forum = (TextView) convertView.findViewById(R.id.tv_forum);
        holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);

        holder.tv_title.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        holder.tv_title.setText(Utils.trim(item.getTitle()));
        if (item.isNew()) {
            holder.tv_title.setTextColor(ContextCompat.getColor(mFragment.getActivity(), R.color.red));
        }

        if (TextUtils.isEmpty(item.getInfo())) {
            holder.tv_info.setVisibility(View.GONE);
        } else {
            holder.tv_info.setVisibility(View.VISIBLE);
            if (mType == SimpleListLoader.TYPE_THREAD_NOTIFY)
                holder.tv_info.setText(Html.fromHtml(item.getInfo()));
            else
                holder.tv_info.setText(item.getInfo());
            holder.tv_info.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        }

        if (TextUtils.isEmpty(item.getTime()) && TextUtils.isEmpty(item.getForum())) {
            holder.tv_time.setVisibility(View.GONE);
            holder.tv_forum.setVisibility(View.GONE);
        } else {
            holder.tv_time.setVisibility(View.VISIBLE);
            holder.tv_forum.setVisibility(View.VISIBLE);
            holder.tv_time.setText(item.getTime());
            holder.tv_forum.setText(item.getForum());
        }

        if (HiSettingsHelper.getInstance().isLoadAvatar()
                && mType != SimpleListLoader.TYPE_SEARCH_USER_THREADS
                && mType != SimpleListLoader.TYPE_FAVORITES
                && mType != SimpleListLoader.TYPE_ATTENTION
                && mType != SimpleListLoader.TYPE_MYPOST
                && mType != SimpleListLoader.TYPE_MYREPLY) {
            holder.iv_avatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(mFragment, holder.iv_avatar, item.getAvatarUrl());
        } else {
            holder.iv_avatar.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tv_title;
        TextView tv_forum;
        TextView tv_info;
        TextView tv_time;
        ImageView iv_avatar;
    }
}
