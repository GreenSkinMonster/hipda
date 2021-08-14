package net.jejer.hipda.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiEditText;

import net.jejer.hipda.R;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.NetworkReadyEvent;
import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.cache.ThreadDetailCache;
import net.jejer.hipda.db.ContentDao;
import net.jejer.hipda.db.HistoryDao;
import net.jejer.hipda.job.EventCallback;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.job.PostEvent;
import net.jejer.hipda.job.PostJob;
import net.jejer.hipda.job.ThreadDetailEvent;
import net.jejer.hipda.job.ThreadDetailJob;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.adapter.RecyclerItemClickListener;
import net.jejer.hipda.ui.adapter.ThreadDetailAdapter;
import net.jejer.hipda.ui.widget.BottomDialog;
import net.jejer.hipda.ui.widget.ContentLoadingView;
import net.jejer.hipda.ui.widget.CountdownButton;
import net.jejer.hipda.ui.widget.HiProgressDialog;
import net.jejer.hipda.ui.widget.OnSingleClickListener;
import net.jejer.hipda.ui.widget.SimpleDivider;
import net.jejer.hipda.ui.widget.SimpleGridMenu;
import net.jejer.hipda.ui.widget.SmoothLinearLayoutManager;
import net.jejer.hipda.ui.widget.XFooterView;
import net.jejer.hipda.ui.widget.XHeaderView;
import net.jejer.hipda.ui.widget.XRecyclerView;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HtmlCompat;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import okhttp3.Request;

public class ThreadDetailFragment extends BaseFragment {
    public static final String ARG_TID_KEY = "tid";
    public static final String ARG_PID_KEY = "pid";
    public static final String ARG_AUTHOR_ID_KEY = "author_id";
    public static final String ARG_TITLE_KEY = "title";
    public static final String ARG_FLOOR_KEY = "floor";
    public static final String ARG_PAGE_KEY = "page";
    public static final String ARG_MAX_PAGE_KEY = "maxPage";

    public static final int LAST_FLOOR_OF_PAGE = Integer.MIN_VALUE;
    public static final int FIRST_FLOOR_OF_PAGE = Integer.MIN_VALUE + 1;
    public static final int LAST_PAGE = Integer.MIN_VALUE;

    public final static int FETCH_NORMAL = 0;
    public final static int FETCH_NEXT = 1;
    public final static int FETCH_PREVIOUS = 2;
    public final static int FETCH_REFRESH = 3;

    public static final int POSITION_NORMAL = 0;
    public static final int POSITION_HEADER = 1;
    public static final int POSITION_FOOTER = 2;

    private Context mCtx;
    private String mTid;
    private String mAuthorId;
    private String mTitle;
    private int mFid;
    private XRecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ThreadDetailAdapter mDetailAdapter;
    final private ThreadDetailCache mCache = new ThreadDetailCache();

    private int mMaxPage = 0;
    private int mMaxPostInPage = HiSettingsHelper.getInstance().getMaxPostsInPage();    // user can configure max posts per page in forum setting

    private int mGotoPage = 1;
    private int mGotoFloor = -1;    // actual floor number in thread, start from 1
    private String mGotoPostId;

    private int mViewBeginPage = 0;
    private int mViewEndPage = 0;

    private View mQuickReply;
    private EmojiEditText mEtReply;
    private CountdownButton mCountdownButton;
    private AppCompatTextView mPageLabel;

    private DetailBean mQuickReplyToPost;
    private int mQuickReplyMode;
    private String mHighlightPostId;
    private String mPendingScrollPostId;
    private int mPostViewTop = -1;
    private int mPostViewHeight = -1;

    private Animation mBlinkAnim;

    private boolean mDataReceived = false;
    private boolean mInloading = false;
    private boolean mHeaderLoading = false;
    private boolean mFooterLoading = false;

    private HiProgressDialog postProgressDialog;
    private ContentLoadingView mLoadingView;
    final private ThreadDetailEventCallback mEventCallback = new ThreadDetailEventCallback();
    private MenuItem mShowAllMenuItem;

    private boolean mHistorySaved = false;
    private int mPendingBlinkFloor;

    private SimpleGridMenu mGridMenu;

    final private View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (oldTop - top > Utils.dpToPx(96)) {
                v.removeOnLayoutChangeListener(this);
                scrollPostForReply(top);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCtx = getActivity();

        setHasOptionsMenu(true);

        if (getArguments() != null) {
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
            if (getArguments().containsKey(ARG_MAX_PAGE_KEY)) {
                mMaxPage = getArguments().getInt(ARG_MAX_PAGE_KEY);
            }
            if (getArguments().containsKey(ARG_PAGE_KEY)) {
                mGotoPage = getArguments().getInt(ARG_PAGE_KEY);
                if (mGotoPage <= 0 && mGotoPage != LAST_PAGE)
                    mGotoPage = 1;
            }
            if (getArguments().containsKey(ARG_FLOOR_KEY)) {
                mGotoFloor = getArguments().getInt(ARG_FLOOR_KEY);
            }
        }

        mBlinkAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thread_detail, parent, false);

