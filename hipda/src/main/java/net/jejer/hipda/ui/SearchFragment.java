package net.jejer.hipda.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.Forum;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SearchBean;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.job.EventCallback;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.job.SimpleListEvent;
import net.jejer.hipda.job.SimpleListJob;
import net.jejer.hipda.ui.adapter.BaseRvAdapter;
import net.jejer.hipda.ui.adapter.KeyValueArrayAdapter;
import net.jejer.hipda.ui.adapter.RecyclerItemClickListener;
import net.jejer.hipda.ui.adapter.SimpleListAdapter;
import net.jejer.hipda.ui.widget.ContentLoadingView;
import net.jejer.hipda.ui.widget.SimpleDivider;
import net.jejer.hipda.ui.widget.XFooterView;
import net.jejer.hipda.ui.widget.XRecyclerView;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by GreenSkinMonster on 2017-07-17.
 */

public class SearchFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private final int mType = SimpleListJob.TYPE_SEARCH;
    public static final String ARG_QUERY = "QUERY";
    public static final String ARG_FID = "FID";

    private static final String PREFERENCE_NAME = "saved_historty";
    private static final String PREFERENCE_KEY = "queries";
    private static final int MAX_HISTORY = 5;

    private XRecyclerView mRecyclerView;
    private SimpleListAdapter mSimpleListAdapter;
    private List<SimpleListItemBean> mSimpleListItemBeans = new ArrayList<>();
    private SwipeRefreshLayout mSwipeLayout;
    private ContentLoadingView mLoadingView;

    private SearchView mSearchView;
    private RelativeLayout mSearchFilterLayout;
    private MenuItem mSearchMenuItem;
    private EditText mSearchTextView;

    private EditText mEtAuthor;
    private CheckBox mCbFulltext;
    private Spinner mSpForum;
    private KeyValueArrayAdapter mSpAdapter;
    private SearchHistoryAdapter mHistoryAdapter;

    private SimpleListEventCallback mEventCallback = new SimpleListEventCallback();

    private boolean mSearchFilterAnimating;

    private SearchBean mSearchBean = new SearchBean();
    private SharedPreferences mPreferences;
    private List<SearchBean> mQueries;

    private int mPage = 1;
    private boolean mInloading = false;
    private int mMaxPage;
    private Drawable mIconDrawable;
    private Drawable mIbDrawable;

    private TextView.OnEditorActionListener mSearchEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            final boolean isEnterEvent = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            final boolean isEnterUpEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_UP;
            final boolean isEnterDownEvent = isEnterEvent && event.getAction() == KeyEvent.ACTION_DOWN;

            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || isEnterUpEvent) {
                if (!TextUtils.isEmpty(mSearchView.getQuery()) || !TextUtils.isEmpty(mEtAuthor.getText())) {
                    makeSearchBean();
                    startSearch();
                }
                return true;
            } else if (isEnterDownEvent) {
                // Capture this event to receive ACTION_UP
                return true;
            } else {
                return false;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mPreferences = getActivity().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getActivity(), new OnItemClickListener());
        mSimpleListAdapter = new SimpleListAdapter(this, mType, itemClickListener);

        mHistoryAdapter = new SearchHistoryAdapter(getActivity(),
                new RecyclerItemClickListener(getActivity(), new HistoryItemClickListener()));

        mIconDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_history).sizeDp(16).color(Color.GRAY);
        mIbDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_close).sizeDp(12).color(Color.GRAY);

        loadQueries();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.rv_threads);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new SimpleDivider(getActivity()));

        mRecyclerView.addOnScrollListener(new OnScrollListener());

        mRecyclerView.setAdapter(mSimpleListAdapter);

        mSearchFilterLayout = (RelativeLayout) view.findViewById(R.id.search_filter_layout);
        ViewCompat.setElevation(mSearchFilterLayout, Utils.dpToPx(4));
        mSearchFilterLayout.setAlpha(0);

        mSpForum = (Spinner) view.findViewById(R.id.sp_forum);
        mSpAdapter = new KeyValueArrayAdapter(getActivity(), R.layout.spinner_row);
        mSpAdapter.setEntryValues(getForumIds());
        mSpAdapter.setEntries(getForumNames());
        mSpForum.setAdapter(mSpAdapter);
        mSpForum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSearchBean.setForum(mSpAdapter.getEntryValue(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEtAuthor = (EditText) view.findViewById(R.id.et_author);
        mEtAuthor.setOnEditorActionListener(mSearchEditorActionListener);

        mCbFulltext = (CheckBox) view.findViewById(R.id.cb_fulltext);
        mCbFulltext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSearchView != null) {
                    if (isChecked) {
                        mSearchView.setQueryHint("搜索全文");
                    } else {
                        mSearchView.setQueryHint("搜索标题");
                    }
                }
            }
        });

        RecyclerView rvHistory = (RecyclerView) view.findViewById(R.id.rv_history);
        rvHistory.setHasFixedSize(true);
        rvHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvHistory.setAdapter(mHistoryAdapter);

        mHistoryAdapter.setDatas(mQueries);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(ColorHelper.getSwipeColor(getActivity()));
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ColorHelper.getSwipeBackgroundColor(getActivity()));
        mSwipeLayout.setEnabled(false);

        mLoadingView = (ContentLoadingView) view.findViewById(R.id.content_loading);
        mLoadingView.setState(ContentLoadingView.NO_DATA);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        setActionBarTitle(R.string.title_drawer_search);

        inflater.inflate(R.menu.menu_search, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        mSearchView.setIconified(false);
        mSearchView.setQueryHint("搜索标题");

        mSearchTextView = ((EditText) mSearchView.findViewById(androidx.appcompat.R.id.search_src_text));
        mSearchTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchTextView.setOnEditorActionListener(mSearchEditorActionListener);
        mSearchTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mSearchTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                if (hasFocus) {
                    if (mSearchFilterLayout.getVisibility() != View.VISIBLE) {
                        restoreSearchBean();
                        showSearchFilter();
                    }
                }
            }
        });
        mSearchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchFilter();
            }
        });

        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        UIUtils.trimChildMargins(mSearchView);
        ImageView closeButton = (ImageView) mSearchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setPadding(0, 0, Utils.dpToPx(4), 0);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mSearchTextView.getText()) &&
                        (mSearchFilterLayout.getVisibility() != View.VISIBLE
                                || TextUtils.isEmpty(mEtAuthor.getText()))) {
                    mSearchView.clearFocus();
                    mSearchView.setQuery("", false);
                    mSearchView.onActionViewCollapsed();
                    mSearchMenuItem.collapseActionView();
                    mEtAuthor.setText("");
                    mCbFulltext.setChecked(false);
                    hideSearchFilter();
                } else {
                    mSearchTextView.setText("");
                    mEtAuthor.setText("");
                    mCbFulltext.setChecked(false);
                }
            }
        });

        Bundle args = getArguments();
        String query = "";
        if (args != null) {
            int fid = args.containsKey(ARG_FID) ? getArguments().getInt(ARG_FID) : 0;
            query = args.containsKey(ARG_QUERY) ? args.getString(ARG_QUERY) : "";
            mSearchView.setQuery(query, false);
            mSearchBean.setForum(fid + "");
            restoreSpForum();
        }
        if (mSearchView.getQuery().length() > 0) {
            makeSearchBean();
            startSearch();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //hide then show mSearchFilterLayout, cannot get it's height on first show
                    //so I use a fixed value here, tell me if you know a better way
                    mSearchFilterLayout.animate()
                            .alpha(0)
                            .setDuration(100)
                            .translationYBy(-300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mSearchFilterLayout.setVisibility(View.GONE);
                                    mSearchFilterAnimating = false;
                                    showSearchFilter();
                                }
                            });
                }
            }, 100);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    protected void startSearch() {
        setActionBarTitle(mSearchBean.getDescription());
        UIUtils.hideSoftKeyboard(getActivity());

        mSimpleListItemBeans.clear();
        mSimpleListAdapter.setDatas(mSimpleListItemBeans);

        collapseSearchView();

        hideSearchFilter();
        refresh();
    }

    private void collapseSearchView() {
        mSearchView.setQuery("", false);
        mSearchView.clearFocus();
        mSearchView.setIconified(true);
        mSearchMenuItem.collapseActionView();
    }

    private void makeSearchBean() {
        mSearchBean.setQuery(mSearchView.getQuery().toString());
        mSearchBean.setForum(mSpAdapter.getEntryValue(mSpForum.getSelectedItemPosition()));
        mSearchBean.setAuthor(mEtAuthor.getText().toString());
        mSearchBean.setFulltext(mCbFulltext.isChecked());
        String title = mSearchBean.getDescription();
        if (TextUtils.isEmpty(title))
            title = getString(R.string.title_drawer_search);
        setActionBarTitle(title);
    }

    private void restoreSearchBean() {
        mEtAuthor.setText(mSearchBean.getAuthor());
        mCbFulltext.setChecked(mSearchBean.isFulltext());
        restoreSpForum();
        mSearchView.setQuery(mSearchBean.getQuery(), false);
    }

    private void restoreSpForum() {
        int position = 0;
        int i = 0;
        for (String fid : getForumIds()) {
            if (fid.equals(mSearchBean.getForum())) {
                position = i;
                break;
            }
            i++;
        }
        mSpForum.setSelection(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (mSearchView != null && mSimpleListAdapter.getDatas().size() > 0) {
            collapseSearchView();
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public boolean onBackPressed() {
        if (!mSearchView.isIconified()) {
            collapseSearchView();
            hideSearchFilter();
            return true;
        }
        return false;
    }

    private void refresh() {
        if (TextUtils.isEmpty(mSearchBean.getAuthor()) && TextUtils.isEmpty(mSearchBean.getQuery())) {
            if (mSwipeLayout.isRefreshing())
                mSwipeLayout.setRefreshing(false);
            return;
        }

        mSwipeLayout.setRefreshing(true);
        mMaxPage = 0;
        mPage = 1;
        SimpleListJob job = new SimpleListJob(getActivity(), mSessionId, mType, mPage, mSearchBean);
        JobMgr.addJob(job);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addQuery(mSearchBean);
                mHistoryAdapter.notifyDataSetChanged();
            }
        }, 500);
    }

    private void addQuery(SearchBean searchBean) {
        if (TextUtils.isEmpty(searchBean.getDescription()))
            return;
        SearchBean bean = searchBean.newCopy();
        if (mQueries.contains(bean))
            mQueries.remove(bean);
        mQueries.add(0, bean);
        while (mQueries.size() > MAX_HISTORY) {
            mQueries.remove(mQueries.size() - 1);
        }
        saveQueries();
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private void showSearchFilter() {
        if (mSearchFilterAnimating || mSearchFilterLayout.getVisibility() == View.VISIBLE)
            return;

        mSearchFilterLayout.setVisibility(View.VISIBLE);
        restoreSpForum();
        mSearchFilterLayout.animate()
                .setDuration(250)
                .alpha(1)
                .translationYBy(mSearchFilterLayout.getHeight())
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mSearchFilterAnimating = false;
                    }
                });
    }

    private void hideSearchFilter() {
        if (mSearchFilterAnimating || mSearchFilterLayout.getVisibility() != View.VISIBLE)
            return;

        mSearchFilterLayout.animate()
                .setDuration(250)
                .alpha(0)
                .translationYBy(-mSearchFilterLayout.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mSearchFilterLayout.setVisibility(View.GONE);
                        mSearchFilterAnimating = false;
                    }
                });
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
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItem) >= totalItemCount - 5) {
                    if (!mInloading) {
                        mInloading = true;
                        if (mPage < mMaxPage) {
                            mPage++;
                            mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                            SimpleListJob job = new SimpleListJob(getActivity(), mSessionId, mType, mPage, mSearchBean);
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
            collapseSearchView();
            hideSearchFilter();
            if (position < 0 || position >= mSimpleListAdapter.getItemCount()) {
                return;
            }
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);
            if (item == null)
                return;
            if (HiUtils.isValidId(item.getTid()) || HiUtils.isValidId(item.getPid())) {
                FragmentUtils.showThreadActivity(getActivity(), false, item.getTid(), item.getTitle(), -1, -1, item.getPid(), -1);
            }
        }

        @Override
        public void onLongItemClick(View view, int position) {
            collapseSearchView();
            hideSearchFilter();
            if (position < 0 || position >= mSimpleListAdapter.getItemCount()) {
                return;
            }
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);
            if (item == null)
                return;

            if (HiUtils.isValidId(item.getTid()) || HiUtils.isValidId(item.getPid())) {
                showLastPage(item);
            } else if (HiUtils.isValidId(item.getUid())) {
                FragmentUtils.showUserInfoActivity(getActivity(), false, item.getUid(), item.getAuthor());
            }
        }

        @Override
        public void onDoubleTap(View view, int position) {
        }
    }

    private class HistoryItemClickListener implements RecyclerItemClickListener.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
            hideSearchFilter();
            if (position < 0 || position >= mHistoryAdapter.getItemCount()) {
                return;
            }
            SearchBean item = mHistoryAdapter.getItem(position);
            mSearchBean = item.newCopy();
            startSearch();
        }

        @Override
        public void onLongItemClick(View view, int position) {
        }

        @Override
        public void onDoubleTap(View view, int position) {
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
            floor = ThreadDetailFragment.LAST_FLOOR_OF_PAGE;
        }
        FragmentUtils.showThreadActivity(getActivity(), false, item.getTid(), item.getTitle(), page, floor, postId, -1);
    }

    private void saveQueries() {
        Gson gson = new Gson();
        String v = gson.toJson(mQueries, new TypeToken<List<SearchBean>>() {
        }.getType());
        mPreferences.edit().putString(PREFERENCE_KEY, v).apply();
    }

    private void loadQueries() {
        String v = mPreferences.getString(PREFERENCE_KEY, "");
        try {
            Gson gson = new Gson();
            mQueries = gson.fromJson(v, new TypeToken<List<SearchBean>>() {
            }.getType());
        } catch (Exception e) {
            Logger.e(e);
        }
        if (mQueries == null)
            mQueries = new ArrayList<>();
    }

    private String[] getForumIds() {
        List<Integer> forums = HiSettingsHelper.getInstance().getForums();
        String[] forumIds = new String[forums.size() + 1];
        forumIds[0] = "0";
        int i = 1;
        for (Integer id : forums) {
            forumIds[i++] = String.valueOf(id);
        }
        return forumIds;
    }

    private String[] getForumNames() {
        List<Integer> forums = HiSettingsHelper.getInstance().getForums();
        String[] forumNames = new String[forums.size() + 1];
        forumNames[0] = "全部版块";
        int i = 1;
        for (Integer id : forums) {
            forumNames[i++] = HiUtils.getForumNameByFid(id);
        }
        return forumNames;
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

    private class SearchHistoryAdapter extends BaseRvAdapter<SearchBean> {

        private LayoutInflater mInflater;

        SearchHistoryAdapter(Context context, RecyclerItemClickListener itemClickListener) {
            mInflater = LayoutInflater.from(context);
            mItemClickListener = itemClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType) {
            return new ViewHolderImpl(mInflater.inflate(R.layout.item_search_history, parent, false));
        }

        @Override
        public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, final int position) {
            final ViewHolderImpl holder = (ViewHolderImpl) viewHolder;

            SearchBean item = getItem(position);
            holder.textview.setText(item.getDescription());
            Forum forum = HiUtils.getForumByFid(Utils.parseInt(item.getForum()));
            if (forum != null) {
                holder.imageview.setImageDrawable(new IconicsDrawable(getActivity(), forum.getIcon()).sizeDp(16).color(Color.GRAY));
            } else {
                holder.imageview.setImageDrawable(mIconDrawable);
            }
            holder.ib_remove.setImageDrawable(mIbDrawable);
            holder.ib_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHistoryAdapter.getDatas().remove(position);
                    mHistoryAdapter.notifyItemRemoved(position);
                    mHistoryAdapter.notifyItemRangeChanged(position, mHistoryAdapter.getItemCount());
                    saveQueries();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mHistoryAdapter.notifyDataSetChanged();
                        }
                    }, 350);
                }
            });
        }

        private class ViewHolderImpl extends RecyclerView.ViewHolder {
            TextView textview;
            ImageView imageview;
            ImageButton ib_remove;

            ViewHolderImpl(View itemView) {
                super(itemView);
                textview = (TextView) itemView.findViewById(R.id.textview);
                imageview = (ImageView) itemView.findViewById(R.id.icon);
                ib_remove = (ImageButton) itemView.findViewById(R.id.ib_remove);
            }
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

}
