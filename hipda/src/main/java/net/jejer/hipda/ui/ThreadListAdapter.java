package net.jejer.hipda.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import net.jejer.hipda.R;
import net.jejer.hipda.bean.ThreadBean;

public class ThreadListAdapter extends ArrayAdapter<ThreadBean> {

	private LayoutInflater mInflater;
	//private Context mCtx;
	//private List<ThreadBean> threads;

	public ThreadListAdapter(Context context, int resource,
			List<ThreadBean> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		mInflater = LayoutInflater.from(context);
		//mCtx = context;
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
			holder = (ViewHolder)convertView.getTag();
		}

		holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);  
		holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);  
		holder.tv_viewcounter = (TextView) convertView.findViewById(R.id.tv_viewcounter);  
		holder.tv_replycounter = (TextView) convertView.findViewById(R.id.tv_replycounter);  
		holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time); 
		holder.iv_image_indicator = (ImageView) convertView.findViewById(R.id.iv_image_indicator); 


		holder.tv_author.setText(thread.getAuthor());  
		holder.tv_title.setText(thread.getTitle());  
		if (thread.getCountCmts() != null) {
			holder.tv_replycounter.setText(thread.getCountCmts());
		}
		if (thread.getCountViews() != null) {
			holder.tv_viewcounter.setText(thread.getCountViews());
		}
		holder.tv_time.setText("| "+thread.getTimeCreate());

		if (thread.getHavePic()) {
			holder.iv_image_indicator.setVisibility(View.VISIBLE);
		} else {
			holder.iv_image_indicator.setVisibility(View.GONE);
		}

		return convertView;
	}

	private static class ViewHolder {
		TextView tv_title;
		TextView tv_author;
		TextView tv_viewcounter;
		TextView tv_replycounter;
		TextView tv_time;
		ImageView iv_image_indicator;
	}
}
