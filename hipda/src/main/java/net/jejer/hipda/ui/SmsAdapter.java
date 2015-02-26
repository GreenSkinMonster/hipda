package net.jejer.hipda.ui;

import java.util.ArrayList;
import com.android.volley.toolbox.NetworkImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.async.VolleyHelper;
import net.jejer.hipda.bean.SimpleListItemBean;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SmsAdapter extends ArrayAdapter<SimpleListItemBean> {
	private LayoutInflater mInflater;
	//private Context mCtx;

	public SmsAdapter(Context context, int resource) {
		super(context, resource, new ArrayList<SimpleListItemBean>());
		// TODO Auto-generated constructor stub
		mInflater = LayoutInflater.from(context);
		//mCtx = context;
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
			holder = (ViewHolder)convertView.getTag();
		}

		holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);  
		holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);  
		holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time); 
		holder.iv_avatar = (NetworkImageView) convertView.findViewById(R.id.iv_avatar); 

		holder.tv_author.setText(item.getAuthor());  
		holder.tv_time.setText(item.getTime());
		holder.tv_content.setText(item.getInfo());

		holder.iv_avatar.setImageUrl(item.getAvatarUrl(), VolleyHelper.getInstance().getAvatarLoader());
		holder.iv_avatar.setDefaultImageResId(R.drawable.google_user);
		holder.iv_avatar.setErrorImageResId(R.drawable.google_user);

		return convertView;
	}

	private static class ViewHolder {
		TextView tv_author;
		TextView tv_content;
		TextView tv_time;
		NetworkImageView iv_avatar;
	}
}
