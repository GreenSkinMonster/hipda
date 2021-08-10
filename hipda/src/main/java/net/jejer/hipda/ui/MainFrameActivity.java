package net.jejer.hipda.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.vanniktech.emoji.EmojiHandler;

import net.jejer.hipda.R;
import net.jejer.hipda.async.LoginEvent;
import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.async.NetworkReadyEvent;
import net.jejer.hipda.async.TaskHelper;
import net.jejer.hipda.bean.Forum;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.Profile;
import net.jejer.hipda.bean.ProfileComparator;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.job.SimpleListJob;
import net.jejer.hipda.service.NotiHelper;
import net.jejer.hipda.service.NotiWorker;
import net.jejer.hipda.ui.widget.FABHideOnScrollBehavior;
import net.jejer.hipda.ui.widget.HiProgressDialog;
import net.jejer.hipda.ui.widget.LoginDialog;
import net.jejer.hipda.ui.widget.OnSingleClickListener;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.DrawerHelper;
import net.jejer.hipda.utils.HiParserThreadList;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainFrameActivity extends BaseActivity {

    public final static int PERMISSIONS_REQUEST_CODE_STORAGE = 200;
    private final static int DRAG_SENSITIVITY = Utils.dpToPx(32);

    private Drawer mDrawer;
    private AccountHeader mAccountHeader;

    private NetworkStateReceiver mNetworkReceiver;
    private LoginDialog mLoginDialog;
    private HiProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.setActivityTheme(this);

        setContentView(R.layout.activity_main_frame);
        mRootView = findViewById(R.id.main_activity_root_view);
        mMainFrameContainer = findViewById(R.id.main_frame_container);
        mAppBarLayout = findViewById(R.id.appbar_layout);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        GlideHelper.initDefaultFiles();
        GlideHelper.refreshUserIcon(MainFrameActivity.this);
        EmojiHandler.init(UIUtils.isDayTheme(MainFrameActivity.this));
        NotiHelper.initNotificationChannel();

        EventBus.getDefault().register(this);
        setupDrawer();
        updateAppBarScrollFlag();

        mMainFab = findViewById(R.id.fab_main);
        mNotiificationFab = findViewById(R.id.fab_notification);

        //hack, to avoid MainFrameActivity be created more than once
        if (HiApplication.getMainActivityCount() > 1) {
            finish();
            return;
        }

        if (UIUtils.isTablet(this)) {
            mMainFab.setSize(FloatingActionButton.SIZE_NORMAL);
            mNotiificationFab.setSize(FloatingActionButton.SIZE_NORMAL);
        }

        updateFabGravity();

        mNetworkReceiver = new NetworkStateReceiver();
        registerReceiver(mNetworkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (savedInstanceState == null) {
            TaskHelper.updateImageHost();

            int fid = HiSettingsHelper.getInstance().getLastForumId();
            FragmentArgs args = FragmentUtils.parse(getIntent());
            if (args != null && args.getType() == FragmentArgs.TYPE_FORUM) {
                fid = args.getFid();
            }
            FragmentUtils.showForum(getSupportFragmentManager(), fid);

            if (args != null && args.getType() != FragmentArgs.TYPE_FORUM) {
                args.setSkipEnterAnim(true);
                args.setFid(fid);
                if (args.getType() == FragmentArgs.TYPE_NEW_THREAD)
                    args.setParentId(mSessionId);
                FragmentUtils.show(this, args);
            }

            TaskHelper.runDailyTask(false);
            NotiWorker.scheduleOrCancelWork();

            if (HiApplication.isUpdated()) {
                HiApplication.setUpdated(false);
                UIUtils.showReleaseNotesDialog(this);
            } else {
                if (HiSettingsHelper.getInstance().isAutoUpdateCheckable()) {
                    //new UpdateHelper(this, true).check();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isFinishing())
            return;
        FragmentArgs args = FragmentUtils.parse(intent);
        if (args != null) {
            HiParserThreadList.holdFetchNotify();
            args.setSkipEnterAnim(true);
            FragmentUtils.show(this, args);
        }
    }

    private void setupDrawer() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                GlideHelper.loadAvatar(Glide.with(MainFrameActivity.this), imageView, uri.toString());
            }

            @Override
            public void cancel(ImageView imageView) {
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return new IconicsDrawable(ctx, GoogleMaterial.Icon.gmd_account_circle).color(Color.WHITE).sizeDp(48);
            }
        });

        // Create the AccountHeader
        mAccountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withAccountHeader(R.layout.material_drawer_account_header)
                .withHeaderBackground(R.drawable.header)
                .withTextColor(ContextCompat.getColor(this, R.color.md_grey_300))
                .withDividerBelowHeader(false)
                .withCompactStyle(false)
                .withSelectionListEnabled(false)
                .withThreeSmallProfileImages(true)
                .withOnAccountHeaderProfileImageListener(new ProfileImageListener())
                .build();

        ImageView addAccountImageView = mAccountHeader.getView().findViewById(R.id.material_drawer_account_add);
        ImageView logoutAccountImageView = mAccountHeader.getView().findViewById(R.id.material_drawer_account_logout);

        addAccountImageView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                closeDrawer();
                showLoginDialog();
            }
        });

        logoutAccountImageView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                closeDrawer();
                if (LoginHelper.isLoggedIn()) {
                    showLogoutDialog();
                }
            }
        });

        updateAccountHeader();

        ArrayList<IDrawerItem> drawerItems = new ArrayList<>();
        drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.SMS));
        drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.THREAD_NOTIFY));
        drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.HISTORIES));
        drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.NEW_POSTS));

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


        if (subItems.size() > 0)
            drawerItems.add(
                    new ExpandableDrawerItem()
                            .withName(R.string.title_drawer_expandable)
                            .withIcon(GoogleMaterial.Icon.gmd_more_horiz)
                            .withIdentifier(Constants.DRAWER_NO_ACTION)
                            .withSelectable(false)
                            .withSubItems(subItems.toArray(new IDrawerItem[0])
                            ));

        drawerItems.add(new DividerDrawerItem());
        if (HiSettingsHelper.THEME_AUTO.equals(HiSettingsHelper.getInstance().getTheme())) {
            drawerItems.add(DrawerHelper.getPrimaryMenuItem(DrawerHelper.DrawerItem.SETTINGS));
        } else {
            drawerItems.add(new SwitchDrawerItem()
                    .withName(R.string.title_drawer_setting)
                    .withIdentifier(Constants.DRAWER_SETTINGS)
                    .withIcon(GoogleMaterial.Icon.gmd_settings)
                    .withChecked(UIUtils.isNightTheme(MainFrameActivity.this))
                    .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                            if (UIUtils.isNightTheme(MainFrameActivity.this) != isChecked) {
                                final DrawerLayout.DrawerListener nightModeDrawerListener = new DrawerLayout.DrawerListener() {
                                    @Override
                                    public void onDrawerSlide(View drawerView, float slideOffset) {
                                    }

                                    @Override
                                    public void onDrawerOpened(View drawerView) {
                                    }

                                    @Override
                                    public void onDrawerClosed(View drawerView) {
                                        mDrawer.getDrawerLayout().removeDrawerListener(this);
                                        UIUtils.setDayNightTheme();
                                    }

                                    @Override
                                    public void onDrawerStateChanged(int newState) {
                                    }
                                };
                                HiSettingsHelper.getInstance().setTheme(UIUtils.isDayTheme(MainFrameActivity.this) ? HiSettingsHelper.THEME_DARK : HiSettingsHelper.THEME_LIGHT);

                                mDrawer.getDrawerLayout().addDrawerListener(nightModeDrawerListener);
                                mDrawer.closeDrawer();
                            }
                        }
                    }));
        }
        drawerItems.add(new DividerDrawerItem());
        List<Integer> forums = HiSettingsHelper.getInstance().getForums();
        for (int fid : forums) {
            Forum forum = HiUtils.getForumByFid(fid);
            if (forum != null)
                drawerItems.add(new PrimaryDrawerItem().withName(forum.getName())
                        .withIdentifier(forum.getId())
                        .withIcon(forum.getIcon()));
        }

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(mAccountHeader)
                .withTranslucentStatusBar(true)
                .withDrawerItems(drawerItems)
                .withStickyFooterDivider(false)
                .withStickyFooterShadow(false)
                .withOnDrawerItemClickListener(new DrawerItemClickListener())
                .build();

        mDrawer.getRecyclerView().setVerticalScrollBarEnabled(false);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawer.isDrawerOpen())
                    mDrawer.closeDrawer();
                else
                    mDrawer.openDrawer();
            }
        });

        mToolbar.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //get top displaying fragment
                Fragment fg = getSupportFragmentManager().findFragmentById(R.id.main_frame_container);
                if (fg instanceof BaseFragment) {
                    ((BaseFragment) fg).scrollToTop();
                }
            }
        });

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
            mMainFab.show();
        }
    }

    public void updateAccountHeader() {
        if (mAccountHeader != null) {
            ProfileDrawerItem[] items = getProfileDrawerItems();
            mAccountHeader.clear();
            mAccountHeader.addProfiles(items);
        }
        ImageView loginImageView = mAccountHeader.getView().findViewById(R.id.material_drawer_account_logout);
        if (loginImageView != null) {
            if (LoginHelper.isLoggedIn()) {
                loginImageView.setVisibility(View.VISIBLE);
            } else {
                loginImageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (HiApplication.getSettingStatus() == HiApplication.RESTART) {
            HiApplication.setSettingStatus(HiApplication.IDLE);
            Utils.restartActivity(this);
        } else if (HiApplication.getSettingStatus() == HiApplication.RECREATE) {
            HiApplication.setSettingStatus(HiApplication.IDLE);
            recreateActivity();
        } else if (HiApplication.getSettingStatus() == HiApplication.RELOAD) {
            HiApplication.setSettingStatus(HiApplication.IDLE);
            updateAppBarScrollFlag();
            updateFabGravity();

            Fragment fg = getSupportFragmentManager().findFragmentByTag(ThreadListFragment.class.getName());
            if (fg instanceof ThreadListFragment) {
                ((ThreadListFragment) fg).notifyDataSetChanged();
            }
        } else {
            if (!LoginHelper.isLoggedIn())
                showLoginDialog();
        }
    }

    @Override
    public void onDestroy() {
        if (mNetworkReceiver != null)
            unregisterReceiver(mNetworkReceiver);
        EventBus.getDefault().unregister(this);
        dismissLoginDialog();
        super.onDestroy();
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
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.main_frame_container);

        if (fragment instanceof BaseFragment) {
            if (((BaseFragment) fragment).onBackPressed())
                return;
        }

        finishWithDefault();
    }

    private float mStartX;
    private float mStartY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartX = ev.getX();
                mStartY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                float deltaX = ev.getX() - mStartX;
                float deltaY = Math.abs(ev.getY() - mStartY);

                if (deltaX >= DRAG_SENSITIVITY && deltaY < 0.5 * deltaX) {
                    if (!mDrawer.isDrawerOpen()) {
                        mDrawer.openDrawer();
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private class DrawerItemClickListener implements Drawer.OnDrawerItemClickListener {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem iDrawerItem) {

            if (iDrawerItem.getIdentifier() == Constants.DRAWER_NO_ACTION)
                return false;

            switch ((int) iDrawerItem.getIdentifier()) {
                case Constants.DRAWER_NEW_POSTS:
                    FragmentUtils.showSimpleListActivity(MainFrameActivity.this, false, SimpleListJob.TYPE_NEW_POSTS);
                    break;
                case Constants.DRAWER_MYPOST:
                    FragmentUtils.showSimpleListActivity(MainFrameActivity.this, false, SimpleListJob.TYPE_MYPOST);
                    break;
                case Constants.DRAWER_MYREPLY:
                    FragmentUtils.showSimpleListActivity(MainFrameActivity.this, false, SimpleListJob.TYPE_MYREPLY);
                    break;
                case Constants.DRAWER_FAVORITES:
                    FragmentUtils.showSimpleListActivity(MainFrameActivity.this, false, SimpleListJob.TYPE_FAVORITES);
                    break;
                case Constants.DRAWER_HISTORIES:
                    FragmentUtils.showSimpleListActivity(MainFrameActivity.this, false, SimpleListJob.TYPE_HISTORIES);
                    break;
                case Constants.DRAWER_SMS:
                    FragmentUtils.showSimpleListActivity(MainFrameActivity.this, false, SimpleListJob.TYPE_SMS);
                    break;
                case Constants.DRAWER_THREADNOTIFY:
                    FragmentUtils.showSimpleListActivity(MainFrameActivity.this, false, SimpleListJob.TYPE_THREAD_NOTIFY);
                    break;
                case Constants.DRAWER_SETTINGS:
                    Intent intent = new Intent(MainFrameActivity.this, SettingActivity.class);
                    ActivityCompat.startActivity(MainFrameActivity.this, intent,
                            FragmentUtils.getAnimBundle(MainFrameActivity.this, false));
                    break;
                default:
                    int forumId = (int) iDrawerItem.getIdentifier();
                    FragmentUtils.showForum(getSupportFragmentManager(), forumId);
                    break;
            }

            return false;
        }

    }

    private class ProfileImageListener implements AccountHeader.OnAccountHeaderProfileImageListener {

        @Override
        public boolean onProfileImageClick(View view, IProfile drawerProfile, boolean current) {
            ProfileDrawerItem item = (ProfileDrawerItem) drawerProfile;
            String username = item.getName().toString();
            Profile profile = HiSettingsHelper.getInstance().getProfile(username);
            if (current) {
                if (LoginHelper.isLoggedIn()) {
                    UIUtils.toast("长按头像退出登录");
                } else {
                    showLoginDialog();
                }
            } else if (profile != null) {
                LoginHelper.logout();
                if (mNotiificationFab != null)
                    mNotiificationFab.hide();

                HiSettingsHelper.getInstance().setUsername(profile.getUsername());
                HiSettingsHelper.getInstance().setPassword(profile.getPassword());
                HiSettingsHelper.getInstance().setSecQuestion(profile.getSecQuestion());
                HiSettingsHelper.getInstance().setSecAnswer(profile.getSecAnswer());
                HiSettingsHelper.getInstance().setUid("");

                doLoginProgress();
            }
            closeDrawer();
            expandAppBar();
            return true;
        }

        @Override
        public boolean onProfileImageLongClick(View view, IProfile drawerProfile, boolean current) {
            ProfileDrawerItem item = (ProfileDrawerItem) drawerProfile;
            final String username = item.getName().toString();
            if (LoginHelper.isLoggedIn() && username.equalsIgnoreCase(HiSettingsHelper.getInstance().getUsername())) {
                showLogoutDialog();
            } else if (!current) {
                showRemoveProfileDialog(username);
            } else {
                showLoginDialog();
            }
            closeDrawer();
            return true;
        }
    }

    public void doLoginProgress() {
        final String username = HiSettingsHelper.getInstance().getUsername();
        progressDialog = HiProgressDialog.show(this, "<" + username + "> 正在登录...");

        final LoginHelper loginHelper = new LoginHelper(this);

        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                return loginHelper.login(true);
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (result == Constants.STATUS_SUCCESS) {
                    UIUtils.toast("登录成功");
                    TaskHelper.runDailyTask(true);
                    progressDialog.dismiss();
                } else {
                    if (result == Constants.STATUS_FAIL_ABORT) {
                        HiSettingsHelper.getInstance().removeProfile(username);
                        HiSettingsHelper.getInstance().setUsername("");
                        HiSettingsHelper.getInstance().setPassword("");
                        HiSettingsHelper.getInstance().setSecQuestion("");
                        HiSettingsHelper.getInstance().setSecAnswer("");
                    }
                    updateAccountHeader();
                    progressDialog.dismissError(loginHelper.getErrorMsg());
                }
            }
        }.execute();
    }

    private void showRemoveProfileDialog(String username) {
        AlertDialog dialog = new AlertDialog.Builder(MainFrameActivity.this)
                .setTitle("清除登录信息？")
                .setMessage("确认清除用户 <" + username + "> 的登录信息？\n")
                .setPositiveButton(getResources().getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HiSettingsHelper.getInstance().removeProfile(username);
                                updateAccountHeader();
                                UIUtils.toast("<" + username + "> 登录信息已经清除");
                            }
                        })
                .setNegativeButton(getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .create();
        dialog.show();
    }

    private void showLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(MainFrameActivity.this)
                .setTitle("退出登录？")
                .setMessage("退出当前登录用户 <" + HiSettingsHelper.getInstance().getUsername() + "> ？\n")
                .setPositiveButton("退出",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout(HiSettingsHelper.getInstance().getUsername(), false);
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .setNeutralButton("退出并清除登录信息", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout(HiSettingsHelper.getInstance().getUsername(), true);
                    }
                }).create();
        dialog.show();
    }

    private void logout(final String username, boolean removeProfile) {
        HiProgressDialog progressDialog = HiProgressDialog.show(MainFrameActivity.this, "正在退出...");
        LoginHelper.logout();
        if (mNotiificationFab != null)
            mNotiificationFab.hide();

        String message = "已退出登录";
        if (removeProfile) {
            HiSettingsHelper.getInstance().removeProfile(username);
            message += "并清除登录信息";
        }

        updateAccountHeader();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_frame_container);
        if (fragment != null && fragment instanceof ThreadListFragment) {
            ((ThreadListFragment) fragment).enterNotLoginState();
        }

        progressDialog.dismiss(message);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoginDialog();
            }
        }, 1000);
    }

    private class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            HiSettingsHelper.updateMobileNetworkStatus(context);
            EventBus.getDefault().post(new NetworkReadyEvent());
        }
    }

    public void updateDrawerBadge() {
        int smsCount = NotiHelper.getCurrentNotification().getSmsCount();
        int threadCount = NotiHelper.getCurrentNotification().getThreadCount();
        int threadNotifyIndex = mDrawer.getPosition(Constants.DRAWER_THREADNOTIFY);
        if (threadNotifyIndex != -1) {
            PrimaryDrawerItem drawerItem = (PrimaryDrawerItem) mDrawer.getDrawerItem(Constants.DRAWER_THREADNOTIFY);
            if (threadCount > 0) {
                drawerItem.withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));
                mDrawer.updateBadge(Constants.DRAWER_THREADNOTIFY, new StringHolder(threadCount + ""));
            } else {
                drawerItem.withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.background_grey));
                mDrawer.updateBadge(Constants.DRAWER_THREADNOTIFY, new StringHolder("0"));
            }
        }
        int smsNotifyIndex = mDrawer.getPosition(Constants.DRAWER_SMS);
        if (smsNotifyIndex != -1) {
            PrimaryDrawerItem drawerItem = (PrimaryDrawerItem) mDrawer.getDrawerItem(Constants.DRAWER_SMS);
            if (smsCount > 0) {
                drawerItem.withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));
                mDrawer.updateBadge(Constants.DRAWER_SMS, new StringHolder(smsCount + ""));
            } else {
                drawerItem.withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.background_grey));
                mDrawer.updateBadge(Constants.DRAWER_SMS, new StringHolder("0"));
            }
        }
    }

    void setDrawerSelection(int forumId) {
        if (mDrawer != null && !mDrawer.isDrawerOpen()) {
            int position = mDrawer.getPosition(forumId);
            if (mDrawer.getCurrentSelectedPosition() != position)
                mDrawer.setSelectionAtPosition(position, false);
        }
    }

    public void setActionBarDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        if (showHomeAsUp) {
            mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        }
    }

    void syncActionBarState() {
        if (mDrawer != null)
            mDrawer.getActionBarDrawerToggle().syncState();
    }

    private void closeDrawer() {
        if (mDrawer != null && mDrawer.isDrawerOpen())
            mDrawer.closeDrawer();
    }

    private void recreateActivity() {
        HiUtils.updateBaseUrls();
        UIUtils.setDayNightTheme();
        //avoid “RuntimeException: Performing pause of activity that is not resumed”
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    getWindow().setWindowAnimations(R.style.ThemeTransitionAnimation);
                    recreate();
                } catch (Exception e) {
                    Utils.restartActivity(MainFrameActivity.this);
                }
            }
        }, 5);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginEvent event) {
        Fragment fg = getSupportFragmentManager().findFragmentByTag(ThreadListFragment.class.getName());
        if (fg instanceof ThreadListFragment) {
            fg.setHasOptionsMenu(true);
            invalidateOptionsMenu();
            if (event.mManual)
                ((ThreadListFragment) fg).onRefresh();
        }
        updateAccountHeader();
        dismissLoginDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    UIUtils.toast("授权成功");
                }
                break;
            }
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    UIUtils.askForStoragePermission(this);
                }
                break;
            }
        }
    }

    public void showLoginDialog() {
        if (mLoginDialog == null || !mLoginDialog.isShowing()) {
            mLoginDialog = new LoginDialog(this);
            mLoginDialog.setTitle("用户登录");
            mLoginDialog.show();
        }
    }

    public void dismissLoginDialog() {
        if (mLoginDialog != null) {
            if (mLoginDialog.isShowing())
                mLoginDialog.dismiss();
            mLoginDialog = null;
        }
    }

    private ProfileDrawerItem[] getProfileDrawerItems() {
        List<ProfileDrawerItem> profileDrawerItems = new ArrayList<>();
        Profile activeProfile;
        if (LoginHelper.isLoggedIn()) {
            String username = HiSettingsHelper.getInstance().getUsername();
            activeProfile = HiSettingsHelper.getInstance().getProfile(username);
            if (activeProfile == null) {
                HiSettingsHelper.getInstance().saveCurrentProfile();
                activeProfile = HiSettingsHelper.getInstance().getProfile(username);
            }
            if (activeProfile != null) {
                profileDrawerItems.add(
                        new ProfileDrawerItem()
                                .withName(activeProfile.getUsername())
                                .withEmail("UID:" + activeProfile.getUid())
                                .withIcon(HiUtils.getAvatarUrlByUid(activeProfile.getUid()))
                );
            } else {
                //shouldn't happend
                profileDrawerItems.add(
                        new ProfileDrawerItem()
                                .withName(HiSettingsHelper.getInstance().getUsername())
                                .withEmail("UID:" + HiSettingsHelper.getInstance().getUid())
                                .withIcon(HiUtils.getAvatarUrlByUid(HiSettingsHelper.getInstance().getUid()))
                );
            }
        } else {
            profileDrawerItems.add(
                    new ProfileDrawerItem()
                            .withName("<未登录>")
                            .withEmail("")
                            .withIcon(GlideHelper.DEFAULT_AVATAR_FILE != null ? GlideHelper.DEFAULT_AVATAR_FILE.getAbsolutePath() : "")
            );
        }
        Map<String, Profile> profiles = HiSettingsHelper.getInstance().getProfiles();
        ProfileComparator comparator = new ProfileComparator(HiSettingsHelper.getInstance().getProfiles());
        TreeMap<String, Profile> sortedProfiles = new TreeMap<>(comparator);
        sortedProfiles.putAll(profiles);
        for (Profile profile : sortedProfiles.values()) {
            if (!profile.getUsername().equalsIgnoreCase(HiSettingsHelper.getInstance().getUsername())) {
                profileDrawerItems.add(
                        new ProfileDrawerItem()
                                .withName(profile.getUsername())
                                .withEmail("UID:" + profile.getUid())
                                .withIcon(HiUtils.getAvatarUrlByUid(profile.getUid()))
                );
            }
        }
        return profileDrawerItems.toArray(new ProfileDrawerItem[0]);
    }

}