        mRecyclerView = view.findViewById(R.id.rv_thread_details);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mLayoutManager = new SmoothLinearLayoutManager(mCtx);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new SimpleDivider(getActivity()));

        mRecyclerView.addOnScrollListener(new OnScrollListener());
        mRecyclerView.setHeaderState(XHeaderView.STATE_HIDDEN);
        mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(mCtx, new OnItemClickListener());
        mDetailAdapter = new ThreadDetailAdapter(mCtx, this, itemClickListener,
                new GoToFloorOnClickListener(), new AvatarOnClickListener(), new WarningOnClickListener());

        mRecyclerView.setAdapter(mDetailAdapter);

        mRecyclerView.setXRecyclerListener(new XRecyclerView.XRecyclerListener() {
            @Override
            public void onHeaderReady() {
//                mCurrentPage--;
//                mGotoFloor = LAST_FLOOR;
//                showOrLoadPage();
//                prefetchPreviousPage();
            }

            @Override
            public void onFooterReady() {
//                mCurrentPage++;
//                mGotoFloor = FIRST_FLOOR;
//                showOrLoadPage();
            }

            @Override
            public void atEnd() {
                mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                refreshAtEnd();
            }

            @Override
            public void onFooterError() {
                if (mViewEndPage == mMaxPage) {
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

        mLoadingView = view.findViewById(R.id.content_loading);
        mLoadingView.setErrorStateListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mInloading) {
                    mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                    refresh();
                }
            }
        });

        mQuickReply = ((ThreadDetailActivity) getActivity()).getQuickReplyView();
        mEtReply = mQuickReply.findViewById(R.id.tv_reply_text);
        mEtReply.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        mPageLabel = view.findViewById(R.id.tv_page_label);

        mCountdownButton = mQuickReply.findViewById(R.id.countdown_button);
        mCountdownButton.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_send).sizeDp(28).color(Color.GRAY));
        mCountdownButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String replyText = mEtReply.getText().toString();
                if (Utils.getWordCount(replyText) < 5) {
                    UIUtils.toast("字数必须大于5");
                } else {
                    PostBean postBean = new PostBean();
                    postBean.setContent(replyText);
                    postBean.setTid(mTid);
                    postBean.setFid(mFid);
                    if (mQuickReplyToPost != null) {
                        postBean.setPid(mQuickReplyToPost.getPostId());
                        postBean.setFloor(mQuickReplyToPost.getFloor());
                    }

                    JobMgr.addJob(new PostJob(mSessionId, mQuickReplyMode, null, postBean, true));

                    UIUtils.hideSoftKeyboard(getActivity());
                    scrollToBottom();
                }
            }
        });
        mCountdownButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String replyText = mEtReply.getText().toString();
                showPost(replyText);
                hideQuickReply(true);
                return true;
            }
        });

        ImageButton ibEmojiSwitch = mQuickReply.findViewById(R.id.ib_emoji_switch);
        setUpEmojiPopup(mEtReply, ibEmojiSwitch);

        setActionBarTitle(mTitle);
