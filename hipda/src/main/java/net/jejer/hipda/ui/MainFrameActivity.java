package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.jejer.hipda.R;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.async.VolleyHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.AvatarUrlCache;
import net.jejer.hipda.glide.GlideHelper;

public class MainFrameActivity extends Activity
		implements ThreadDetailFragment.AvatarUrlUpdated {
	private final String LOG_TAG = getClass().getSimpleName();

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private boolean mEnableSwipe = false;
	private OnSwipeTouchListener mSwipeListener;
	private Fragment mOnSwipeCallback = null;
	private int mQuit = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreate");
		// Init Settings
		HiSettingsHelper.getInstance().init(this);
		if (HiSettingsHelper.getInstance().isNightTheme()) {
			setTheme(R.style.ThemeNight);
		}

		AvatarUrlCache.getInstance().init(this);
		GlideHelper.init(this);

		// Init Volley
		VolleyHelper.getInstance().init(this);
		NotifyHelper.getInstance().init(this);

		super.onCreate(savedInstanceState);

		if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == HiSettingsHelper.getInstance().getScreenOrietation()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == HiSettingsHelper.getInstance().getScreenOrietation()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
		}

		setContentView(R.layout.activity_main_frame);


		// Prepare Drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		String[] drwerListTitle = getResources().getStringArray(R.array.left_menu);
		DrawerAdapter adapter = new DrawerAdapter(this, R.layout.item_drawer, drwerListTitle);
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setHomeButtonEnabled(true);

		// Prepare gesture detector
		mSwipeListener = new OnSwipeTouchListener(this) {
			public void onSwipeRight() {
				//Log.v(LOG_TAG, "onSwipeRight");
				if (HiSettingsHelper.getInstance().isGestureBack() && !HiSettingsHelper.getInstance().getIsLandscape()) {
					popFragment(false);
				}
			}

			public void onSwipeTop() {
				if (mOnSwipeCallback != null) {
					if (mOnSwipeCallback instanceof ThreadDetailFragment) {
						((ThreadDetailFragment) mOnSwipeCallback).onSwipeTop();
					} else if (mOnSwipeCallback instanceof SmsFragment) {
						((SmsFragment) mOnSwipeCallback).onSwipeTop();
					}
				}
			}

			public void onSwipeBottom() {
				if (mOnSwipeCallback != null) {
					if (mOnSwipeCallback instanceof ThreadDetailFragment) {
						((ThreadDetailFragment) mOnSwipeCallback).onSwipeBottom();
					} else if (mOnSwipeCallback instanceof SmsFragment) {
						((SmsFragment) mOnSwipeCallback).onSwipeBottom();
					}
				}
			}
		};
		findViewById(R.id.main_frame_container).setOnTouchListener(mSwipeListener);

		if (findViewById(R.id.thread_detail_container_in_main) != null) {
			HiSettingsHelper.getInstance().setIsLandscape(true);
		} else {
			HiSettingsHelper.getInstance().setIsLandscape(false);
		}


		// Prepare Fragments
		getFragmentManager().addOnBackStackChangedListener(new BackStackChangedListener());
		getFragmentManager().beginTransaction()
				.replace(R.id.main_frame_container, new ThreadListFragment(), ThreadListFragment.class.getName())
				.commit();

		// Check if Account info exist
		if (!HiSettingsHelper.getInstance().isLoginInfoValid()) {
			getFragmentManager().beginTransaction()
					.replace(R.id.main_frame_container, new SettingsFragment(), SettingsFragment.class.getName())
					.addToBackStack(SettingsFragment.class.getName())
					.commit();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		AvatarUrlCache.getInstance().save();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main_frame, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(LOG_TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
			case android.R.id.home:
				popFragment(false);
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		Log.v(LOG_TAG, "onBackPressed");

		if (!popFragment(true)) {
			mQuit++;
//            if (mQuit == 1) {
//                Toast.makeText(this, "再按一次退出HiPDA", Toast.LENGTH_LONG).show();
//            } else {
			finish();
//            }
		}

	}

	public boolean popFragment(boolean backPressed) {
		Log.v(LOG_TAG, "popFragment");
		FragmentManager fm = getFragmentManager();
		int count = fm.getBackStackEntryCount();
		Log.v(LOG_TAG, "before pop, count=" + count);
		if (count > 0) {
			fm.popBackStackImmediate();
			count = fm.getBackStackEntryCount();
			Log.v(LOG_TAG, "after pop, count=" + count);
			if (count > 0) {
				FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(count - 1);
				String str = backEntry.getName();
				Log.v(LOG_TAG, "after pop, name=" + str);
				Fragment fragment = getFragmentManager().findFragmentByTag(str);

				if (fragment != null) {
					fragment.setHasOptionsMenu(true);
				}
			} else {
				Fragment fg = fm.findFragmentById(R.id.main_frame_container);
				if (fg != null) {
					fg.setHasOptionsMenu(true);
				}
			}
			return true;
		} else {
			if (!backPressed) {
				mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.openDrawer(Gravity.LEFT);
				} else {
					mDrawerLayout.closeDrawers();
				}
			}
			return false;
		}

	}

	@Override
	public void onAvatarUrlUpdated() {
		Fragment fragment = getFragmentManager().findFragmentByTag(ThreadListFragment.class.getName());
		if (fragment instanceof ThreadListFragment) {
			((ThreadListFragment) fragment).refreshAvatars();
		}
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			switch (position) {
				case 0:    // forum
					getFragmentManager().beginTransaction()
							.replace(R.id.main_frame_container, new ThreadListFragment(), ThreadListFragment.class.getName())
							.commit();
					break;
				case 1:    // search
					Bundle searchBundle = new Bundle();
					searchBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_SEARCH);
					SimpleListFragment searchFragment = new SimpleListFragment();
					searchFragment.setArguments(searchBundle);
					getFragmentManager().beginTransaction()
							.replace(R.id.main_frame_container, searchFragment, SimpleListFragment.class.getName())
							.commit();
					break;
				case 2:    // my posts
					Bundle postsBundle = new Bundle();
					postsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_MYPOST);
					SimpleListFragment postsFragment = new SimpleListFragment();
					postsFragment.setArguments(postsBundle);
					getFragmentManager().beginTransaction()
							.replace(R.id.main_frame_container, postsFragment, SimpleListFragment.class.getName())
							.commit();
					break;
				case 3:    // my reply
					Bundle replyBundle = new Bundle();
					replyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_MYREPLY);
					SimpleListFragment replyFragment = new SimpleListFragment();
					replyFragment.setArguments(replyBundle);
					getFragmentManager().beginTransaction()
							.replace(R.id.main_frame_container, replyFragment, SimpleListFragment.class.getName())
							.commit();
					break;
				case 4:    // my favorites
					Bundle favBundle = new Bundle();
					favBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_FAVORITES);
					SimpleListFragment favFragment = new SimpleListFragment();
					favFragment.setArguments(favBundle);
					getFragmentManager().beginTransaction()
							.replace(R.id.main_frame_container, favFragment, SimpleListFragment.class.getName())
							.commit();
					break;
				case 5:    // sms
					Bundle smsBundle = new Bundle();
					smsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_SMS);
					SimpleListFragment smsFragment = new SimpleListFragment();
					smsFragment.setArguments(smsBundle);
					getFragmentManager().beginTransaction()
							.replace(R.id.main_frame_container, smsFragment, SimpleListFragment.class.getName())
							.commit();
					break;
				case 6:    // thread notify
					Bundle notifyBundle = new Bundle();
					notifyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_THREADNOTIFY);
					SimpleListFragment notifyFragment = new SimpleListFragment();
					notifyFragment.setArguments(notifyBundle);
					getFragmentManager().beginTransaction()
							.replace(R.id.main_frame_container, notifyFragment, SimpleListFragment.class.getName())
							.commit();
					break;
				case 7:    // settings
					getFragmentManager().beginTransaction()
							.replace(R.id.main_frame_container, new SettingsFragment(), SettingsFragment.class.getName())
							.addToBackStack(SettingsFragment.class.getName())
							.commit();
					break;
				case 8:    // switch day/night theme
					HiSettingsHelper.getInstance().setNightTheme(!HiSettingsHelper.getInstance().isNightTheme());
					Intent intent = getIntent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					finish();
					startActivity(intent);
					break;
				default:
					break;
			}

			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	private class BackStackChangedListener implements FragmentManager.OnBackStackChangedListener {

		@Override
		public void onBackStackChanged() {
			//Log.v(LOG_TAG, "onBackStackChanged");

			// reset back key press counter
			mQuit = 0;

			// Make sure drawer only showed in top fragment
			// Make sure swipe only worked in second fragment
			FragmentManager fm = getFragmentManager();
			Log.v(LOG_TAG, "getBackStackEntryCount = " + String.valueOf(fm.getBackStackEntryCount()));
			if (!HiSettingsHelper.getInstance().getIsLandscape()) {
				if (fm.getBackStackEntryCount() > 0) {
					mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
					mEnableSwipe = true;
				} else {
					mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
					mEnableSwipe = false;
				}
			}
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//Log.v(LOG_TAG, "dispatchTouchEvent");
		if (mEnableSwipe) {
			//Log.v(LOG_TAG, "do dispatchTouchEvent");
			mSwipeListener.onTouch(null, ev);
		}
		return super.dispatchTouchEvent(ev);
	}

	public void registOnSwipeCallback(Fragment f) {
		mOnSwipeCallback = f;
	}
}
