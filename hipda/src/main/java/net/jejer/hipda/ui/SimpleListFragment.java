package net.jejer.hipda.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.NetworkReadyEvent;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.job.EventCallback;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.job.SimpleListEvent;
import net.jejer.hipda.job.SimpleListJob;
import net.jejer.hipda.job.SmsRefreshEvent;
import net.jejer.hipda.ui.adapter.RecyclerItemClickListener;
import net.jejer.hipda.ui.adapter.SimpleListAdapter;
import net.jejer.hipda.ui.widget.ContentLoadingView;
import net.jejer.hipda.ui.widget.HiProgressDialog;
import net.jejer.hipda.ui.widget.SimpleDivider;
import net.jejer.hipda.ui.widget.SimplePopupMenu;
import net.jejer.hipda.ui.widget.XFooterView;
import net.jejer.hipda.ui.widget.XRecyclerView;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleListFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener, PostSmsAsyncTask.SmsPostListener {
    public static final String ARG_TYPE = "type";

    private int mType;

    private XRecyclerView mRecyclerView;
    private SimpleListAdapter mSimpleListAdapter;
    private List<SimpleListItemBean> mSimpleListItemBeans = new ArrayList<>();
    private String mQuery = "";
    private SwipeRefreshLayout mSwipeLayout;
    private ContentLoadingView mLoadingView;
    private HiProgressDialog mSmsPostProgressDialog;
    private SimpleListEventCallback mEventCallback = new SimpleListEventCallback();

    private int mPage = 1;
    private boolean mInloading = false;
    private int mMaxPage;
    private int mFirstVisibleItem = 0;
    private String mFormhash;

    private MenuItem mFavoritesMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_TYPE)) {
            mType = getArguments().getInt(ARG_TYPE);
        }

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getActivity(), new OnItemClickListener());
        mSimpleListAdapter = new SimpleListAdapter(this, mType, itemClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_list, container, false);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.rv_threads);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new SimpleDivider(getActivity()));

        mRecyclerView.addOnScrollListener(new OnScrollListener());

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(ColorHelper.getSwipeColor(getActivity()));
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ColorHelper.getSwipeBackgroundColor(getActivity()));
        mSwipeLayout.setEnabled(false);

        mLoadingView = (ContentLoadingView) view.findViewById(R.id.content_loading);
        mLoadingView.setErrorStateListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mInloading) {
                    mInloading = true;
                    mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                    refresh();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setAdapter(mSimpleListAdapter);

        switch (mType) {
            case SimpleListJob.TYPE_MYREPLY:
            case SimpleListJob.TYPE_MYPOST:
            case SimpleListJob.TYPE_SMS:
            case SimpleListJob.TYPE_THREAD_NOTIFY:
            case SimpleListJob.TYPE_FAVORITES:
            case SimpleListJob.TYPE_ATTENTION:
            case SimpleListJob.TYPE_HISTORIES:
                if (mSimpleListItemBeans.size() == 0) {
                    mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);
                    mLoadingView.setState(ContentLoadingView.LOADING);
                    SimpleListJob job = new SimpleListJob(getActivity(), mSessionId, mType, mPage, mQuery);
                    JobMgr.addJob(job);
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        switch (mType) {
            case SimpleListJob.TYPE_MYREPLY:
                setActionBarTitle(R.string.title_drawer_myreply);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListJob.TYPE_MYPOST:
                setActionBarTitle(R.string.title_drawer_mypost);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListJob.TYPE_SMS:
                setActionBarTitle(R.string.title_drawer_sms);
                inflater.inflate(R.menu.menu_sms_list, menu);
                menu.findItem(R.id.action_send_sms).setIcon(new IconicsDrawable(getActivity(),
                        GoogleMaterial.Icon.gmd_insert_comment).actionBar()
                        .color(HiSettingsHelper.getInstance().getToolbarTextColor()));
                break;
            case SimpleListJob.TYPE_THREAD_NOTIFY:
                setActionBarTitle(R.string.title_drawer_notify);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListJob.TYPE_FAVORITES:
                setActionBarTitle(R.string.title_my_favorites);
                inflater.inflate(R.menu.menu_favorites, menu);
                mFavoritesMenuItem = menu.getItem(0);
                mFavoritesMenuItem.setTitle(R.string.action_attention);
                break;
            case SimpleListJob.TYPE_HISTORIES:
                setActionBarTitle(R.string.title_drawer_histories);
                break;
            case SimpleListJob.TYPE_ATTENTION:
                setActionBarTitle(R.string.title_my_attention);
                inflater.inflate(R.menu.menu_favorites, menu);
                mFavoritesMenuItem = menu.getItem(0);
                mFavoritesMenuItem.setTitle(R.string.action_favorites);
                break;

            default:
                break;
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_favories:
                mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                if (mFavoritesMenuItem.getTitle().toString().equals(getString(R.string.action_attention))) {
                    mFavoritesMenuItem.setTitle(R.string.action_favorites);
                    mType = SimpleListJob.TYPE_ATTENTION;
                    setActionBarTitle(R.string.title_my_attention);
                } else {
                    mFavoritesMenuItem.setTitle(R.string.action_attention);
                    mType = SimpleListJob.TYPE_FAVORITES;
                    setActionBarTitle(R.string.title_my_favorites);
                }
                mSimpleListItemBeans.clear();
                mSimpleListAdapter.setDatas(mSimpleListItemBeans);
                mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);
                refresh();
                return true;
            case R.id.action_send_sms:
                showSendSmsDialog("", "", this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        mMaxPage = 0;
        mPage = 1;
        SimpleListJob job = new SimpleListJob(getActivity(), mSessionId, mType, mPage, mQuery);
        JobMgr.addJob(job);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    public void scrollToTop() {
        stopScroll();
        mRecyclerView.scrollToPosition(0);
    }

    public void stopScroll() {
        mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        int visibleItemCount, totalItemCount;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) {
                LinearLayoutManager mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + mFirstVisibleItem) >= totalItemCount - 5) {
                    if (!mInloading) {
                        mInloading = true;
                        if (mPage < mMaxPage) {
                            mPage++;
                            mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                            SimpleListJob job = new SimpleListJob(getActivity(), mSessionId, mType, mPage, mQuery);
                            JobMgr.addJob(job);
                        } else {
                            mRecyclerView.setFooterState(XFooterView.STATE_END);
                        }
                    }
                }
            }
        }
    }

    private class OnItemClickListener implements RecyclerItemClickListener.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
            if (position < 0 || position >= mSimpleListAdapter.getItemCount()) {
                return;
            }
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);
            if (item == null)
                return;
            if (mType == SimpleListJob.TYPE_SMS) {
                FragmentUtils.showSmsActivity(getActivity(), false, item.getUid(), item.getAuthor());
            } else {
                if (HiUtils.isValidId(item.getTid()) || HiUtils.isValidId(item.getPid())) {
                    FragmentUtils.showThreadActivity(getActivity(), false, item.getTid(), item.getTitle(), -1, -1, item.getPid(), -1);
                } else if (HiUtils.isValidId(item.getUid())) {
                    FragmentUtils.showUserInfoActivity(getActivity(), false, item.getUid(), item.getAuthor());
                }
            }
        }

        @Override
        public void onLongItemClick(View view, int position) {
            if (position < 0 || position >= mSimpleListAdapter.getItemCount()) {
                return;
            }
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);
            if (item == null)
                return;
            if (mType == SimpleListJob.TYPE_SMS) {
            } else if (mType == SimpleListJob.TYPE_FAVORITES) {
                showFavoriteActionDialog(item);
            } else if (mType == SimpleListJob.TYPE_ATTENTION) {
                showAttentionActionDialog(item);
            } else {
                if (HiUtils.isValidId(item.getTid()) || HiUtils.isValidId(item.getPid())) {
                    showLastPage(item);
                } else if (HiUtils.isValidId(item.getUid())) {
                    FragmentUtils.showUserInfoActivity(getActivity(), false, item.getUid(), item.getAuthor());
                }
            }
        }

        @Override
        public void onDoubleTap(View view, int position) {
        }
    }

    private void showFavoriteActionDialog(final SimpleListItemBean item) {
        final SimplePopupMenu popupMenu = new SimplePopupMenu(getActivity());
        popupMenu.add("cancel", "取消收藏", new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FavoriteHelper.getInstance().deleteFavorite(getActivity(), mFormhash, FavoriteHelper.TYPE_FAVORITE, item.getTid());
                removeItem(item);
            }
        });
        popupMenu.add("last_page", "转到最新回复", new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showLastPage(item);
            }
        });
        popupMenu.show();
    }

    private void showAttentionActionDialog(final SimpleListItemBean item) {
        SimplePopupMenu popupMenu = new SimplePopupMenu(getActivity());
        popupMenu.add("cancel", "取消关注", new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FavoriteHelper.getInstance().deleteFavorite(getActivity(), mFormhash, FavoriteHelper.TYPE_ATTENTION, item.getTid());
                removeItem(item);
            }
        });
        popupMenu.add("last_page", "转到最新回复",
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        showLastPage(item);
                    }
                });
        popupMenu.show();
    }

    private void removeItem(SimpleListItemBean item) {
        int pos = -1;
        for (int i = 0; i < mSimpleListAdapter.getDatas().size(); i++) {
            SimpleListItemBean bean = mSimpleListAdapter.getItem(mSimpleListAdapter.getHeaderCount() + i);
            if (item.getTid().equals(bean.getTid())) {
                pos = mSimpleListAdapter.getHeaderCount() + i;
                break;
            }
        }
        if (pos != -1) {
            mSimpleListAdapter.getDatas().remove(pos);
            mSimpleListAdapter.notifyItemRemoved(pos);
            if (mSimpleListAdapter.getItemCount() - pos - 1 > 0)
                mSimpleListAdapter.notifyItemRangeChanged(pos, mSimpleListAdapter.getItemCount() - pos - 1);
        } else {
            refresh();
        }
    }

    private void showLastPage(SimpleListItemBean item) {
        String postId = "";
        int page = -1;
        int floor = -1;
        if (HiUtils.isValidId(item.getPid())) {
            postId = item.getPid();
        } else {
            page = ThreadDetailFragment.LAST_PAGE;
            floor = ThreadDetailFragment.LAST_FLOOR;
        }
        FragmentUtils.showThreadActivity(getActivity(), false, item.getTid(), item.getTitle(), page, floor, postId, -1);
    }

    @Override
    public void onSmsPrePost() {
        mSmsPostProgressDialog = HiProgressDialog.show(getActivity(), "正在发送...");
    }

    @Override
    public void onSmsPostDone(int status, final String message, AlertDialog dialog) {
        if (status == Constants.STATUS_SUCCESS) {
            mSmsPostProgressDialog.dismiss(message);
            if (dialog != null)
                dialog.dismiss();
            onRefresh();
        } else {
            mSmsPostProgressDialog.dismissError(message);
        }
    }

    private class SimpleListEventCallback extends EventCallback<SimpleListEvent> {

        @Override
        public void onFail(SimpleListEvent event) {
            mSwipeLayout.setEnabled(true);
            mSwipeLayout.setRefreshing(false);
            if (mSimpleListItemBeans.size() == 0)
                mLoadingView.setState(ContentLoadingView.ERROR);
            else
                mLoadingView.setState(ContentLoadingView.CONTENT);

            mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);
            mInloading = false;

            UIUtils.errorSnack(getView(), event.mMessage, event.mDetail);
        }

        @Override
        public void onSuccess(SimpleListEvent event) {
            mSwipeLayout.setEnabled(true);
            mSwipeLayout.setRefreshing(false);
            mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);
            mInloading = false;
            mFormhash = event.mFormhash;

            SimpleListBean list = event.mData;
            if (list == null || list.getCount() == 0) {
                if (mPage == 1) {
                    mSimpleListItemBeans.clear();
                    mSimpleListAdapter.setDatas(mSimpleListItemBeans);
                }
                mLoadingView.setState(ContentLoadingView.NO_DATA);
                return;
            }

            mLoadingView.setState(ContentLoadingView.CONTENT);

            if (mType == SimpleListJob.TYPE_FAVORITES
                    || mType == SimpleListJob.TYPE_ATTENTION) {
                String item = mType == SimpleListJob.TYPE_FAVORITES ? FavoriteHelper.TYPE_FAVORITE : FavoriteHelper.TYPE_ATTENTION;
                Set<String> tids = new HashSet<>();
                List<SimpleListItemBean> beans = list.getAll();
                for (SimpleListItemBean itemBean : beans) {
                    tids.add(itemBean.getTid());
                }
                FavoriteHelper.getInstance().addToCahce(item, tids);
            }

            if (mType == SimpleListJob.TYPE_SMS)
                NotificationMgr.getCurrentNotification().clearSmsCount();
            if (mType == SimpleListJob.TYPE_THREAD_NOTIFY)
                NotificationMgr.getCurrentNotification().setThreadCount(0);

            if (mPage == 1) {
                mSimpleListItemBeans.clear();
            }
            mMaxPage = list.getMaxPage();
            mSimpleListItemBeans.addAll(list.getAll());
            mSimpleListAdapter.setDatas(mSimpleListItemBeans);
        }

        @Override
        public void onFailRelogin(SimpleListEvent event) {
            mSwipeLayout.setEnabled(true);
            mSwipeLayout.setRefreshing(false);
            mSimpleListItemBeans.clear();
            mSimpleListAdapter.notifyDataSetChanged();
            mLoadingView.setState(ContentLoadingView.NOT_LOGIN);
            mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);
            mInloading = false;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SimpleListEvent event) {
        if (!mSessionId.equals(event.mSessionId))
            return;
        EventBus.getDefault().removeStickyEvent(event);
        mEventCallback.process(event);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SmsRefreshEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        if (mType == SimpleListJob.TYPE_SMS)
            onRefresh();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkReadyEvent event) {
        if (!mInloading && mSimpleListItemBeans.size() == 0)
            refresh();
    }

}