//        setActionBarSubtitle(mCurrentPage > 0 && mMaxPage > 0 ? mCurrentPage + "/" + mMaxPage : "?");

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
            if (mDetailAdapter.getDataCount() == 0) {
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

        mShowAllMenuItem = menu.findItem(R.id.action_show_all);
        mShowAllMenuItem.setIcon(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_eject)
                .color(UIUtils.getToolbarTextColor(getActivity())).sizeDp(16));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem favoritesMenuItem = menu.findItem(R.id.action_add_favorite);
        if (favoritesMenuItem != null) {
            if (FavoriteHelper.getInstance().isInFavorite(mTid)) {
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
            mShowAllMenuItem.setVisible(!TextUtils.isEmpty(mAuthorId));
        }
    }

    @SuppressLint("NonConstantResourceId")
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
                if (mViewBeginPage > 1)
                    url += "&page=" + mViewBeginPage;
                try {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setStartAnimations(getActivity(), R.anim.slide_in_right, 0);
                    builder.setExitAnimations(getActivity(), 0, R.anim.slide_out_right);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
                } catch (Exception e) {
                    UIUtils.toast("没有找到浏览器应用 : " + e.getMessage());
                }
                return true;
            case R.id.action_copy_url:
                ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("THREAD URL FROM HiPDA", HiUtils.DetailListUrl + mTid);
                clipboard.setPrimaryClip(clip);
                UIUtils.toast("帖子地址已经复制到粘贴板");
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
                showPost("");
                return true;
            case R.id.action_refresh_detail:
                mLoadingView.setState(ContentLoadingView.LOAD_NOW);
                refresh();
                return true;
            case R.id.action_goto:
                showGotoPageDialog();
                return true;
            case R.id.action_add_favorite:
                if (FavoriteHelper.getInstance().isInFavorite(mTid))
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
                mMainFab.show();
            }

            mMainFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showQuickReply();
                }
            });
        }
    }

    private void showPost(String text) {
        if (mQuickReplyToPost != null) {
            FragmentUtils.showPostActivity(getActivity(), mQuickReplyMode,
                    mSessionId, mFid, mTid, -1,
                    mQuickReplyToPost.getPostId(),
                    mQuickReplyToPost.getFloor(),
                    mQuickReplyToPost.getAuthor(), text, mQuickReplyToPost.getContents().getCopyText());
        } else {
            FragmentUtils.showPostActivity(getActivity(), mQuickReplyMode,
                    mSessionId, mFid, mTid, -1,
                    null, -1, null, text, null);
        }
    }

    private void refresh() {
        mInloading = true;
        mLoadingView.setState(ContentLoadingView.LOADING);
        startJob(mGotoPage, FETCH_REFRESH, POSITION_NORMAL);
    }

    private void refreshAtEnd() {
        mFooterLoading = true;
        startJob(mViewEndPage, FETCH_REFRESH, POSITION_FOOTER);
    }

    private void startJob(int page, int fetchType, int loadingPosition) {
        ThreadDetailJob job = new ThreadDetailJob(mCtx, mSessionId, mTid, mAuthorId, mGotoPostId, page, fetchType, loadingPosition);
        JobMgr.addJob(job);
    }

    public boolean isInAuthorOnlyMode() {
        return !TextUtils.isEmpty(mAuthorId);
    }

    public void enterAuthorOnlyMode(String authorId) {
        mViewBeginPage = 0;
        mViewEndPage = 0;
        mMaxPage = 0;
        mAuthorId = authorId;
        mGotoPage = 1;
        mGotoFloor = FIRST_FLOOR_OF_PAGE;

        updatePageLabel();
        mShowAllMenuItem.setVisible(true);
        expandAppBar();
        hideQuickReply(true);

        mCache.clear();
        mDetailAdapter.clear();

        mLoadingView.setState(ContentLoadingView.LOAD_NOW);
        startJob(mGotoPage, FETCH_REFRESH, POSITION_NORMAL);
    }

    public void cancelAuthorOnlyMode() {
        mViewBeginPage = 0;
        mViewEndPage = 0;
        mMaxPage = 0;
        mAuthorId = "";
        mGotoPage = 1;
        mGotoFloor = FIRST_FLOOR_OF_PAGE;

        updatePageLabel();
        mShowAllMenuItem.setVisible(false);
        expandAppBar();
        hideQuickReply(true);

        mCache.clear();
        mDetailAdapter.clear();

        mLoadingView.setState(ContentLoadingView.LOAD_NOW);
        startJob(mGotoPage, FETCH_REFRESH, POSITION_NORMAL);
    }

    public DetailBean getCachedPost(String postId) {
        return mCache.getPostByPostId(postId);
    }

    public ArrayList<ContentImg> getImagesInPage(int page) {
        DetailListBean detailListBean = mCache.get(page);
        if (detailListBean != null) {
            return detailListBean.getContentImages();
        }
        return null;
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
//                    if (pos != position)
//                        UIUtils.toast("position1 : " + position + ", position2=" + pos);
                }
            }
            if (detailBean == null)
                return;

            showGridMenu(detailBean);
        }

        @Override
        public void onDoubleTap(View view, int position) {
            showGotoPageDialog();
        }
    }

    private void showGridMenu(final DetailBean detailBean) {
        if (mGridMenu != null)
            return;
        mGridMenu = new SimpleGridMenu(getActivity());
        mGridMenu.setTitle(detailBean.getFloor() + "# " + detailBean.getAuthor());
        mGridMenu.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mGridMenu = null;
            }
        });

        mGridMenu.add("copy", "复制文字", new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("COPY FROM HiPDA", detailBean.getContents().getCopyText());
                clipboard.setPrimaryClip(clip);
                UIUtils.toast("文字已复制");
            }
        });
        String authorText = isInAuthorOnlyMode() ? getString(R.string.action_show_all) : getString(R.string.action_only_floor_author);
        mGridMenu.add("author", authorText,
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (isInAuthorOnlyMode()) {
                            cancelAuthorOnlyMode();
                        } else {
                            enterAuthorOnlyMode(detailBean.getUid());
                        }
                    }
                });
        mGridMenu.add("share", "分享",
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "帖子 ：" + mTitle + "\n" +
                                HiUtils.RedirectToPostUrl.replace("{tid}", mTid).replace("{pid}", detailBean.getPostId()) + "\n" +
                                detailBean.getFloor() + "#  作者 ：" + detailBean.getAuthor() + "\n\n" +
                                detailBean.getContents().getCopyText();
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "分享文字内容"));
                    }
                });
        mGridMenu.add("select_text", "文字选择",
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        detailBean.setSelectMode(true);
                        int pos = mDetailAdapter.getPositionByPostId(detailBean.getPostId());
                        if (pos != -1)
                            mDetailAdapter.notifyItemChanged(pos);
//                        UIUtils.showMessageDialog(getActivity(),
//                                detailBean.getFloor() + "# " + detailBean.getAuthor(),
//                                detailBean.getContents().getCopyText().trim(),
//                                true);
                    }
                });
        mGridMenu.add("reply", "回复",
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        showQuickReply(PostHelper.MODE_REPLY_POST, detailBean);
                    }
                },
                GoogleMaterial.Icon.gmd_open_in_new, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentUtils.showPostActivity(getActivity(), PostHelper.MODE_REPLY_POST,
                                mSessionId, mFid, mTid, -1,
                                detailBean.getPostId(), detailBean.getFloor(),
                                detailBean.getAuthor(), null, detailBean.getContents().getCopyText());
                        hideQuickReply(true);
                    }
                });
        mGridMenu.add("quote", "引用",
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        showQuickReply(PostHelper.MODE_QUOTE_POST, detailBean);
                    }
                },
                GoogleMaterial.Icon.gmd_open_in_new, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentUtils.showPostActivity(getActivity(), PostHelper.MODE_QUOTE_POST,
                                mSessionId, mFid, mTid, -1,
                                detailBean.getPostId(), detailBean.getFloor(),
                                detailBean.getAuthor(), null, detailBean.getContents().getCopyText());
                        hideQuickReply(true);
                    }
                });
        if (HiSettingsHelper.getInstance().getUsername().equalsIgnoreCase(detailBean.getAuthor())
                || HiSettingsHelper.getInstance().getUid().equals(detailBean.getUid())) {
            mGridMenu.add("delete", "快速删除",
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            showDeletePostDialog(detailBean);
                        }
                    });
            mGridMenu.add("edit", "编辑",
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            FragmentUtils.showPostActivity(getActivity(), PostHelper.MODE_EDIT_POST,
                                    mSessionId, mFid, mTid, detailBean.getPage(),
                                    detailBean.getPostId(), detailBean.getFloor(),
                                    null, null, null);
                        }
                    });
        }
        mGridMenu.show();
    }

    private void showDeletePostDialog(final DetailBean detailBean) {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setTitle("删除本帖？");
        popDialog.setMessage(HtmlCompat.fromHtml("确认删除发表的内容吗？<br><br><font color=red>注意：此操作不可恢复。" +
                "<br><br>如帖子可以删除，则进行删除，否则清空帖子内容以及图片和附件。</font>"));
        popDialog.setPositiveButton("删除",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PostBean postBean = new PostBean();
                        postBean.setFid(mFid);
                        postBean.setTid(mTid);
                        postBean.setPid(detailBean.getPostId());
                        postBean.setFloor(detailBean.getFloor());
                        postBean.setContent(detailBean.getContents().getContent());
                        if (detailBean.getFloor() == 1)
                            postBean.setSubject(mTitle);

                        JobMgr.addJob(new PostJob(mSessionId, PostHelper.MODE_QUICK_DELETE, null, postBean, false));
                    }
                });
        popDialog.setIcon(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_exclamation_circle).sizeDp(24).color(Color.RED));
        popDialog.setNegativeButton("取消", null);
        popDialog.create().show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGridMenu != null)
            mGridMenu.dismiss();
    }

    @Override
    public void onDestroy() {
        if (Utils.isMemoryUsageHigh())
            Glide.get(getActivity()).clearMemory();
        super.onDestroy();
    }

    public void scrollToTop() {
        stopScroll();
        mGotoPage = mViewEndPage - 1;
        if (mGotoPage < 1)
            mGotoPage = 1;
        mGotoFloor = FIRST_FLOOR_OF_PAGE;
        showOrLoadPage();
    }

    public void scrollToBottom() {
        if (HiSettingsHelper.getInstance().isAppBarCollapsible()) {
            ((BaseActivity) getActivity()).mAppBarLayout.setExpanded(false, true);
        }
        mRecyclerView.scrollToBottom();
    }

    public void stopScroll() {
        mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    private synchronized void prefetchNextPage() {
        if (mViewEndPage <= 0)
            return;
        final int currentPage = mViewEndPage;
        if (currentPage < mMaxPage) {
            if (mCache.get(currentPage + 1) == null) {
                if (!mFooterLoading) {
                    mFooterLoading = true;
                    prefetchPage(currentPage + 1, FETCH_NEXT, POSITION_FOOTER);
                    mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                }
            } else {
                mDetailAdapter.addDatas(mCache.get(currentPage + 1));
            }
        }
    }

    private synchronized void prefetchPreviousPage() {
        if (mViewBeginPage <= 0)
            return;
        final int currentPage = mViewBeginPage;
        if (currentPage > 1) {
            if (mCache.get(currentPage - 1) == null) {
                if (!mHeaderLoading) {
                    mHeaderLoading = true;
                    prefetchPage(currentPage - 1, FETCH_PREVIOUS, POSITION_HEADER);
                    mRecyclerView.setHeaderState(XHeaderView.STATE_LOADING);
                }
            } else {
                mDetailAdapter.addDatas(mCache.get(currentPage - 1));
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
        final int[] gotoPageHolder = new int[1];
        gotoPageHolder[0] = mViewBeginPage;
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_goto_page, null);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        final TextView tvPage = view.findViewById(R.id.tv_page);
        final ImageButton btnFirstPage = view.findViewById(R.id.btn_fisrt_page);
        final ImageButton btnLastPage = view.findViewById(R.id.btn_last_page);
        final ImageButton btnNextPage = view.findViewById(R.id.btn_next_page);
        final ImageButton btnPreviousPage = view.findViewById(R.id.btn_previous_page);
        final SeekBar sbGotoPage = view.findViewById(R.id.sb_page);
        Button btnPageBottom = view.findViewById(R.id.btn_page_bottom);
        Button btnGoto = view.findViewById(R.id.btn_goto_page);

        final BottomSheetDialog dialog = new BottomDialog(getActivity());

        tvTitle.setText(mTitle);
        btnFirstPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_fast_backward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnLastPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_fast_forward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnNextPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_step_forward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnPreviousPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_step_backward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));

        tvPage.setText("第 " + String.valueOf(gotoPageHolder[0]) + " / " + (mMaxPage) + " 页");

        btnPageBottom.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                prefetchNextPage();
                scrollToBottom();
                dialog.dismiss();
            }
        });
        btnGoto.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mGotoPage = gotoPageHolder[0];
                mGotoFloor = FIRST_FLOOR_OF_PAGE;
                showOrLoadPage();
                dialog.dismiss();
            }
        });

        sbGotoPage.setMax(mMaxPage - 1);
        sbGotoPage.setProgress(mViewBeginPage - 1);
        sbGotoPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gotoPageHolder[0] = progress + 1; //start from 0
                tvPage.setText("第 " + String.valueOf(gotoPageHolder[0]) + " / " + (mMaxPage) + " 页");
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
                mGotoPage = 1;
                mGotoFloor = FIRST_FLOOR_OF_PAGE;
                showOrLoadPage();
                dialog.dismiss();
            }
        });

        btnLastPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGotoPage = mMaxPage;
                mGotoFloor = LAST_FLOOR_OF_PAGE;
                showOrLoadPage();
                dialog.dismiss();
            }
        });

        btnNextPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGotoPage = mViewBeginPage;
                if (mGotoPage < mMaxPage) {
                    mGotoPage++;
                    mGotoFloor = FIRST_FLOOR_OF_PAGE;
                    showOrLoadPage();
                }
                dialog.dismiss();
            }
        });

        btnPreviousPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGotoPage = mViewBeginPage;
                if (mGotoPage > 1) {
                    mGotoPage--;
                    mGotoFloor = FIRST_FLOOR_OF_PAGE;
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
        showQuickReply(PostHelper.MODE_REPLY_THREAD, null);
    }

    public void showQuickReply(int mode, final DetailBean detailBean) {
        mCountdownButton.setCountdown(PostHelper.getWaitTimeToPost());

        mQuickReplyMode = mode;
        mQuickReplyToPost = detailBean;

        if (mode == PostHelper.MODE_QUOTE_POST) {
            mEtReply.setHint("引用 " + detailBean.getFloor() + "# " + detailBean.getAuthor());
        } else if (mode == PostHelper.MODE_REPLY_POST) {
            mEtReply.setHint("回复 " + detailBean.getFloor() + "# " + detailBean.getAuthor());
        } else {
            mEtReply.setHint(R.string.action_quick_reply);
        }

        mQuickReply.setVisibility(View.VISIBLE);
        mQuickReply.bringToFront();
        mMainFab.setVisibility(View.GONE);
        showSoftKeyboard();

        if (HiSettingsHelper.getInstance().isGestureBack())
            ((ThreadDetailActivity) getActivity()).setSwipeBackEnable(false);

        if (mode != PostHelper.MODE_NEW_THREAD && mQuickReplyToPost != null) {
            int pos = mDetailAdapter.getPositionByPostId(mQuickReplyToPost.getPostId());
            if (pos != -1) {
                View view = mLayoutManager.findViewByPosition(pos);
                if (view != null) {
                    View rootView = ((ThreadDetailActivity) getActivity()).getRootView();
                    mPostViewTop = UIUtils.getRelativeTop(view, (ViewGroup) rootView);
                    mPostViewHeight = view.getHeight();
                }
            }

            highlightPost(mQuickReplyToPost.getPostId());
            mPendingScrollPostId = mQuickReplyToPost.getPostId();
            mQuickReply.addOnLayoutChangeListener(mOnLayoutChangeListener);
        } else {
            deHighlightPostId();
        }
    }

    public boolean hideQuickReply(boolean clearReplyTo) {
        if (HiSettingsHelper.getInstance().isGestureBack())
            ((ThreadDetailActivity) getActivity()).setSwipeBackEnable(true);

        if (clearReplyTo) {
            deHighlightPostId();
            mQuickReplyMode = PostHelper.MODE_REPLY_THREAD;
            mQuickReplyToPost = null;
        }
        mMainFab.show();
        if (mQuickReply.getVisibility() == View.VISIBLE) {
            mQuickReply.setVisibility(View.INVISIBLE);
            return true;
        }
        return false;
    }

    private void showSoftKeyboard() {
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                mEtReply.requestFocus();
                mEtReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                mEtReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                mEtReply.setSelection(mEtReply.getText().length());
            }
        }, 100);
    }

    private class GoToFloorOnClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            if (!TextUtils.isEmpty(mAuthorId)) {
                UIUtils.toast("请先退出只查看该作者模式");
                return;
            }
            int floor = (Integer) view.getTag();
            gotoFloor(floor);
        }
    }

    public void gotoFloor(int floor) {
        mGotoPage = (floor - 1) / mMaxPostInPage + 1; // page start from 1
        mGotoFloor = floor;

        int position = mDetailAdapter.getPositionByFloor(floor);
        if (position != -1) {
            mRecyclerView.scrollToPosition(position);
            DetailBean detailBean = mDetailAdapter.getItem(position);
            if (detailBean != null) {
                blinkItemView(detailBean.getPostId());
            }
            mGotoFloor = -1;
        } else {
            mPendingBlinkFloor = floor;
            showOrLoadPage();
        }
    }

    private void blinkItemView(final String postId) {
        new Handler().postDelayed(() -> {
            int pos = mDetailAdapter.getPositionByPostId(postId);
            View view = mLayoutManager.findViewByPosition(pos);
            if (view != null && ViewCompat.isAttachedToWindow(view)) {
                view.findViewById(R.id.floor).startAnimation(mBlinkAnim);
            }
        }, 150);
    }

    private void highlightPost(final String postId) {
        if (mHighlightPostId != null && !mHighlightPostId.equals(postId))
            deHighlightPostId();
        int pos = mDetailAdapter.getPositionByPostId(postId);
        if (pos != -1) {
            DetailBean detailBean = mDetailAdapter.getItem(pos);
            detailBean.setHighlightMode(true);
            mDetailAdapter.notifyItemChanged(pos);
            mHighlightPostId = postId;
        }
    }

    private void deHighlightPostId() {
        if (mHighlightPostId == null)
            return;
        int pos = mDetailAdapter.getPositionByPostId(mHighlightPostId);
        DetailBean detailBean = mDetailAdapter.getItem(pos);
        if (detailBean != null) {
            detailBean.setHighlightMode(false);
            mDetailAdapter.notifyItemChanged(pos);
        } else {
            detailBean = mCache.getPostByPostId(mHighlightPostId);
            if (detailBean != null)
                detailBean.setHighlightMode(false);
        }
    }

    private void showOrLoadPage() {
        showOrLoadPage(false);
    }

    private void showOrLoadPage(boolean refresh) {

        setActionBarTitle(mTitle);

        if (mCache.get(mGotoPage) != null) {

            final int gotoFloor = mGotoFloor;
            final String gotoPostId = mGotoPostId;
            final int gotoPage = mGotoPage;
            mGotoPostId = null;
            mGotoFloor = -1;
            mGotoPage = -1;

            mDetailAdapter.addDatas(mCache.get(gotoPage));

            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    int position = -1;
                    if (gotoFloor == LAST_FLOOR_OF_PAGE) {
                        position = mDetailAdapter.getPositionByFloor(mCache.getLastFloorOfPage(gotoPage));
                    } else if (gotoFloor == FIRST_FLOOR_OF_PAGE) {
                        position = mDetailAdapter.getPositionByFloor(mCache.getFirstFloorOfPage(gotoPage));
                    } else if (gotoFloor != -1) {
                        position = mDetailAdapter.getPositionByFloor(gotoFloor);
                    } else if (HiUtils.isValidId(gotoPostId)) {
                        position = mDetailAdapter.getPositionByPostId(gotoPostId);
                        blinkItemView(gotoPostId);
                    }

                    if (position >= 0) {
                        mRecyclerView.scrollToPosition(position);
                    }

                    if (mPendingBlinkFloor > 0) {
                        int pos = mDetailAdapter.getPositionByFloor(mPendingBlinkFloor);
                        DetailBean detailBean = mDetailAdapter.getItem(pos);
                        if (detailBean != null)
                            blinkItemView(detailBean.getPostId());
                        mPendingBlinkFloor = 0;
                    }
                }
            });

            showMainFab();

            if (gotoPage == 1) {
                mRecyclerView.setHeaderState(XHeaderView.STATE_HIDDEN);
            }
            if (gotoPage == mMaxPage) {
                mRecyclerView.setFooterState(XFooterView.STATE_END);
            }
