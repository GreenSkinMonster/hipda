package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.bean.UserInfoBean;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.volley.HiStringRequest;
import net.jejer.hipda.volley.VolleyHelper;

import java.util.ArrayList;
import java.util.List;

public class UserinfoFragment extends BaseFragment {

    public static final String ARG_USERNAME = "USERNAME";
    public static final String ARG_UID = "UID";

    private String mUid;
    private String mUsername;

    private ImageView mAvatarView;
    private TextView mDetailView;
    private TextView mUsernameView;

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

        mSimpleListAdapter = new SimpleListAdapter(getActivity(), SimpleListLoader.TYPE_SEARCH_USER_THREADS);
        mCallbacks = new SearchThreadByUidLoaderCallbacks();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userinfo, container, false);
        view.setClickable(false);

        mAvatarView = (ImageView) view.findViewById(R.id.userinfo_avatar);

        mUsernameView = (TextView) view.findViewById(R.id.userinfo_username);
        mUsernameView.setText(mUsername);
        mUsernameView.setTextSize(HiSettingsHelper.getPostTextSize() + 2);

        mDetailView = (TextView) view.findViewById(R.id.userinfo_detail);
        mDetailView.setText("正在获取信息...");
        mDetailView.setTextSize(HiSettingsHelper.getPostTextSize());

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

        StringRequest sReq = new HiStringRequest(getActivity(), HiUtils.UserInfoUrl + mUid,
                new OnDetailLoadComplete(),
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logger.e(error);
                        mDetailView.setText("获取信息失败, 请重试." + VolleyHelper.getErrorReason(error));
                    }
                });
        VolleyHelper.getInstance().add(sReq);

        mThreadListView.setAdapter(mSimpleListAdapter);
        mThreadListView.setOnItemClickListener(new OnItemClickCallback());
        mThreadListView.setOnScrollListener(new OnScrollCallback());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.v("onCreateOptionsMenu");

        menu.clear();
        inflater.inflate(R.menu.menu_userinfo, menu);
        menu.findItem(R.id.action_send_sms).setIcon(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_message).actionBarSize().color(Color.WHITE));

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
                showSendSmsDialog();
                return true;
            case R.id.action_blacklist:
                blacklistUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void blacklistUser() {
        List<String> blackList = HiSettingsHelper.getInstance().getBlanklistUsernames();
        if (!blackList.contains(mUsername)) {
            blackList.add(mUsername);
            HiSettingsHelper.getInstance().setBlanklistUsernames(blackList);
            Toast.makeText(getActivity(), "已经将用户 " + mUsername + " 添加到黑名单", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "用户 " + mUsername + " 已经在黑名单中", Toast.LENGTH_SHORT).show();
        }
    }

    class OnDetailLoadComplete implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            UserInfoBean info = HiParser.parseUserInfo(response);
            if (info != null) {
                if (HiSettingsHelper.getInstance().isLoadAvatar()) {
                    mAvatarView.setVisibility(View.VISIBLE);
                    GlideHelper.loadAvatar(getActivity(), mAvatarView, info.getAvatarUrl());
                } else {
                    mAvatarView.setVisibility(View.GONE);
                }
                mDetailView.setText(info.getDetail());
                if (TextUtils.isEmpty(mUsername)) {
                    mUsername = info.getUsername();
                    mUsernameView.setText(mUsername);
                }
            } else {
                mDetailView.setText("解析信息失败, 请重试.");
            }
        }
    }

    private void showSendSmsDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_userinfo_sms, null);

        final EditText smsTextView = (EditText) viewlayout.findViewById(R.id.et_userinfo_sms);

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setTitle("发送短消息给 " + mUsername);
        popDialog.setView(viewlayout);
        // Add the buttons
        popDialog.setPositiveButton("发送",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new PostSmsAsyncTask(getActivity(), mUid, null).execute(smsTextView.getText().toString());
                    }
                });
        popDialog.setNegativeButton("取消", null);
        popDialog.create().show();
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

            Bundle bun = new Bundle();
            bun.putString(ThreadDetailFragment.ARG_TID_KEY, item.getTid());
            bun.putString(ThreadDetailFragment.ARG_TITLE_KEY, item.getTitle());
            Fragment fragment = new ThreadDetailFragment();
            fragment.setArguments(bun);
            getFragmentManager().beginTransaction()
                    .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                    .addToBackStack(ThreadDetailFragment.class.getName())
                    .commit();
        }
    }

}
