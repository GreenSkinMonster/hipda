package net.jejer.hipda.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;

public class DrawerAdapter extends ArrayAdapter<String> {
    private Context mCtx;
    private LayoutInflater mInflater;
    private int mItemRsc;

    public DrawerAdapter(Context context, int resource, String[] o) {
        super(context, resource, o);
        mCtx = context;
        mItemRsc = resource;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String title = getItem(position);
        int color = 0;
        Drawable icon = null;

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(mItemRsc, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        holder.iv_color_block = (ImageView) convertView.findViewById(R.id.iv_color_block);
        holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);

        if (title.equals(mCtx.getString(R.string.title_drawer_forum))) {
            color = mCtx.getResources().getColor(R.color.blue);
            icon = mCtx.getResources().getDrawable(R.drawable.google_paragraph_align_left);
        } else if (title.equals(mCtx.getString(R.string.title_drawer_mypost))) {
            color = mCtx.getResources().getColor(R.color.darkorange);
            icon = mCtx.getResources().getDrawable(R.drawable.google_user);
        } else if (title.equals(mCtx.getString(R.string.title_drawer_myreply))) {
            color = mCtx.getResources().getColor(R.color.purple);
            icon = mCtx.getResources().getDrawable(R.drawable.google_pencil);
        } else if (title.equals(mCtx.getString(R.string.title_drawer_setting))) {
            color = mCtx.getResources().getColor(R.color.green);
            icon = mCtx.getResources().getDrawable(R.drawable.google_gear);
        } else if (title.equals(mCtx.getString(R.string.title_drawer_theme))) {
            color = mCtx.getResources().getColor(R.color.orange);
            icon = mCtx.getResources().getDrawable(R.drawable.google_contrast);
        } else if (title.contains(mCtx.getResources().getString(R.string.title_drawer_sms))) {
            NotifyHelper.getInstance().initSmsItemTextView(holder.tv_title);
            color = mCtx.getResources().getColor(R.color.red);
            icon = mCtx.getResources().getDrawable(R.drawable.google_mail);
        } else if (title.contains(mCtx.getResources().getString(R.string.title_drawer_notify))) {
            NotifyHelper.getInstance().initThreadItemTextView(holder.tv_title);
            color = mCtx.getResources().getColor(R.color.darkblue);
            icon = mCtx.getResources().getDrawable(R.drawable.google_info2);
        } else if (title.contains(mCtx.getResources().getString(R.string.title_drawer_newpost))) {
            color = mCtx.getResources().getColor(R.color.darkpurple);
            icon = mCtx.getResources().getDrawable(R.drawable.google_brush);
        } else if (title.contains(mCtx.getResources().getString(R.string.title_drawer_search))) {
            color = mCtx.getResources().getColor(R.color.darkgreen);
            icon = mCtx.getResources().getDrawable(R.drawable.google_zoom);
        } else if (title.contains(mCtx.getResources().getString(R.string.title_drawer_favorites))) {
            color = mCtx.getResources().getColor(R.color.darkorange);
            icon = mCtx.getResources().getDrawable(R.drawable.google_heart);
        }

        holder.tv_title.setText(title);
        holder.iv_color_block.setBackgroundColor(color);
        holder.iv_icon.setImageDrawable(icon);

        return convertView;
    }

    private static class ViewHolder {
        TextView tv_title;
        ImageView iv_icon;
        ImageView iv_color_block;
    }
}