//            else {
//                mRecyclerView.setFooterState(XFooterView.STATE_HIDDEN);
//            }

        } else {
            int fetchType = FETCH_NORMAL;
            if (refresh || mGotoPage == mMaxPage || mGotoPage == LAST_PAGE) {
                fetchType = FETCH_REFRESH;
            }
            mInloading = true;
            mLoadingView.setState(ContentLoadingView.LOAD_NOW);
            startJob(mGotoPage, fetchType, POSITION_NORMAL);
        }
    }

    private void showMainFab() {
        if (mMainFab != null
                && mMainFab.getVisibility() != View.VISIBLE
                && mQuickReply.getVisibility() != View.VISIBLE)
            mMainFab.show();
    }

    private class AvatarOnClickListener extends OnSingleClickListener {
        @Override
        public void onSingleClick(View view) {
            String uid = (String) view.getTag(R.id.avatar_tag_uid);
            String username = (String) view.getTag(R.id.avatar_tag_username);
            FragmentUtils.showUserInfoActivity(getActivity(), false, uid, username);
        }
    }

    private class WarningOnClickListener extends OnSingleClickListener {
        @Override
        public void onSingleClick(View view) {
            if (view.getTag() != null && HiUtils.isValidId(view.getTag().toString())) {
                OkHttpHelper.getInstance().asyncGet(
                        HiUtils.UserWarningUrl.replace("{tid}", mTid).replace("{uid}", view.getTag().toString()),
                        new OkHttpHelper.ResultCallback() {
                            @Override
                            public void onError(Request request, Exception e) {
                                UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                            }

                            @Override
                            public void onResponse(String response) {
                                try {
                                    Document doc = Jsoup.parse(response);
                                    String title = doc.select("h3.float_ctrl").text();
                                    StringBuilder sb = new StringBuilder();
                                    Elements trES = doc.select("div.floatwrap table.list tbody tr");
                                    for (int i = 0; i < trES.size(); i++) {
                                        Element tr = trES.get(i);
                                        sb.append(tr.text()).append("\n");
                                    }
                                    sb.append("\n\n");
                                    sb.append(doc.select("div.moreconf").text());
                                    UIUtils.showMessageDialog(mCtx, title, sb.toString(), false);
                                } catch (Exception e) {
                                    UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                                }
                            }
                        });
            }
        }
    }

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        int firstVisiblesItem, lastVisibleItem, visibleItemCount, totalItemCount;
        long lastFetchNextTime, lastFetchPreTime;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            firstVisiblesItem = mLayoutManager.findFirstVisibleItemPosition();
            lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            if (dy > 0) {
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();

            } else if (dy < 0) {

            }

            int beginPage = 0;
            int endPage = 0;
            DetailBean firstBean = mDetailAdapter.getItem(firstVisiblesItem);
            DetailBean lastBean = mDetailAdapter.getItem(lastVisibleItem);
            if (firstBean == null)
                firstBean = mDetailAdapter.getItem(firstVisiblesItem + 1);
            if (lastBean == null)
                lastBean = mDetailAdapter.getItem(lastVisibleItem - 1);
            if (firstBean != null)
                beginPage = firstBean.getPage();
            if (lastBean != null)
                endPage = lastBean.getPage();
            if (beginPage != mViewBeginPage || endPage != mViewEndPage) {
                mViewBeginPage = beginPage;
                mViewEndPage = endPage;
                updatePageLabel();
            }

            long now = System.currentTimeMillis();
            if ((visibleItemCount + firstVisiblesItem) >= totalItemCount - 3) {
                if (!mFooterLoading && now > lastFetchNextTime + 500 && mViewEndPage < mMaxPage) {
                    lastFetchNextTime = now;
                    prefetchNextPage();
                }
            }
            if (!mHeaderLoading && now > lastFetchPreTime + 500 && firstVisiblesItem < 3 && mViewBeginPage > 1) {
                lastFetchPreTime = now;
                prefetchPreviousPage();
            }

        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && HiSettingsHelper.getInstance().isFabAutoHide()
                    && mRecyclerView.isNearBottom()) {
                showMainFab();
            }
        }
    }

    private void updatePageLabel() {
        if (mViewBeginPage > 0 && mMaxPage > 0) {
            mPageLabel.setText(mViewEndPage + " / " + mMaxPage);
            if (mPageLabel.getVisibility() != View.VISIBLE) {
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
                anim.reset();
                mPageLabel.clearAnimation();
                mPageLabel.startAnimation(anim);
                mPageLabel.setVisibility(View.VISIBLE);
            }
        } else {
            mPageLabel.setText("");
            if (mPageLabel.getVisibility() != View.INVISIBLE) {
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                anim.reset();
                mPageLabel.clearAnimation();
                mPageLabel.startAnimation(anim);
                mPageLabel.setVisibility(View.INVISIBLE);
            }
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
        return hideQuickReply(true);
    }

    private void scrollPostForReply(final int newTop) {
        if (mPendingScrollPostId != null && mQuickReplyToPost != null) {
            int pos = mDetailAdapter.getPositionByPostId(mPendingScrollPostId);
            mPendingScrollPostId = null;
            if (pos != -1 && mQuickReply.getVisibility() == View.VISIBLE) {
                View v = mLayoutManager.getChildAt(0);
                TextView tv = (TextView) v.findViewById(R.id.floor);
                if (tv == null || Utils.parseInt(tv.getText().toString()) != mQuickReplyToPost.getFloor()) {
                    //minus height of quick reply view
                    int replyTop = newTop - 30;
                    View view = mLayoutManager.findViewByPosition(pos);
                    if (view != null) {
                        //post view is visable
                        View rootView = ((ThreadDetailActivity) getActivity()).getRootView();
                        int postTop = UIUtils.getRelativeTop(view, (ViewGroup) rootView);
                        int scroll = postTop - replyTop + view.getHeight();
                        if (scroll > 0)
                            mRecyclerView.smoothScrollBy(0, scroll);
                    } else if (mPostViewTop > 0 && mPostViewHeight > 0) {
                        //post view is not visable, get stored position
                        int scroll = mPostViewTop - replyTop + mPostViewHeight;
                        if (scroll > 0)
                            mRecyclerView.smoothScrollBy(0, scroll);
                    } else {
                        mRecyclerView.smoothScrollToPosition(pos);
                    }
                }
            }
            mPostViewHeight = -1;
            mPostViewTop = -1;
        }
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
                if (mDetailAdapter.getDataCount() == 0) {
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

            mCache.put(details);

            if (event.mLoadingPosition == POSITION_HEADER) {
                mHeaderLoading = false;
                mRecyclerView.setHeaderState(XHeaderView.STATE_HIDDEN);
                if (details.getPage() == mViewBeginPage - 1) {
                    mRecyclerView.post(() -> mDetailAdapter.addDatas(details));
                }
            } else if (event.mLoadingPosition == POSITION_FOOTER) {
                mFooterLoading = false;
                if (details.getPage() == mMaxPage)
                    mRecyclerView.setFooterState(XFooterView.STATE_END);
                if (event.mFectchType == FETCH_NEXT) {
                    mRecyclerView.setFooterState(details.getPage() < mMaxPage ? XFooterView.STATE_READY : XFooterView.STATE_END);
                }
                mRecyclerView.post(() -> mDetailAdapter.addDatas(details));
            } else {
                mInloading = false;
                mLoadingView.setState(ContentLoadingView.CONTENT);
            }

            if (event.mFectchType == FETCH_NORMAL || event.mFectchType == FETCH_REFRESH) {
                if (!mDataReceived) {
                    mDataReceived = true;
                    showMainFab();
                }
                mGotoPage = details.getPage();
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
                HistoryDao.saveHistoryInBackground(mTid, String.valueOf(mFid), mTitle, uid, username, postTime);
            }
        }

        @Override
        public void onFailRelogin(ThreadDetailEvent event) {
            mInloading = false;
            mDetailAdapter.clear();
            mLoadingView.setState(ContentLoadingView.NOT_LOGIN);
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
            if (event.fromQuickReply) {
                mFooterLoading = true;
                mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                hideQuickReply(false);
                mMainFab.hide();
            } else {
                postProgressDialog = HiProgressDialog.show(mCtx, "请稍候...");
            }
        } else if (event.mStatus == Constants.STATUS_SUCCESS) {
            mEtReply.setText("");
            hideQuickReply(true);

            if (postProgressDialog != null) {
                postProgressDialog.dismiss(message);
            }
            if (event.fromQuickReply)
                mFooterLoading = false;

            mGotoFloor = postResult.getFloor();
            DetailListBean details = postResult.getDetailListBean();
            if (details != null)
                mCache.put(details);

            if (postResult.isDelete()) {
                if (mGotoFloor == 1) {
                    //re-post event to thread list
                    event.mSessionId = "";
                    EventBus.getDefault().postSticky(event);
                    //first floor is deleted, meaning whole thread is deleted
                    ((ThreadDetailActivity) getActivity()).finishWithNoSlide();
                } else {
                    showOrLoadPage(true);
                }
            } else if (isInAuthorOnlyMode() && event.mMode != PostHelper.MODE_EDIT_POST) {
                mCache.clear();
                mDetailAdapter.clear();
                if (details != null) {
                    mCache.put(details);
                }
                mGotoPage = LAST_PAGE;
                mGotoFloor = LAST_FLOOR_OF_PAGE;
                mAuthorId = "";
                mShowAllMenuItem.setVisible(false);
                showOrLoadPage(true);
            } else {
                if (details != null)
                    mDetailAdapter.addDatas(details);
                if (event.mMode == PostHelper.MODE_EDIT_POST) {
                    String postId = postResult.getPid();
                    blinkItemView(postId);
                } else {
                    mGotoPage = mMaxPage;
                    mGotoFloor = LAST_FLOOR_OF_PAGE;
                    showOrLoadPage(false);
                }
            }
        } else {
            if (event.fromQuickReply) {
                mFooterLoading = false;
                if (!TextUtils.isEmpty(mEtReply.getText())) {
                    ContentDao.saveContent(mSessionId, mEtReply.getText().toString());
                }
                showQuickReply();
                mRecyclerView.setFooterState(XFooterView.STATE_ERROR);
            }
            if (postProgressDialog != null) {
                postProgressDialog.dismissError(message);
            } else {
                UIUtils.errorSnack(getView(), message, "");
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkReadyEvent event) {
        if (!mInloading && mDetailAdapter.getDataCount() == 0) {
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
