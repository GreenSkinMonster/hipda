package net.jejer.hipda.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.Utils;

public class SmsAdapter extends HiAdapter<SimpleListItemBean> {
    private View.OnClickListener mAvatarListener;
    private LayoutInflater mInflater;
    private Context mCtx;
    private FragmentManager mFragmentManager;

    public SmsAdapter(Context context, FragmentManager fm, View.OnClickListener avatarListener) {
        mAvatarListener = avatarListener;
        mInflater = LayoutInflater.from(context);
        mCtx = context;
        mFragmentManager = fm;
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
        holder.tv_content = (TextViewWithEmoticon) convertView.findViewById(R.id.tv_content);
        holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        holder.tv_isnew = (TextView) convertView.findViewById(R.id.tv_isnew);
        holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);

        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            holder.iv_avatar.setVisibility(View.VISIBLE);
            holder.iv_avatar.setTag(R.id.avatar_tag_uid, item.getUid());
            holder.iv_avatar.setTag(R.id.avatar_tag_username, item.getAuthor());
            if (!TextUtils.isEmpty(item.getUid())) {
                holder.iv_avatar.setOnClickListener(mAvatarListener);
            }
            GlideHelper.loadAvatar(mCtx, holder.iv_avatar, item.getAvatarUrl());
        } else {
            holder.iv_avatar.setVisibility(View.GONE);
        }

        holder.tv_author.setText(item.getAuthor());
        holder.tv_time.setText(Utils.shortyTime(item.getTime()));
        holder.tv_content.setFragmentManager(mFragmentManager);
        holder.tv_content.setText(item.getInfo());
        holder.tv_content.setFocusable(false);

        holder.tv_content.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());

        if (item.isNew())
            holder.tv_isnew.setVisibility(View.VISIBLE);
        else
            holder.tv_isnew.setVisibility(View.GONE);

        return convertView;
    }

    private static class ViewHolder {
        TextView tv_author;
        TextViewWithEmoticon tv_content;
        TextView tv_time;
        TextView tv_isnew;
        ImageView iv_avatar;
    }
}
