package net.jejer.hipda.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;

import java.util.ArrayList;
import java.util.List;

public class SimpleListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final String LOG_TAG = getClass().getSimpleName();
    public static final String ARG_TYPE = "type";

    private int mType;

    private ListView mThreadListView;
    private TextView mTipBar;
    private SimpleListAdapter mSimpleListAdapter;
    private LoaderManager.LoaderCallbacks<SimpleListBean> mCallbacks;
    private String mQuery = "";
    private SearchView searchView = null;
    private SwipeRefreshLayout swipeLayout;

    private int mPage = 1;
    private boolean mInloading = false;
    private int mMaxPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(LOG_TAG, "onCreate");
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_TYPE)) {
            mType = getArguments().getInt(ARG_TYPE);
        }

        List<SimpleListItemBean> a = new ArrayList<SimpleListItemBean>();
        mSimpleListAdapter = new SimpleListAdapter(getActivity(), R.layout.item_simple_list, a, mType);
        mCallbacks = new SimpleThreadListLoaderCallbacks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_thread_list, container, false);
        mThreadListView = (ListView) view.findViewById(R.id.lv_threads);
        mTipBar = (TextView) view.findViewById(R.id.thread_list_tipbar);
        mTipBar.setVisibility(View.GONE);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.icon_blue);
        swipeLayout.setEnabled(false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v(LOG_TAG, "onActivityCreated");

        // destroyLoader called here to avoid onLoadFinished called when onResume
        getLoaderManager().destroyLoader(0);
        mThreadListView.setAdapter(mSimpleListAdapter);
        mThreadListView.setOnItemClickListener(new OnItemClickCallback());
        mThreadListView.setOnItemLongClickListener(new OnItemLongClickCallback());
        mThreadListView.setOnScrollListener(new OnScrollCallback());

        switch (mType) {
            case SimpleListLoader.TYPE_MYREPLY:
            case SimpleListLoader.TYPE_MYPOST:
            case SimpleListLoader.TYPE_SMS:
            case SimpleListLoader.TYPE_THREADNOTIFY:
            case SimpleListLoader.TYPE_FAVORITES:
                getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
                break;
            case SimpleListLoader.TYPE_SEARCH:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(LOG_TAG, "onCreateOptionsMenu");

        menu.clear();

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        switch (mType) {
            case SimpleListLoader.TYPE_MYREPLY:
                getActivity().getActionBar().setTitle(R.string.title_drawer_myreply);
                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_MYPOST:
                getActivity().getActionBar().setTitle(R.string.title_drawer_mypost);
                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_SMS:
                getActivity().getActionBar().setTitle(R.string.title_drawer_sms);
                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_THREADNOTIFY:
                getActivity().getActionBar().setTitle(R.string.title_drawer_notify);
                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_FAVORITES:
                getActivity().getActionBar().setTitle(R.string.title_drawer_favorites);
                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_SEARCH:
                getActivity().getActionBar().setTitle(R.string.title_drawer_search);
                inflater.inflate(R.menu.menu_search, menu);
                searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
                searchView.setIconified(false);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        mQuery = query;
                        mSimpleListAdapter.clear();
                        // Close SoftKeyboard
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
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
        Log.v(LOG_TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void refresh() {
        mMaxPage = 0;
        mPage = 1;
        mSimpleListAdapter.clear();
        getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    public class OnScrollCallback implements AbsListView.OnScrollListener {

        int mLastVisibleItem = 0;
        int mVisibleItemCount = 0;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            mLastVisibleItem = firstVisibleItem;
            mVisibleItemCount = visibleItemCount;

            if (totalItemCount > 2 && firstVisibleItem + visibleItemCount > totalItemCount - 2) {
                if (!mInloading) {
                    mInloading = true;
                    if (mPage < mMaxPage) {
                        mPage++;
                        getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
                    } else {
                        if (mMaxPage > 0)
                            Toast.makeText(getActivity(), "已经是最后一页，共 " + mMaxPage + " 页", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }


    }

    public class OnItemClickCallback implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> listView, View itemView, int position,
                                long row) {

            setHasOptionsMenu(false);
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);

            Bundle bun = new Bundle();
            Fragment fragment = null;
            if (mType == SimpleListLoader.TYPE_SMS) {
                bun.putString(SmsFragment.ARG_ID, item.getAuthor());
                bun.putString(SmsFragment.ARG_UID, item.getUid());
                fragment = new SmsFragment();
            } else {
                bun.putString(ThreadDetailFragment.ARG_TID_KEY, item.getTid());
                bun.putString(ThreadDetailFragment.ARG_TITLE_KEY, item.getTitle());
                if (!TextUtils.isEmpty(item.getPid())) {
                    bun.putString(ThreadDetailFragment.ARG_PID_KEY, item.getPid());
                }
                fragment = new ThreadDetailFragment();
            }
            fragment.setArguments(bun);
            if (HiSettingsHelper.getInstance().getIsLandscape()) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.thread_detail_container_in_main, fragment, ThreadDetailFragment.class.getName())
                        .addToBackStack(ThreadDetailFragment.class.getName())
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                        .addToBackStack(ThreadDetailFragment.class.getName())
                        .commit();
            }
        }
    }

    public class OnItemLongClickCallback implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long row) {
            setHasOptionsMenu(false);
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);

            Bundle bun = new Bundle();
            Fragment fragment;
            if (mType == SimpleListLoader.TYPE_SMS) {
                return true;
            } else {
                bun.putString(ThreadDetailFragment.ARG_TID_KEY, item.getTid());
                bun.putString(ThreadDetailFragment.ARG_TITLE_KEY, item.getTitle());
                bun.putInt(ThreadDetailFragment.ARG_PAGE_KEY, ThreadDetailFragment.LAST_PAGE);
                bun.putInt(ThreadDetailFragment.ARG_FLOOR_KEY, ThreadDetailFragment.LAST_FLOOR);
                fragment = new ThreadDetailFragment();
            }
            fragment.setArguments(bun);
            if (HiSettingsHelper.getInstance().getIsLandscape()) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.thread_detail_container_in_main, fragment, ThreadDetailFragment.class.getName())
                        .addToBackStack(ThreadDetailFragment.class.getName())
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                        .addToBackStack(ThreadDetailFragment.class.getName())
                        .commit();
            }

            return true;
        }
    }

    public class SimpleThreadListLoaderCallbacks implements LoaderManager.LoaderCallbacks<SimpleListBean> {

        @Override
        public Loader<SimpleListBean> onCreateLoader(int arg0, Bundle arg1) {
            if (mMaxPage > 1)
                mTipBar.setText("正在加载" +
                        (mMaxPage > 1 ? ("第 " + mPage + " 页，共 " + mMaxPage + " 页") : ""));
            else
                mTipBar.setText("加载中...");

            mTipBar.setVisibility(View.VISIBLE);
            mTipBar.bringToFront();
            if (!swipeLayout.isRefreshing())
                swipeLayout.setEnabled(false);

            return new SimpleListLoader(getActivity(),
                    mType,
                    mPage,
                    mQuery);
        }

        @Override
        public void onLoadFinished(Loader<SimpleListBean> loader,
                                   SimpleListBean list) {
            mTipBar.setVisibility(View.INVISIBLE);
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);
            mInloading = false;

            if (list == null || list.getCount() == 0) {
                Toast.makeText(SimpleListFragment.this.getActivity(),
                        "没有数据", Toast.LENGTH_LONG).show();
                return;
            }

            mMaxPage = list.getMaxPage();
            mSimpleListAdapter.addAll(list.getAll());
        }

        @Override
        public void onLoaderReset(Loader<SimpleListBean> arg0) {
            Log.v(LOG_TAG, "onLoaderReset");

            mTipBar.setVisibility(View.INVISIBLE);
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);
            mInloading = false;
        }

    }
}
