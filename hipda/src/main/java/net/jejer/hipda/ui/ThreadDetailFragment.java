package net.jejer.hipda.ui;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiEditText;

import net.jejer.hipda.BuildConfig;
import net.jejer.hipda.R;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.NetworkReadyEvent;
import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.cache.ThreadDetailCache;
import net.jejer.hipda.db.HistoryDao;
import net.jejer.hipda.glide.GlideImageView;
import net.jejer.hipda.job.EventCallback;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.job.PostEvent;
import net.jejer.hipda.job.PostJob;
import net.jejer.hipda.job.ThreadDetailEvent;
import net.jejer.hipda.job.ThreadDetailJob;
import net.jejer.hipda.ui.adapter.RecyclerItemClickListener;
import net.jejer.hipda.ui.adapter.ThreadDetailAdapter;
import net.jejer.hipda.ui.widget.BottomDialog;
import net.jejer.hipda.ui.widget.ContentLoadingView;
import net.jejer.hipda.ui.widget.SimpleDivider;
import net.jejer.hipda.ui.widget.XFooterView;
import net.jejer.hipda.ui.widget.XHeaderView;
import net.jejer.hipda.ui.widget.XRecyclerView;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.ImageSizeUtils;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ThreadDetailFragment extends BaseFragment {
    public static final String ARG_TID_KEY = "tid";
    public static final String ARG_PID_KEY = "pid";
    public static final String ARG_AUTHOR_ID_KEY = "author_id";
    public static final String ARG_TITLE_KEY = "title";
    public static final String ARG_FLOOR_KEY = "floor";
    public static final String ARG_PAGE_KEY = "page";
    public static final String ARG_MAX_PAGE_KEY = "maxPage";

    public static final int LAST_FLOOR = Integer.MIN_VALUE;
    public static final int FIRST_FLOOR = Integer.MIN_VALUE + 1;
    public static final int LAST_PAGE = Integer.MIN_VALUE;

    public final static int FETCH_NORMAL = 0;
    public final static int FETCH_NEXT = 1;
    public final static int FETCH_PREVIOUS = 2;
    public final static int FETCH_REFRESH = 3;
    public final static int FETCH_SILENT = 4;

    public static final int POSITION_NORMAL = 0;
    public static final int POSITION_HEADER = 1;
    public static final int POSITION_FOOTER = 2;

    private Context mCtx;
    private String mTid;
    private String mGotoPostId;
    private String mAuthorId;
    private String mTitle;
    private String mFid;
    private XRecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ThreadDetailAdapter mDetailAdapter;
    private ThreadDetailCache mCache = new ThreadDetailCache();
    private List<DetailBean> mDetailBeans = new ArrayList<>();

    public static int mMaxImageDecodeWidth = ImageSizeUtils.NORMAL_IMAGE_DECODE_WIDTH;

    private int mCurrentPage = 1;
    private int mMaxPage = 0;
    private int mGoToPage = 1;
    private int mMaxPostInPage = HiSettingsHelper.getInstance().getMaxPostsInPage();    // user can configure max posts per page in forum setting
    private int mGotoFloor = -1;    // actual floor number in thread, start from 1

    private View mQuickReply;
    private EmojiEditText mEtReply;
    private ImageButton mIbEmojiSwitch;
    private TextView mTvCountdown;
    private ImageButton mIbPostReply;
    private CountDownTimer mCountDownTimer;

    private String mBlinkPostId;
    private Animation mBlinkAnim;

    private boolean mDataReceived = false;
    private boolean mInloading = false;
    private boolean mHeaderLoading = false;
    private boolean mFooterLoading = false;

    private HiProgressDialog postProgressDialog;
    private ContentLoadingView mLoadingView;
    private ThreadDetailEventCallback mEventCallback = new ThreadDetailEventCallback();
    private MenuItem mShowAllMenuItem;

    private boolean mHistorySaved = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCtx = getActivity();

        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_TID_KEY)) {
            mTid = getArguments().getString(ARG_TID_KEY);
        }
        if (getArguments().containsKey(ARG_PID_KEY)) {
            mGotoPostId = getArguments().getString(ARG_PID_KEY);
        }
        if (getArguments().containsKey(ARG_AUTHOR_ID_KEY)) {
            mAuthorId = getArguments().getString(ARG_AUTHOR_ID_KEY);
        }
        if (getArguments().containsKey(ARG_TITLE_KEY)) {
            mTitle = getArguments().getString(ARG_TITLE_KEY);
        }
        if (getArguments().containsKey(ARG_PAGE_KEY)) {
            mCurrentPage = getArguments().getInt(ARG_PAGE_KEY);
            if (mCurrentPage <= 0 && mCurrentPage != LAST_PAGE)
                mCurrentPage = 1;
        }
        if (getArguments().containsKey(ARG_MAX_PAGE_KEY)) {
            mMaxPage = getArguments().getInt(ARG_MAX_PAGE_KEY);
        }
        if (getArguments().containsKey(ARG_FLOOR_KEY)) {
            mGotoFloor = getArguments().getInt(ARG_FLOOR_KEY);
        }

        mBlinkAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thread_detail, parent, false);

        mRecyclerView = (XRecyclerView) view.findViewById(R.id.rv_thread_details);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mLayoutManager = new LinearLayoutManager(mCtx);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new SimpleDivider(
                HiSettingsHelper.getInstance().isNightMode()
                        ? ContextCompat.getDrawable(mCtx, R.drawable.line_divider_night)
                        : ContextCompat.getDrawable(mCtx, R.drawable.line_divider_day)));

        mRecyclerView.addOnScrollListener(new OnScrollListener());

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(mCtx, new OnItemClickListener());
        mDetailAdapter = new ThreadDetailAdapter(mCtx, this, itemClickListener,
                new GoToFloorOnClickListener(), new AvatarOnClickListener());
        mDetailAdapter.setDatas(mDetailBeans);

        mRecyclerView.setAdapter(mDetailAdapter);

        mRecyclerView.setXRecyclerListener(new XRecyclerView.XRecyclerListener() {
            @Override
            public void onHeaderReady() {
                mCurrentPage--;
                mGotoFloor = LAST_FLOOR;
                showOrLoadPage();
            }

            @Override
            public void onFooterReady() {
                mCurrentPage++;
                mGotoFloor = FIRST_FLOOR;
                showOrLoadPage();
            }

            @Override
            public void atEnd() {
                mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                refreshAtEnd();
            }

            @Override
            public void onFooterError() {
                if (mCurrentPage == mMaxPage) {
                    atEnd();
                } else {
                    prefetchNextPage();
                }
            }

            @Override
            public void onHeaderError() {
                prefetchPreviousPage();
            }
        });

        mLoadingView = (ContentLoadingView) view.findViewById(R.id.content_loading);
        mLoadingView.setErrorStateListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mInloading) {
                    mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                    refresh();
                }
            }
        });

        mQuickReply = view.findViewById(R.id.quick_reply);
        mEtReply = (EmojiEditText) mQuickReply.findViewById(R.id.tv_reply_text);
        mEtReply.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());

        mTvCountdown = (TextView) mQuickReply.findViewById(R.id.tv_countdown);

        mIbPostReply = (ImageButton) mQuickReply.findViewById(R.id.ib_reply_post);
        mIbPostReply.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_send).sizeDp(28).color(Color.GRAY));
        mIbPostReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyText = mEtReply.getText().toString();
                if (Utils.getWordCount(replyText) < 5) {
                    Toast.makeText(getActivity(), "字数必须大于5", Toast.LENGTH_LONG).show();
                } else {
                    PostBean postBean = new PostBean();
                    postBean.setContent(replyText);
                    postBean.setTid(mTid);

                    JobMgr.addJob(new PostJob(mSessionId, PostHelper.MODE_QUICK_REPLY, null, postBean));

                    UIUtils.hideSoftKeyboard(getActivity());
                }
            }
        });
        mIbPostReply.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setHasOptionsMenu(false);
                String replyText = mEtReply.getText().toString();
                showPost(replyText);
                hideQuickReply();
                return true;
            }
        });

        mIbEmojiSwitch = (ImageButton) mQuickReply.findViewById(R.id.ib_goto_post);
        setUpEmojiPopup(mEtReply, mIbEmojiSwitch);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCtx = getActivity();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (!mInloading) {
            if (mDetailBeans.size() == 0) {
                refresh();
            } else {
                mLoadingView.setState(ContentLoadingView.CONTENT);
            }
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_thread_detail, menu);

        setActionBarTitle((mCurrentPage > 0 && mMaxPage > 0 ? "(" + mCurrentPage + "/" + mMaxPage + ") " : "")
                + mTitle);
        setActionBarDisplayHomeAsUpEnabled(true);

        mShowAllMenuItem = menu.findItem(R.id.action_show_all);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem favoritesMenuItem = menu.findItem(R.id.action_add_favorite);
        if (favoritesMenuItem != null) {
            if (FavoriteHelper.getInstance().isInFavortie(mTid)) {
                favoritesMenuItem.setTitle(R.string.action_remove_favorite);
            } else {
                favoritesMenuItem.setTitle(R.string.action_add_favorite);
            }
        }

        MenuItem attentionMenuItem = menu.findItem(R.id.action_add_attention);
        if (attentionMenuItem != null) {
            if (FavoriteHelper.getInstance().isInAttention(mTid)) {
                attentionMenuItem.setTitle(R.string.action_remove_attention);
            } else {
                attentionMenuItem.setTitle(R.string.action_add_attention);
            }
        }

        MenuItem authorMenuItem = menu.findItem(R.id.action_only_author);
        if (authorMenuItem != null) {
            if (isInAuthorOnlyMode()) {
                authorMenuItem.setTitle(R.string.action_show_all);
            } else {
                authorMenuItem.setTitle(R.string.action_only_author);
            }
        }

        if (mShowAllMenuItem != null) {
            if (TextUtils.isEmpty(mAuthorId)) {
                mShowAllMenuItem.setVisible(false);
            } else {
                mShowAllMenuItem.setVisible(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_only_author:
                if (isInAuthorOnlyMode()) {
                    cancelAuthorOnlyMode();
                } else {
                    if (mCache.get(1) != null) {
                        DetailBean detailBean = mCache.get(1).getAll().get(0);
                        enterAuthorOnlyMode(detailBean.getUid());
                    } else {
                        enterAuthorOnlyMode(ThreadDetailJob.FIND_AUTHOR_ID);
                    }
                }
                return true;
            case R.id.action_open_url:
                String url = HiUtils.DetailListUrl + mTid;
                if (mCurrentPage > 1)
                    url += "&page=" + mCurrentPage;
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "text/html");
                    List<ResolveInfo> list = mCtx.getPackageManager().queryIntentActivities(intent, 0);

                    if (list.size() == 0) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        list = mCtx.getPackageManager().queryIntentActivities(intent, 0);

                        ArrayList<Intent> targetIntents = new ArrayList<>();
                        String myPkgName = BuildConfig.APPLICATION_ID;
                        for (ResolveInfo currentInfo : list) {
                            String packageName = currentInfo.activityInfo.packageName;
                            if (!myPkgName.equals(packageName)) {
                                Intent targetIntent = new Intent(android.content.Intent.ACTION_VIEW);
                                targetIntent.setData(Uri.parse(url));
                                targetIntent.setPackage(packageName);
                                targetIntents.add(targetIntent);
                            }
                        }

                        if (targetIntents.size() > 0) {
                            Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), getString(R.string.action_open_url));
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[targetIntents.size()]));
                            startActivity(chooserIntent);
                        } else {
                            Toast.makeText(mCtx, "没有找到浏览器应用", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Toast.makeText(mCtx, "没有找到浏览器应用 : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_copy_url:
                ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("THREAD URL FROM HiPDA", HiUtils.DetailListUrl + mTid);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mCtx, "帖子地址已经复制到粘贴板", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share_thread:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = HiUtils.DetailListUrl + mTid + "\n"
                        + "主题：" + mTitle + "\n";
                if (mCache.get(1) != null && mCache.get(1).getAll().size() > 0)
                    shareBody += ("作者：" + mCache.get(1).getAll().get(0).getAuthor());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "分享帖子"));
                return true;
            case R.id.action_reply:
                setHasOptionsMenu(false);
                showPost("");
                return true;
            case R.id.action_refresh_detail:
                mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                refresh();
                return true;
            case R.id.action_add_favorite:
                if (FavoriteHelper.getInstance().isInFavortie(mTid))
                    FavoriteHelper.getInstance().removeFavorite(mCtx, FavoriteHelper.TYPE_FAVORITE, mTid);
                else
                    FavoriteHelper.getInstance().addFavorite(mCtx, FavoriteHelper.TYPE_FAVORITE, mTid);
                return true;
            case R.id.action_add_attention:
                if (FavoriteHelper.getInstance().isInAttention(mTid))
                    FavoriteHelper.getInstance().removeFavorite(mCtx, FavoriteHelper.TYPE_ATTENTION, mTid);
                else
                    FavoriteHelper.getInstance().addFavorite(mCtx, FavoriteHelper.TYPE_ATTENTION, mTid);
                return true;
            case R.id.action_show_all:
                cancelAuthorOnlyMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    void setupFab() {
        if (mMainFab != null) {
            if (!mDataReceived) {
                mMainFab.hide();
            } else {
                mMainFab.setEnabled(true);
                mMainFab.show();
            }

            mMainFab.setImageResource(R.drawable.ic_reply_white_24dp);
            mMainFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showQuickReply();
                    (new Handler()).postDelayed(new Runnable() {
                        public void run() {
                            mEtReply.requestFocus();
                            mEtReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                            mEtReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                        }
                    }, 100);
                }
            });
        }
        if (mNotificationFab != null) {
            mNotificationFab.setEnabled(false);
            mNotificationFab.hide();
        }
    }

    private void showPost(String text) {
        Bundle arguments = new Bundle();
        arguments.putString(PostFragment.ARG_TID_KEY, mTid);
        arguments.putInt(PostFragment.ARG_MODE_KEY, PostHelper.MODE_REPLY_THREAD);
        arguments.putString(PostFragment.ARG_TEXT_KEY, text);

        PostFragment fragment = new PostFragment();
        fragment.setParentSessionId(mSessionId);
        fragment.setArguments(arguments);
        setHasOptionsMenu(false);

        FragmentUtils.showFragment(getFragmentManager(), fragment);
    }

    private void refresh() {
        mInloading = true;
        mLoadingView.setState(ContentLoadingView.LOADING);
        startJob(mCurrentPage, FETCH_REFRESH, POSITION_NORMAL);
    }

    private void refreshAtEnd() {
        mFooterLoading = true;
        startJob(mCurrentPage, FETCH_REFRESH, POSITION_FOOTER);
    }

    public void showTheadTitle() {
        Toast.makeText(mCtx, mTitle, Toast.LENGTH_SHORT).show();
    }

    private void startJob(int page, int fetchType, int loadingPosition) {
        ThreadDetailJob job = new ThreadDetailJob(mCtx, mSessionId, mTid, mAuthorId, mGotoPostId, page, fetchType, loadingPosition);
        JobMgr.addJob(job);
    }

    public boolean isInAuthorOnlyMode() {
        return !TextUtils.isEmpty(mAuthorId);
    }

    public void enterAuthorOnlyMode(String authorId) {
        mAuthorId = authorId;
        mCurrentPage = 1;
        mGotoFloor = FIRST_FLOOR;
        mLoadingView.setState(ContentLoadingView.LOAD_NOW);
        mShowAllMenuItem.setVisible(true);
        startJob(mCurrentPage, FETCH_NORMAL, POSITION_NORMAL);
    }

    public void cancelAuthorOnlyMode() {
        mAuthorId = "";
        mCurrentPage = 1;
        mGotoFloor = FIRST_FLOOR;
        mLoadingView.setState(ContentLoadingView.LOAD_NOW);
        mShowAllMenuItem.setVisible(false);
        startJob(mCurrentPage, FETCH_NORMAL, POSITION_NORMAL);
    }

    public DetailBean getCachedPost(String postId) {
        return mCache.getPostByPostId(postId);
    }

    private class OnItemClickListener implements RecyclerItemClickListener.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
        }

        @Override
        public void onLongItemClick(View view, int position) {
//            DetailBean detailBean = mDetailAdapter.getItem(position);
            DetailBean detailBean = null;
            TextView floorView = (TextView) view.findViewById(R.id.floor);
            if (floorView != null) {
                String floor = floorView.getText().toString();
                if (!TextUtils.isEmpty(floor) && TextUtils.isDigitsOnly(floor)) {
                    int pos = mDetailAdapter.getPositionByFloor(Integer.parseInt(floor));
                    detailBean = mDetailAdapter.getItem(pos);
                }
            }
            if (detailBean == null) {
                return;
            }

            ThreadDetailActionModeCallback cb = new ThreadDetailActionModeCallback(ThreadDetailFragment.this,
                    mFid, mTid, mTitle, detailBean);
            ((AppCompatActivity) getActivity()).startSupportActionMode(cb);
        }

        @Override
        public void onDoubleTap(View view, int position) {
            showGotoPageDialog();
        }
    }

    @Override
    public void onDestroy() {
        getLoaderManager().destroyLoader(0);
        if (Utils.isMemoryUsageHigh())
            Glide.get(getActivity()).clearMemory();
        super.onDestroy();
    }

    public void scrollToTop() {
        stopScroll();
        prefetchPreviousPage();
        mRecyclerView.scrollToTop();
    }

    public void stopScroll() {
        mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    private synchronized void prefetchNextPage() {
        if (mCurrentPage < mMaxPage) {
            if (mCache.get(mCurrentPage + 1) == null) {
                if (!mFooterLoading) {
                    mFooterLoading = true;
                    prefetchPage(mCurrentPage + 1, FETCH_NEXT, POSITION_FOOTER);
                    mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                }
            } else {
                mRecyclerView.setFooterState(XFooterView.STATE_READY);
            }
        }
    }

    private synchronized void prefetchPreviousPage() {
        if (mCurrentPage > 1) {
            if (mCache.get(mCurrentPage - 1) == null) {
                if (!mHeaderLoading) {
                    mHeaderLoading = true;
                    prefetchPage(mCurrentPage - 1, FETCH_PREVIOUS, POSITION_HEADER);
                    mRecyclerView.setHeaderState(XHeaderView.STATE_LOADING);
                }
            } else {
                mRecyclerView.setHeaderState(XHeaderView.STATE_READY);
            }
        }
    }

    private void prefetchPage(int page, int fetchType, int loadingPosition) {
        if (mCache.get(page) == null) {
            if (page < 1 || page > mMaxPage)
                return;
            startJob(page, fetchType, loadingPosition);
        }
    }

    private void showGotoPageDialog() {
        mGoToPage = mCurrentPage;
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_goto_page, null);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        final TextView tvPage = (TextView) view.findViewById(R.id.tv_page);
        final ImageButton btnFirstPage = (ImageButton) view.findViewById(R.id.btn_fisrt_page);
        final ImageButton btnLastPage = (ImageButton) view.findViewById(R.id.btn_last_page);
        final ImageButton btnNextPage = (ImageButton) view.findViewById(R.id.btn_next_page);
        final ImageButton btnPreviousPage = (ImageButton) view.findViewById(R.id.btn_previous_page);
        final SeekBar sbGotoPage = (SeekBar) view.findViewById(R.id.sb_page);
        Button btnPageBottom = (Button) view.findViewById(R.id.btn_page_bottom);
        Button btnGoto = (Button) view.findViewById(R.id.btn_goto_page);

        final BottomSheetDialog dialog = new BottomDialog(getActivity());

        tvTitle.setText(mTitle);
        btnFirstPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_fast_backward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnLastPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_fast_forward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnNextPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_step_forward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnPreviousPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_step_backward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));

        tvPage.setText("第 " + String.valueOf(mGoToPage) + " / " + (mMaxPage) + " 页");

        btnPageBottom.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mRecyclerView.scrollToBottom();
                prefetchNextPage();
                dialog.dismiss();
            }
        });
        btnGoto.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mCurrentPage = mGoToPage;
                showOrLoadPage();
                dialog.dismiss();
            }
        });

        sbGotoPage.setMax(mMaxPage - 1);
        sbGotoPage.setProgress(mCurrentPage - 1);
        sbGotoPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGoToPage = progress + 1; //start from 0
                tvPage.setText("第 " + String.valueOf(mGoToPage) + " / " + (mMaxPage) + " 页");
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }
        });

        btnFirstPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentPage = 1;
                mGotoFloor = FIRST_FLOOR;
                showOrLoadPage();
                dialog.dismiss();
            }
        });

        btnLastPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentPage = mMaxPage;
                mGotoFloor = LAST_FLOOR;
                showOrLoadPage();
                dialog.dismiss();
            }
        });

        btnNextPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentPage < mMaxPage) {
                    mCurrentPage++;
                    mGotoFloor = FIRST_FLOOR;
                    showOrLoadPage();
                }
                dialog.dismiss();
            }
        });

        btnPreviousPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentPage > 1) {
                    mCurrentPage--;
                    mGotoFloor = FIRST_FLOOR;
                    showOrLoadPage();
                }
                dialog.dismiss();
            }
        });

        dialog.setContentView(view);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.show();
    }

    private void showQuickReply() {
        int timeToWait = PostHelper.getWaitTimeToPost();
        if (timeToWait > 0) {
            mIbPostReply.setVisibility(View.GONE);
            mTvCountdown.setText(timeToWait + "");
            mTvCountdown.setVisibility(View.VISIBLE);
            mCountDownTimer = new CountDownTimer(timeToWait * 1000, 500) {

                public void onTick(long millisUntilFinished) {
                    mTvCountdown.setText((millisUntilFinished / 1000) + "");
                }

                public void onFinish() {
                    mTvCountdown.setVisibility(View.GONE);
                    mIbPostReply.setVisibility(View.VISIBLE);
                }
            }.start();
        } else {
            mIbPostReply.setVisibility(View.VISIBLE);
            mTvCountdown.setVisibility(View.GONE);
        }
        mQuickReply.setVisibility(View.VISIBLE);
        mQuickReply.bringToFront();
        mMainFab.hide();
        mMainFab.setEnabled(false);
    }

    public boolean hideQuickReply() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        if (mQuickReply != null && mQuickReply.getVisibility() == View.VISIBLE) {
            mEtReply.setText("");
            mQuickReply.setVisibility(View.INVISIBLE);
            mMainFab.setEnabled(true);
            mMainFab.show();
            return true;
        }
        return false;
    }

    public class GoToFloorOnClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            if (!TextUtils.isEmpty(mAuthorId)) {
                Toast.makeText(getActivity(), "请先退出只查看该作者模式", Toast.LENGTH_SHORT).show();
                return;
            }
            int floor = (Integer) view.getTag();
            gotoFloor(floor);
        }
    }

    public void gotoFloor(int floor) {
        mGoToPage = (floor - 1) / mMaxPostInPage + 1; // page start from 1
        mGotoFloor = floor;

        if (mGoToPage != mCurrentPage) {
            mCurrentPage = mGoToPage;
            showOrLoadPage();
        } else {
            int position = mDetailAdapter.getPositionByFloor(floor);
            mRecyclerView.scrollToPosition(position);
            blinkItemView(position);
            mGotoFloor = -1;
        }
    }

    private void blinkItemView(int position) {
        DetailBean detailBean = mDetailAdapter.getItem(position);
        if (detailBean != null) {
            mBlinkPostId = detailBean.getPostId();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int pos = mDetailAdapter.getPositionByPostId(mBlinkPostId);
                    mBlinkPostId = null;
                    View view = mLayoutManager.findViewByPosition(pos);
                    if (view != null && ViewCompat.isAttachedToWindow(view)) {
                        View floorView = view.findViewById(R.id.floor);
                        if (floorView != null)
                            floorView.startAnimation(mBlinkAnim);
                    }
                }
            }, 150);
        }
    }

    private void showOrLoadPage() {
        showOrLoadPage(false);
    }

    private void showOrLoadPage(boolean refresh) {
        setActionBarTitle((mCurrentPage > 0 && mMaxPage > 0 ? "(" + mCurrentPage + "/" + mMaxPage + ") " : "")
                + mTitle);

        if (mCache.get(mCurrentPage) != null) {
            mDetailBeans = mCache.get(mCurrentPage).getAll();
            mDetailAdapter.setDatas(mDetailBeans);

            if (mCurrentPage == 1) {
                mRecyclerView.setHeaderState(XHeaderView.STATE_HIDDEN);
            }
            if (mCurrentPage == mMaxPage) {
                mRecyclerView.setFooterState(XFooterView.STATE_END);
            }

            int position = -1;
            if (mGotoFloor == LAST_FLOOR) {
                position = mDetailAdapter.getItemCount() - 1;
            } else if (mGotoFloor == FIRST_FLOOR) {
                position = 0;
            } else if (mGotoFloor != -1) {
                position = mDetailAdapter.getPositionByFloor(mGotoFloor);
            } else if (HiUtils.isValidId(mGotoPostId)) {
                position = mDetailAdapter.getPositionByPostId(mGotoPostId);
            }
            if (position >= 0) {
                mRecyclerView.scrollToPosition(position);
                blinkItemView(position);
            }
            mGotoPostId = null;
            mGotoFloor = -1;

            if (mCurrentPage > 1 && position < 5) {
                prefetchPreviousPage();
            }
            if (mCurrentPage < mMaxPage && position > mDetailAdapter.getItemCount() - 5) {
                prefetchNextPage();
            }
            mMainFab.show();
        } else {
            int fetchType = FETCH_NORMAL;
            if (refresh || mCurrentPage == mMaxPage || mCurrentPage == LAST_PAGE) {
                fetchType = FETCH_REFRESH;
            }
            mInloading = true;
            mLoadingView.setState(ContentLoadingView.LOADING);
            startJob(mCurrentPage, fetchType, POSITION_NORMAL);
        }
    }

    class AvatarOnClickListener extends OnSingleClickListener {
        @Override
        public void onSingleClick(View view) {
            String uid = (String) view.getTag(R.id.avatar_tag_uid);
            String username = (String) view.getTag(R.id.avatar_tag_username);

            Bundle arguments = new Bundle();
            arguments.putString(UserinfoFragment.ARG_UID, uid);
            arguments.putString(UserinfoFragment.ARG_USERNAME, username);
            UserinfoFragment fragment = new UserinfoFragment();
            fragment.setArguments(arguments);

            setHasOptionsMenu(false);

            FragmentUtils.showFragment(getFragmentManager(), fragment);
        }
    }

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        int firstVisiblesItem, lastVisibleItem, visibleItemCount, totalItemCount;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            firstVisiblesItem = mLayoutManager.findFirstVisibleItemPosition();
            lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            if (dy > 0) {
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                if ((visibleItemCount + firstVisiblesItem) >= totalItemCount - 3) {
                    if (!mFooterLoading && mCurrentPage < mMaxPage) {
                        prefetchNextPage();
                    }
                }
            } else if (dy < 0) {
                if (!mHeaderLoading && firstVisiblesItem < 3 && mCurrentPage > 1) {
                    prefetchPreviousPage();
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && HiSettingsHelper.getInstance().isFabAutoHide()
                    && mRecyclerView.isNearBottom()) {
                if (mMainFab != null && mMainFab.isEnabled() && mMainFab.getVisibility() != View.VISIBLE)
                    mMainFab.show();
            }
        }
    }

    public void startImageGallery(int imageIndex, GlideImageView imageView) {
        if (!HiApplication.isActivityVisible() || getActivity() == null) {
            return;
        }

        DetailListBean detailListBean = mCache.get(mCurrentPage);
        if (detailListBean == null) {
            Toast.makeText(getActivity(), "帖子还未加载完成", Toast.LENGTH_LONG).show();
            return;
        }

        imageView.stopCurrentGif();
        if (detailListBean.getContentImages().size() > 0) {
            Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeScaleUpAnimation(imageView, 0, 0, imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
            intent.putExtra(ImageViewerActivity.KEY_IMAGE_INDEX, imageIndex);
            intent.putParcelableArrayListExtra(ImageViewerActivity.KEY_IMAGES, mCache.get(mCurrentPage).getContentImages());
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        } else {
            Toast.makeText(mCtx, "本页没有图片", Toast.LENGTH_SHORT).show();
        }
    }

    public String getTid() {
        return mTid;
    }

    @Override
    public boolean onBackPressed() {
        if (mEmojiPopup != null && mEmojiPopup.isShowing()) {
            mEmojiPopup.dismiss();
        }
        return hideQuickReply();
    }

    private class ThreadDetailEventCallback extends EventCallback<ThreadDetailEvent> {
        @Override
        public void onFail(ThreadDetailEvent event) {
            if (event.mLoadingPosition == POSITION_HEADER) {
                mHeaderLoading = false;
                mRecyclerView.setHeaderState(XHeaderView.STATE_ERROR);
            } else if (event.mLoadingPosition == POSITION_FOOTER) {
                mFooterLoading = false;
                mRecyclerView.setFooterState(XFooterView.STATE_ERROR);
            } else {
                mInloading = false;
                if (mDetailBeans.size() == 0) {
                    mLoadingView.setState(ContentLoadingView.ERROR);
                }
                UIUtils.errorSnack(getView(), event.mMessage, event.mDetail);
            }
        }

        @Override
        public void onSuccess(ThreadDetailEvent event) {
            DetailListBean details = event.mData;

            mMaxPostInPage = HiSettingsHelper.getInstance().getMaxPostsInPage();
            if (ThreadDetailJob.FIND_AUTHOR_ID.equals(mAuthorId))
                mAuthorId = event.mAuthorId;

            // Set title
            if (details.getTitle() != null && !details.getTitle().isEmpty()) {
                mTitle = details.getTitle();
            }

            mFid = details.getFid();
            if (TextUtils.isEmpty(mTid))
                mTid = details.getTid();

            // Set MaxPage earlier than showOrLoadPage()
            mMaxPage = details.getLastPage();
            mMaxImageDecodeWidth = ImageSizeUtils.getDecodeSize(details.getImagesCount());

            mCache.put(details.getPage(), details);

            if (event.mLoadingPosition == POSITION_HEADER) {
                mHeaderLoading = false;
                mRecyclerView.setHeaderState(XHeaderView.STATE_READY);
            } else if (event.mLoadingPosition == POSITION_FOOTER) {
                mFooterLoading = false;
                if (event.mFectchType == FETCH_NEXT)
                    mRecyclerView.setFooterState(XFooterView.STATE_READY);
            } else {
                mInloading = false;
                mLoadingView.setState(ContentLoadingView.CONTENT);
            }

            if (event.mFectchType == FETCH_NORMAL || event.mFectchType == FETCH_REFRESH) {
                if (!mDataReceived) {
                    mDataReceived = true;
                    mMainFab.setEnabled(true);
                    mMainFab.show();
                }
                mDetailBeans = details.getAll();
                mDetailAdapter.setDatas(mDetailBeans);
                mCurrentPage = details.getPage();

                showOrLoadPage();
            }

            if (!mHistorySaved || details.getPage() == 1) {
                mHistorySaved = true;
                String uid = null, username = null, postTime = null;
                if (details.getPage() == 1 && details.getCount() > 0) {
                    DetailBean detailBean = details.getAll().get(0);
                    uid = detailBean.getUid();
                    username = detailBean.getAuthor();
                    postTime = detailBean.getTimePost();
                }
                HistoryDao.saveHistoryInBackground(mTid, mFid, mTitle, uid, username, postTime);
            }
        }

        @Override
        public void onFailRelogin(ThreadDetailEvent event) {
            showLoginDialog();
        }

    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PostEvent event) {
        if (!mSessionId.equals(event.mSessionId))
            return;

        EventBus.getDefault().removeStickyEvent(event);

        String message = event.mMessage;
        PostBean postResult = event.mPostResult;

        if (event.mStatus == Constants.STATUS_IN_PROGRESS) {
            postProgressDialog = HiProgressDialog.show(mCtx, "请稍候...");
        } else if (event.mStatus == Constants.STATUS_SUCCESS) {
            //pop post fragment on success
            Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
            if (fg instanceof PostFragment) {
                ((BaseFragment) fg).popFragment();
            } else if (mQuickReply.getVisibility() == View.VISIBLE) {
                mEtReply.setText("");
                hideQuickReply();
            }

            if (postProgressDialog != null) {
                postProgressDialog.dismiss(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_SHORT).show();
            }

            mGotoFloor = postResult.getFloor();

            if (postResult.getDelete() == 1) {
                if (mGotoFloor == 1) {
                    //first floor is deleted, meaning whole thread is deleted
                    String fid = postResult.getFid();
                    FragmentUtils.showForum(getFragmentManager(), HiUtils.isValidId(fid) ? Integer.parseInt(fid) : 0);
                } else {
                    //this floor is deleted, so goto upper floor
                    mGotoFloor--;
                    mCache.remove(mCurrentPage);
                    showOrLoadPage(true);
                }
            } else {
                if (event.mMode != PostHelper.MODE_EDIT_POST) {
                    mCurrentPage = mMaxPage;
                    mGotoFloor = LAST_FLOOR;
                }
                mCache.remove(mCurrentPage);
                showOrLoadPage(true);
            }
        } else {
            if (postProgressDialog != null) {
                postProgressDialog.dismissError(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkReadyEvent event) {
        if (!mInloading && mDetailBeans.size() == 0) {
            refresh();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ThreadDetailEvent event) {
        if (!mSessionId.equals(event.mSessionId))
            return;
        EventBus.getDefault().removeStickyEvent(event);
        mEventCallback.process(event);
    }

}
