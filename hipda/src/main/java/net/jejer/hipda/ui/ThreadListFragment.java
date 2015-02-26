package net.jejer.hipda.ui;


import java.util.ArrayList;
import java.util.List;

import net.jejer.hipda.R;
import net.jejer.hipda.async.ThreadListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.utils.HiUtils;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ActionBar.OnNavigationListener;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;


public class ThreadListFragment extends Fragment {
	public final static int STAGE_ERROR = -1;
	public final static int STAGE_CLEAN = 0;
	public final static int STAGE_RELOGIN = 1;
	public final static int STAGE_GET_WEBPAGE = 2;
	public final static int STAGE_PARSE = 3;
	public final static int STAGE_DONE = 4;
	public final static int STAGE_PREFETCH = 5;
	public final static String STAGE_ERROR_KEY = "ERROR_MSG";

	private final String LOG_TAG = getClass().getSimpleName();
	private Context mCtx;
	private int mForumId = 0;
	private int mPage = 1;
	private int mForumSelect = -1;
	private LoaderManager.LoaderCallbacks<ThreadListBean> mCallbacks;
	private OnNavigationListener mOnNavigationListener;
	private SpinnerAdapter mSpinnerAdapter;
	private ThreadListAdapter mThreadListAdapter;
	private ListView mThreadListView;
	private TextView mTipBar;
	private boolean mInloading = false;
	private Handler mMsgHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);

		mCtx = getActivity();

		setHasOptionsMenu(true);
		mCallbacks = new ThreadListLoaderCallbacks();
		List<ThreadBean> a = new ArrayList<ThreadBean>();
		mThreadListAdapter = new ThreadListAdapter(mCtx, R.layout.item_thread_list, a);

		mMsgHandler = new Handler(new ThreadListMsgHandler());

		mOnNavigationListener = new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				Log.v(LOG_TAG, "onNavigationItemSelected = " + String.valueOf(position));
				//Toast.makeText(mCtx, String.valueOf(position), Toast.LENGTH_LONG).show();

				int forumId = HiUtils.getForumID(mCtx, itemId);
				if (mForumId != forumId) {
					mForumId = forumId;
					mForumSelect = getActivity().getActionBar().getSelectedNavigationIndex();
					refresh();
				}

				return true;
			}
		};
		mSpinnerAdapter = ArrayAdapter.createFromResource(mCtx, R.array.forums,
				android.R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_thread_list, container, false);
		mThreadListView = (ListView)view.findViewById(R.id.lv_threads);
		mTipBar = (TextView)view.findViewById(R.id.thread_list_tipbar);
		mTipBar.setVisibility(View.INVISIBLE);
		mTipBar.bringToFront();

		if (HiSettingsHelper.getInstance().isEinkOptimization()) {
			ImageView mBtnPageup = (ImageView)view.findViewById(R.id.btn_list_pageup);
			mBtnPageup.setVisibility(View.VISIBLE);
			mBtnPageup.setOnClickListener(new ImageView.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					int index = mThreadListView.getFirstVisiblePosition()-mThreadListView.getChildCount()+1;
					mThreadListView.setSelection(index<0?0:index);
				}
			});


			ImageView mBtnPagedown = (ImageView)view.findViewById(R.id.btn_list_pagedown);
			mBtnPagedown.setVisibility(View.VISIBLE);
			mBtnPagedown.setOnClickListener(new ImageView.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mThreadListView.setSelection(mThreadListView.getLastVisiblePosition());
				}
			});
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		// destroyLoader called here to avoid onLoadFinished called when onResume
		//LoaderManager.enableDebugLogging(true);
		getLoaderManager().initLoader(0, null, mCallbacks);
		mThreadListView.setAdapter(mThreadListAdapter);
		mThreadListView.setOnItemClickListener(new OnItemClickCallback());
		mThreadListView.setOnScrollListener(new OnScrollCallback());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.v(LOG_TAG, "onCreateOptionsMenu");

		menu.clear();
		inflater.inflate(R.menu.menu_thread_list, menu);

		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getActivity().getActionBar().setTitle("");
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		getActivity().getActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
		getActivity().getActionBar().setSelectedNavigationItem(mForumSelect==-1?0:mForumSelect);

		super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		Log.v(LOG_TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Implemented in activity
			return false;
		case R.id.action_refresh_list:
			refresh();
			return true;
		case R.id.action_thread_list_settings:
			showThreadListSettingsDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		//Log.v(LOG_TAG, "onPause");
	}

	@Override
	public void onResume() {
		super.onResume();
		//Log.v(LOG_TAG, "onResume");
	}

	@Override
	public void onDestroy() {
		//Log.v(LOG_TAG, "onDestory");
		getLoaderManager().destroyLoader(0);
		super.onDestroy();
	}

	private void refresh() {
		//Log.v(LOG_TAG, "refresh() called");
		mPage = 1;
		mThreadListAdapter.clear();
		getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
		Log.v(LOG_TAG, "restartLoader() called");
	}

	public class OnScrollCallback implements AbsListView.OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (totalItemCount > 2 && firstVisibleItem + visibleItemCount > totalItemCount - 2) {

				if (!mInloading) {
					mInloading = true;
					mPage++;
					//Log.v(LOG_TAG, "overScroll autoload triggerd, load page " + String.valueOf(mPage));

					getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
					//Log.v(LOG_TAG, "restartLoader() called");
				}
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

	}
	private class OnItemClickCallback implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> listView, View itemView, int position,
				long row) {
			// TODO Auto-generated method stub

			//Log.v(LOG_TAG, "onItemClick");

			ThreadBean thread = mThreadListAdapter.getItem(position);

			setHasOptionsMenu(false);
			if (HiSettingsHelper.getInstance().getIsLandscape()) {
				Bundle arguments = new Bundle();
				arguments.putString(ThreadDetailFragment.ARG_TID_KEY, thread.getTid());
				arguments.putString(ThreadDetailFragment.ARG_TITLE_KEY, thread.getTitle());
				ThreadDetailFragment fragment = new ThreadDetailFragment();
				fragment.setArguments(arguments);
				getFragmentManager().beginTransaction()
				.replace(R.id.thread_detail_container_in_main, fragment, ThreadDetailFragment.class.getName())
				.addToBackStack(ThreadDetailFragment.class.getName())
				.commit();
			} else {

				Bundle arguments = new Bundle();
				arguments.putString(ThreadDetailFragment.ARG_TID_KEY, thread.getTid());
				arguments.putString(ThreadDetailFragment.ARG_TITLE_KEY, thread.getTitle());
				ThreadDetailFragment fragment = new ThreadDetailFragment();
				fragment.setArguments(arguments);
				if (HiSettingsHelper.getInstance().isEinkOptimization()) {
					getFragmentManager().beginTransaction()
					.add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
					.addToBackStack(ThreadDetailFragment.class.getName())
					.commit();
				} else {
					getFragmentManager().beginTransaction()
					.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
					.add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
					.addToBackStack(ThreadDetailFragment.class.getName())
					.commit();
				}

			}
		}

	}

	private class ThreadListLoaderCallbacks implements LoaderManager.LoaderCallbacks<ThreadListBean> {

		@Override
		public Loader<ThreadListBean> onCreateLoader(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub

			return new ThreadListLoader(mCtx, mMsgHandler, mForumId, mPage);
		}

		@Override
		public void onLoadFinished(Loader<ThreadListBean> arg0,
				ThreadListBean arg1) {
			Log.v(LOG_TAG, "onLoadFinished enter");

			mInloading = false;

			if(arg1 == null) {
				// May be login error, error message should be populated in login async task
				if (mPage > 1) {mPage--;}
				return;
			} else if (arg1.count == 0) {
				// Page load fail.
				if (mPage > 1) {mPage--;}

				Message msgError = Message.obtain();
				msgError.what = STAGE_ERROR;
				Bundle b = new Bundle();
				b.putString(STAGE_ERROR_KEY, "页面加载失败");
				msgError.setData(b);
				mMsgHandler.sendMessage(msgError);

				return;
			}

			// Remove duplicate
			int count = 0;
			for (ThreadBean newthread : arg1.threads) {
				boolean duplicate = false;
				for (int i = 0; i < mThreadListAdapter.getCount(); i++) {
					ThreadBean oldthread = mThreadListAdapter.getItem(i);
					if (newthread.getTid().equals(oldthread.getTid())) {
						duplicate = true;
						break;
					}
				}
				if (!duplicate) {
					mThreadListAdapter.add(newthread);
					count++;
				}
			}
			Log.v(LOG_TAG, "New Threads Added: " + count);

			Message msgDone = Message.obtain();
			msgDone.what = STAGE_DONE;
			mMsgHandler.sendMessage(msgDone);
			Message msgClean = Message.obtain();
			msgClean.what = STAGE_CLEAN;
			mMsgHandler.sendMessageDelayed(msgClean, 1000*1);
		}

		@Override
		public void onLoaderReset(Loader<ThreadListBean> arg0) {
			// TODO Auto-generated method stub
			Log.v(LOG_TAG, "onLoaderReset enter");

			mInloading = false;
			mTipBar.setVisibility(View.INVISIBLE);
		}

	}

	private void showThreadListSettingsDialog() {
		final LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View viewlayout = inflater.inflate(R.layout.dialog_thread_list_settings, null);

		final Switch sShowPicOnMobileNetwork = (Switch)viewlayout.findViewById(R.id.sw_load_pic_on_mobile_network);
		final Switch sPrefetch = (Switch)viewlayout.findViewById(R.id.sw_prefetch);
		final Switch sShowStickThreads = (Switch)viewlayout.findViewById(R.id.sw_show_stick_threads);
		final Switch sSortByPostTime = (Switch)viewlayout.findViewById(R.id.sw_sort_by_post_time);

		sShowPicOnMobileNetwork.setChecked(HiSettingsHelper.getInstance().isLoadImgOnMobileNwk());
		sShowPicOnMobileNetwork.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				HiSettingsHelper.getInstance().setLoadImgOnMobileNwk(arg1);
			}});
		sPrefetch.setChecked(HiSettingsHelper.getInstance().isPreFetch());
		sPrefetch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				HiSettingsHelper.getInstance().setPreFetch(arg1);
			}});
		sShowStickThreads.setChecked(HiSettingsHelper.getInstance().isShowStickThreads());
		sShowStickThreads.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				HiSettingsHelper.getInstance().setShowStickThreads(arg1);
			}});
		sSortByPostTime.setChecked(HiSettingsHelper.getInstance().isSortByPostTime());
		sSortByPostTime.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				HiSettingsHelper.getInstance().setSortByPostTime(arg1);
			}});

		final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
		popDialog.setTitle("帖子设置");
		popDialog.setView(viewlayout);
		// Add the buttons
		popDialog.setPositiveButton("OK", null);
		popDialog.create().show();
	}

	private class ThreadListMsgHandler implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {
			String page = "(第" + mPage + "页)";

			switch (msg.what) {
			case STAGE_ERROR:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.red));
				Bundle b = msg.getData();
				mTipBar.setText(b.getString(STAGE_ERROR_KEY));
				Log.e(LOG_TAG, b.getString(STAGE_ERROR_KEY));
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case STAGE_CLEAN:
				mTipBar.setVisibility(View.INVISIBLE);
				break;
			case STAGE_DONE:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.green));
				mTipBar.setText(page+"加载完成");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case STAGE_RELOGIN:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
				mTipBar.setText("正在登录");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case STAGE_GET_WEBPAGE:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
				mTipBar.setText(page+"正在获取页面");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case STAGE_PARSE:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.orange));
				mTipBar.setText(page+"正在解析页面");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			}
			return false;
		}
	}
}