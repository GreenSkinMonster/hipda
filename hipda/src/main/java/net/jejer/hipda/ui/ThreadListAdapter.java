package net.jejer.hipda.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.Utils;

import java.util.HashMap;
import java.util.List;

public class ThreadListAdapter extends ArrayAdapter<ThreadBean> {

    private LayoutInflater mInflater;
    private Context mCtx;
    //private List<ThreadBean> threads;
    private HashMap<String, ViewHolder> holders = new HashMap<String, ViewHolder>();

    public ThreadListAdapter(Context context, int resource,
                             List<ThreadBean> objects) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mCtx = context;
        //threads = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThreadBean thread = getItem(position);

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.item_thread_list, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
        holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
        holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        holder.tv_viewcounter = (TextView) convertView.findViewById(R.id.tv_viewcounter);
        holder.tv_replycounter = (TextView) convertView.findViewById(R.id.tv_replycounter);
        holder.tv_create_time = (TextView) convertView.findViewById(R.id.tv_create_time);
        //holder.tv_update_time = (TextView) convertView.findViewById(R.id.tv_update_time);
        holder.iv_image_indicator = (ImageView) convertView.findViewById(R.id.iv_image_indicator);

        holders.put(thread.getTid(), holder);

        holder.tv_author.setText(thread.getAuthor());

        holder.tv_title.setText(thread.getTitle());
        holder.tv_title.setTextSize(HiSettingsHelper.getTitleTextSize());
        if (thread.getCountCmts() != null) {
            holder.tv_replycounter.setText(thread.getCountCmts());
        }
        if (thread.getCountViews() != null) {
            holder.tv_viewcounter.setText(thread.getCountViews());
        }
        holder.tv_create_time.setText(Utils.shortyTime(thread.getTimeCreate()));
        //holder.tv_update_time.setText(Utils.shortyTime(thread.getTimeUpdate()));

        if (thread.getHavePic()) {
            holder.iv_image_indicator.setVisibility(View.VISIBLE);
        } else {
            holder.iv_image_indicator.setVisibility(View.GONE);
        }

        if (HiSettingsHelper.getInstance().isShowThreadListAvatar()) {
            holder.avatar.setVisibility(View.VISIBLE);
            GlideHelper.loadAvatar(getContext(), holder.avatar, thread.getAvatarUrl());
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
        TextView tv_viewcounter;
        TextView tv_replycounter;
        TextView tv_create_time;
        //TextView tv_update_time;
        ImageView iv_image_indicator;
    }
}
