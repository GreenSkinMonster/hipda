package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import net.jejer.hipda.R;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.async.UpdateHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageManager;
import net.jejer.hipda.utils.ACRAUtils;
import net.jejer.hipda.utils.ColorUtils;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.volley.VolleyHelper;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainFrameActivity extends AppCompatActivity {

    private OnSwipeTouchListener mSwipeListener;
    private Fragment mOnSwipeCallback = null;
    private int mQuit = 0;

    public Drawer drawerResult;
    private AccountHeader headerResult;
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.v("onCreate");

        HiSettingsHelper.getInstance().init(this);
        UpdateHelper.updateApp(this);
        GlideImageManager.init(this);

        ACRAUtils.init(this);
        GlideHelper.init(this);

        // Init Volley
        VolleyHelper.getInstance().init(this);
        FavoriteHelper.getInstance().init(this);

        super.onCreate(savedInstanceState);

        if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == HiSettingsHelper.getInstance().getScreenOrietation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == HiSettingsHelper.getInstance().getScreenOrietation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }

        setTheme(HiUtils.getThemeValue(HiSettingsHelper.getInstance().getTheme()));
        if (Build.VERSION.SDK_INT >= 21 && HiSettingsHelper.getInstance().isNavBarColored()) {
            getWindow().setNavigationBarColor(ColorUtils.getColorPrimary(this));
        }

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
            if (!NotificationMgr.isAlarmRnning(this))
                NotificationMgr.startAlarm(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        FragmentArgs args = FragmentUtils.parse(intent);
        if (args != null) {
            clearBackStacks(false);
            FragmentUtils.show(getFragmentManager(), args);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setupDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        DrawerImageLoader.init(new DrawerImageLoader.IDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                //clear tag or glide will throw execption
                //imageView.setTag(null);
                Glide.with(imageView.getContext())
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
        String username = VolleyHelper.getInstance().isLoggedIn() ? HiSettingsHelper.getInstance().getUsername() : "<未登录>";
        String avatarUrl = VolleyHelper.getInstance().isLoggedIn() ? HiUtils.getAvatarUrlByUid(HiSettingsHelper.getInstance().getUid()) : "";
        headerResult = new AccountHeaderBuilder()
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
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_mypost).withIdentifier(DrawerItem.MY_POST.id).withIcon(GoogleMaterial.Icon.gmd_assignment_ind));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_myreply).withIdentifier(DrawerItem.MY_REPLY.id).withIcon(GoogleMaterial.Icon.gmd_forum));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_favorites).withIdentifier(DrawerItem.MY_FAVORITES.id).withIcon(GoogleMaterial.Icon.gmd_favorite));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_sms).withIdentifier(DrawerItem.SMS.id).withIcon(GoogleMaterial.Icon.gmd_mail).withBadgeTextColor(Color.RED));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_notify).withIdentifier(DrawerItem.THREAD_NOTIFY.id).withIcon(GoogleMaterial.Icon.gmd_notifications).withBadgeTextColor(Color.RED));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.title_drawer_setting)
                .withIdentifier(DrawerItem.SETTINGS.id)
                .withIcon(GoogleMaterial.Icon.gmd_settings));

        ArrayList<IDrawerItem> stickyDrawerItems = new ArrayList<>();
        for (int i = 0; i < HiUtils.FORUM_IDS.length; i++) {
            if (HiUtils.isForumEnabled(HiUtils.FORUM_IDS[i]))
                stickyDrawerItems.add(new PrimaryDrawerItem().withName(HiUtils.FORUMS[i])
                        .withIdentifier(HiUtils.FORUM_IDS[i])
                        .withIcon(HiUtils.FORUM_ICONS[i]));
        }

        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(true)
                .withDrawerItems(drawerItems)
                .withStickyDrawerItems(stickyDrawerItems)
                .withOnDrawerItemClickListener(new DrawerItemClickListener())
                .build();

        //fix input layout problem when withTranslucentStatusBar enabled
        drawerResult.keyboardSupportEnabled(this, true);
        drawerResult.getListView().setVerticalScrollBarEnabled(false);

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
        String username = VolleyHelper.getInstance().isLoggedIn() ? HiSettingsHelper.getInstance().getUsername() : "<未登录>";
        String avatarUrl = VolleyHelper.getInstance().isLoggedIn() ? HiUtils.getAvatarUrlByUid(HiSettingsHelper.getInstance().getUid()) : "";
        headerResult.removeProfile(0);
        headerResult.addProfile(new ProfileDrawerItem()
                .withEmail(username)
                .withIcon(avatarUrl), 0);
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

        if (drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
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
                if (drawerResult.isDrawerOpen())
                    drawerResult.closeDrawer();
                else
                    drawerResult.openDrawer();
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
        public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
            //clear all backStacks from menu click
            clearBackStacks(false);

            switch (iDrawerItem.getIdentifier()) {
                case Constants.DRAWER_SEARCH:    // search
                    Bundle searchBundle = new Bundle();
                    searchBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_SEARCH);
                    SimpleListFragment searchFragment = new SimpleListFragment();
                    searchFragment.setArguments(searchBundle);
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(0, 0, 0, R.anim.slide_out_right)
                            .replace(R.id.main_frame_container, searchFragment, SimpleListFragment.class.getName())
                            .addToBackStack(SimpleListFragment.class.getName())
                            .commit();
                    break;
                case Constants.DRAWER_MYPOST:    // my posts
                    Bundle postsBundle = new Bundle();
                    postsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_MYPOST);
                    SimpleListFragment postsFragment = new SimpleListFragment();
                    postsFragment.setArguments(postsBundle);
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(0, 0, 0, R.anim.slide_out_right)
                            .replace(R.id.main_frame_container, postsFragment, SimpleListFragment.class.getName())
                            .addToBackStack(SimpleListFragment.class.getName())
                            .commit();
                    break;
                case Constants.DRAWER_MYREPLY:    // my reply
                    Bundle replyBundle = new Bundle();
                    replyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_MYREPLY);
                    SimpleListFragment replyFragment = new SimpleListFragment();
                    replyFragment.setArguments(replyBundle);
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(0, 0, 0, R.anim.slide_out_right)
                            .replace(R.id.main_frame_container, replyFragment, SimpleListFragment.class.getName())
                            .addToBackStack(SimpleListFragment.class.getName())
                            .commit();
                    break;
                case Constants.DRAWER_FAVORITES:    // my favorites
                    Bundle favBundle = new Bundle();
                    favBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_FAVORITES);
                    SimpleListFragment favFragment = new SimpleListFragment();
                    favFragment.setArguments(favBundle);
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(0, 0, 0, R.anim.slide_out_right)
                            .replace(R.id.main_frame_container, favFragment, SimpleListFragment.class.getName())
                            .addToBackStack(SimpleListFragment.class.getName())
                            .commit();
                    break;
                case Constants.DRAWER_SMS:    // sms
                    FragmentUtils.showSms(getFragmentManager());
                    break;
                case Constants.DRAWER_THREADNOTIFY:    // thread notify
                    FragmentUtils.showThreadNotify(getFragmentManager());
                    break;
                case Constants.DRAWER_SETTINGS:    // settings
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(0, 0, 0, R.anim.slide_out_right)
                            .replace(R.id.main_frame_container, new SettingsFragment(), SettingsFragment.class.getName())
                            .addToBackStack(SettingsFragment.class.getName())
                            .commit();
                    break;
                default:
                    //for forums
                    int forumId = iDrawerItem.getIdentifier();
                    ThreadListFragment threadListFragment = new ThreadListFragment();
                    Bundle argments = new Bundle();
                    argments.putInt(ThreadListFragment.ARG_FID_KEY, forumId);
                    threadListFragment.setArguments(argments);

                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_frame_container, threadListFragment, ThreadListFragment.class.getName())
                            .commit();
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

            if (fm.getBackStackEntryCount() > 0) {
                drawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                if (getSupportActionBar() != null)
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                drawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
            }

            if (HiSettingsHelper.getInstance().isGestureBack()) {
                if (fm.getBackStackEntryCount() > 0) {
                    drawerResult.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else {
                    drawerResult.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
        int smsCount = NotificationMgr.getSmsCount();
        int threadCount = NotificationMgr.getThreanCount();
        int threadNotifyIndex = drawerResult.getPositionFromIdentifier(Constants.DRAWER_THREADNOTIFY);
        if (threadNotifyIndex != -1) {
            if (threadCount > 0) {
                drawerResult.updateBadge(threadCount + "", threadNotifyIndex);
            } else {
                drawerResult.updateBadge("", threadNotifyIndex);
            }
        }
        int smsNotifyIndex = drawerResult.getPositionFromIdentifier(Constants.DRAWER_SMS);
        if (smsNotifyIndex != -1) {
            if (smsCount > 0) {
                drawerResult.updateBadge(smsCount + "", smsNotifyIndex);
            } else {
                drawerResult.updateBadge("", smsNotifyIndex);
            }
        }
    }

}
