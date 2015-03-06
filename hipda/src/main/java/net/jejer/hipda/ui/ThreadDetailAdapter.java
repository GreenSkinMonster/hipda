package net.jejer.hipda.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentAbs;
import net.jejer.hipda.bean.ContentAttach;
import net.jejer.hipda.bean.ContentGoToFloor;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.ContentQuote;
import net.jejer.hipda.bean.ContentText;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.glide.GlideScaleViewTarget;
import net.jejer.hipda.utils.HiUtils;

import java.util.List;

public class ThreadDetailAdapter extends ArrayAdapter<DetailBean> {

	private Context mCtx;
	private LayoutInflater mInflater;
	private Button.OnClickListener mGoToFloorListener;
	private View.OnClickListener mAvatarListener;
	private FragmentManager mFragmentManager;

	public ThreadDetailAdapter(Context context, FragmentManager fm, int resource,
							   List<DetailBean> objects, Button.OnClickListener gotoFloorListener, View.OnClickListener avatarListener) {
		super(context, resource, objects);
		mCtx = context;
		mFragmentManager = fm;
		mInflater = LayoutInflater.from(context);
		mGoToFloorListener = gotoFloorListener;
		mAvatarListener = avatarListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DetailBean detail = getItem(position);

		ViewHolder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = mInflater.inflate(R.layout.item_thread_detail, null);

			holder = new ViewHolder();
			holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
			holder.author = (TextView) convertView.findViewById(R.id.author);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.floor = (TextView) convertView.findViewById(R.id.floor);
			holder.postStatus = (TextView) convertView.findViewById(R.id.post_status);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.author.setText(detail.getAuthor());
		holder.time.setText(detail.getTimePost());
		holder.floor.setText(detail.getFloor() + "#");
		String postStaus = detail.getPostStatus();
		if (postStaus != null && postStaus.length() > 0)
			holder.postStatus.setText(postStaus);
		else
			holder.postStatus.setVisibility(View.GONE);

		if (HiSettingsHelper.getInstance().isShowThreadListAvatar()) {
			holder.avatar.setVisibility(View.VISIBLE);
			Glide.with(getContext())
					.load(detail.getAvatarUrl())
					.centerCrop()
//					.placeholder(R.drawable.google_user)
					.error(R.drawable.google_user)
					.crossFade()
					.into(holder.avatar);
		} else {
			holder.avatar.setVisibility(View.GONE);
		}
		holder.avatar.setTag(R.id.avatar_tag_uid, detail.getUid());
		holder.avatar.setTag(R.id.avatar_tag_username, detail.getAuthor());
		holder.avatar.setOnClickListener(mAvatarListener);

		LinearLayout contentView = (LinearLayout) convertView.findViewById(R.id.content_layout);
		contentView.removeAllViews();
		for (int i = 0; i < detail.getContents().getSize(); i++) {
			ContentAbs content = detail.getContents().get(i);
			if (content instanceof ContentText) {
				TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
				tv.setFragmentManager(mFragmentManager);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17 + HiSettingsHelper.getInstance().getPostTextsizeAdj());
				tv.setMovementMethod(LinkMovementMethod.getInstance());
				//dirty hack, remove one <br> after poststatus
				String cnt = content.getContent();
				if (postStaus != null && postStaus.length() > 0 && cnt.startsWith("<br><br>")) {
					cnt = cnt.substring("<br>".length());
				}
				tv.setText(cnt);
				//setAutoLinkMask have conflict with setMovementMethod
				//tv.setAutoLinkMask(Linkify.WEB_URLS);
				tv.setFocusable(false);
				contentView.addView(tv);
			} else if (content instanceof ContentImg) {
				final String imageUrl = content.getContent();

				GlideImageView giv = new GlideImageView(mCtx);
				giv.setFocusable(false);
				contentView.addView(giv);
				giv.setUrl(imageUrl);

				int maxViewWidth = 1080;
				int lowerImageWidth = 320;

				ThreadDetailFragment fragment = (ThreadDetailFragment) mFragmentManager.findFragmentByTag(ThreadDetailFragment.class.getName());
				if (fragment != null && fragment.getView() != null) {
					maxViewWidth = fragment.getView().getWidth();
				}

				if (HiUtils.isAutoLoadImg(mCtx)) {
					Glide.with(getContext())
							.load(imageUrl)
							.diskCacheStrategy(DiskCacheStrategy.ALL)
							.placeholder(R.drawable.ic_action_picture)
							.error(R.drawable.tapatalk_image_broken)
							.into(new GlideScaleViewTarget(giv, lowerImageWidth, maxViewWidth, imageUrl));
				} else {
					giv.setImageResource(R.drawable.ic_action_picture);
				}

			} else if (content instanceof ContentAttach) {
				TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
				tv.setFragmentManager(mFragmentManager);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17 + HiSettingsHelper.getInstance().getPostTextsizeAdj());
				tv.setMovementMethod(LinkMovementMethod.getInstance());
				tv.setText(content.getContent());
				tv.setFocusable(false);
				contentView.addView(tv);
			} else if (content instanceof ContentQuote) {
				TextView tv = new TextView(mCtx);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17 + HiSettingsHelper.getInstance().getPostTextsizeAdj());
				tv.setAutoLinkMask(Linkify.WEB_URLS);
				tv.setText(content.getContent());
				tv.setFocusable(false);    // make convertView long clickable.
				contentView.addView(tv);
			} else if (content instanceof ContentGoToFloor) {
				Button btnGotoFloor = new Button(mCtx);
				btnGotoFloor.setBackgroundColor(mCtx.getResources().getColor(R.color.background_silver));
				btnGotoFloor.setText(content.getContent());
				btnGotoFloor.setTag(((ContentGoToFloor) content).getFloor());
				btnGotoFloor.setOnClickListener(mGoToFloorListener);
				btnGotoFloor.setFocusable(false);    // make convertView long clickable.
				contentView.addView(btnGotoFloor);
			}
		}

		return convertView;
	}

	private static class ViewHolder {
		ImageView avatar;
		TextView author;
		TextView floor;
		TextView postStatus;
		TextView time;
	}
}
