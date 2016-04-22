package net.jejer.hipda.ui;

import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;

import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.ui.widget.ThreadItemLayout;

public class ThreadListAdapter extends HiAdapter<ThreadBean> {

    private RequestManager mGlide;

    public ThreadListAdapter(RequestManager glide) {
        mGlide = glide;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThreadBean thread = getItem(position);

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = new ThreadItemLayout(parent.getContext(), mGlide);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.mItemLayout = (ThreadItemLayout) convertView;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mItemLayout.setData(thread);
        return convertView;
    }

    private static class ViewHolder {
        ThreadItemLayout mItemLayout;
    }
}
