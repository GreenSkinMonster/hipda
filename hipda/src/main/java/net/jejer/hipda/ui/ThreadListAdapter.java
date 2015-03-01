package net.jejer.hipda.ui;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.async.VolleyHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.cache.AvatarUrlCache;

public class ThreadListAdapter extends ArrayAdapter<ThreadBean> {

	private LayoutInflater mInflater;
	//private Context mCtx;
	//private List<ThreadBean> threads;
	private HashMap<Integer, ViewHolder> holders = new HashMap<Integer, ViewHolder>();

	public ThreadListAdapter(Context context, int resource,
							 List<ThreadBean> objects) {
		super(context, resource, objects);
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
			holder = (ViewHolder) convertView.getTag();
		}

		holders.put(position, holder);

		holder.avatar = (NetworkImageView) convertView.findViewById(R.id.iv_avatar);
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
		holder.tv_time.setText("| " + thread.getTimeCreate());

		if (thread.getHavePic()) {
			holder.iv_image_indicator.setVisibility(View.VISIBLE);
		} else {
			holder.iv_image_indicator.setVisibility(View.GONE);
		}

		holder.avatar.setImageUrl(thread.getAvatarUrl(), VolleyHelper.getInstance().getAvatarLoader());
		holder.avatar.setDefaultImageResId(R.drawable.google_user);
		holder.avatar.setErrorImageResId(R.drawable.google_user);
		holder.avatar.setTag(R.id.avatar_tag_uid, thread.getAuthorId());
		holder.avatar.setTag(R.id.avatar_tag_username, thread.getAuthor());
		//holder.avatar.setOnClickListener(mAvatarListener);

		return convertView;
	}

	public void refreshAvatars() {
		long start = System.currentTimeMillis();
		boolean changed = false;
		for (int i = 0; i < getCount(); i++) {
			ThreadBean thread = getItem(i);
            if ((thread.getAvatarUrl() == null || thread.getAvatarUrl().length() == 0) && AvatarUrlCache.get(thread.getAuthorId()).length() > 0) {
                thread.setAvatarUrl(AvatarUrlCache.get(thread.getAuthorId()));
                ViewHolder holder = holders.get(i);
				if (holder != null) {
					holder.avatar.setImageUrl(thread.getAvatarUrl(), VolleyHelper.getInstance().getAvatarLoader());
					changed = true;
				}
			}
		}
		Log.v("ThreadListAdapter", "refreshAvatars size=" + getCount() + ", time used : " + (System.currentTimeMillis() - start) + " ms");
		if (changed)
			notifyDataSetChanged();
	}

	private static class ViewHolder {
		NetworkImageView avatar;
		TextView tv_title;
		TextView tv_author;
		TextView tv_viewcounter;
		TextView tv_replycounter;
		TextView tv_time;
		ImageView iv_image_indicator;
	}
}
