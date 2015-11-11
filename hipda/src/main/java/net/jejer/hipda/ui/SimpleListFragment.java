package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Loader;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.NotificationMgr;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleListFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener, PostSmsAsyncTask.SmsPostListener {
    public static final String ARG_TYPE = "type";

    private int mType;

    private ListView mThreadListView;
    private View mFooterView;
    private TextView mTipBar;
    private SimpleListAdapter mSimpleListAdapter;
    private List<SimpleListItemBean> mSimpleListItemBeans = new ArrayList<>();
    private LoaderManager.LoaderCallbacks<SimpleListBean> mCallbacks;
    private String mQuery = "";
    private SearchView searchView = null;
    private SwipeRefreshLayout swipeLayout;
    private ContentLoadingProgressBar loadingProgressBar;
    private HiProgressDialog smsPostProgressDialog;

    private int mPage = 1;
    private boolean mInloading = false;
    private int mMaxPage;

    private static String mPrefixSearchFullText;

    private MenuItem mFavoritesMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.v("onCreate");
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_TYPE)) {
            mType = getArguments().getInt(ARG_TYPE);
        }

        mSimpleListAdapter = new SimpleListAdapter(this, mType);
        mCallbacks = new SimpleThreadListLoaderCallbacks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.v("onCreateView");
        View view = inflater.inflate(R.layout.fragment_thread_list, container, false);
        mThreadListView = (ListView) view.findViewById(R.id.lv_threads);

        mFooterView = inflater.inflate(R.layout.vw_thread_list_footer, mThreadListView, false);
        mThreadListView.addFooterView(mFooterView);
        ProgressBar progressBar = (ProgressBar) mFooterView.findViewById(R.id.footer_progressbar);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(Color.LTGRAY, android.graphics.PorterDuff.Mode.SRC_IN);

        mTipBar = (TextView) view.findViewById(R.id.thread_list_tipbar);
        mTipBar.setVisibility(View.GONE);
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
        swipeLayout.setEnabled(false);

        loadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.list_loading);
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
            case SimpleListLoader.TYPE_THREAD_NOTIFY:
            case SimpleListLoader.TYPE_FAVORITES:
            case SimpleListLoader.TYPE_ATTENTION:
                if (mSimpleListAdapter.getCount() == 0) {
                    loadingProgressBar.show();
                    getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
                }
                break;
            case SimpleListLoader.TYPE_SEARCH:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.v("onCreateOptionsMenu");

        menu.clear();

        setActionBarDisplayHomeAsUpEnabled(true);
        switch (mType) {
            case SimpleListLoader.TYPE_MYREPLY:
                setActionBarTitle(R.string.title_drawer_myreply);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_MYPOST:
                setActionBarTitle(R.string.title_drawer_mypost);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_SMS:
                setActionBarTitle(R.string.title_drawer_sms);
                inflater.inflate(R.menu.menu_sms_list, menu);
                menu.findItem(R.id.action_send_sms).setIcon(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_comment_edit).actionBar().color(Color.WHITE));
                break;
            case SimpleListLoader.TYPE_THREAD_NOTIFY:
                setActionBarTitle(R.string.title_drawer_notify);
