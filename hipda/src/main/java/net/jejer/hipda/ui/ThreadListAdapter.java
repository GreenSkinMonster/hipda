package net.jejer.hipda.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.cache.AvatarUrlCache;

import java.util.HashMap;
import java.util.List;

public class ThreadListAdapter extends ArrayAdapter<ThreadBean> {

	private LayoutInflater mInflater;
	//private Context mCtx;
	//private List<ThreadBean> threads;
	private HashMap<String, ViewHolder> holders = new HashMap<String, ViewHolder>();

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

		holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
		holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
		holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
		holder.tv_viewcounter = (TextView) convertView.findViewById(R.id.tv_viewcounter);
		holder.tv_replycounter = (TextView) convertView.findViewById(R.id.tv_replycounter);
		holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
		holder.iv_image_indicator = (ImageView) convertView.findViewById(R.id.iv_image_indicator);

		holders.put(thread.getTid(), holder);


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

        if (HiSettingsHelper.getInstance().isShowThreadListAvatar()) {
			//holder.avatar.setImageUrl(thread.getAvatarUrl(), VolleyHelper.getInstance().getAvatarLoader());
			holder.avatar.setVisibility(View.VISIBLE);
			Glide.with(getContext())
					.load(thread.getAvatarUrl())
					.centerCrop()
							//.placeholder(R.drawable.google_user)
					.crossFade()
					.into(holder.avatar);
		} else {
			//holder.avatar.setImageUrl("", VolleyHelper.getInstance().getAvatarLoader());
			holder.avatar.setVisibility(View.GONE);
        }
		//holder.avatar.setDefaultImageResId(R.drawable.google_user);
		//holder.avatar.setErrorImageResId(R.drawable.google_user);
		holder.avatar.setTag(R.id.avatar_tag_uid, thread.getAuthorId());
		holder.avatar.setTag(R.id.avatar_tag_username, thread.getAuthor());
		//holder.avatar.setOnClickListener(mAvatarListener);

		return convertView;
	}

    public void markAvatars(int startPostion, int count) {
        if (count > 0 && startPostion >= 0) {
            for (int i = startPostion; i < startPostion + count; i++) {
                if (i < getCount()) {
                    ThreadBean thread = getItem(i);
                    AvatarUrlCache.getInstance().markDirty(thread.getAuthorId());
                }
            }
        }
        AvatarUrlCache.getInstance().fetchAvatarUrls(this);
    }

    public void refreshAvatars() {
		if (HiSettingsHelper.getInstance().isShowThreadListAvatar()
				&& AvatarUrlCache.getInstance().isUpdated()) {
			AvatarUrlCache.getInstance().setUpdated(false);
			boolean changed = true;
			long start = System.currentTimeMillis();
			for (int i = 0; i < getCount(); i++) {
				ThreadBean thread = getItem(i);
				if (!AvatarUrlCache.getInstance().get(thread.getAuthorId()).equals((thread.getAvatarUrl()))) {
					ViewHolder holder = holders.get(thread.getTid());
					if (holder != null
                            && thread.getAuthorId().equals(holder.avatar.getTag(R.id.avatar_tag_uid))) {
                        Glide.with(getContext())
								.load(thread.getAvatarUrl())
								.centerCrop()
								.crossFade()
								.into(holder.avatar);
						thread.setAvatarUrl(AvatarUrlCache.getInstance().get(thread.getAuthorId()));
					}
				}
			}
			if (changed)
				notifyDataSetChanged();
			Log.v("ThreadListAdapter", "refreshAvatars size=" + getCount() + ", time used : " + (System.currentTimeMillis() - start) + " ms");
		}
	}

	private static class ViewHolder {
		ImageView avatar;
		TextView tv_title;
		TextView tv_author;
		TextView tv_viewcounter;
		TextView tv_replycounter;
		TextView tv_time;
		ImageView iv_image_indicator;
	}
}
