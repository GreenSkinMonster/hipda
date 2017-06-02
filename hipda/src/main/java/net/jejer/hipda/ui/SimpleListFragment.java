package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.NetworkReadyEvent;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.job.EventCallback;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.job.SimpleListEvent;
import net.jejer.hipda.job.SimpleListJob;
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
import java.util.LinkedHashMap;
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
    private SearchView searchView = null;
    private SwipeRefreshLayout swipeLayout;
    private ContentLoadingView mLoadingView;
    private HiProgressDialog smsPostProgressDialog;
    private SimpleListEventCallback mEventCallback = new SimpleListEventCallback();

    private int mPage = 1;
    private boolean mInloading = false;
    private int mMaxPage;
    private int mFirstVisibleItem = 0;
    private String mFormhash;

    private static String mPrefixSearchFullText;

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

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(ColorHelper.getSwipeColor(getActivity()));
        swipeLayout.setProgressBackgroundColorSchemeColor(ColorHelper.getSwipeBackgroundColor(getActivity()));
        swipeLayout.setEnabled(false);

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
            case SimpleListJob.TYPE_SEARCH:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        setActionBarDisplayHomeAsUpEnabled(true);
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
                menu.findItem(R.id.action_send_sms).setIcon(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_insert_comment).actionBar().color(Color.WHITE));
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
            case SimpleListJob.TYPE_SEARCH:
                setActionBarTitle(R.string.title_drawer_search);
                mPrefixSearchFullText = getActivity().getResources().getString(R.string.prefix_search_fulltext);

                inflater.inflate(R.menu.menu_search, menu);
                searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
                searchView.setIconified(false);
                searchView.setQueryHint("按标题搜索");
                if (!TextUtils.isEmpty(mQuery)) {
                    searchView.setQuery(mQuery, false);
                    searchView.clearFocus();
                }
                searchView.setSuggestionsAdapter(new SearchSuggestionsAdapter(getActivity()));

                AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(getActivity().getResources().getIdentifier("android:id/search_src_text", null, null));
                search_text.setThreshold(1);

                searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                    @Override
                    public boolean onSuggestionClick(int position) {
                        String s = searchView.getSuggestionsAdapter().getCursor().getString(1);
                        searchView.setQuery(s, true);
                        searchView.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onSuggestionSelect(int position) {
                        return false;
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        mQuery = query;
                        mSimpleListItemBeans.clear();
                        mSimpleListAdapter.setDatas(mSimpleListItemBeans);
                        UIUtils.hideSoftKeyboard(getActivity());
                        refresh();
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
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
//                            if (mMaxPage > 0)
//                                Toast.makeText(getActivity(), "已经是最后一页，共 " + mMaxPage + " 页", Toast.LENGTH_SHORT).show();
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
            setHasOptionsMenu(false);
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);

            Fragment listFragment = getFragmentManager().findFragmentByTag(ThreadListFragment.class.getName());
            if (listFragment != null)
                listFragment.setHasOptionsMenu(false);

            if (mType == SimpleListJob.TYPE_SMS) {
                FragmentUtils.showSmsDetail(getFragmentManager(), false, item.getUid(), item.getAuthor());
            } else {
                if (HiUtils.isValidId(item.getTid()) || HiUtils.isValidId(item.getPid())) {
                    FragmentUtils.showThread(getFragmentManager(), false, item.getTid(), item.getTitle(), -1, -1, item.getPid(), -1);
                } else if (HiUtils.isValidId(item.getUid())) {
                    FragmentUtils.showSpace(getFragmentManager(), false, item.getUid(), item.getAuthor());
                }
            }
        }

        @Override
        public void onLongItemClick(View view, int position) {
            Fragment listFragment = getFragmentManager().findFragmentByTag(ThreadListFragment.class.getName());
            if (listFragment != null)
                listFragment.setHasOptionsMenu(false);

            SimpleListItemBean item = mSimpleListAdapter.getItem(position);
            if (mType == SimpleListJob.TYPE_SMS) {
            } else if (mType == SimpleListJob.TYPE_FAVORITES) {
                showFavoriteActionDialog(position, item);
            } else if (mType == SimpleListJob.TYPE_ATTENTION) {
                showAttentionActionDialog(position, item);
            } else {
                if (HiUtils.isValidId(item.getTid()) || HiUtils.isValidId(item.getPid())) {
                    showLastPage(item);
                } else if (HiUtils.isValidId(item.getUid())) {
                    FragmentUtils.showSpace(getFragmentManager(), false, item.getUid(), item.getAuthor());
                }
            }
        }

        @Override
        public void onDoubleTap(View view, int position) {
        }
    }

    private void showFavoriteActionDialog(final int itemPosition, final SimpleListItemBean item) {
        LinkedHashMap<String, String> actions = new LinkedHashMap<>();
        actions.put("cancel", "取消收藏");
        actions.put("last_page", "转到最新回复");

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long row) {
                String action = (String) view.getTag();
                if ("cancel".equals(action)) {
                    mSimpleListAdapter.getDatas().remove(itemPosition);
                    mSimpleListAdapter.notifyItemRemoved(itemPosition);
                    if (mSimpleListAdapter.getItemCount() - itemPosition - 1 > 0)
                        mSimpleListAdapter.notifyItemRangeChanged(itemPosition, mSimpleListAdapter.getItemCount() - itemPosition - 1);
                    FavoriteHelper.getInstance().deleteFavorite(getActivity(), mFormhash, FavoriteHelper.TYPE_FAVORITE, item.getTid());
                } else {
                    showLastPage(item);
                }
            }
        };

        SimplePopupMenu popupMenu = new SimplePopupMenu(getActivity());
        popupMenu.setActions(actions);
        popupMenu.setListener(listener);
        popupMenu.show();
    }

    private void showAttentionActionDialog(final int itemPosition, final SimpleListItemBean item) {
        LinkedHashMap<String, String> actions = new LinkedHashMap<>();
        actions.put("cancel", "取消关注");
        actions.put("last_page", "转到最新回复");

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long row) {
                String action = (String) view.getTag();
                if ("cancel".equals(action)) {
                    mSimpleListAdapter.getDatas().remove(itemPosition);
                    mSimpleListAdapter.notifyItemRemoved(itemPosition);
                    if (mSimpleListAdapter.getItemCount() - itemPosition - 1 > 0)
                        mSimpleListAdapter.notifyItemRangeChanged(itemPosition, mSimpleListAdapter.getItemCount() - itemPosition - 1);
                    FavoriteHelper.getInstance().deleteFavorite(getActivity(), mFormhash, FavoriteHelper.TYPE_ATTENTION, item.getTid());
                } else {
                    showLastPage(item);
                }
            }
        };

        SimplePopupMenu popupMenu = new SimplePopupMenu(getActivity());
        popupMenu.setActions(actions);
        popupMenu.setListener(listener);
        popupMenu.show();
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
        setHasOptionsMenu(false);
        FragmentUtils.showThread(getFragmentManager(), false, item.getTid(), item.getTitle(), page, floor, postId, -1);
    }

    public static class SearchSuggestionsAdapter extends SimpleCursorAdapter {
        private static final String[] mFields = {"_id", "result"};
        private static final String[] mVisible = {"result"};
        private static final int[] mViewIds = {android.R.id.text1};

        public SearchSuggestionsAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, null, mVisible, mViewIds, 0);
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            return new SuggestionsCursor(constraint);
        }

        private class SuggestionsCursor extends AbstractCursor {
            private ArrayList<String> mResults;

            public SuggestionsCursor(CharSequence constraint) {
                mResults = new ArrayList<>();
                String query = (constraint != null ? constraint.toString() : "").trim();
                query = query.startsWith(SimpleListFragment.mPrefixSearchFullText) ? query.substring(SimpleListFragment.mPrefixSearchFullText.length()).trim() : query;
                mResults.add(SimpleListFragment.mPrefixSearchFullText + query);
            }

            @Override
            public int getCount() {
                return mResults.size();
            }

            @Override
            public String[] getColumnNames() {
                return mFields;
            }

            @Override
            public long getLong(int column) {
                if (column == 0) {
                    return mPos;
                }
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public String getString(int column) {
                if (column == 1) {
                    return mResults.get(mPos);
                }
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public short getShort(int column) {
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public int getInt(int column) {
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public float getFloat(int column) {
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public double getDouble(int column) {
                throw new UnsupportedOperationException("unimplemented");
            }

            @Override
            public boolean isNull(int column) {
                return false;
            }
        }
    }

    @Override
    public void onSmsPrePost() {
        smsPostProgressDialog = HiProgressDialog.show(getActivity(), "正在发送...");
    }

    @Override
    public void onSmsPostDone(int status, final String message, AlertDialog dialog) {
        if (status == Constants.STATUS_SUCCESS) {
            smsPostProgressDialog.dismiss(message);
            if (dialog != null)
                dialog.dismiss();
            onRefresh();
        } else {
            smsPostProgressDialog.dismissError(message);
        }
    }

    private class SimpleListEventCallback extends EventCallback<SimpleListEvent> {

        @Override
        public void onFail(SimpleListEvent event) {
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);
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
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);
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
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);
            if (mSimpleListItemBeans.size() == 0)
                mLoadingView.setState(ContentLoadingView.ERROR);
            else
                mLoadingView.setState(ContentLoadingView.CONTENT);
            mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);
            mInloading = false;

            showLoginDialog();
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkReadyEvent event) {
        if (!mInloading && mSimpleListItemBeans.size() == 0)
            refresh();
    }

}
