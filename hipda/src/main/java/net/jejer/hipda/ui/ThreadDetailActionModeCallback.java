package net.jejer.hipda.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostAsyncTask;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.HiSettingsHelper;

public class ThreadDetailActionModeCallback implements ActionMode.Callback {
	private ThreadDetailFragment mFragment;
	private String mTid;
	private DetailBean mDetailBean;

	public ThreadDetailActionModeCallback(ThreadDetailFragment fragment, String tid, DetailBean detailBean) {
		mFragment = fragment;
		mTid = tid;
		mDetailBean = detailBean;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

		Bundle arguments = new Bundle();
		PostFragment fragment = new PostFragment();

		switch (item.getItemId()) {
			case R.id.action_edit:
				if (mDetailBean.getAuthor().equals(HiSettingsHelper.getInstance().getUsername())) {
					mFragment.setHasOptionsMenu(false);

					//arguments.putString(PostFragment.ARG_FID_KEY, "2");
					arguments.putString(PostFragment.ARG_TID_KEY, mTid);
					arguments.putString(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
					arguments.putString(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
					arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_EDIT_POST);

					fragment.setArguments(arguments);
					fragment.setPostListener(mFragment);

					mFragment.getFragmentManager().beginTransaction()
							.add(R.id.main_frame_container, fragment, PostFragment.class.getName())
							.addToBackStack(PostFragment.class.getName())
							.commit();
					mode.finish();
					return true;
				}
				break;
			case R.id.action_reply:
				mFragment.setHasOptionsMenu(false);

				arguments.putString(PostFragment.ARG_TID_KEY, mTid);
				arguments.putString(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
				arguments.putString(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
				arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_REPLY_POST);

				fragment.setArguments(arguments);
				fragment.setPostListener(mFragment);

				mFragment.getFragmentManager().beginTransaction()
						.add(R.id.main_frame_container, fragment, PostFragment.class.getName())
						.addToBackStack(PostFragment.class.getName())
						.commit();
				mode.finish();
				return true;
			case R.id.action_quote:
				mFragment.setHasOptionsMenu(false);

				arguments.putString(PostFragment.ARG_TID_KEY, mTid);
				arguments.putString(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
				arguments.putString(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
				arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_QUOTE_POST);

				fragment.setArguments(arguments);
				fragment.setPostListener(mFragment);

				mFragment.getFragmentManager().beginTransaction()
						.add(R.id.main_frame_container, fragment, PostFragment.class.getName())
						.addToBackStack(PostFragment.class.getName())
						.commit();
				mode.finish();
				return true;
			case R.id.action_copy:
				ClipboardManager clipboard = (ClipboardManager) mFragment.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("COPY FROM HiPDA", mDetailBean.getContents().getCopyText());
				clipboard.setPrimaryClip(clip);
				mode.finish();
				return true;
			default:
				break;
		}
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		menu.clear();

		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.contextual_menu_thread_detail, menu);

		if (!mDetailBean.getAuthor().equals(HiSettingsHelper.getInstance().getUsername())) {
			MenuItem item = menu.findItem(R.id.action_edit);
			item.setVisible(false);
		}

		mode.setTitle(mDetailBean.getFloor() + "#");
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

}
