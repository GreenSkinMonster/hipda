package net.jejer.hipda.ui;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import net.jejer.hipda.R;
import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.async.ThreadListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.NotificationBean;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.job.PostEvent;
import net.jejer.hipda.utils.ColorUtils;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ThreadListFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String ARG_FID_KEY = "fid";

    public final static int STAGE_NOT_LOGIN = -2;
    public final static int STAGE_ERROR = -1;
    public final static int STAGE_CLEAN = 0;
    public final static int STAGE_RELOGIN = 1;
    public final static int STAGE_GET_WEBPAGE = 2;
    public final static int STAGE_PARSE = 3;
    public final static int STAGE_DONE = 4;
    public final static int STAGE_PREFETCH = 5;
    public final static int STAGE_REFRESH = 6;
    public final static String STAGE_ERROR_KEY = "ERROR_MSG";
    public final static String STAGE_DETAIL_KEY = "ERROR_DETAIL";

    private Context mCtx;
    private int mForumId = 0;
    private int mPage = 1;
    private LoaderManager.LoaderCallbacks<ThreadListBean> mCallbacks;
    private ThreadListAdapter mThreadListAdapter;
    private List<ThreadBean> mThreadBeans = new ArrayList<>();
    private ListView mThreadListView;
    private ProgressBar mFooterProgressBar;
    private TextView mTipBar;
    private boolean mInloading = false;
    private Handler mMsgHandler;
    private HiProgressDialog postProgressDialog;
    private SwipeRefreshLayout swipeLayout;
    private FloatingActionMenu mFam;
    private FloatingActionButton mFabNotify;
    private ContentLoadingProgressBar loadingProgressBar;
    private int mFirstVisibleItem = 0;

    private MenuItem mForumTypeMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.v("onCreate");
        super.onCreate(savedInstanceState);

        mCtx = getActivity();

        if (getArguments() != null && getArguments().containsKey(ARG_FID_KEY)) {
            mForumId = getArguments().getInt(ARG_FID_KEY);
        }
        int forumIdx = HiUtils.getForumIndexByFid(mForumId);
        if (forumIdx == -1 || !HiUtils.isForumEnabled(mForumId)) {
            mForumId = HiUtils.FID_DISCOVERY;
        }

        HiSettingsHelper.getInstance().setLastForumId(mForumId);

        setHasOptionsMenu(true);
        mCallbacks = new ThreadListLoaderCallbacks();
        mThreadListAdapter = new ThreadListAdapter(this);

        mMsgHandler = new Handler(new ThreadListMsgHandler());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.v("onCreateView");
        View view = inflater.inflate(R.layout.fragment_thread_list, container, false);
        mThreadListView = (ListView) view.findViewById(R.id.lv_threads);

        View mFooterView = inflater.inflate(R.layout.vw_thread_list_footer, mThreadListView, false);
        mFooterProgressBar = (ProgressBar) mFooterView.findViewById(R.id.footer_progressbar);
        mFooterProgressBar.getIndeterminateDrawable()
                .setColorFilter(Color.LTGRAY, android.graphics.PorterDuff.Mode.SRC_IN);
        mThreadListView.addFooterView(mFooterView);

        mTipBar = (TextView) view.findViewById(R.id.thread_list_tipbar);
        mTipBar.setVisibility(View.INVISIBLE);
        mTipBar.bringToFront();
        mTipBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTipBar.setVisibility(View.INVISIBLE);
                if (HiSettingsHelper.getInstance().isErrorReportMode()) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("ERROR TIP FROM HiPDA", mTipBar.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), "错误信息已经复制至粘贴板", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "请在\"设置-其它\"中启用\"显示详细错误信息\"后再进行反馈", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.icon_blue);

        loadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.list_loading);

        mFam = (FloatingActionMenu) view.findViewById(R.id.fam_actions);
        mFam.setVisibility(View.INVISIBLE);

        FloatingActionButton fabRefresh = (FloatingActionButton) view.findViewById(R.id.action_fab_refresh);
        fabRefresh.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_refresh_alt).color(Color.WHITE).sizeDp(FAB_ICON_SIZE_DP + 4));
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(true);
                loadingProgressBar.showNow();
                if (swipeLayout.isShown())
                    swipeLayout.setRefreshing(false);
                refresh();
            }
        });

        FloatingActionButton fabNewThread = (FloatingActionButton) view.findViewById(R.id.action_fab_new_thread);
        fabNewThread.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_edit).color(Color.WHITE).sizeDp(FAB_ICON_SIZE_DP));
        fabNewThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(true);
                newThread();
            }
        });

        mFabNotify = (FloatingActionButton) view.findViewById(R.id.action_fab_notify);
        mFabNotify.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_email).color(Color.WHITE).sizeDp(FAB_ICON_SIZE_DP));
        mFabNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationBean bean = NotificationMgr.getCurrentNotification();
                if (bean.getSmsCount() == 1
                        && bean.getThreadCount() == 0
                        && HiUtils.isValidId(bean.getUid())
                        && !TextUtils.isEmpty(bean.getUsername())) {
                    FragmentUtils.showSmsDetail(getFragmentManager(), true, bean.getUid(), bean.getUsername());
                    NotificationMgr.getCurrentNotification().clearSmsCount();
                    showNotification();
                } else if (bean.getSmsCount() > 0) {
                    FragmentUtils.showSmsList(getFragmentManager(), true);
                } else if (bean.getThreadCount() > 0) {
                    FragmentUtils.showThreadNotify(getFragmentManager(), true);
                    NotificationMgr.getCurrentNotification().setThreadCount(0);
                    showNotification();
                } else {
                    Toast.makeText(mCtx, "没有未处理的通知", Toast.LENGTH_SHORT).show();
                    mFabNotify.setVisibility(View.GONE);
                }
            }
        });

        mThreadListView.setSelection(mFirstVisibleItem);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mCtx = getActivity();
            mFam.setVisibility(View.VISIBLE);
        }

        mThreadListView.setAdapter(mThreadListAdapter);
        mThreadListView.setOnItemClickListener(new OnItemClickCallback());
        mThreadListView.setOnItemLongClickListener(new OnItemLongClickCallback());
        mThreadListView.setOnScrollListener(new OnScrollCallback());
        mThreadListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mFam.isOpened()) {
                    mFam.close(false);
                }
                return false;
            }
        });

        if (mThreadListAdapter.getCount() == 0) {
            loadingProgressBar.show();
            mInloading = true;
            getLoaderManager().initLoader(0, null, mCallbacks);
            getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (!mInloading) {
            if (mThreadBeans.size() == 0) {
                refresh();
            } else {
                swipeLayout.setRefreshing(false);
                loadingProgressBar.hide();
                hideListViewFooter();
            }
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.v("onCreateOptionsMenu");

        menu.clear();
        inflater.inflate(R.menu.menu_thread_list, menu);
        if (mForumId == HiUtils.FID_BS) {
            mForumTypeMenuItem = menu.findItem(R.id.action_filter_by_type);
            mForumTypeMenuItem.setVisible(true);
            String typeId = HiSettingsHelper.getInstance().getBSTypeId();
            int typeIdIndex = HiUtils.getBSTypeIndexByFid(typeId);
            if (typeIdIndex == -1) typeIdIndex = 0;
            if (mCtx != null)
                mForumTypeMenuItem.setIcon(new IconicsDrawable(mCtx, HiUtils.BS_TYPE_ICONS[typeIdIndex]).color(Color.WHITE).actionBar());
        }

        int forumIdx = HiUtils.getForumIndexByFid(mForumId);

        setActionBarTitle(HiUtils.FORUMS[forumIdx]);
        setActionBarDisplayHomeAsUpEnabled(false);
        syncActionBarState();

        setDrawerSelection(mForumId);

        if (LoginHelper.isLoggedIn()) {
            showNotification();
        } else if (!HiSettingsHelper.getInstance().isLoginInfoValid()) {
            if (mThreadListAdapter != null) {
                mThreadBeans.clear();
                mThreadListAdapter.setBeans(mThreadBeans);
            }
            showLoginDialog();
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.v("onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
//            case R.id.action_refresh_list:
//                refresh();
//                return true;
            case R.id.action_thread_list_settings:
                showThreadListSettingsDialog();
                return true;
            case R.id.action_new_thread:
                newThread();
                return true;
            case R.id.action_filter_by_type:
                showForumTypesDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void newThread() {
        Bundle arguments = new Bundle();
        arguments.putInt(PostFragment.ARG_MODE_KEY, PostHelper.MODE_NEW_THREAD);
        arguments.putString(PostFragment.ARG_FID_KEY, mForumId + "");

        PostFragment fragment = new PostFragment();
        fragment.setParentSessionId(mSessionId);

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
    }

    public void resetActionBarTitle() {
        int forumIdx = HiUtils.getForumIndexByFid(mForumId);
        setActionBarTitle(HiUtils.FORUMS[forumIdx]);
        setActionBarDisplayHomeAsUpEnabled(false);
        syncActionBarState();
    }

    @Override
    public void onDestroy() {
        Logger.v("onDestory");
        getLoaderManager().destroyLoader(0);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Logger.v("onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void refresh() {
        mPage = 1;
        mThreadListView.setSelection(0);
        hideListViewFooter();
        mInloading = true;
        getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
    }

    @Override
    public void onRefresh() {
        refresh();
        loadingProgressBar.hide();
    }

    public class OnScrollCallback implements AbsListView.OnScrollListener {

        int mVisibleItemCount = 0;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            mFirstVisibleItem = firstVisibleItem;
            mVisibleItemCount = visibleItemCount;

            if (totalItemCount > 2 && firstVisibleItem + visibleItemCount > totalItemCount - 2) {
                if (!mInloading) {
                    mPage++;
                    showListViewFooter();
                    mInloading = true;
                    getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

    }

    private class OnItemClickCallback extends OnViewItemSingleClickListener {

        @Override
        public void onItemSingleClick(AdapterView<?> listView, View itemView, int position, long row) {
            //avoid footer click event ???
            if (position >= mThreadListAdapter.getCount())
                return;
            ThreadBean thread = mThreadListAdapter.getItem(position);
            String tid = thread.getTid();
            String title = thread.getTitle();
            setHasOptionsMenu(false);
            FragmentUtils.showThread(getFragmentManager(), false, tid, title, -1, -1, null, thread.getMaxPage());
        }

    }

    private class OnItemLongClickCallback implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long row) {
            ThreadBean thread = mThreadListAdapter.getItem(position);
            String tid = thread.getTid();
            String title = thread.getTitle();
            int page = 1;
            int maxPostsInPage = HiSettingsHelper.getInstance().getMaxPostsInPage();
            if (maxPostsInPage > 0 && TextUtils.isDigitsOnly(thread.getCountCmts())) {
                page = (int) Math.ceil((Integer.parseInt(thread.getCountCmts()) + 1) * 1.0f / maxPostsInPage);
            }
            setHasOptionsMenu(false);
            FragmentUtils.showThread(getFragmentManager(), false, tid, title, page, ThreadDetailFragment.LAST_FLOOR, null, thread.getMaxPage());
            return true;
        }
    }

    private class ThreadListLoaderCallbacks implements LoaderManager.LoaderCallbacks<ThreadListBean> {

        @Override
        public Loader<ThreadListBean> onCreateLoader(int arg0, Bundle arg1) {
            if (mPage == 1 && !swipeLayout.isRefreshing())
                loadingProgressBar.show();
            return new ThreadListLoader(mCtx, mMsgHandler, mForumId, mPage);
        }

        @Override
        public void onLoadFinished(Loader<ThreadListBean> loader, ThreadListBean threads) {
            Logger.v("onLoadFinished enter");

            mInloading = false;
            swipeLayout.setRefreshing(false);
            loadingProgressBar.hide();
            hideListViewFooter();

            if (threads == null) {
                if (mPage > 1) {
                    mPage--;
                }
                return;
            } else if (threads.count == 0) {

                if (threads.parsed && mPage <= 5 && !HiSettingsHelper.getInstance().isShowStickThreads()) {
                    mPage++;
                    mInloading = true;
                    getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
                    if (HiSettingsHelper.getInstance().getMaxPostsInPage() < HiUtils.MAX_THREADS_IN_PAGE)
                        Toast.makeText(mCtx, "置顶贴较多，请在网页版论坛 个人中心 \n将 论坛个性化设定 - 每页主题 设为 默认", Toast.LENGTH_LONG).show();
                    return;
                }

                // Page load fail.
                if (mPage > 1) {
                    mPage--;
                }

                Message msgError = Message.obtain();
                msgError.what = STAGE_ERROR;
                Bundle b = new Bundle();
                b.putString(STAGE_ERROR_KEY, "页面加载失败");
                msgError.setData(b);
                mMsgHandler.sendMessage(msgError);
                return;
            }

            if (TextUtils.isEmpty(HiSettingsHelper.getInstance().getUid())
                    && !TextUtils.isEmpty(threads.getUid())) {
                HiSettingsHelper.getInstance().setUid(threads.getUid());
                if (getActivity() != null)
                    ((MainFrameActivity) getActivity()).updateAccountHeader();
            }

            if (mPage == 1) {
                mThreadBeans.clear();
                mThreadBeans.addAll(threads.threads);
                mThreadListAdapter.setBeans(mThreadBeans);
                mThreadListView.setSelection(0);
            } else {
                for (ThreadBean newthread : threads.threads) {
                    boolean duplicate = false;
                    for (int i = 0; i < mThreadListAdapter.getCount(); i++) {
                        ThreadBean oldthread = mThreadListAdapter.getItem(i);
                        if (newthread.getTid().equals(oldthread.getTid())) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) {
                        mThreadBeans.add(newthread);
                    }
                }
                mThreadListAdapter.setBeans(mThreadBeans);
            }

            showNotification();

            Message msgDone = Message.obtain();
            msgDone.what = STAGE_DONE;
            mMsgHandler.sendMessage(msgDone);
            Message msgClean = Message.obtain();
            msgClean.what = STAGE_CLEAN;
            mMsgHandler.sendMessageDelayed(msgClean, 1000);

            mFam.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoaderReset(Loader<ThreadListBean> arg0) {
            Logger.v("onLoaderReset enter");

            mInloading = false;
            mTipBar.setVisibility(View.INVISIBLE);
            swipeLayout.setRefreshing(false);
            loadingProgressBar.hide();
            hideListViewFooter();
        }

    }

    private void hideListViewFooter() {
        mFooterProgressBar.setVisibility(View.GONE);
    }

    private void showListViewFooter() {
        mFooterProgressBar.setVisibility(View.VISIBLE);
    }

    private void showThreadListSettingsDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_thread_list_settings, null);

        final Switch sShowStickThreads = (Switch) viewlayout.findViewById(R.id.sw_show_stick_threads);
        final Switch sSortByPostTime = (Switch) viewlayout.findViewById(R.id.sw_sort_by_post_time);
        final Switch sShowPostType = (Switch) viewlayout.findViewById(R.id.sw_show_post_type);

        sShowStickThreads.setChecked(HiSettingsHelper.getInstance().isShowStickThreads());
        sShowStickThreads.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                HiSettingsHelper.getInstance().setShowStickThreads(arg1);
            }
        });
        sShowPostType.setChecked(HiSettingsHelper.getInstance().isShowPostType());
        sShowPostType.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                HiSettingsHelper.getInstance().setShowPostType(arg1);
                mThreadListAdapter.notifyDataSetChanged();
            }
        });
        sSortByPostTime.setChecked(HiSettingsHelper.getInstance().isSortByPostTime(mForumId));
        sSortByPostTime.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                HiSettingsHelper.getInstance().setSortByPostTime(mForumId, arg0.isChecked());
            }
        });

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setTitle(mCtx.getResources().getString(R.string.action_thread_list_settings));
        popDialog.setView(viewlayout);
        // Add the buttons
        popDialog.setPositiveButton(getResources().getString(android.R.string.ok), null);
        popDialog.create().show();
    }

    private void showForumTypesDialog() {
        final String currentTypeId = HiSettingsHelper.getInstance().getBSTypeId();
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_forum_types, null);

        final ListView listView = (ListView) viewlayout.findViewById(R.id.lv_forum_types);

        listView.setAdapter(new ForumTypesAdapter(getActivity()));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                dialog.dismiss();
                if (!HiUtils.BS_TYPE_IDS[position].equals(currentTypeId)) {
                    loadingProgressBar.showNow();
                    HiSettingsHelper.getInstance().setBSTypeId(HiUtils.BS_TYPE_IDS[position]);
                    if (mForumTypeMenuItem != null) {
                        mForumTypeMenuItem.setIcon(new IconicsDrawable(getActivity(), HiUtils.BS_TYPE_ICONS[position]).color(Color.WHITE).actionBar());
                    }
                    refresh();
                }
            }
        });

    }

    public void showNotification() {
        if (mFabNotify == null)
            return;
        int smsCount = NotificationMgr.getCurrentNotification().getSmsCount();
        int threadCount = NotificationMgr.getCurrentNotification().getThreadCount();
        if (smsCount > 0) {
            mFabNotify.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_email).color(Color.WHITE).sizeDp(FAB_ICON_SIZE_DP));
            mFabNotify.setVisibility(View.VISIBLE);
        } else if (threadCount > 0) {
            mFabNotify.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_notifications).color(Color.WHITE).sizeDp(FAB_ICON_SIZE_DP));
            mFabNotify.setVisibility(View.VISIBLE);
        } else {
            if (mFabNotify.getVisibility() == View.VISIBLE)
                mFabNotify.setVisibility(View.GONE);
        }

        if (getActivity() != null) {
            ((MainFrameActivity) getActivity()).updateDrawerBadge();
        }

    }

    public void scrollToTop() {
        stopScroll();
        mThreadListView.setSelection(0);
    }

    public void stopScroll() {
        mThreadListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    private class ThreadListMsgHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            String page = "(第" + mPage + "页)";
            Bundle b = msg.getData();
            switch (msg.what) {
                case STAGE_ERROR:
                    UIUtils.errorSnack(getView(),
                            b.getString(STAGE_ERROR_KEY),
                            b.getString(STAGE_DETAIL_KEY))
                            .show();
                    break;
                case STAGE_CLEAN:
                    mTipBar.setVisibility(View.INVISIBLE);
                    break;
                case STAGE_DONE:
//                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.green));
//                    mTipBar.setText(page + "加载完成");
//                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case STAGE_RELOGIN:
                    mTipBar.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.purple));
                    mTipBar.setText("正在登录");
                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case STAGE_GET_WEBPAGE:
