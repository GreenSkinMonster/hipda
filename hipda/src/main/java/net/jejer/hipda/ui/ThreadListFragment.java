package net.jejer.hipda.ui;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.LoginHelper;
import net.jejer.hipda.async.PostAsyncTask;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.async.ThreadListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import java.util.ArrayList;
import java.util.List;


public class ThreadListFragment extends BaseFragment
        implements PostAsyncTask.PostListener, SwipeRefreshLayout.OnRefreshListener {

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

    private Context mCtx;
    private int mForumId = 0;
    private int mPage = 1;
    private LoaderManager.LoaderCallbacks<ThreadListBean> mCallbacks;
    private ThreadListAdapter mThreadListAdapter;
    private List<ThreadBean> mThreadBeans = new ArrayList<>();
    private ListView mThreadListView;
    private TextView mTipBar;
    private boolean mInloading = false;
    private Handler mMsgHandler;
    private HiProgressDialog postProgressDialog;
    private SwipeRefreshLayout swipeLayout;
    private FloatingActionMenu mFam;
    private FloatingActionButton mFabNotify;
    private boolean mShowNotifyToast = true;
    private int mFirstVisibleItem = 0;

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
        mThreadListAdapter = new ThreadListAdapter(mCtx);

        mMsgHandler = new Handler(new ThreadListMsgHandler());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.v("onCreateView");
        View view = inflater.inflate(R.layout.fragment_thread_list, container, false);
        mThreadListView = (ListView) view.findViewById(R.id.lv_threads);
        mTipBar = (TextView) view.findViewById(R.id.thread_list_tipbar);
        mTipBar.setVisibility(View.INVISIBLE);
        mTipBar.bringToFront();

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.icon_blue);

        mFam = (FloatingActionMenu) view.findViewById(R.id.fam_actions);
        mFam.setVisibility(View.INVISIBLE);

        FloatingActionButton fabRefresh = (FloatingActionButton) view.findViewById(R.id.action_fab_refresh);
        fabRefresh.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_refresh).color(Color.WHITE));
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(true);
                refresh();
            }
        });

        FloatingActionButton fabNewThread = (FloatingActionButton) view.findViewById(R.id.action_fab_new_thread);
        fabNewThread.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_create).color(Color.WHITE));
        fabNewThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(true);
                newThread();
            }
        });

        mFabNotify = (FloatingActionButton) view.findViewById(R.id.action_fab_notify);
        mFabNotify.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_mail).color(Color.WHITE));
        mFabNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NotifyHelper.getInstance().getCntSMS() > 0) {
                    Bundle smsBundle = new Bundle();
                    smsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_SMS);
                    SimpleListFragment smsFragment = new SimpleListFragment();
                    smsFragment.setArguments(smsBundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_frame_container, smsFragment, SimpleListFragment.class.getName())
                            .addToBackStack(SimpleListFragment.class.getName())
                            .commit();
                } else if (NotifyHelper.getInstance().getCntThread() > 0) {
                    Bundle notifyBundle = new Bundle();
                    notifyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_THREADNOTIFY);
                    SimpleListFragment notifyFragment = new SimpleListFragment();
                    notifyFragment.setArguments(notifyBundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_frame_container, notifyFragment, SimpleListFragment.class.getName())
                            .addToBackStack(SimpleListFragment.class.getName())
                            .commit();
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
        Logger.v("onActivityCreated");
        super.onActivityCreated(savedInstanceState);

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
            getLoaderManager().initLoader(0, null, mCallbacks);
            getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.v("onCreateOptionsMenu");

        menu.clear();
        inflater.inflate(R.menu.menu_thread_list, menu);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void newThread() {
        Bundle arguments = new Bundle();
        arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_NEW_THREAD);
        arguments.putString(PostFragment.ARG_FID_KEY, mForumId + "");

        PostFragment fragment = new PostFragment();
        fragment.setArguments(arguments);
        fragment.setPostListener(this);

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

    @Override
    public void onPause() {
        super.onPause();
        //Logger.v( "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        //Logger.v( "onResume");
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

    private void refresh() {
        mPage = 1;
        mThreadListView.setSelection(0);
        getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
    }

    @Override
    public void onPrePost() {
        postProgressDialog = HiProgressDialog.show(mCtx, "正在发表...");
    }

    @Override
    public void onPostDone(int mode, int status, String message, PostBean postBean) {
        if (status == Constants.STATUS_SUCCESS) {
            if (postProgressDialog != null) {
                postProgressDialog.dismiss(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_SHORT).show();
            }

            showThreadDetailFragment(postBean.getTid(), postBean.getSubject(), -1, -1);

            //refresh thread list
            refresh();

        } else {
            if (postProgressDialog != null) {
                postProgressDialog.dismissError(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRefresh() {
        refresh();
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
                    mInloading = true;
                    mPage++;
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
        public void onItemSingleClick(AdapterView<?> listView, View itemView, int position,
                                      long row) {
            ThreadBean thread = mThreadListAdapter.getItem(position);
            String tid = thread.getTid();
            String title = thread.getTitle();
            showThreadDetailFragment(tid, title, -1, -1);
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
            showThreadDetailFragment(tid, title, page, ThreadDetailFragment.LAST_FLOOR);
            return true;
        }
    }

    private void showThreadDetailFragment(String tid, String title, int page, int floor) {
        setHasOptionsMenu(false);

        Bundle arguments = new Bundle();
        arguments.putString(ThreadDetailFragment.ARG_TID_KEY, tid);
        arguments.putString(ThreadDetailFragment.ARG_TITLE_KEY, title);
        if (page != -1)
            arguments.putInt(ThreadDetailFragment.ARG_PAGE_KEY, page);
        if (floor != -1)
            arguments.putInt(ThreadDetailFragment.ARG_FLOOR_KEY, floor);
        ThreadDetailFragment fragment = new ThreadDetailFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                .addToBackStack(ThreadDetailFragment.class.getName())
                .commit();
    }

    private class ThreadListLoaderCallbacks implements LoaderManager.LoaderCallbacks<ThreadListBean> {

        @Override
        public Loader<ThreadListBean> onCreateLoader(int arg0, Bundle arg1) {
            if (!swipeLayout.isRefreshing())
                swipeLayout.setEnabled(false);
            return new ThreadListLoader(mCtx, mMsgHandler, mForumId, mPage);
        }

        @Override
        public void onLoadFinished(Loader<ThreadListBean> loader,
                                   ThreadListBean threads) {
            Logger.v("onLoadFinished enter");

            mInloading = false;
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);

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

            int count = 0;
            if (mPage == 1) {
                mThreadBeans.clear();
                for (ThreadBean newthread : threads.threads) {
                    mThreadBeans.add(newthread);
                    count++;
                }
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
                        count++;
                    }
                }
                mThreadListAdapter.setBeans(mThreadBeans);
            }
            Logger.v("New Threads Added: " + count + ", Total = " + mThreadListAdapter.getCount());

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
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);
        }

    }

    private void showThreadListSettingsDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_thread_list_settings, null);

        final Switch sShowPicOnMobileNetwork = (Switch) viewlayout.findViewById(R.id.sw_load_pic_on_mobile_network);
        final Switch sPrefetch = (Switch) viewlayout.findViewById(R.id.sw_prefetch);
        final Switch sShowStickThreads = (Switch) viewlayout.findViewById(R.id.sw_show_stick_threads);
        final Switch sSortByPostTime = (Switch) viewlayout.findViewById(R.id.sw_sort_by_post_time);
        final Switch sShowThreadListAvatar = (Switch) viewlayout.findViewById(R.id.sw_threadlist_avatar);
        final Switch sShowPostType = (Switch) viewlayout.findViewById(R.id.sw_show_post_type);

        sShowPicOnMobileNetwork.setChecked(HiSettingsHelper.getInstance().isLoadImgOnMobileNwk());
        sShowPicOnMobileNetwork.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                HiSettingsHelper.getInstance().setLoadImgOnMobileNwk(arg1);
            }
        });
        sShowThreadListAvatar.setChecked(HiSettingsHelper.getInstance().isShowThreadListAvatar());
        sShowThreadListAvatar.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                HiSettingsHelper.getInstance().setShowThreadListAvatar(arg1);
                mThreadListAdapter.notifyDataSetChanged();
            }
        });
        sPrefetch.setChecked(HiSettingsHelper.getInstance().isPreFetch());
        sPrefetch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                HiSettingsHelper.getInstance().setPreFetch(arg1);
            }
        });
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

    public void showNotification() {
        if (mFabNotify == null)
            return;
        int smsCount = NotifyHelper.getInstance().getCntSMS();
        int threadCount = NotifyHelper.getInstance().getCntThread();
        if (smsCount + threadCount > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("您有 ");
            if (smsCount > 0) {
                sb.append(smsCount).append(" 条新的短消息");
            }
            if (threadCount > 0) {
                if (sb.length() > 3)
                    sb.append(", ");
                sb.append(threadCount).append(" 条新的帖子通知");
            }
            if (mShowNotifyToast) {
                Toast.makeText(mCtx, sb.toString(), Toast.LENGTH_SHORT).show();
                mShowNotifyToast = false;
            }

            if (smsCount > 0)
                mFabNotify.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_mail).color(Color.WHITE));
            else
                mFabNotify.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_notifications).color(Color.WHITE));

            mFabNotify.setVisibility(View.VISIBLE);
        } else {
            mShowNotifyToast = true;
            if (mFabNotify.getVisibility() == View.VISIBLE)
                mFabNotify.setVisibility(View.GONE);
        }
    }

    private class ThreadListMsgHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            String page = "(第" + mPage + "页)";
            Bundle b = msg.getData();
            switch (msg.what) {
                case STAGE_ERROR:
                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.red));
                    mTipBar.setText(b.getString(STAGE_ERROR_KEY));
                    Logger.e(b.getString(STAGE_ERROR_KEY));
                    mTipBar.setVisibility(View.VISIBLE);
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
                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
                    mTipBar.setText("正在登录");
                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case STAGE_GET_WEBPAGE:
                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
                    mTipBar.setText(page + "正在获取页面");
                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case STAGE_PARSE:
                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.orange));
                    mTipBar.setText(page + "正在解析页面");
                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case STAGE_REFRESH:
                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.orange));
                    mTipBar.setText("正在刷新");
                    mTipBar.setVisibility(View.VISIBLE);
                    refresh();
                    break;
                case STAGE_NOT_LOGIN:
                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.pink));
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
}