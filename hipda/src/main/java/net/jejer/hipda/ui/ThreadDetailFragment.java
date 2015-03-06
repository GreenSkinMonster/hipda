package net.jejer.hipda.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.DetailListLoader;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.PostAsyncTask;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;

import java.util.ArrayList;
import java.util.List;


public class ThreadDetailFragment extends Fragment {
	public static final String ARG_TID_KEY = "tid";
	public static final String ARG_TITLE_KEY = "title";

	private final String LOG_TAG = getClass().getSimpleName();

	private Context mCtx;
	private String mTid;
	private String mTitle;
	private XListView mDetailListView;
	private TextView mTipBar;
	private TextView mTitleView;
	private ThreadListLoaderCallbacks mLoaderCallbacks;
	private ThreadDetailAdapter mAdapter;
	private int mCurrentPage = 1;
	private int mMaxPage = 1;
	private int mGoToPage = 1;
	private int mMaxPostInPage = 1;	// for goto floor, user can configure max post per page
	private int mOffsetInPage = -1;	// for goto floor
	private boolean mInloading = false;
	private TextView mReplyTextTv;
	private ImageButton mPostReplyIb;
	private View quickReply;
	private Handler mMsgHandler;
	private boolean mAuthorOnly = false;

	private SparseArray<DetailListBean> mCache = new  SparseArray<DetailListBean>();
	public static final String LOADER_PAGE_KEY = "LOADER_PAGE_KEY";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);

		((MainFrameActivity)getActivity()).registOnSwipeCallback(this);
		mCtx = getActivity();

		setHasOptionsMenu(true);

		if (getArguments().containsKey(ARG_TID_KEY)) {
			mTid = getArguments().getString(ARG_TID_KEY);
		}
		if (getArguments().containsKey(ARG_TITLE_KEY)) {
			mTitle = getArguments().getString(ARG_TITLE_KEY);
		}

		mLoaderCallbacks = new ThreadListLoaderCallbacks();
		List<DetailBean> a = new ArrayList<DetailBean>();
		mAdapter = new ThreadDetailAdapter(mCtx, getFragmentManager(), R.layout.item_thread_detail, a,
				new GoToFloorOnClickListener(), new AvatarOnClickListener());

		mMsgHandler = new Handler(new ThreadDetailMsgHandler());
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_thread_detail, container, false);

		mDetailListView = (XListView)view.findViewById(R.id.lv_thread_details);
		mDetailListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mTipBar = (TextView)view.findViewById(R.id.thread_detail_tipbar);
		mTipBar.bringToFront();

		if (!HiSettingsHelper.getInstance().getIsLandscape()) {
			mDetailListView.addHeaderView(inflater.inflate(R.layout.head_thread_detail, null));
			mTitleView = (TextView)view.findViewById(R.id.thread_detail_title);
			mTitleView.setText(mTitle);
		}
		mDetailListView.setPullLoadEnable(false);
		mDetailListView.setPullRefreshEnable(false);
		mDetailListView.setXListViewListener(new XListView.IXListViewListener() {
			@Override
			public void onRefresh() {
				//Previous Page
				if (mCurrentPage > 1) {
					mCurrentPage--;
					//getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();

				}
				mDetailListView.stopRefresh();
				showOrLoadPage();
			}
			@Override
			public void onLoadMore() {
				//Next Page
				if (mCurrentPage < mMaxPage) {
					mCurrentPage++;
					//getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();

				}
				mDetailListView.stopLoadMore();
				showOrLoadPage();
			}
		});

		quickReply = view.findViewById(R.id.inc_quick_reply);
		mReplyTextTv = (TextView) quickReply.findViewById(R.id.tv_reply_text);
		mPostReplyIb = (ImageButton) quickReply.findViewById(R.id.ib_reply_post);
		mPostReplyIb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String replyText = mReplyTextTv.getText().toString();
				if (replyText.length() < 5) {
					Toast.makeText(getActivity(), "字数必须大于5", Toast.LENGTH_LONG).show();
				} else {
					//ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
					//ClipData clip = ClipData.newPlainText("AUTO SAVE FROM HiPDA", replyText);
					//clipboard.setPrimaryClip(clip);
					mReplyTextTv.setText("");
					quickReply.setVisibility(View.INVISIBLE);
					new PostAsyncTask(getActivity(), PostAsyncTask.MODE_REPLY_THREAD, null).execute(replyText, mTid, "", "", "");
					// Close SoftKeyboard
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mReplyTextTv.getWindowToken(), 0);
				}
			}
		});

		quickReply.bringToFront();

		if (HiSettingsHelper.getInstance().isEinkOptimization()) {
			ImageView mBtnPageup = (ImageView)view.findViewById(R.id.btn_detail_pageup);
			mBtnPageup.setVisibility(View.VISIBLE);
			mBtnPageup.setOnClickListener(new ImageView.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					int index = mDetailListView.getFirstVisiblePosition()-mDetailListView.getChildCount()+1;
					mDetailListView.setSelection(index<0?0:index);
				}
			});

			ImageView mBtnPagedown = (ImageView)view.findViewById(R.id.btn_detail_pagedown);
			mBtnPagedown.setVisibility(View.VISIBLE);
			mBtnPagedown.setOnClickListener(new ImageView.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (mDetailListView.getLastVisiblePosition() == mDetailListView.getFirstVisiblePosition()) {
						int offset = mDetailListView.getChildAt(0).getTop();
						int height = mDetailListView.getHeight();
						int item_height = mDetailListView.getChildAt(0).getHeight();
						if (item_height < Math.abs(offset)) {
							if (mDetailListView.getFirstVisiblePosition()+1 < mDetailListView.getCount()) {
								mDetailListView.setSelection(mDetailListView.getFirstVisiblePosition()+1);
							}
						} else {
							mDetailListView.setSelectionFromTop(mDetailListView.getFirstVisiblePosition(), offset - height);
						}
					} else {
						mDetailListView.setSelection(mDetailListView.getLastVisiblePosition());
					}
				}
			});
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		mDetailListView.setAdapter(mAdapter);
		mDetailListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mDetailListView.setOnItemLongClickListener(new OnItemLongClickCallback());

		getLoaderManager().initLoader(0, new Bundle(), mLoaderCallbacks);
		//getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
		showOrLoadPage();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.v(LOG_TAG, "onCreateOptionsMenu");

		menu.clear();
		inflater.inflate(R.menu.menu_thread_detail, menu);

		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setTitle(mTitle);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

		super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		Log.v(LOG_TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Implemented in activity
			return false;
		case R.id.action_open_url:
			String url = HiUtils.DetailListUrl+mTid;
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			return true;
		case R.id.action_reply:
			setHasOptionsMenu(false);
			Bundle arguments = new Bundle();
			arguments.putString(PostFragment.ARG_TID_KEY, mTid);
			arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_REPLY_THREAD);
			PostFragment fragment = new PostFragment();
			fragment.setArguments(arguments);
			if (HiSettingsHelper.getInstance().getIsLandscape()) {
				getFragmentManager().beginTransaction()
				.add(R.id.main_frame_container, fragment, PostFragment.class.getName())
				.addToBackStack(PostFragment.class.getName())
				.commit();
			} else {
				getFragmentManager().beginTransaction()
				.add(R.id.main_frame_container, fragment, PostFragment.class.getName())
				.addToBackStack(PostFragment.class.getName())
				.commit();
			}

			return true;
		case R.id.action_refresh_detail:
			refresh();
			return true;
		case R.id.action_goto:
			if (mAuthorOnly) {
				Toast.makeText(getActivity(), "请先退出只看楼主模式", Toast.LENGTH_LONG).show();
				return true;
			}
			showGotoPageDialog();
			return true;
		case R.id.action_only_author:
			mAuthorOnly = !mAuthorOnly;
			mAdapter.clear();
			mCurrentPage = 1;
			if (mAuthorOnly) {
				mDetailListView.setPullLoadEnable(false);
				mDetailListView.setPullRefreshEnable(false);
				getActivity().getActionBar().setTitle("(只看楼主)"+mTitle);
				showAndLoadAuthorOnly();
			} else {
				showOrLoadPage();
			}
			return true;
		case R.id.action_add_favorite:
			FavoriteHelper.getInstance().addFavorite(mCtx, mTid, mTitle);
			return true;
		case R.id.action_remove_favorite:
			FavoriteHelper.getInstance().removeFavorite(mCtx, mTid, mTitle);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh() {
		//Log.v(LOG_TAG, "refresh() called");
		Bundle b = new Bundle();
		b.putInt(LOADER_PAGE_KEY, mCurrentPage);
		getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
	}

	@Override
	public void onResume() {
		//Log.v(LOG_TAG, "onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		//Log.v(LOG_TAG, "onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		//Log.v(LOG_TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		//Log.v(LOG_TAG, "onDestory");
		getLoaderManager().destroyLoader(0);
		((MainFrameActivity)getActivity()).registOnSwipeCallback(null);
		super.onDestroy();
	}

	public class ThreadListLoaderCallbacks implements LoaderManager.LoaderCallbacks<DetailListBean> {

		@Override
		public Loader<DetailListBean> onCreateLoader(int id, Bundle args) {
			Log.v(LOG_TAG, "onCreateLoader");

			if (mInloading) {
				return null;
			}

			//mAdapter.clear();

			quickReply.setVisibility(View.INVISIBLE);

			// Re-enable after load complete if needed.
			mDetailListView.setPullLoadEnable(false);
			mDetailListView.setPullRefreshEnable(false);

			//VolleyHelper.getInstance().cancelAll();
			return new DetailListLoader(mCtx, mMsgHandler, mTid, args.getInt(LOADER_PAGE_KEY, 1));
		}

		@Override
		public void onLoadFinished(Loader<DetailListBean> loader,
				DetailListBean details) {
			Log.v(LOG_TAG, "onLoadFinished");

			mInloading = false;

			if(details == null) {
				// May be login error, error message should be populated in login async task
				return;
			} else if (details.getCount() == 0) {
				// Page load fail.

				Message msgError = Message.obtain();
				msgError.what = ThreadListFragment.STAGE_ERROR;
				Bundle b = new Bundle();
				b.putString(ThreadListFragment.STAGE_ERROR_KEY, "页面加载失败");
				msgError.setData(b);
				mMsgHandler.sendMessage(msgError);

				return;
			}

			Message msgClean = Message.obtain();
			msgClean.what = ThreadListFragment.STAGE_CLEAN;
			mMsgHandler.sendMessage(msgClean);

			// Set title
			if (details.getTitle() != null && !details.getTitle().isEmpty()) {
				mTitle = details.getTitle();
				if (mTitleView != null) {
					mTitleView.setText(mTitle);
				}
			}

			// Set MaxPage earlier than showOrLoadPage()
			mMaxPage = details.getLastPage();

			//mAdapter.addAll(details.getAll());
			mCache.put(details.getPage(), details);
			if (!mAuthorOnly && mCurrentPage == details.getPage()) {
				showOrLoadPage();
			} else if (mAuthorOnly) {
				showAndLoadAuthorOnly();
			}

			if (details.getCount() > mMaxPostInPage) {
				mMaxPostInPage = details.getCount();
			}

			//			Message msgDone = Message.obtain();
			//			msgDone.what = ThreadListFragment.STAGE_DONE;
			//			Bundle b = new Bundle();
			//			b.putInt(ThreadDetailFragment.LOADER_PAGE_KEY, details.getPage());
			//			msgDone.setData(b);
			//			mMsgHandler.sendMessage(msgDone);

			//when press back, hide quickreply first
//			if (getView() != null) {
//				getView().setFocusableInTouchMode(true);
//				getView().requestFocus();
//				getView().setOnKeyListener(new View.OnKeyListener() {
//					@Override
//					public boolean onKey(View v, int keyCode, KeyEvent event) {
//						if (event.getAction() == KeyEvent.ACTION_DOWN) {
//							if (keyCode == KeyEvent.KEYCODE_BACK) {
//								if (quickReply.getVisibility() == View.VISIBLE) {
//									quickReply.setVisibility(View.INVISIBLE);
//									return true;
//								}
//								return false;
//							}
//						}
//						return false;
//					}
//				});
//			}

            mCallback.onAvatarUrlUpdated();
        }



		@Override
		public void onLoaderReset(Loader<DetailListBean> arg0) {
			//Log.v(LOG_TAG, "onLoaderReset");

			mInloading = false;
			mTipBar.setVisibility(View.INVISIBLE);
		}

	}

	public void onSwipeTop() {
		//Log.v(LOG_TAG, "onSwipeTop");
		quickReply.setVisibility(View.INVISIBLE);
	}
	public void onSwipeBottom() {
		//Log.v(LOG_TAG, "onSwipeBottom");
		quickReply.bringToFront();
		quickReply.setVisibility(View.VISIBLE);
	}

	private class OnItemLongClickCallback implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			mDetailListView.setItemChecked(position, true);

			position = position - mDetailListView.getHeaderViewsCount();
			if (position > mAdapter.getCount()) {
				return false;
			}

			ThreadDetailActionModeCallback cb = new ThreadDetailActionModeCallback(ThreadDetailFragment.this, mTid,
					mAdapter.getItem(position));
			getActivity().startActionMode(cb);

			return true;
		}
	}

	private void showGotoPageDialog() {
		mGoToPage = mCurrentPage;
		final LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View viewlayout = inflater.inflate(R.layout.dialog_goto_page, null);
		final Button btnLastPage = (Button)viewlayout.findViewById(R.id.btn_last_page);
		final SeekBar sbGotoPage = (SeekBar)viewlayout.findViewById(R.id.sb_page);
		btnLastPage.setText(String.valueOf(mGoToPage)+"/"+String.valueOf(mMaxPage));
		btnLastPage.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View view) {
				sbGotoPage.setProgress(mMaxPage - 1);
			}});

		// Fuck Android SeekBar, always start from 0
		sbGotoPage.setMax(mMaxPage-1);
		sbGotoPage.setProgress(mCurrentPage-1);
		sbGotoPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mGoToPage = progress + 1; //start from 0
				btnLastPage.setText(String.valueOf(mGoToPage)+"/"+String.valueOf(mMaxPage));
			}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {}
		});

		final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
		popDialog.setTitle("转到第N页");
		popDialog.setView(viewlayout);
		// Add the buttons
		popDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked OK button
				mCurrentPage = mGoToPage;
				//getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
				showOrLoadPage();
			}
		});
		popDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});
		popDialog.create().show();
	}

	public class GoToFloorOnClickListener implements Button.OnClickListener {
		@Override
		public void onClick(View view) {
			mAuthorOnly = false;

			int floor = (Integer)view.getTag();
			mGoToPage = floor/mMaxPostInPage + 1; // page start from 1
			mOffsetInPage = floor%mMaxPostInPage - 1; // offset start from 0

			if (mGoToPage != mCurrentPage) {
				mCurrentPage = mGoToPage;
				//getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
				showOrLoadPage();
			} else {
				mDetailListView.setSelection(mOffsetInPage + mDetailListView.getHeaderViewsCount());
				mOffsetInPage = -1;
			}
		}
	}

	private class ThreadDetailMsgHandler implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {
			int page = 0;
			Bundle bundle = msg.getData();
			if (bundle != null) {
				page = bundle.getInt(LOADER_PAGE_KEY, 0);
			}
			String pageStr = "(第" + page + "页)";

			switch (msg.what) {
			case ThreadListFragment.STAGE_ERROR:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.red));
				Bundle b = msg.getData();
				mTipBar.setText(b.getString(ThreadListFragment.STAGE_ERROR_KEY));
				Log.e(LOG_TAG, b.getString(ThreadListFragment.STAGE_ERROR_KEY));
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case ThreadListFragment.STAGE_CLEAN:
				mTipBar.setVisibility(View.INVISIBLE);
				break;
			case ThreadListFragment.STAGE_DONE:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.green));
				mTipBar.setText(pageStr+"加载完成");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case ThreadListFragment.STAGE_RELOGIN:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
				mTipBar.setText("正在登录");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case ThreadListFragment.STAGE_GET_WEBPAGE:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
				mTipBar.setText(pageStr+"正在获取页面");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case ThreadListFragment.STAGE_PARSE:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.orange));
				mTipBar.setText(pageStr+"正在解析页面");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			case ThreadListFragment.STAGE_PREFETCH:
				mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.green));
				mTipBar.setText("正在预读下一页");
				mTipBar.setVisibility(View.VISIBLE);
				break;
			}
			return false;
		}
	}

	private void showOrLoadPage() {
		getActivity().getActionBar().setTitle("("+
				String.valueOf(mCurrentPage)+"/"+String.valueOf(mMaxPage)
				+")"+mTitle);

		if (mCache.get(mCurrentPage) != null) {
			mAdapter.clear();
			mAdapter.addAll(mCache.get(mCurrentPage).getAll());
			mAdapter.notifyDataSetChanged();
			mDetailListView.setSelection(0);
		} else {
			Bundle b = new Bundle();
			b.putInt(LOADER_PAGE_KEY, mCurrentPage);
			getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
		}

		// Prefetch
		if (HiSettingsHelper.getInstance().isPreFetch()
				&& mCache.get(mCurrentPage+1) == null
				&& mCurrentPage < mMaxPage) {
			Bundle b = new Bundle();
			b.putInt(LOADER_PAGE_KEY, mCurrentPage+1);
			getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
		}

		if (mCurrentPage == 1) {
			mDetailListView.setPullRefreshEnable(false);
		} else {
			mDetailListView.setPullRefreshEnable(true);
		}
		if (mCurrentPage == mMaxPage) {
			mDetailListView.setPullLoadEnable(false);
		} else {
			mDetailListView.setPullLoadEnable(true);
		}

		if (mOffsetInPage != -1) {
			mDetailListView.setSelection(mOffsetInPage + mDetailListView.getHeaderViewsCount());
			mOffsetInPage = -1;
		}
	}

	private void addAuthorPosts(DetailListBean details) {
		for (DetailBean detail : details.getAll()) {
			if (detail.getAuthor().equals(mCache.get(1).getAll().get(0).getAuthor())) {
				mAdapter.add(detail);
			}
		}
	}

	private void showAndLoadAuthorOnly() {
		while (mCache.get(mCurrentPage) != null && mCurrentPage <= mMaxPage) {
			addAuthorPosts(mCache.get(mCurrentPage));
			mCurrentPage++;
		}

		if (mCurrentPage <= mMaxPage) {
			Bundle b = new Bundle();
			b.putInt(LOADER_PAGE_KEY, mCurrentPage);
			getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
		}
	}

	class AvatarOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			String uid = (String) arg0.getTag(R.id.avatar_tag_uid);
			String username = (String) arg0.getTag(R.id.avatar_tag_username);
			Log.v(LOG_TAG, "AvatarOnClickListener.onClick, username="+username+" uid="+uid);

			Bundle arguments = new Bundle();
			arguments.putString(UserinfoFragment.ARG_UID, uid);
			arguments.putString(UserinfoFragment.ARG_USERNAME, username);
			UserinfoFragment fragment = new UserinfoFragment();
			fragment.setArguments(arguments);

			setHasOptionsMenu(false);
			if (HiSettingsHelper.getInstance().getIsLandscape()) {
				getFragmentManager().beginTransaction()
				.replace(R.id.thread_detail_container_in_main, fragment, ThreadDetailFragment.class.getName())
				.addToBackStack(ThreadDetailFragment.class.getName())
				.commit();
			} else {
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


    AvatarUrlUpdated mCallback;

    public interface AvatarUrlUpdated {
        public void onAvatarUrlUpdated();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (AvatarUrlUpdated) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TextClicked");
        }
    }

}
