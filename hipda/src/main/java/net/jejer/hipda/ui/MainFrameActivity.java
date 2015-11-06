package net.jejer.hipda.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import net.jejer.hipda.R;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.LoginEvent;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.setting.SettingMainFragment;
import net.jejer.hipda.utils.ColorUtils;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.NotificationMgr;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainFrameActivity extends AppCompatActivity {

    public final static int PERMISSIONS_REQUEST_CODE = 200;

    private OnSwipeTouchListener mSwipeListener;
    private Fragment mOnSwipeCallback = null;
    private int mQuit = 0;

    public Drawer drawer;
    private AccountHeader accountHeader;
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.v("onCreate");

        if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == HiSettingsHelper.getInstance().getScreenOrietation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == HiSettingsHelper.getInstance().getScreenOrietation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }

        EventBus.getDefault().register(this);

        setTheme(HiUtils.getThemeValue(HiSettingsHelper.getInstance().getActiveTheme()));
        if (Build.VERSION.SDK_INT >= 21 && HiSettingsHelper.getInstance().isNavBarColored()) {
            getWindow().setNavigationBarColor(ColorUtils.getColorPrimary(this));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_frame);

        setupDrawer();

        // Prepare gesture detector
        mSwipeListener = new OnSwipeTouchListener(this) {
            public void onSwipeRight() {
                if (HiSettingsHelper.getInstance().isGestureBack()
                        && !HiSettingsHelper.getInstance().getIsLandscape()
                        && !(getFragmentManager().findFragmentByTag(PostFragment.class.getName()) instanceof PostFragment)) {
                    popFragment(false);
                }
            }
        };
        findViewById(R.id.main_frame_container).setOnTouchListener(mSwipeListener);

        // Prepare Fragments
        getFragmentManager().addOnBackStackChangedListener(new BackStackChangedListener());

        if (savedInstanceState == null) {
            int fid = HiSettingsHelper.getInstance().getLastForumId();

            FragmentArgs args = FragmentUtils.parse(getIntent());
            if (args != null && args.getType() == FragmentArgs.TYPE_FORUM)
                fid = args.getFid();

            clearBackStacks(false);
            FragmentUtils.showForum(getFragmentManager(), fid);

            if (args != null)
                FragmentUtils.show(getFragmentManager(), args);

            if (HiSettingsHelper.getInstance().isAutoUpdateCheckable()) {
                new UpdateHelper(this, true).check();
            }

            FavoriteHelper.getInstance().updateCache();

            if (HiSettingsHelper.getInstance().isNotiTaskEnabled()) {
                if (!NotificationMgr.isAlarmRuning(this))
                    NotificationMgr.startAlarm(this);
            }

            askForPermission();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        FragmentArgs args = FragmentUtils.parse(intent);
        if (args != null) {
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                //clear tag or glide will throw execption
                //imageView.setTag(null);
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
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_search).withIdentifier(DrawerItem.SEARCH.id).withIcon(GoogleMaterial.Icon.gmd_search));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_mypost).withIdentifier(DrawerItem.MY_POST.id).withIcon(GoogleMaterial.Icon.gmd_assignment_account));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_myreply).withIdentifier(DrawerItem.MY_REPLY.id).withIcon(GoogleMaterial.Icon.gmd_comments));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_favorites).withIdentifier(DrawerItem.MY_FAVORITES.id).withIcon(GoogleMaterial.Icon.gmd_favorite));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_sms).withIdentifier(DrawerItem.SMS.id).withIcon(GoogleMaterial.Icon.gmd_email)
                .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.grey)));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_notify).withIdentifier(DrawerItem.THREAD_NOTIFY.id).withIcon(GoogleMaterial.Icon.gmd_notifications)
                .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.grey)));

        ArrayList<IDrawerItem> stickyDrawerItems = new ArrayList<>();
        stickyDrawerItems.add(new DividerDrawerItem());
        stickyDrawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_setting)
                .withIdentifier(DrawerItem.SETTINGS.id)
                .withIcon(GoogleMaterial.Icon.gmd_settings));
        if (!TextUtils.isEmpty(HiSettingsHelper.getInstance().getNightTheme())) {
            stickyDrawerItems.add(new SwitchDrawerItem()
                    .withName("夜间模式")
                    .withIdentifier(Constants.DRAWER_NIGHT_MODE)
                    .withIcon(GoogleMaterial.Icon.gmd_brightness_medium)
                    .withChecked(HiSettingsHelper.getInstance().isNightMode())
                    .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                            HiSettingsHelper.getInstance().setNightMode(isChecked);
                            ColorUtils.clear();
                            MainFrameActivity.this.finish();
                            startActivity(new Intent(MainFrameActivity.this.getApplicationContext(), MainFrameActivity.this.getClass()));
                            System.exit(0);
                        }
                    }));
        }
        stickyDrawerItems.add(new DividerDrawerItem());
        for (int i = 0; i < HiUtils.FORUM_IDS.length; i++) {
            if (HiUtils.isForumEnabled(HiUtils.FORUM_IDS[i]))
                stickyDrawerItems.add(new PrimaryDrawerItem().withName(HiUtils.FORUMS[i])
                        .withIdentifier(HiUtils.FORUM_IDS[i])
                        .withIcon(HiUtils.FORUM_ICONS[i]));
        }

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withTranslucentStatusBar(true)
                .withDrawerItems(drawerItems)
                .withStickyDrawerItems(stickyDrawerItems)
                .withStickyFooterDivider(false)
                .withStickyFooterShadow(false)
                .withOnDrawerItemClickListener(new DrawerItemClickListener())
                .build();

        //fix input layout problem when withTranslucentStatusBar enabled
        drawer.keyboardSupportEnabled(this, true);
        //drawer.getRecyclerView().setVerticalScrollBarEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                popFragment(false);
            }
        });

        toolbar.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //get top displaying fragment
                Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
                if (fg instanceof BaseFragment) {
                    ((BaseFragment) fg).scrollToTop();
                }
            }
        });

        toolbar.setOnLongClickListener(new View.OnLongClickListener() {
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
        Logger.v("onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                popFragment(false);
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

        if (mOnSwipeCallback instanceof ThreadDetailFragment) {
            if (((ThreadDetailFragment) mOnSwipeCallback).hideQuickReply())
                return;
        }

        Fragment postFragment = getFragmentManager().findFragmentByTag(PostFragment.class.getName());
        if (postFragment instanceof PostFragment && ((PostFragment) postFragment).isUserInputted()) {
            Dialog dialog = new AlertDialog.Builder(this)
                    .setTitle("放弃发表？")
                    .setMessage("\n确认放弃已输入的内容吗？\n")
                    .setPositiveButton(getResources().getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    popFragment(true);
                                }
                            })
                    .setNegativeButton(getResources().getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
            dialog.show();
        } else {
            if (!popFragment(true)) {
                mQuit++;
                if (mQuit == 1 && HiSettingsHelper.getInstance().getIsLandscape()) {
                    Toast.makeText(this, "再按一次退出HiPDA", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                }
            }
        }

    }

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        ActionMode actionMode = super.startSupportActionMode(callback);
        mActionMode = actionMode;
        return actionMode;
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        mActionMode = null;
    }

    public boolean popFragment(boolean backPressed) {
        FragmentManager fm = getFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            fm.popBackStackImmediate();
            count = fm.getBackStackEntryCount();
            if (count > 0) {
                FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(count - 1);
                String str = backEntry.getName();
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
                if (drawer.isDrawerOpen())
                    drawer.closeDrawer();
                else
                    drawer.openDrawer();
            }
            return false;
        }

    }

    public enum DrawerItem {
        SEARCH(Constants.DRAWER_SEARCH),
        MY_POST(Constants.DRAWER_MYPOST),
        MY_REPLY(Constants.DRAWER_MYREPLY),
        MY_FAVORITES(Constants.DRAWER_FAVORITES),
        SMS(Constants.DRAWER_SMS),
        THREAD_NOTIFY(Constants.DRAWER_THREADNOTIFY),
        SETTINGS(Constants.DRAWER_SETTINGS);

        public final int id;

        DrawerItem(int id) {
            this.id = id;
        }
    }

    private class DrawerItemClickListener implements Drawer.OnDrawerItemClickListener {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem iDrawerItem) {

            if (iDrawerItem.getIdentifier() == Constants.DRAWER_NIGHT_MODE)
                return false;

            //clear all backStacks from menu click
            clearBackStacks(false);

            switch (iDrawerItem.getIdentifier()) {
                case Constants.DRAWER_SEARCH:    // search
                    Bundle searchBundle = new Bundle();
                    searchBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_SEARCH);
                    SimpleListFragment searchFragment = new SimpleListFragment();
                    searchFragment.setArguments(searchBundle);
                    FragmentUtils.showFragment(getFragmentManager(), searchFragment, true);
                    break;
                case Constants.DRAWER_MYPOST:    // my posts
                    Bundle postsBundle = new Bundle();
                    postsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_MYPOST);
                    SimpleListFragment postsFragment = new SimpleListFragment();
                    postsFragment.setArguments(postsBundle);
                    FragmentUtils.showFragment(getFragmentManager(), postsFragment, true);
                    break;
                case Constants.DRAWER_MYREPLY:    // my reply
                    Bundle replyBundle = new Bundle();
                    replyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_MYREPLY);
                    SimpleListFragment replyFragment = new SimpleListFragment();
                    replyFragment.setArguments(replyBundle);
                    FragmentUtils.showFragment(getFragmentManager(), replyFragment, true);
                    break;
                case Constants.DRAWER_FAVORITES:    // my favorites
                    Bundle favBundle = new Bundle();
                    favBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_FAVORITES);
                    SimpleListFragment favFragment = new SimpleListFragment();
                    favFragment.setArguments(favBundle);
                    FragmentUtils.showFragment(getFragmentManager(), favFragment, true);
                    break;
                case Constants.DRAWER_SMS:    // sms
                    FragmentUtils.showSmsList(getFragmentManager(), true);
                    break;
                case Constants.DRAWER_THREADNOTIFY:    // thread notify
                    FragmentUtils.showThreadNotify(getFragmentManager(), true);
                    break;
                case Constants.DRAWER_SETTINGS:    // settings
                    Fragment fragment = new SettingMainFragment();
                    getFragmentManager().beginTransaction()
                            //.setCustomAnimations(0, 0, 0, R.anim.slide_out_right)
                            .replace(R.id.main_frame_container, fragment, fragment.getClass().getName())
                            .addToBackStack(fragment.getClass().getName())
                            .commit();
                    break;
                default:
                    //for forums
                    int forumId = iDrawerItem.getIdentifier();
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
            mQuit = 0;

            if (mActionMode != null) {
                try {
                    mActionMode.finish();
                    mActionMode = null;
                } catch (Exception ignored) {
                }
            }

            FragmentManager fm = getFragmentManager();
            setDrawerHomeIdicator(fm.getBackStackEntryCount() > 0);

            if (HiSettingsHelper.getInstance().isGestureBack()) {
                if (fm.getBackStackEntryCount() > 0) {
                    drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else {
                    drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }
            Logger.v("getBackStackEntryCount = " + String.valueOf(fm.getBackStackEntryCount()));
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (HiSettingsHelper.getInstance().isGestureBack()) {
            mSwipeListener.onTouch(null, ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void registOnSwipeCallback(Fragment f) {
        mOnSwipeCallback = f;
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

    @SuppressWarnings("unused")
    public void onEventMainThread(LoginEvent event) {
        updateAccountHeader();
    }

    public void askForPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))
                Toast.makeText(this, "下载、上传图片或者附件需要您授权存储空间读写的权限", Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