//                inflater.inflate(R.menu.menu_simple_thread_list, menu);
                break;
            case SimpleListLoader.TYPE_FAVORITES:
                setActionBarTitle(R.string.title_my_favorites);
                inflater.inflate(R.menu.menu_favorites, menu);
                mFavoritesMenuItem = menu.getItem(0);
                mFavoritesMenuItem.setTitle(R.string.action_attention);
                break;
            case SimpleListLoader.TYPE_ATTENTION:
                setActionBarTitle(R.string.title_my_attention);
                inflater.inflate(R.menu.menu_favorites, menu);
                mFavoritesMenuItem = menu.getItem(0);
                mFavoritesMenuItem.setTitle(R.string.action_favorites);
                break;
            case SimpleListLoader.TYPE_SEARCH:
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

                AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
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
            case R.id.action_favories:
                loadingProgressBar.showNow();
                if (mFavoritesMenuItem.getTitle().toString().equals(getString(R.string.action_attention))) {
                    mFavoritesMenuItem.setTitle(R.string.action_favorites);
                    mType = SimpleListLoader.TYPE_ATTENTION;
                    setActionBarTitle(R.string.title_my_attention);
                } else {
                    mFavoritesMenuItem.setTitle(R.string.action_attention);
                    mType = SimpleListLoader.TYPE_FAVORITES;
                    setActionBarTitle(R.string.title_my_favorites);
                }
                mSimpleListItemBeans.clear();
                mSimpleListAdapter.setBeans(mSimpleListItemBeans);
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
        getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    public void scrollToTop() {
        stopScroll();
        mThreadListView.setSelection(0);
    }

    public void stopScroll() {
        mThreadListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
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
                        mFooterView.getLayoutParams().height = Utils.dpToPx(getActivity(), 48);
                        mFooterView.setVisibility(View.VISIBLE);
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
        public void onItemClick(AdapterView<?> listView, View itemView, int position, long row) {
            setHasOptionsMenu(false);
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);

            Fragment listFragment = getFragmentManager().findFragmentByTag(ThreadListFragment.class.getName());
            if (listFragment != null)
                listFragment.setHasOptionsMenu(false);

            if (mType == SimpleListLoader.TYPE_SMS) {
                FragmentUtils.showSmsDetail(getFragmentManager(), false, item.getUid(), item.getAuthor());
            } else {
                FragmentUtils.showThread(getFragmentManager(), false, item.getTid(), item.getTitle(), -1, -1, item.getPid(), -1);
            }
        }
    }

    public class OnItemLongClickCallback implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long row) {
            setHasOptionsMenu(false);
            Fragment listFragment = getFragmentManager().findFragmentByTag(ThreadListFragment.class.getName());
            if (listFragment != null)
                listFragment.setHasOptionsMenu(false);

            SimpleListItemBean item = mSimpleListAdapter.getItem(position);
            if (mType == SimpleListLoader.TYPE_SMS) {
                return true;
            } else {
                String postId = "";
                int page = -1;
                int floor = -1;
                if (HiUtils.isValidId(item.getPid())) {
                    postId = item.getPid();
                } else {
                    page = ThreadDetailFragment.LAST_PAGE;
                    floor = ThreadDetailFragment.LAST_FLOOR;
                }
                FragmentUtils.showThread(getFragmentManager(), false, item.getTid(), item.getTitle(), page, floor, postId, -1);
            }
            return true;
        }
    }

    public class SimpleThreadListLoaderCallbacks implements LoaderManager.LoaderCallbacks<SimpleListBean> {

        @Override
        public Loader<SimpleListBean> onCreateLoader(int arg0, Bundle arg1) {
            if (!swipeLayout.isRefreshing() && !loadingProgressBar.isShown())
                loadingProgressBar.show();

            if (!swipeLayout.isRefreshing())
                swipeLayout.setEnabled(false);

            return new SimpleListLoader(getActivity(),
                    mType,
                    mPage,
                    mQuery);
        }

        @Override
        public void onLoadFinished(Loader<SimpleListBean> loader, SimpleListBean list) {
            mTipBar.setVisibility(View.INVISIBLE);
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(false);
            loadingProgressBar.hide();
            mFooterView.setVisibility(View.GONE);
            mFooterView.getLayoutParams().height = 1;
            mInloading = false;

            if (list == null || list.getCount() == 0) {
                if (mPage == 1) {
                    mSimpleListItemBeans.clear();
                    mSimpleListAdapter.setBeans(mSimpleListItemBeans);
                }
                Toast.makeText(SimpleListFragment.this.getActivity(),
                        "没有数据", Toast.LENGTH_LONG).show();
                return;
            }

            if (mType == SimpleListLoader.TYPE_FAVORITES
                    || mType == SimpleListLoader.TYPE_ATTENTION) {
                String item = mType == SimpleListLoader.TYPE_FAVORITES ? FavoriteHelper.TYPE_FAVORITE : FavoriteHelper.TYPE_ATTENTION;
                Set<String> tids = new HashSet<>();
                List<SimpleListItemBean> beans = list.getAll();
                for (SimpleListItemBean itemBean : beans) {
                    tids.add(itemBean.getTid());
                }
                FavoriteHelper.getInstance().addToCahce(item, tids);
            }

            if (mType == SimpleListLoader.TYPE_SMS)
                NotificationMgr.getCurrentNotification().clearSmsCount();
            if (mType == SimpleListLoader.TYPE_THREAD_NOTIFY)
                NotificationMgr.getCurrentNotification().setThreadCount(0);

            if (mPage == 1) {
                mSimpleListItemBeans.clear();
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
            loadingProgressBar.hide();
            mFooterView.setVisibility(View.GONE);
            mFooterView.getLayoutParams().height = 1;
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

}
