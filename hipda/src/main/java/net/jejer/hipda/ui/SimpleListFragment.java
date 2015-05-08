package net.jejer.hipda.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class SimpleListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String ARG_TYPE = "type";

    private int mType;

    private ListView mThreadListView;
    private TextView mTipBar;
    private SimpleListAdapter mSimpleListAdapter;
    private List<SimpleListItemBean> mSimpleListItemBeans = new ArrayList<>();
    private LoaderManager.LoaderCallbacks<SimpleListBean> mCallbacks;
    private String mQuery = "";
    private SearchView searchView = null;
    private SwipeRefreshLayout swipeLayout;

    private int mPage = 1;
    private boolean mInloading = false;
    private int mMaxPage;

    private static String mPrefixSearchFullText;
    private static String mPrefixSearchHistory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.v("onCreate");
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_TYPE)) {
            mType = getArguments().getInt(ARG_TYPE);
        }

        mSimpleListAdapter = new SimpleListAdapter(getActivity(), mType);
        mCallbacks = new SimpleThreadListLoaderCallbacks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.v("onCreateView");
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
        Logger.v("onActivityCreated");

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
        Logger.v("onCreateOptionsMenu");

        menu.clear();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (mType) {
            case SimpleListLoader.TYPE_MYREPLY:
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_drawer_myreply);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_MYPOST:
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_drawer_mypost);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_SMS:
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_drawer_sms);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_THREADNOTIFY:
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_drawer_notify);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_FAVORITES:
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_drawer_favorites);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_SEARCH:
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_drawer_search);
                mPrefixSearchFullText = getActivity().getResources().getString(R.string.prefix_search_fulltext);
                mPrefixSearchHistory = getActivity().getResources().getString(R.string.prefix_search_history);

                inflater.inflate(R.menu.menu_search, menu);
                searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
                searchView.setIconified(false);
                searchView.setQueryHint("按标题搜索");
                if (!TextUtils.isEmpty(mQuery)) {
                    searchView.setQuery(mQuery, false);
                    searchView.clearFocus();
                }
                searchView.setSuggestionsAdapter(new SearchSuggestionsAdapter(getActivity()));

                AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
                search_text.setThreshold(1);

                searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                    @Override
                    public boolean onSuggestionClick(int position) {
                        String s = searchView.getSuggestionsAdapter().getCursor().getString(1);
                        if (s.startsWith(mPrefixSearchHistory)) {
                            s = s.substring(mPrefixSearchHistory.length()).trim();
                        }
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
                        mSimpleListAdapter.setBeans(mSimpleListItemBeans);
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
        Logger.v("onOptionsItemSelected");
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
        mSimpleListItemBeans.clear();
        mSimpleListAdapter.setBeans(mSimpleListItemBeans);
        getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    public class OnScrollCallback implements AbsListView.OnScrollListener {

        int mFirstVisibleItem = 0;
        int mVisibleItemCount = 0;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            mFirstVisibleItem = firstVisibleItem;
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

            getFragmentManager().beginTransaction()
                    .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                    .addToBackStack(ThreadDetailFragment.class.getName())
                    .commit();
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
                if (!TextUtils.isEmpty(item.getPid())) {
                    //full text search
                    bun.putString(ThreadDetailFragment.ARG_PID_KEY, item.getPid());
                } else {
                    bun.putInt(ThreadDetailFragment.ARG_PAGE_KEY, ThreadDetailFragment.LAST_PAGE);
                    bun.putInt(ThreadDetailFragment.ARG_FLOOR_KEY, ThreadDetailFragment.LAST_FLOOR);
                }
                fragment = new ThreadDetailFragment();
            }
            fragment.setArguments(bun);

            getFragmentManager().beginTransaction()
                    .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                    .addToBackStack(ThreadDetailFragment.class.getName())
                    .commit();

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
            mSimpleListItemBeans.addAll(list.getAll());
            mSimpleListAdapter.setBeans(mSimpleListItemBeans);
        }

        @Override
        public void onLoaderReset(Loader<SimpleListBean> arg0) {
            Logger.v("onLoaderReset");

            mTipBar.setVisibility(View.INVISIBLE);
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);
            mInloading = false;
        }

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
                mResults = new ArrayList<String>();
                String query = (constraint != null ? constraint.toString() : "").trim();
                query = query.startsWith(SimpleListFragment.mPrefixSearchFullText) ? query.substring(SimpleListFragment.mPrefixSearchFullText.length()).trim() : query;
                mResults.add(SimpleListFragment.mPrefixSearchFullText + query);
                //not finished yet, maybe don't need search history at all
//                for (int i = 0; i < count; i++) {
//                    mResults.add(mPrefixSearchHistory + (i + 1));
//                }
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

}
