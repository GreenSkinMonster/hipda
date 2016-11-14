package net.jejer.hipda.ui.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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
import net.jejer.hipda.ui.BaseFragment;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.HtmlCompat;
import net.jejer.hipda.utils.Utils;

/**
 * Created by GreenSkinMonster on 2016-11-14.
 */

public class SimpleListAdapter extends BaseRvAdapter<SimpleListItemBean> {

    private LayoutInflater mInflater;
    private BaseFragment mFragment;
    private int mType;

    public SimpleListAdapter(BaseFragment fragment, int type, RecyclerItemClickListener itemClickListener) {
        mInflater = LayoutInflater.from(fragment.getActivity());
        mFragment = fragment;
        mType = type;
        mListener = itemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType) {
        return new ViewHolderImpl(mInflater.inflate(R.layout.item_simple_list, parent, false));
    }

    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolderImpl holder = (ViewHolderImpl) viewHolder;

        SimpleListItemBean item = getItem(position);

        holder.tv_title.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        holder.tv_title.setText(Utils.trim(item.getTitle()));
        if (item.isNew()) {
            holder.tv_title.setTextColor(ContextCompat.getColor(mFragment.getActivity(), R.color.red));
        } else {
            holder.tv_title.setTextColor(ColorHelper.getDefaultTextColor(mFragment.getActivity()));
        }

        if (TextUtils.isEmpty(item.getInfo())) {
            holder.tv_info.setVisibility(View.GONE);
        } else {
            holder.tv_info.setVisibility(View.VISIBLE);
            if (mType == SimpleListLoader.TYPE_THREAD_NOTIFY)
                holder.tv_info.setText(HtmlCompat.fromHtml(item.getInfo()));
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
    }

    private static class ViewHolderImpl extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_forum;
        TextView tv_info;
        TextView tv_time;
        ImageView iv_avatar;

        ViewHolderImpl(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_info = (TextView) itemView.findViewById(R.id.tv_info);
            tv_forum = (TextView) itemView.findViewById(R.id.tv_forum);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            iv_avatar = (ImageView) itemView.findViewById(R.id.iv_avatar);
        }
    }

}
