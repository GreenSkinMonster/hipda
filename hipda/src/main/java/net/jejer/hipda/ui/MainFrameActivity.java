package net.jejer.hipda.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.vanniktech.emoji.EmojiPopup;

import net.jejer.hipda.R;
import net.jejer.hipda.async.LoginEvent;
import net.jejer.hipda.async.NetworkReadyEvent;
import net.jejer.hipda.async.TaskHelper;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.job.SimpleListJob;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.setting.SettingMainFragment;
import net.jejer.hipda.ui.widget.FABHideOnScrollBehavior;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.DrawerHelper;
import net.jejer.hipda.utils.HiParserThreadList;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainFrameActivity extends AppCompatActivity {

    public final static int PERMISSIONS_REQUEST_CODE = 200;

    private OnSwipeTouchListener mSwipeListener;

    public Drawer drawer;
    private AccountHeader accountHeader;
    private ActionMode mActionMode;
    private View rootView;
    private View mMainFrameContainer;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private FloatingActionButton mMainFab;
    private FloatingActionButton mNotiificationFab;

    private NetworkStateReceiver mNetworkReceiver = new NetworkStateReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == HiSettingsHelper.getInstance().getScreenOrietation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == HiSettingsHelper.getInstance().getScreenOrietation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }

        setTheme(HiUtils.getThemeValue(this,
                HiSettingsHelper.getInstance().getActiveTheme(),
                HiSettingsHelper.getInstance().getPrimaryColor()));
        if (Build.VERSION.SDK_INT >= 21 && HiSettingsHelper.getInstance().isNavBarColored()) {
            getWindow().setNavigationBarColor(ColorHelper.getColorPrimary(this));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_frame);
        rootView = findViewById(R.id.main_activity_root_view);
        mMainFrameContainer = findViewById(R.id.main_frame_container);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

        EventBus.getDefault().register(this);
        setupDrawer();
        updateAppBarScrollFlag();

        mMainFab = (FloatingActionButton) findViewById(R.id.fab_main);
        mNotiificationFab = (FloatingActionButton) findViewById(R.id.fab_notification);
        mMainFab.setEnabled(false);
        mNotiificationFab.setEnabled(false);

        if (UIUtils.isTablet(this)) {
            mMainFab.setSize(FloatingActionButton.SIZE_NORMAL);
            mNotiificationFab.setSize(FloatingActionButton.SIZE_NORMAL);
        }

        updateFabGravity();

        // Prepare gesture detector
        mSwipeListener = new OnSwipeTouchListener(this) {
            public void onSwipeRight() {
                if (HiSettingsHelper.getInstance().isGestureBack()
                        && !HiSettingsHelper.getInstance().getIsLandscape()
                        && !(getFragmentManager().findFragmentByTag(PostFragment.class.getName()) instanceof PostFragment)) {
                    popFragment();
                }
            }
        };
        mMainFrameContainer.setOnTouchListener(mSwipeListener);

        // Prepare Fragments
        getFragmentManager().addOnBackStackChangedListener(new BackStackChangedListener());

        registerReceiver(mNetworkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (savedInstanceState == null) {
            TaskHelper.updateImageHost();

            int fid = HiSettingsHelper.getInstance().getLastForumId();

            FragmentArgs args = FragmentUtils.parse(getIntent());
            if (args != null && args.getType() == FragmentArgs.TYPE_FORUM)
                fid = args.getFid();

            clearBackStacks(false);
            FragmentUtils.showForum(getFragmentManager(), fid);

            if (args != null)
                FragmentUtils.show(getFragmentManager(), args);

            TaskHelper.runDailyTask(false);

            if (HiSettingsHelper.getInstance().isNotiTaskEnabled()) {
                if (!NotificationMgr.isAlarmRuning(this))
                    NotificationMgr.startAlarm(this);
            }
            UIUtils.askForPermission(this);
            if (HiApplication.isUpdated()) {
                HiApplication.setUpdated(false);
                UIUtils.showReleaseNotesDialog(this);
            } else {
                if (HiSettingsHelper.getInstance().isAutoUpdateCheckable()) {
                    new UpdateHelper(this, true).check();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        FragmentArgs args = FragmentUtils.parse(intent);
        if (args != null) {
            HiParserThreadList.holdFetchNotify();
            clearBackStacks(false);
            args.setDirectOpen(true);
            FragmentUtils.show(getFragmentManager(), args);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (!TextUtils.isEmpty(HiSettingsHelper.getInstance().getFont()))
            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        else
            super.attachBaseContext(newBase);
    }

    private void setupDrawer() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(MainFrameActivity.this)
                        .load(uri)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return new IconicsDrawable(ctx, FontAwesome.Icon.faw_user).color(Color.WHITE);
            }
        });

        // Create the AccountHeader
        String username = OkHttpHelper.getInstance().isLoggedIn() ? HiSettingsHelper.getInstance().getUsername() : "<未登录>";
        String avatarUrl = OkHttpHelper.getInstance().isLoggedIn() ? HiUtils.getAvatarUrlByUid(HiSettingsHelper.getInstance().getUid()) : "";
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withCompactStyle(true)
                .withSelectionListEnabled(false)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withEmail(username)
                                .withIcon(avatarUrl)
                )
                .build();

        ArrayList<IDrawerItem> drawerItems = new ArrayList<>();
        drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.SEARCH));
        drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.SMS));
        drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.THREAD_NOTIFY));

        Set<String> freqMenuIds = HiSettingsHelper.getInstance().getFreqMenus();
        Collection<IDrawerItem> subItems = new ArrayList<>();
        if (freqMenuIds.contains("" + DrawerHelper.DrawerItem.MY_POST.id))
            drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.MY_POST));
        else
            subItems.add(DrawerHelper.getSecondaryMenuItem(DrawerHelper.DrawerItem.MY_POST));

        if (freqMenuIds.contains("" + DrawerHelper.DrawerItem.MY_REPLY.id))
            drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.MY_REPLY));
        else
            subItems.add(DrawerHelper.getSecondaryMenuItem(DrawerHelper.DrawerItem.MY_REPLY));

        if (freqMenuIds.contains("" + DrawerHelper.DrawerItem.MY_FAVORITES.id))
            drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.MY_FAVORITES));
        else
            subItems.add(DrawerHelper.getSecondaryMenuItem(DrawerHelper.DrawerItem.MY_FAVORITES));

        if (freqMenuIds.contains("" + DrawerHelper.DrawerItem.HISTORIES.id))
            drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.HISTORIES));
        else
            subItems.add(DrawerHelper.getSecondaryMenuItem(DrawerHelper.DrawerItem.HISTORIES));

        if (subItems.size() > 0)
            drawerItems.add(
                    new ExpandableDrawerItem()
                            .withName(R.string.title_drawer_expandable)
                            .withIcon(GoogleMaterial.Icon.gmd_more_horiz)
                            .withIdentifier(Constants.DRAWER_NO_ACTION)
                            .withSelectable(false)
                            .withSubItems(subItems.toArray(new IDrawerItem[subItems.size()])
                            ));

        drawerItems.add(new DividerDrawerItem());
        drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.SETTINGS));
        if (!TextUtils.isEmpty(HiSettingsHelper.getInstance().getNightTheme())) {
            drawerItems.add(new SwitchDrawerItem()
                    .withName(R.string.title_drawer_night_mode)
                    .withIdentifier(Constants.DRAWER_NIGHT_MODE)
                    .withIcon(GoogleMaterial.Icon.gmd_brightness_medium)
                    .withChecked(HiSettingsHelper.getInstance().isNightMode())
                    .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                            if (HiSettingsHelper.getInstance().isNightMode() != isChecked) {
                                HiSettingsHelper.getInstance().setNightMode(isChecked);
                                Utils.restartActivity(MainFrameActivity.this);
                            }
                        }
                    }));
        }
        drawerItems.add(new DividerDrawerItem());
        for (int i = 0; i < HiUtils.FORUM_IDS.length; i++) {
            if (HiUtils.isForumEnabled(HiUtils.FORUM_IDS[i]))
                drawerItems.add(new PrimaryDrawerItem().withName(HiUtils.FORUM_NAMES[i])
                        .withIdentifier(HiUtils.FORUM_IDS[i])
                        .withIcon(HiUtils.FORUM_ICONS[i]));
        }

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(accountHeader)
                .withTranslucentStatusBar(true)
                .withDrawerItems(drawerItems)
                .withStickyFooterDivider(false)
                .withStickyFooterShadow(false)
                .withOnDrawerItemClickListener(new DrawerItemClickListener())
                .build();

        drawer.getRecyclerView().setVerticalScrollBarEnabled(false);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.hideSoftKeyboard(MainFrameActivity.this);
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    if (drawer.isDrawerOpen())
                        drawer.closeDrawer();
                    else
                        drawer.openDrawer();
                } else {
                    popFragment();
                }
            }
        });

        mToolbar.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //get top displaying fragment
                Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
                if (fg instanceof BaseFragment) {
                    ((BaseFragment) fg).scrollToTop();
                }
            }
        });

        mToolbar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
                if (fg instanceof ThreadDetailFragment) {
                    ((ThreadDetailFragment) fg).showTheadTitle();
                }
                return true;
            }
        });

    }

    public void updateAppBarScrollFlag() {
        setAppBarCollapsible(HiSettingsHelper.getInstance().isAppBarCollapsible());
    }

    private void setAppBarCollapsible(boolean collapsible) {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        if (collapsible) {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            mAppBarLayout.setTag("1");
        } else {
            params.setScrollFlags(0);
            mAppBarLayout.setTag("0");
            if (mMainFrameContainer.getPaddingBottom() != 0) {
                mMainFrameContainer.setPadding(
                        mMainFrameContainer.getPaddingLeft(),
                        mMainFrameContainer.getPaddingTop(),
                        mMainFrameContainer.getPaddingRight(),
                        0);
                mMainFrameContainer.requestLayout();
            }
        }
    }

    public void updateFabGravity() {
        CoordinatorLayout.LayoutParams mainFabParams = (CoordinatorLayout.LayoutParams) mMainFab.getLayoutParams();
        CoordinatorLayout.LayoutParams notiFabParams = (CoordinatorLayout.LayoutParams) mNotiificationFab.getLayoutParams();
        if (HiSettingsHelper.getInstance().isFabLeftSide()) {
            mainFabParams.anchorGravity = Gravity.BOTTOM | Gravity.LEFT | Gravity.END;
        } else {
            mainFabParams.anchorGravity = Gravity.BOTTOM | Gravity.RIGHT | Gravity.END;
        }
        if (HiSettingsHelper.getInstance().isFabAutoHide()) {
            mainFabParams.setBehavior(new FABHideOnScrollBehavior());
            notiFabParams.setBehavior(new FABHideOnScrollBehavior());
        } else {
            mainFabParams.setBehavior(null);
            notiFabParams.setBehavior(null);
            mMainFab.setEnabled(true);
            mNotiificationFab.setEnabled(true);
            mMainFab.show();
        }
    }

    public void updateAccountHeader() {
        if (accountHeader != null) {
            String username = OkHttpHelper.getInstance().isLoggedIn() ? HiSettingsHelper.getInstance().getUsername() : "<未登录>";
            String avatarUrl = OkHttpHelper.getInstance().isLoggedIn() ? HiUtils.getAvatarUrlByUid(HiSettingsHelper.getInstance().getUid()) : "";
            accountHeader.removeProfile(0);
            accountHeader.addProfile(new ProfileDrawerItem()
                    .withEmail(username)
                    .withIcon(avatarUrl), 0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mNetworkReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HiApplication.activityResumed();
        Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
        if (fg instanceof ThreadListFragment) {
            clearBackStacks(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        HiApplication.activityPaused();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main_frame, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                UIUtils.hideSoftKeyboard(this);
                popFragment();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return;
        }

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.main_frame_container);

        if (fragment instanceof BaseFragment) {
            if (((BaseFragment) fragment).onBackPressed())
                return;
        }

        if (!popFragment()) {
            finish();
        }

    }

    @Override
    public ActionMode startSupportActionMode(@NonNull ActionMode.Callback callback) {
        ActionMode actionMode = super.startSupportActionMode(callback);
        mActionMode = actionMode;
        return actionMode;
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        mActionMode = null;
    }

    public boolean popFragment() {
        FragmentManager fm = getFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            try {
                fm.popBackStackImmediate();
            } catch (IllegalStateException ignored) {
                // There's no way to avoid getting this if saveInstanceState has already been called.
            }
            Fragment fg = fm.findFragmentById(R.id.main_frame_container);
            if (fg != null)
                fg.setHasOptionsMenu(true);
            return true;
        }
        return false;
    }

    private class DrawerItemClickListener implements Drawer.OnDrawerItemClickListener {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem iDrawerItem) {

            if (iDrawerItem.getIdentifier() == Constants.DRAWER_NIGHT_MODE)
                return false;
            if (iDrawerItem.getIdentifier() == Constants.DRAWER_NO_ACTION)
                return false;

            //clear all backStacks from menu click
            clearBackStacks(false);

            switch ((int) iDrawerItem.getIdentifier()) {
                case Constants.DRAWER_SEARCH:
                    Bundle searchBundle = new Bundle();
                    searchBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListJob.TYPE_SEARCH);
                    SimpleListFragment searchFragment = new SimpleListFragment();
                    searchFragment.setArguments(searchBundle);
                    FragmentUtils.showFragment(getFragmentManager(), searchFragment, true);
                    break;
                case Constants.DRAWER_MYPOST:
                    Bundle postsBundle = new Bundle();
                    postsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListJob.TYPE_MYPOST);
                    SimpleListFragment postsFragment = new SimpleListFragment();
                    postsFragment.setArguments(postsBundle);
                    FragmentUtils.showFragment(getFragmentManager(), postsFragment, true);
                    break;
                case Constants.DRAWER_MYREPLY:
                    Bundle replyBundle = new Bundle();
                    replyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListJob.TYPE_MYREPLY);
                    SimpleListFragment replyFragment = new SimpleListFragment();
                    replyFragment.setArguments(replyBundle);
                    FragmentUtils.showFragment(getFragmentManager(), replyFragment, true);
                    break;
                case Constants.DRAWER_FAVORITES:
                    Bundle favBundle = new Bundle();
                    favBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListJob.TYPE_FAVORITES);
                    SimpleListFragment favFragment = new SimpleListFragment();
                    favFragment.setArguments(favBundle);
                    FragmentUtils.showFragment(getFragmentManager(), favFragment, true);
                    break;
                case Constants.DRAWER_HISTORIES:
                    Bundle hisBundle = new Bundle();
                    hisBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListJob.TYPE_HISTORIES);
                    SimpleListFragment hisFragment = new SimpleListFragment();
                    hisFragment.setArguments(hisBundle);
                    FragmentUtils.showFragment(getFragmentManager(), hisFragment, true);
                    break;
                case Constants.DRAWER_SMS:
                    FragmentUtils.showSmsList(getFragmentManager(), true);
                    break;
                case Constants.DRAWER_THREADNOTIFY:
                    FragmentUtils.showThreadNotify(getFragmentManager(), true);
                    break;
                case Constants.DRAWER_SETTINGS:
                    Fragment fragment = new SettingMainFragment();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_frame_container, fragment, fragment.getClass().getName())
                            .addToBackStack(fragment.getClass().getName())
                            .commit();
                    break;
                default:
                    //for forums
                    int forumId = (int) iDrawerItem.getIdentifier();
                    FragmentUtils.showForum(getFragmentManager(), forumId);
                    break;
            }

            return false;
        }

    }

    private void clearBackStacks(boolean resetActionBarTitle) {
        FragmentManager fm = getFragmentManager();
        while (fm.getBackStackEntryCount() > 0) {
            fm.popBackStackImmediate();
        }

        if (resetActionBarTitle) {
            Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
            if (fg instanceof ThreadListFragment) {
                ((ThreadListFragment) fg).resetActionBarTitle();
            }
        }
    }

    private class BackStackChangedListener implements FragmentManager.OnBackStackChangedListener {

        @Override
        public void onBackStackChanged() {
            if (mActionMode != null) {
                try {
                    mActionMode.finish();
                    mActionMode = null;
                } catch (Exception ignored) {
                }
            }

            FragmentManager fm = getFragmentManager();
            setDrawerHomeIdicator(fm.getBackStackEntryCount() > 0);

            if (HiSettingsHelper.getInstance().isAppBarCollapsible()) {
                Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
                //set flag every time, or setting fragment's last item is not visible
                setAppBarCollapsible(fg instanceof BaseFragment
                        && ((BaseFragment) fg).isAppBarCollapsible());
            }

            if (HiSettingsHelper.getInstance().isGestureBack()) {
                if (fm.getBackStackEntryCount() > 0) {
                    drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else {
                    drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }
        }
    }

    private class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            HiSettingsHelper.updateMobileNetworkStatus(context);
            EventBus.getDefault().post(new NetworkReadyEvent());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (HiSettingsHelper.getInstance().isGestureBack()) {
            mSwipeListener.onTouch(null, ev);
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return true;
        }
    }

    public void updateDrawerBadge() {
        int smsCount = NotificationMgr.getCurrentNotification().getSmsCount();
        int threadCount = NotificationMgr.getCurrentNotification().getThreadCount();
        int threadNotifyIndex = drawer.getPosition(Constants.DRAWER_THREADNOTIFY);
        if (threadNotifyIndex != -1) {
            PrimaryDrawerItem drawerItem = (PrimaryDrawerItem) drawer.getDrawerItem(Constants.DRAWER_THREADNOTIFY);
            if (threadCount > 0) {
                drawerItem.withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));
                drawer.updateBadge(Constants.DRAWER_THREADNOTIFY, new StringHolder(threadCount + ""));
            } else {
                drawerItem.withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.background_grey));
                drawer.updateBadge(Constants.DRAWER_THREADNOTIFY, new StringHolder("0"));
            }
        }
        int smsNotifyIndex = drawer.getPosition(Constants.DRAWER_SMS);
        if (smsNotifyIndex != -1) {
            PrimaryDrawerItem drawerItem = (PrimaryDrawerItem) drawer.getDrawerItem(Constants.DRAWER_SMS);
            if (smsCount > 0) {
                drawerItem.withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));
                drawer.updateBadge(Constants.DRAWER_SMS, new StringHolder(smsCount + ""));
            } else {
                drawerItem.withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.background_grey));
                drawer.updateBadge(Constants.DRAWER_SMS, new StringHolder("0"));
            }
        }
    }

    public void setDrawerHomeIdicator(boolean showHomeAsUp) {
        if (showHomeAsUp) {
            drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        }
    }

    public EmojiPopup.Builder getEmojiBuilder() {
        return EmojiPopup.Builder.fromRootView(rootView);
    }

    public FloatingActionButton getMainFab() {
        return mMainFab;
    }

    public FloatingActionButton getNotificationFab() {
        return mNotiificationFab;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginEvent event) {
        if (event.mManual) {
            clearBackStacks(true);
            Fragment fg = getFragmentManager().findFragmentByTag(ThreadListFragment.class.getName());
            if (fg instanceof ThreadListFragment) {
                ((ThreadListFragment) fg).onRefresh();
            }
        }
        updateAccountHeader();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
