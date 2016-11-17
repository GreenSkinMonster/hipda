package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
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
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ThreadDetailCache;
import net.jejer.hipda.db.HistoryDao;
import net.jejer.hipda.glide.GifTransformation;
import net.jejer.hipda.glide.GlideBitmapTarget;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageView;
import net.jejer.hipda.glide.ImageReadyInfo;
import net.jejer.hipda.glide.ThreadImageDecoder;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.job.PostEvent;
import net.jejer.hipda.job.PostJob;
import net.jejer.hipda.job.ThreadDetailEvent;
import net.jejer.hipda.job.ThreadDetailJob;
import net.jejer.hipda.ui.adapter.RecyclerItemClickListener;
import net.jejer.hipda.ui.adapter.ThreadDetailAdapter;
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
    public static final String ARG_TITLE_KEY = "title";
    public static final String ARG_FLOOR_KEY = "floor";
    public static final String ARG_PAGE_KEY = "page";
    public static final String ARG_MAX_PAGE_KEY = "maxPage";

    public static final int LAST_FLOOR = Integer.MIN_VALUE;
    public static final int LAST_PAGE = Integer.MIN_VALUE;

    private Context mCtx;
    private String mTid;
    private String mGotoPostId;
    private String mTitle;
    private String mFid;
    private XRecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ThreadDetailAdapter mDetailAdapter;
    private ThreadDetailCache mCache = new ThreadDetailCache();
    private List<DetailBean> mDetailBeans = new ArrayList<>();

    private int mMaxImageDecodeWidth = ImageSizeUtils.NORMAL_IMAGE_DECODE_WIDTH;

    private int mCurrentPage = 1;
    private int mMaxPage = 0;
    private int mGoToPage = 1;
    private int mMaxPostInPage = HiSettingsHelper.getInstance().getMaxPostsInPage();    // user can configure max posts per page in forum setting
    private int mFloorOfPage = -1;    // for every page start form 1
    private boolean mInloading = false;
    private boolean mPrefetching = false;
    private EmojiEditText mEtReply;
    private ImageButton mIbEmojiSwitch;
    private View quickReply;
    private boolean mAuthorOnly = false;
    private boolean mDataReceived = false;

    public final static int FETCH_NORMAL = 0;
    public final static int FETCH_NEXT = 1;
    public final static int FETCH_PREVIOUS = 2;
    public final static int FETCH_REFRESH = 3;
    public final static int FETCH_SILENT = 4;

    private HiProgressDialog postProgressDialog;
    private ContentLoadingProgressBar mLoadingProgressBar;

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
            mFloorOfPage = getArguments().getInt(ARG_FLOOR_KEY);
        }
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
        EventBus.getDefault().register(mDetailAdapter);

        mRecyclerView.setXRecyclerListener(new XRecyclerView.XRecyclerListener() {
            @Override
            public void onLoadPrevious() {
                mCurrentPage--;
                mFloorOfPage = LAST_FLOOR;
                showOrLoadPage();
            }

            @Override
            public void onLoadNext() {
                mCurrentPage++;
                mFloorOfPage = 0;
                showOrLoadPage();
            }

            @Override
            public void atEnd() {
                mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
                mFloorOfPage = LAST_FLOOR;
                refresh();
            }
        });

        mLoadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.detail_loading);

        quickReply = view.findViewById(R.id.quick_reply);
        mEtReply = (EmojiEditText) quickReply.findViewById(R.id.tv_reply_text);
        mEtReply.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());

        ImageButton mPostReplyIb = (ImageButton) quickReply.findViewById(R.id.ib_reply_post);
        mPostReplyIb.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_send).sizeDp(28).color(Color.GRAY));
        mPostReplyIb.setOnClickListener(new View.OnClickListener() {
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
                    ((MainFrameActivity) getActivity()).getMainFab().show();
                }
            }
        });
        mPostReplyIb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setHasOptionsMenu(false);
                String replyText = mEtReply.getText().toString();
                showPost(replyText);
                hideQuickReply();
                return true;
            }
        });

        mIbEmojiSwitch = (ImageButton) quickReply.findViewById(R.id.ib_goto_post);
        setUpEmojiPopup(mEtReply, mIbEmojiSwitch);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCtx = getActivity();
        }
        showOrLoadPage();
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
                mLoadingProgressBar.hide();
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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem favoritesMenuItem = menu.findItem(R.id.action_add_favorite);
        if (favoritesMenuItem == null)
            return;
        if (FavoriteHelper.getInstance().isInFavortie(mTid)) {
            favoritesMenuItem.setTitle(R.string.action_remove_favorite);
        } else {
            favoritesMenuItem.setTitle(R.string.action_add_favorite);
        }

        MenuItem attentionMenuItem = menu.findItem(R.id.action_add_attention);
        if (FavoriteHelper.getInstance().isInAttention(mTid)) {
            attentionMenuItem.setTitle(R.string.action_remove_attention);
        } else {
            attentionMenuItem.setTitle(R.string.action_add_attention);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
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
                mLoadingProgressBar.showNow();
                refresh();
                return true;
            case R.id.action_image_gallery:
                startImageGallery(0);
                return true;
            case R.id.action_only_author:
                mAuthorOnly = !mAuthorOnly;
                mDetailBeans.clear();
                mDetailAdapter.setDatas(mDetailBeans);
                mCurrentPage = 1;
                if (mAuthorOnly) {
                    setActionBarTitle("(只看楼主) " + mTitle);
                    showAndLoadAuthorOnly();
                } else {
                    showOrLoadPage();
                }
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    void setupFab() {
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
                quickReply.setVisibility(View.VISIBLE);
                quickReply.bringToFront();
                mMainFab.hide();
                mMainFab.setEnabled(false);
                (new Handler()).postDelayed(new Runnable() {
                    public void run() {
                        mEtReply.requestFocus();
                        mEtReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                        mEtReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                    }
                }, 100);
            }
        });

        mNotificationFab.setEnabled(false);
        mNotificationFab.hide();
    }

    private void showPost(String text) {
        Bundle arguments = new Bundle();
        arguments.putString(PostFragment.ARG_TID_KEY, mTid);
        arguments.putInt(PostFragment.ARG_MODE_KEY, PostHelper.MODE_REPLY_THREAD);
        arguments.putString(PostFragment.ARG_TEXT_KEY, text);
        PostFragment fragment = new PostFragment();
        fragment.setParentSessionId(mSessionId);

        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .add(R.id.main_frame_container, fragment, fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    private void refresh() {
        mInloading = true;
        ThreadDetailJob job = new ThreadDetailJob(mCtx, mSessionId, mTid, mGotoPostId, mCurrentPage, FETCH_REFRESH);
        JobMgr.addJob(job);
    }

    public void showTheadTitle() {
        Toast.makeText(mCtx, mTitle, Toast.LENGTH_SHORT).show();
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
            DetailBean detailBean = mDetailAdapter.getItem(position);
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        JobMgr.cancelJobs(mSessionId);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(mDetailAdapter);
        super.onDestroyView();
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

    private void prefetchNextPage() {
        if (mCurrentPage < mMaxPage) {
            if (mCache.get(mCurrentPage + 1) == null) {
                prefetchPage(mCurrentPage + 1, FETCH_NEXT);
                mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
            } else {
                mRecyclerView.setFooterState(XFooterView.STATE_READY);
            }
        }
    }

    private void prefetchPreviousPage() {
        if (mCurrentPage > 1) {
            if (mCache.get(mCurrentPage - 1) == null) {
                prefetchPage(mCurrentPage - 1, FETCH_PREVIOUS);
                mRecyclerView.setHeaderState(XHeaderView.STATE_LOADING);
            } else {
                mRecyclerView.setHeaderState(XHeaderView.STATE_READY);
            }
        }
    }

    private void prefetchPage(int page, int fetchType) {
        if (!mPrefetching && mCache.get(page) == null) {
            mPrefetching = true;
            if (page < 1 || page > mMaxPage)
                return;
            ThreadDetailJob job = new ThreadDetailJob(mCtx, mSessionId, mTid, mGotoPostId, page, fetchType);
            JobMgr.addJob(job);
        }
    }

    private void showGotoPageDialog() {
        if (mAuthorOnly) {
            Toast.makeText(getActivity(), "请先退出只看楼主模式", Toast.LENGTH_LONG).show();
            return;
        }
        mGoToPage = mCurrentPage;
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_goto_page, null);
        final ImageButton btnFirstPage = (ImageButton) viewlayout.findViewById(R.id.btn_fisrt_page);
        final ImageButton btnLastPage = (ImageButton) viewlayout.findViewById(R.id.btn_last_page);
        final ImageButton btnNextPage = (ImageButton) viewlayout.findViewById(R.id.btn_next_page);
        final ImageButton btnPreviousPage = (ImageButton) viewlayout.findViewById(R.id.btn_previous_page);
        final SeekBar sbGotoPage = (SeekBar) viewlayout.findViewById(R.id.sb_page);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog dialog;

        btnFirstPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_fast_backward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnLastPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_fast_forward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnNextPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_step_forward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));
        btnPreviousPage.setImageDrawable(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_step_backward).sizeDp(24).color(ColorHelper.getColorAccent(getActivity())));

        builder.setTitle("第 " + String.valueOf(mGoToPage) + " / " + (mMaxPage) + " 页");
        builder.setView(viewlayout);

        builder.setPositiveButton(getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCurrentPage = mGoToPage;
                        showOrLoadPage();
                    }
                });
        builder.setNegativeButton(getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        builder.setNeutralButton("页尾",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopScroll();
                        mRecyclerView.scrollToBottom();
                        prefetchNextPage();
                    }
                });
        dialog = builder.create();

        // Fuck Android SeekBar, always start from 0
        sbGotoPage.setMax(mMaxPage - 1);
        sbGotoPage.setProgress(mCurrentPage - 1);
        sbGotoPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGoToPage = progress + 1; //start from 0
                dialog.setTitle("第 " + String.valueOf(mGoToPage) + " / " + (mMaxPage) + " 页");
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
                showOrLoadPage();
                dialog.dismiss();
            }
        });

        btnLastPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentPage = mMaxPage;
                mFloorOfPage = LAST_FLOOR;
                showOrLoadPage();
                dialog.dismiss();
            }
        });

        btnNextPage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentPage < mMaxPage) {
                    mCurrentPage++;
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
                    showOrLoadPage();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public boolean hideQuickReply() {
        if (quickReply != null && quickReply.getVisibility() == View.VISIBLE) {
            mEtReply.setText("");
            quickReply.setVisibility(View.INVISIBLE);
            mMainFab.setEnabled(true);
            mMainFab.show();
            return true;
        }
        return false;
    }

    public class GoToFloorOnClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            mAuthorOnly = false;

            int floor = (Integer) view.getTag();
            gotoFloor(floor);
        }
    }

    public void gotoFloor(int floor) {
        mGoToPage = (floor - 1) / mMaxPostInPage + 1; // page start from 1

        mFloorOfPage = floor % mMaxPostInPage; // floor start from 1
        if (mFloorOfPage == 0) {
            mFloorOfPage = mMaxPostInPage;
        }

        if (mGoToPage != mCurrentPage) {
            mCurrentPage = mGoToPage;
            showOrLoadPage();
        } else {
            mRecyclerView.scrollToPosition(mFloorOfPage + mDetailAdapter.getHeaderCount() - 1);
            mFloorOfPage = -1;
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

            int position = 0;
            if (mFloorOfPage == LAST_FLOOR) {
                position = mDetailAdapter.getItemCount() - 1;
            } else {
                if (!TextUtils.isEmpty(mGotoPostId)) {
                    position = mDetailAdapter.getPositionByPostId(mGotoPostId);
                } else if (mFloorOfPage >= 0) {
                    position = mDetailAdapter.getPositionByFloor(mFloorOfPage);
                }
                if (position < 0)
                    position = 0;
            }
            mRecyclerView.scrollToPosition(position);
            mGotoPostId = null;
            mFloorOfPage = -1;

            if (mCurrentPage > 1 && position < 5) {
                prefetchPreviousPage();
            }
            if (mCurrentPage < mMaxPage && position > mDetailAdapter.getItemCount() - 5) {
                prefetchNextPage();
            }
        } else {
            int fetchType = FETCH_NORMAL;
            if (refresh || mCurrentPage == mMaxPage || mCurrentPage == LAST_PAGE) {
                fetchType = FETCH_REFRESH;
            }
            mInloading = true;
            ThreadDetailJob job = new ThreadDetailJob(mCtx, mSessionId, mTid, mGotoPostId, mCurrentPage, fetchType);
            JobMgr.addJob(job);
        }
    }

    private void addAuthorPosts(DetailListBean details) {
        for (DetailBean detail : details.getAll()) {
            if (detail.getAuthor().equals(mCache.get(1).getAll().get(0).getAuthor())) {
                mDetailBeans.add(detail);
            }
        }
        mDetailAdapter.setDatas(mDetailBeans);
    }

    private void showAndLoadAuthorOnly() {
        while (mCache.get(mCurrentPage) != null && mCurrentPage <= mMaxPage) {
            addAuthorPosts(mCache.get(mCurrentPage));
            mCurrentPage++;
        }

        if (mCurrentPage <= mMaxPage) {
            mInloading = true;
            ThreadDetailJob job = new ThreadDetailJob(mCtx, mSessionId, mTid, mGotoPostId, mCurrentPage, FETCH_NORMAL);
            JobMgr.addJob(job);
        }
    }

    public void loadImage(String imageUrl, GlideImageView giv) {
        if (mCtx == null || giv == null)
            return;
        if (Build.VERSION.SDK_INT >= 17
                && (mCtx instanceof Activity)
                && ((Activity) mCtx).isDestroyed())
            return;
        if (!isAdded() || isDetached())
            return;

        ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(imageUrl);

        if (imageReadyInfo != null && imageReadyInfo.isReady()) {
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) giv.getLayoutParams();
            layoutParams.width = imageReadyInfo.getDisplayWidth();
            layoutParams.height = imageReadyInfo.getDisplayHeight();
            if (imageReadyInfo.getDisplayWidth() > GlideImageView.MIN_SCALE_WIDTH
                    || imageReadyInfo.isGif()) {
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                giv.setImageReadyInfo(imageReadyInfo);
                giv.setClickToViewBigImage();
            }

            if (GlideHelper.isOkToLoad(ThreadDetailFragment.this)) {
                if (imageReadyInfo.isGif()) {
                    Glide.with(ThreadDetailFragment.this)
                            .load(imageUrl)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transform(new GifTransformation(mCtx))
                            .into(new GlideBitmapTarget(giv, imageReadyInfo.getDisplayWidth(), imageReadyInfo.getDisplayHeight()));
                } else {
                    Glide.with(ThreadDetailFragment.this)
                            .load(imageUrl)
                            .asBitmap()
                            .cacheDecoder(new FileToStreamDecoder<>(new ThreadImageDecoder(mMaxImageDecodeWidth, imageReadyInfo)))
                            .imageDecoder(new ThreadImageDecoder(mMaxImageDecodeWidth, imageReadyInfo))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new GlideBitmapTarget(giv, imageReadyInfo.getDisplayWidth(), imageReadyInfo.getDisplayHeight()));
                }
            }
        } else {
            giv.setImageResource(R.drawable.image_broken);
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
                    if (!mPrefetching && mCurrentPage < mMaxPage) {
                        prefetchNextPage();
                    }
                }
            } else if (dy < 0) {
                if (!mPrefetching && firstVisiblesItem < 3 && mCurrentPage > 1) {
                    prefetchPreviousPage();
                }
            }
        }
    }

    public void startImageGallery(int imageIndex) {
        if (!HiApplication.isActivityVisible()) {
            return;
        }
        if (mAuthorOnly) {
            Toast.makeText(getActivity(), "请先退出只看楼主模式", Toast.LENGTH_LONG).show();
            return;
        }
        DetailListBean detailListBean = mCache.get(mCurrentPage);
        if (detailListBean == null) {
            Toast.makeText(getActivity(), "帖子还未加载完成", Toast.LENGTH_LONG).show();
            return;
        }
        if (detailListBean.getContentImages().size() > 0) {
            PopupImageDialog popupImageDialog = new PopupImageDialog();
            popupImageDialog.init(detailListBean, imageIndex, mSessionId);
            popupImageDialog.show(getFragmentManager(), PopupImageDialog.class.getName());
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
            } else if (quickReply.getVisibility() == View.VISIBLE) {
                mEtReply.setText("");
                quickReply.setVisibility(View.INVISIBLE);
            }

            if (postProgressDialog != null) {
                postProgressDialog.dismiss(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_SHORT).show();
            }

            int floor = postResult.getFloor();
            if (floor == LAST_FLOOR || floor > 0)
                mFloorOfPage = floor;

            if (postResult.getDelete() == 1) {
                //delete post
                if (floor == 1) {
                    String fid = postResult.getFid();
                    FragmentUtils.showForum(getFragmentManager(), HiUtils.isValidId(fid) ? Integer.parseInt(fid) : 0);
                } else {
                    mCache.remove(mCurrentPage);
                    showOrLoadPage(true);
                }
            } else {
                //edit post
                if (!mAuthorOnly) {
                    if (event.mMode != PostHelper.MODE_EDIT_POST) {
                        mCurrentPage = mMaxPage;
                        mFloorOfPage = LAST_FLOOR;
                    }
                    mCache.remove(mCurrentPage);
                    showOrLoadPage(true);
                }
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

        //in progress
        if (event.mStatus == Constants.STATUS_IN_PROGRESS) {
            if (event.mFectchType == FETCH_NORMAL) {
                mLoadingProgressBar.show();
            } else if (event.mFectchType == FETCH_PREVIOUS) {
                mRecyclerView.setHeaderState(XHeaderView.STATE_LOADING);
            } else if (event.mFectchType == FETCH_NEXT) {
                mRecyclerView.setFooterState(XFooterView.STATE_LOADING);
            } else if (event.mFectchType == FETCH_REFRESH) {
                mLoadingProgressBar.show();
            }
            return;
        }

        //error
        if (event.mStatus == Constants.STATUS_FAIL_RELOGIN) {
            getFragmentManager().popBackStackImmediate();
            return;
        }
        if (event.mStatus == Constants.STATUS_FAIL || event.mStatus == Constants.STATUS_FAIL_ABORT) {
            UIUtils.errorSnack(getView(), event.mMessage, event.mDetail);
            if (event.mFectchType == FETCH_PREVIOUS) {
                mPrefetching = false;
            } else if (event.mFectchType == FETCH_NEXT) {
                mPrefetching = false;
            } else {
                mInloading = false;
                mLoadingProgressBar.hide();
            }
            return;
        }

        int status = event.mStatus;
        DetailListBean details = event.mData;

        //success
        mMaxPostInPage = HiSettingsHelper.getInstance().getMaxPostsInPage();

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

        if (event.mFectchType == FETCH_PREVIOUS) {
            mPrefetching = false;
            mRecyclerView.setHeaderState(XHeaderView.STATE_READY);
        } else if (event.mFectchType == FETCH_NEXT) {
            mPrefetching = false;
            mRecyclerView.setFooterState(XFooterView.STATE_READY);
        } else {
            mInloading = false;
            mLoadingProgressBar.hide();
            if (!mDataReceived) {
                mDataReceived = true;
                mMainFab.setEnabled(true);
                mMainFab.show();
            }
            mDetailBeans = details.getAll();
            mDetailAdapter.setDatas(mDetailBeans);
            if (event.mFectchType == FETCH_NORMAL || event.mFectchType == FETCH_REFRESH) {
                mCurrentPage = details.getPage();
            }

            if (mCurrentPage == LAST_PAGE) {
                mCurrentPage = mMaxPage;
            }
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

}
