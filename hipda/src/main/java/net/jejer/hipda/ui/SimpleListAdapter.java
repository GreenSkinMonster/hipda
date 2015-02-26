package net.jejer.hipda.ui;

import java.util.List;

import com.android.volley.toolbox.NetworkImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.async.VolleyHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SimpleListAdapter extends ArrayAdapter<SimpleListItemBean> {
	private LayoutInflater mInflater;
	private Context mCtx;
	private int mType;

	public SimpleListAdapter(Context context, int resource,
			List<SimpleListItemBean> objects, int type) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
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
		holder.iv_item_indicator = (NetworkImageView) convertView.findViewById(R.id.iv_item_indicator); 

		String str;
		holder.tv_title.setText(item.getTitle());  
		if (item.isNew()) {
			holder.tv_title.setTextColor(mCtx.getResources().getColor(R.color.red));
		}
		// TODO set to default color if item is not new!

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

		holder.iv_item_indicator.setDefaultImageResId(R.drawable.google_speaker);
		if (mType == SimpleListLoader.TYPE_SMS) {
			holder.iv_item_indicator.setImageUrl(item.getAvatarUrl(), VolleyHelper.getInstance().getAvatarLoader());

		}

		return convertView;
	}

	private static class ViewHolder {
		TextView tv_title;
		TextView tv_info;
		TextView tv_time;
		NetworkImageView iv_item_indicator;
	}
}
