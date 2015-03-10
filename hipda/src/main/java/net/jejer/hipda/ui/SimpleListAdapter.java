package net.jejer.hipda.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.cache.AvatarUrlCache;
import net.jejer.hipda.glide.GlideHelper;

import java.util.List;

public class SimpleListAdapter extends ArrayAdapter<SimpleListItemBean> {
	private LayoutInflater mInflater;
	private Context mCtx;
	private int mType;

	public SimpleListAdapter(Context context, int resource,
			List<SimpleListItemBean> objects, int type) {
		super(context, resource, objects);
		mInflater = LayoutInflater.from(context);
		mCtx = context;
		mType = type;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleListItemBean item = getItem(position);

		ViewHolder holder;
		if (convertView == null || convertView.getTag() == null) {
			convertView = mInflater.inflate(R.layout.item_simple_list, null); 
			holder = new ViewHolder();  
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}

		holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);  
		holder.tv_info = (TextView) convertView.findViewById(R.id.tv_info);  
		holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time); 
		holder.iv_item_indicator = (ImageView) convertView.findViewById(R.id.iv_item_indicator);

		String str;
		holder.tv_title.setText(item.getTitle());  
		if (item.isNew()) {
			holder.tv_title.setTextColor(mCtx.getResources().getColor(R.color.red));
		}

		str = item.getInfo();
		if (str == null || str.isEmpty()) {
			holder.tv_info.setHeight(0);
		} else {
			holder.tv_info.setText(str);
		}

		str = item.getTime();
		if (str == null || str.isEmpty()) {
			holder.tv_info.setHeight(0);
		} else {
			holder.tv_time.setText(str);
		}

		if (HiSettingsHelper.getInstance().isShowThreadListAvatar()) {
			String avatarUrl = item.getAvatarUrl();
			if (TextUtils.isEmpty(avatarUrl)) {
				avatarUrl = AvatarUrlCache.getInstance().get(item.getId());
			} else {
				avatarUrl = avatarUrl.replaceAll("small", "middle");
			}
			holder.iv_item_indicator.setVisibility(View.VISIBLE);
			GlideHelper.loadAvatar(getContext(), holder.iv_item_indicator, avatarUrl);
		} else {
			holder.iv_item_indicator.setVisibility(View.GONE);
		}

		return convertView;
	}

	private static class ViewHolder {
		TextView tv_title;
		TextView tv_info;
		TextView tv_time;
		ImageView iv_item_indicator;
	}
}