//                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
//                    mTipBar.setText(page + "正在获取页面");
//                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case STAGE_PARSE:
//                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.orange));
//                    mTipBar.setText(page + "正在解析页面");
//                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case STAGE_REFRESH:
//                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.orange));
//                    mTipBar.setText("正在刷新");
//                    mTipBar.setVisibility(View.VISIBLE);
                    refresh();
                    break;
                case STAGE_NOT_LOGIN:
                    mTipBar.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.pink));
                    mTipBar.setText(b.getString(STAGE_ERROR_KEY));
                    mTipBar.setVisibility(View.VISIBLE);
                    mThreadBeans.clear();
                    mThreadListAdapter.setBeans(mThreadBeans);
                    showLoginDialog();
                    break;
            }
            return false;
        }
    }

    private void showLoginDialog() {
        LoginDialog dialog = LoginDialog.getInstance(getActivity());
        if (dialog != null) {
            dialog.setHandler(mMsgHandler);
            dialog.setTitle("用户登录");
            dialog.show();
        }
    }

    private class ForumTypesAdapter extends ArrayAdapter {

        public ForumTypesAdapter(Context context) {
            super(context, 0, HiUtils.BS_TYPES);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View row = inflater.inflate(R.layout.item_forum_type, parent, false);
            IconicsImageView icon = (IconicsImageView) row.findViewById(R.id.forum_type_icon);
            TextView text = (TextView) row.findViewById(R.id.forum_type_text);

            text.setText(HiUtils.BS_TYPES[position]);
            if (position == HiUtils.getBSTypeIndexByFid(HiSettingsHelper.getInstance().getBSTypeId())) {
                icon.setImageDrawable(new IconicsDrawable(getActivity(), HiUtils.BS_TYPE_ICONS[position]).color(ColorUtils.getColorAccent(getActivity())).sizeDp(20));
                text.setTextColor(ColorUtils.getColorAccent(getActivity()));
            } else {
                icon.setImageDrawable(new IconicsDrawable(getActivity(), HiUtils.BS_TYPE_ICONS[position]).color(ColorUtils.getDefaultTextColor(getActivity())).sizeDp(20));
            }

            return row;
        }
    }


    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PostEvent event) {

        if (!mSessionId.equals(event.mSessionId))
            return;

        String message = event.mMessage;
        PostBean postResult = event.mPostResult;

        if (event.mStatus == Constants.STATUS_IN_PROGRESS) {
            postProgressDialog = HiProgressDialog.show(mCtx, "正在发表...");
        } else if (event.mStatus == Constants.STATUS_SUCCESS) {
            //pop post fragment on success
            Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
            if (fg instanceof PostFragment) {
                ((BaseFragment) fg).popFragment();
            }

            if (postProgressDialog != null) {
                postProgressDialog.dismiss(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_SHORT).show();
            }

            setHasOptionsMenu(false);
            FragmentUtils.showThread(getFragmentManager(), true, postResult.getTid(), postResult.getSubject(), -1, -1, null, -1);

            //refresh thread list
            refresh();
        } else {
            if (postProgressDialog != null) {
                postProgressDialog.dismissError(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_LONG).show();
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

}