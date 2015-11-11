package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.okhttp.Request;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.bean.UserInfoBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class UserinfoFragment extends BaseFragment implements PostSmsAsyncTask.SmsPostListener {

    public static final String ARG_USERNAME = "USERNAME";
    public static final String ARG_UID = "UID";

    private String mUid;
    private String mUsername;

    private ImageView mAvatarView;
    private TextView mDetailView;
    private TextView mUsernameView;
    private TextView mOnlineView;

    private ListView mThreadListView;
    private SimpleListAdapter mSimpleListAdapter;
    private List<SimpleListItemBean> mSimpleListItemBeans = new ArrayList<>();
    private int mFirstVisibleItem = 0;

    private Button mButton;
    private LoaderManager.LoaderCallbacks<SimpleListBean> mCallbacks;

    private boolean isShowThreads;
    private boolean isThreadsLoaded;

    private int mPage = 1;
    private boolean mInloading = false;
    private String mSearchIdUrl;
    private int mMaxPage;

    private HiProgressDialog smsPostProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.v("onCreate");
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_USERNAME)) {
            mUsername = getArguments().getString(ARG_USERNAME);
        }

        if (getArguments().containsKey(ARG_UID)) {
            mUid = getArguments().getString(ARG_UID);
        }

        mSimpleListAdapter = new SimpleListAdapter(this, SimpleListLoader.TYPE_SEARCH_USER_THREADS);
        mCallbacks = new SearchThreadByUidLoaderCallbacks();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userinfo, container, false);
        view.setClickable(false);

        mAvatarView = (ImageView) view.findViewById(R.id.userinfo_avatar);
        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            mAvatarView.setVisibility(View.VISIBLE);
        } else {
            mAvatarView.setVisibility(View.GONE);
        }

        mUsernameView = (TextView) view.findViewById(R.id.userinfo_username);
        mUsernameView.setText(mUsername);
        mUsernameView.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() + 2);

        mOnlineView = (TextView) view.findViewById(R.id.user_online);
        mOnlineView.setVisibility(View.INVISIBLE);

        mDetailView = (TextView) view.findViewById(R.id.userinfo_detail);
        mDetailView.setText("正在获取信息...");
        mDetailView.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());

        //to avoid click through this view
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mThreadListView = (ListView) view.findViewById(R.id.lv_search_threads);
        mButton = (Button) view.findViewById(R.id.btn_search_threads);
        mButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                isShowThreads = !isShowThreads;
                if (isShowThreads) {
                    mButton.setText("显示信息");
                    mDetailView.setVisibility(View.GONE);
                    mThreadListView.setVisibility(View.VISIBLE);
                    if (!isThreadsLoaded) {
                        mButton.setEnabled(false);
                        getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
                    }
                } else {
                    mButton.setText("搜索帖子");
                    mThreadListView.setVisibility(View.GONE);
                    mDetailView.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mDetailView.setVisibility(View.VISIBLE);
            mThreadListView.setVisibility(View.GONE);
        }
        OkHttpHelper.getInstance().asyncGet(HiUtils.UserInfoUrl + mUid, new UserInfoCallback());

        mThreadListView.setAdapter(mSimpleListAdapter);
        mThreadListView.setOnItemClickListener(new OnItemClickCallback());
        mThreadListView.setOnScrollListener(new OnScrollCallback());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.v("onCreateOptionsMenu");

        menu.clear();
        inflater.inflate(R.menu.menu_userinfo, menu);
        menu.findItem(R.id.action_send_sms).setIcon(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_comment_edit).actionBar().color(Color.WHITE));

        setActionBarDisplayHomeAsUpEnabled(true);
        setActionBarTitle(mUsername);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_send_sms:
                showSendSmsDialog(mUid, mUsername, this);
                return true;
            case R.id.action_blacklist:
                blacklistUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void blacklistUser() {
        if (!HiSettingsHelper.getInstance().isUserBlack(mUsername)) {
            HiSettingsHelper.getInstance().addToBlacklist(mUsername);
            Toast.makeText(getActivity(), "已经将用户 " + mUsername + " 添加到黑名单", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "用户 " + mUsername + " 已经在黑名单中", Toast.LENGTH_SHORT).show();
        }
    }

    class UserInfoCallback implements OkHttpHelper.ResultCallback {
        @Override
        public void onError(Request request, Exception e) {
            Logger.e(e);
            mDetailView.setText("获取信息失败 : " + OkHttpHelper.getErrorMessage(e));
        }

        @Override
        public void onResponse(String response) {
            UserInfoBean info = HiParser.parseUserInfo(response);
            if (info != null) {
                if (HiSettingsHelper.getInstance().isLoadAvatar()) {
                    mAvatarView.setVisibility(View.VISIBLE);
                    GlideHelper.loadAvatar(UserinfoFragment.this, mAvatarView, info.getAvatarUrl());
                } else {
                    mAvatarView.setVisibility(View.GONE);
                }
                mDetailView.setText(info.getDetail());
                if (TextUtils.isEmpty(mUsername)) {
                    mUsername = info.getUsername();
                    mUsernameView.setText(mUsername);
                    setActionBarTitle(mUsername);
                }
                if (info.isOnline()) {
                    mOnlineView.setVisibility(View.VISIBLE);
                } else {
                    mOnlineView.setVisibility(View.INVISIBLE);
                }
            } else {
                mDetailView.setText("解析信息失败, 请重试.");
            }
        }
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
                    if (mPage < mMaxPage) {
                        mPage++;
                        getLoaderManager().restartLoader(0, null, mCallbacks).forceLoad();
                    } else {
                        Toast.makeText(getActivity(), "已经是最后一页，共 " + mMaxPage + " 页", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }


    }

    public class SearchThreadByUidLoaderCallbacks implements LoaderManager.LoaderCallbacks<SimpleListBean> {

        @Override
        public Loader<SimpleListBean> onCreateLoader(int arg0, Bundle arg1) {
            Toast.makeText(getActivity(),
                    "正在加载第 " + mPage + " 页"
                            + (!TextUtils.isEmpty(mSearchIdUrl) ? "，共 " + mMaxPage + " 页" : ""),
                    Toast.LENGTH_SHORT).show();
            return new SimpleListLoader(UserinfoFragment.this.getActivity(),
                    SimpleListLoader.TYPE_SEARCH_USER_THREADS,
                    mPage,
                    TextUtils.isEmpty(mSearchIdUrl) ? mUid : mSearchIdUrl);
        }

        @Override
        public void onLoadFinished(Loader<SimpleListBean> loader,
                                   SimpleListBean list) {
            Logger.v("onLoadFinished enter");

            mInloading = false;

            if (mButton != null)
                mButton.setEnabled(true);

            if (list == null || list.getCount() == 0) {
                Logger.v("onLoadFinished list == null || list.getCount == 0");
                Toast.makeText(getActivity(), "帖子加载失败", Toast.LENGTH_SHORT).show();
                return;
            }

            mSearchIdUrl = list.getSearchIdUrl();
            mMaxPage = list.getMaxPage();
            mSimpleListItemBeans.addAll(list.getAll());
            mSimpleListAdapter.setBeans(mSimpleListItemBeans);
            isThreadsLoaded = true;
        }

        @Override
        public void onLoaderReset(Loader<SimpleListBean> arg0) {
            mInloading = false;
            Logger.v("onLoaderReset");
        }

    }

    public class OnItemClickCallback implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> listView, View itemView, int position,
                                long row) {

            setHasOptionsMenu(false);
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);
            FragmentUtils.showThread(getFragmentManager(), false, item.getTid(), item.getTitle(), -1, -1, null, -1);
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
        } else {
            smsPostProgressDialog.dismissError(message);
        }
    }

}
