package net.jejer.hipda.ui;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.SimpleListBean;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SmsFragment extends Fragment {
	private final String LOG_TAG = getClass().getSimpleName();

	public static final String ARG_ID = "ID";
	public static final String ARG_UID = "UID";

	private String mId;
	private String mUid;
	private SmsAdapter mAdapter;
	private SmsListLoaderCallbacks mLoaderCallbacks;
	private ListView mListView;

	private View mQuickReplyView;
	private TextView mQuickReplyTv;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(LOG_TAG, "onCreate");
		setHasOptionsMenu(true);

		if (getArguments().containsKey(ARG_ID)) {
			mId = getArguments().getString(ARG_ID);
		}
		if (getArguments().containsKey(ARG_UID)) {
			mUid = getArguments().getString(ARG_UID);
		}

		mAdapter = new SmsAdapter(getActivity(), R.layout.item_sms_list);
		mLoaderCallbacks = new SmsListLoaderCallbacks();

		((MainFrameActivity)getActivity()).registOnSwipeCallback(this);
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_sms, container, false);
		mListView = (ListView)view.findViewById(R.id.lv_sms);

		mQuickReplyView = view.findViewById(R.id.inc_quick_reply);
		mQuickReplyTv = (TextView)mQuickReplyView.findViewById(R.id.tv_reply_text);
		ImageButton postIb = (ImageButton)mQuickReplyView.findViewById(R.id.ib_reply_post);
		postIb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String replyText = mQuickReplyTv.getText().toString();
				if (replyText.length() > 0) {
					new PostSmsAsyncTask(getActivity(), mUid).execute(replyText);
					// Close SoftKeyboard
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mQuickReplyTv.getWindowToken(), 0);
				}
			}
		});
		mQuickReplyView.bringToFront();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v(LOG_TAG, "onActivityCreated");

		// destroyLoader called here to avoid onLoadFinished called when onResume
		getLoaderManager().destroyLoader(0);
		mListView.setAdapter(mAdapter);
		getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.v(LOG_TAG, "onCreateOptionsMenu");

		menu.clear();
		//inflater.inflate(R.menu.menu_simple_thread_list, menu);

		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle("与"+mId+"的短消息");

		super.onCreateOptionsMenu(menu,inflater);
	}

	public class SmsListLoaderCallbacks implements LoaderManager.LoaderCallbacks<SimpleListBean> {

		@Override
		public Loader<SimpleListBean> onCreateLoader(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub
			return new SimpleListLoader(SmsFragment.this.getActivity(), SimpleListLoader.TYPE_SMSDETAIL, 1, mUid);
		}

		@Override
		public void onLoadFinished(Loader<SimpleListBean> loader,
				SimpleListBean list) {
			// TODO Auto-generated method stub

			Log.v(LOG_TAG, "onLoadFinished enter");

			if(list == null || list.getCount() == 0) {
				Log.v(LOG_TAG, "onLoadFinished list == null || list.getCount == 0");
				Toast.makeText(SmsFragment.this.getActivity(), 
						"自动加载失败", Toast.LENGTH_LONG).show();
				return;
			}

			Log.v(LOG_TAG, "mThreadListAdapter.addAll(arg1.threads) called, added "+list.getCount());
			mAdapter.addAll(list.getAll());
		}

		@Override
		public void onLoaderReset(Loader<SimpleListBean> arg0) {
			// TODO Auto-generated method stub
			Log.v(LOG_TAG, "onLoaderReset");
		}
	}

	public void onSwipeTop() {
		mQuickReplyView.setVisibility(View.INVISIBLE);
	}
	public void onSwipeBottom() {
		mQuickReplyView.setVisibility(View.VISIBLE);
	}
}
