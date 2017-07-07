package net.jejer.hipda.ui;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.bean.UserInfoBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.job.SimpleListEvent;
import net.jejer.hipda.job.SimpleListJob;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.adapter.RecyclerItemClickListener;
import net.jejer.hipda.ui.adapter.SimpleListAdapter;
import net.jejer.hipda.ui.widget.HiProgressDialog;
import net.jejer.hipda.ui.widget.OnSingleClickListener;
import net.jejer.hipda.ui.widget.SimpleDivider;
import net.jejer.hipda.ui.widget.XFooterView;
import net.jejer.hipda.ui.widget.XRecyclerView;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class UserinfoFragment extends BaseFragment implements PostSmsAsyncTask.SmsPostListener {

    public static final String ARG_USERNAME = "USERNAME";
    public static final String ARG_UID = "UID";

    private String mUid;
    private String mUsername;
    private String mAvatarUrl;

    private ImageView mAvatarView;
    private TextView mDetailView;
    private TextView mUsernameView;
    private TextView mOnlineView;

    private XRecyclerView mRecyclerView;
    private SimpleListAdapter mSimpleListAdapter;
    private List<SimpleListItemBean> mSimpleListItemBeans = new ArrayList<>();

    private Button mButton;

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

        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_USERNAME)) {
            mUsername = getArguments().getString(ARG_USERNAME);
        }

        if (getArguments().containsKey(ARG_UID)) {
            mUid = getArguments().getString(ARG_UID);
        }

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getActivity(), new OnItemClickListener());
        mSimpleListAdapter = new SimpleListAdapter(this, SimpleListJob.TYPE_SEARCH_USER_THREADS, itemClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        view.setClickable(false);

        mAvatarView = (ImageView) view.findViewById(R.id.userinfo_avatar);
        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            mAvatarView.setVisibility(View.VISIBLE);
            mAvatarView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if (!TextUtils.isEmpty(mAvatarUrl)) {
                        GlideHelper.clearAvatarCache(mAvatarUrl);
                        GlideHelper.loadAvatar(UserinfoFragment.this, mAvatarView, mAvatarUrl);
                        UIUtils.toast("头像已经刷新");
                    } else {
                        UIUtils.toast("用户未设置头像");
                    }
                }
            });
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

        mRecyclerView = (XRecyclerView) view.findViewById(R.id.rv_search_threads);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new SimpleDivider(getActivity()));

        mButton = (Button) view.findViewById(R.id.btn_search_threads);
        mButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                isShowThreads = !isShowThreads;
                if (isShowThreads) {
                    mButton.setText("显示信息");
                    mDetailView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    if (!isThreadsLoaded) {
                        mButton.setEnabled(false);
                        SimpleListJob job = new SimpleListJob(UserinfoFragment.this.getActivity(), mSessionId,
                                SimpleListJob.TYPE_SEARCH_USER_THREADS,
                                mPage,
                                TextUtils.isEmpty(mSearchIdUrl) ? mUid : mSearchIdUrl);
                        JobMgr.addJob(job);
                    }
                } else {
                    mButton.setText("搜索帖子");
                    mRecyclerView.setVisibility(View.GONE);
                    mDetailView.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mDetailView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
        OkHttpHelper.getInstance().asyncGet(HiUtils.UserInfoUrl + mUid, new UserInfoCallback());

        mRecyclerView.setAdapter(mSimpleListAdapter);
        mRecyclerView.addOnScrollListener(new OnScrollListener());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_userinfo, menu);
        menu.findItem(R.id.action_send_sms).setIcon(new IconicsDrawable(getActivity(),
                GoogleMaterial.Icon.gmd_insert_comment).actionBar()
                .color(HiSettingsHelper.getInstance().getToolbarTextColor()));

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
            UIUtils.toast("已经将用户 " + mUsername + " 添加到黑名单");
        } else {
            UIUtils.toast("用户 " + mUsername + " 已经在黑名单中");
        }
    }

    private class UserInfoCallback implements OkHttpHelper.ResultCallback {
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
                    mAvatarUrl = info.getAvatarUrl();
                } else {
                    mAvatarView.setVisibility(View.GONE);
                }
                mDetailView.setText(info.getDetail());
                mUsername = info.getUsername();
                mUsernameView.setText(mUsername);
                setActionBarTitle(mUsername);
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

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        int visibleItemCount, totalItemCount;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) {
                LinearLayoutManager mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItem) >= totalItemCount - 5) {
                    if (!mInloading) {
                        mInloading = true;
                        if (mPage < mMaxPage) {
                            mPage++;
                            mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                            SimpleListJob job = new SimpleListJob(UserinfoFragment.this.getActivity(), mSessionId,
                                    SimpleListJob.TYPE_SEARCH_USER_THREADS,
                                    mPage,
                                    TextUtils.isEmpty(mSearchIdUrl) ? mUid : mSearchIdUrl);
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
            FragmentUtils.showThreadActivity(getActivity(), false, item.getTid(), item.getTitle(), -1, -1, null, -1);
        }

        @Override
        public void onLongItemClick(View view, int position) {
            if (position < 0 || position >= mSimpleListAdapter.getItemCount()) {
                return;
            }
            SimpleListItemBean item = mSimpleListAdapter.getItem(position);
            FragmentUtils.showThreadActivity(getActivity(), false, item.getTid(), item.getTitle(), ThreadDetailFragment.LAST_PAGE, ThreadDetailFragment.LAST_FLOOR, null, -1);
        }

        @Override
        public void onDoubleTap(View view, int position) {
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

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SimpleListEvent event) {
        if (!mSessionId.equals(event.mSessionId))
            return;

        EventBus.getDefault().removeStickyEvent(event);

        if (event.mStatus == Constants.STATUS_IN_PROGRESS) {
            return;
        }

        SimpleListBean list = event.mData;
        mInloading = false;

        if (mButton != null)
            mButton.setEnabled(true);

        if (list == null || list.getCount() == 0) {
            UIUtils.toast("帖子加载失败");
            return;
        }

        mSearchIdUrl = list.getSearchIdUrl();
        mMaxPage = list.getMaxPage();
        mSimpleListItemBeans.addAll(list.getAll());
        mSimpleListAdapter.setDatas(mSimpleListItemBeans);
        isThreadsLoaded = true;
    }

}
