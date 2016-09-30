package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.GestureDetector;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiEditText;

import net.jejer.hipda.BuildConfig;
import net.jejer.hipda.R;
import net.jejer.hipda.async.DetailListLoader;
import net.jejer.hipda.async.FavoriteHelper;
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
    private XListView mDetailListView;
    private ThreadListLoaderCallbacks mLoaderCallbacks;
    private ThreadDetailAdapter mDetailAdapter;
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
    private Handler mMsgHandler;
    private boolean mAuthorOnly = false;
    private ThreadDetailCache mCache = new ThreadDetailCache();
    public static final String LOADER_PAGE_KEY = "LOADER_PAGE_KEY";
    public static final String LOADER_REFRESH_KEY = "LOADER_REFRESH_KEY";

    private HiProgressDialog postProgressDialog;
    private FloatingActionMenu mFam;
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
        mLoaderCallbacks = new ThreadListLoaderCallbacks();
        mDetailAdapter = new ThreadDetailAdapter(mCtx, this,
                new GoToFloorOnClickListener(), new AvatarOnClickListener());

        EventBus.getDefault().register(mDetailAdapter);

        mMsgHandler = new Handler(new ThreadDetailMsgHandler());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thread_detail, container, false);

        mDetailListView = (XListView) view.findViewById(R.id.lv_thread_details);
        mDetailListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mFam = (FloatingActionMenu) view.findViewById(R.id.multiple_actions);
        mFam.setVisibility(View.INVISIBLE);

        mLoadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.detail_loading);

        FloatingActionButton fabRefresh = (FloatingActionButton) view.findViewById(R.id.action_fab_refresh);
        fabRefresh.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_refresh).color(Color.WHITE).sizeDp(FAB_ICON_SIZE_DP));
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(true);
                mLoadingProgressBar.showNow();
                mFloorOfPage = LAST_FLOOR;
                refresh();
            }
        });

        FloatingActionButton fabQuickReply = (FloatingActionButton) view.findViewById(R.id.action_fab_quick_reply);
        fabQuickReply.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_reply).color(Color.WHITE).sizeDp(FAB_ICON_SIZE_DP));
        fabQuickReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(false);
                mFam.setVisibility(View.INVISIBLE);
                quickReply.setVisibility(View.VISIBLE);
                quickReply.bringToFront();
                (new Handler()).postDelayed(new Runnable() {
                    public void run() {
                        mEtReply.requestFocus();
                        mEtReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                        mEtReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                    }
                }, 100);
            }
        });

        FloatingActionButton fabGotoPage = (FloatingActionButton) view.findViewById(R.id.action_fab_goto_page);
        fabGotoPage.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_swap_horiz).color(Color.WHITE).sizeDp(FAB_ICON_SIZE_DP));
        fabGotoPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(true);
                showGotoPageDialog();
            }
        });

        mDetailListView.setPullLoadEnable(false, mCurrentPage == mMaxPage);
        mDetailListView.setPullRefreshEnable(false, mCurrentPage == 1 ? mTitle : null);
        mDetailListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                //Previous Page
                if (mCurrentPage > 1) {
                    mCurrentPage--;
                }
                mDetailListView.stopRefresh();
                mFloorOfPage = LAST_FLOOR;
                showOrLoadPage();
                quickReply.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadMore() {
                //Next Page
                if (mCurrentPage < mMaxPage) {
                    mCurrentPage++;
                }
                mDetailListView.stopLoadMore(mCurrentPage == mMaxPage);
                showOrLoadPage();
            }
        });
        mDetailListView.hideFooter();

        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showGotoPageDialog();
                return true;
            }
        };

        final GestureDetector detector = new GestureDetector(mCtx, listener);
        detector.setOnDoubleTapListener(listener);

        mDetailListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (mFam.isOpened()) {
                    mFam.close(false);
                }
                return detector.onTouchEvent(event);
            }
        });


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
                    mFam.setVisibility(View.VISIBLE);
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
            mFam.setVisibility(View.VISIBLE);
        } else {
            mLoadingProgressBar.show();
        }

        mDetailListView.setAdapter(mDetailAdapter);
        mDetailListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDetailListView.setOnItemLongClickListener(new OnItemLongClickCallback());
        mDetailListView.setOnScrollListener(new OnScrollCallback());

        getLoaderManager().initLoader(0, new Bundle(), mLoaderCallbacks);
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
                refresh();
                mLoadingProgressBar.showNow();
                return true;
            case R.id.action_image_gallery:
                startImageGallery(0);
                return true;
            case R.id.action_only_author:
                mAuthorOnly = !mAuthorOnly;
                mDetailBeans.clear();
                mDetailAdapter.setBeans(mDetailBeans);
                mCurrentPage = 1;
                if (mAuthorOnly) {
                    mDetailListView.setPullLoadEnable(false, true);
                    mDetailListView.setPullRefreshEnable(false, mCurrentPage == 1 ? mTitle : null);
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
        Bundle b = new Bundle();
        b.putInt(LOADER_PAGE_KEY, mCurrentPage);
        b.putBoolean(LOADER_REFRESH_KEY, true);
        mInloading = true;
        getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
    }

    public void showTheadTitle() {
        Toast.makeText(mCtx, mTitle, Toast.LENGTH_SHORT).show();
    }


    public DetailBean getCachedPost(String postId) {
        return mCache.getPostByPostId(postId);
    }

    private class OnScrollCallback implements AbsListView.OnScrollListener {

        private int mLastVisibleItem;
        private long lastUpdate = System.currentTimeMillis();

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            if (!mInloading && !mPrefetching) {
                if (mLastVisibleItem < firstVisibleItem) {
                    //scroll down, prefetch next page
                    if (firstVisibleItem > Math.round(0.5f * totalItemCount)) {
                        prefetchNextPage(1);
                    }
                }
                if (mLastVisibleItem > firstVisibleItem) {
                    //scroll up, prefetch previous page
                    if (firstVisibleItem < Math.round(0.5f * totalItemCount)) {
                        prefetchNextPage(-1);
                    }
                }
            }
            long now = System.currentTimeMillis();
            if (now - 200 > lastUpdate) {
                mLastVisibleItem = firstVisibleItem;
                lastUpdate = now;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
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
    public void onDestroy() {
        getLoaderManager().destroyLoader(0);
        EventBus.getDefault().unregister(mDetailAdapter);
        if (Utils.isMemoryUsageHigh())
            Glide.get(getActivity()).clearMemory();
        super.onDestroy();
    }

    public void scrollToTop() {
        stopScroll();
        mDetailListView.setSelection(0);
        prefetchNextPage(-1);
    }

    public void stopScroll() {
        mDetailListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    public class ThreadListLoaderCallbacks implements LoaderManager.LoaderCallbacks<DetailListBean> {

        @Override
        public Loader<DetailListBean> onCreateLoader(int id, Bundle args) {
            // Re-enable after load complete if needed.
            mDetailListView.setPullLoadEnable(false, mCurrentPage == mMaxPage);
            mDetailListView.setPullRefreshEnable(false, mCurrentPage == 1 ? mTitle : null);

            return new DetailListLoader(mCtx, mMsgHandler, mTid, mGotoPostId,
                    args.getInt(LOADER_PAGE_KEY, 1),
                    args.getBoolean(LOADER_REFRESH_KEY, false));
        }

        @Override
        public void onLoadFinished(Loader<DetailListBean> loader, final DetailListBean details) {
            mInloading = false;
            mPrefetching = false;
            mLoadingProgressBar.hide();

            mMaxPostInPage = HiSettingsHelper.getInstance().getMaxPostsInPage();
            mFam.setVisibility(View.VISIBLE);

            if (details == null) {
                // May be login error, error message should be populated in login async task
                return;
            } else if (details.getCount() == 0) {
                // Page load fail.

                Message msgError = Message.obtain();
                msgError.what = ThreadListFragment.STAGE_ERROR;
                Bundle b = new Bundle();
                b.putString(ThreadListFragment.STAGE_ERROR_KEY, "页面加载失败");
                msgError.setData(b);
                mMsgHandler.sendMessage(msgError);

                return;
            }

            // Set title
            if (details.getTitle() != null && !details.getTitle().isEmpty()) {
                mTitle = details.getTitle();
            }

            mFid = details.getFid();
            if (TextUtils.isEmpty(mTid))
                mTid = details.getTid();

            // Set MaxPage earlier than showOrLoadPage()
            mMaxPage = details.getLastPage();

            //go to specific post id
            if (!TextUtils.isEmpty(mGotoPostId)) {
                mCurrentPage = details.getPage();
                DetailBean detailBean = details.getPostInPage(mGotoPostId);

                //there is bug in search full text, given post id could'n found in thread
                int floor = 1;
                if (detailBean != null)
                    floor = Integer.parseInt(detailBean.getFloor());

                mFloorOfPage = floor % mMaxPostInPage; // floor start from 1
                if (mFloorOfPage == 0) {
                    mFloorOfPage = mMaxPostInPage;
                }
                mGotoPostId = null;
            } else if (mCurrentPage == LAST_PAGE) {
                mCurrentPage = mMaxPage;
            }

            mCache.put(details.getPage(), details);

            //set image's decode/display size base on image count
            mMaxImageDecodeWidth = ImageSizeUtils.getDecodeSize(details.getImagesCount());

            if (!mAuthorOnly && mCurrentPage == details.getPage()) {
                showOrLoadPage();
            } else if (mAuthorOnly) {
                showAndLoadAuthorOnly();
            }

            setPullLoadStatus();

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
        public void onLoaderReset(Loader<DetailListBean> arg0) {
            mInloading = false;
            mPrefetching = false;
            mLoadingProgressBar.hide();
        }

    }

    private void prefetchNextPage(int pageOffset) {
        if (!mPrefetching && !mAuthorOnly
                && mCache.get(mCurrentPage + pageOffset) == null) {
            int page = mCurrentPage + pageOffset;
            if (page < 1 || page > mMaxPage)
                return;
            if (pageOffset > 0)
                mDetailListView.setFooterLoading();
            else
                mDetailListView.setHeaderLoading(true);

            Bundle b = new Bundle();
            b.putInt(LOADER_PAGE_KEY, page);
            mPrefetching = true;
            getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
        }
    }

    private void setPullLoadStatus() {
        if (mAuthorOnly) {
            mDetailListView.setPullLoadEnable(false, mCurrentPage == mMaxPage);
            mDetailListView.setPullRefreshEnable(false, mCurrentPage == 1 ? mTitle : null);
        } else {
            if (mCurrentPage == 1) {
                mDetailListView.setPullRefreshEnable(false, mCurrentPage == 1 ? mTitle : null);
            } else {
                mDetailListView.setPullRefreshEnable(true, mCurrentPage == 1 ? mTitle : null);
            }
            if (mCurrentPage == mMaxPage) {
                mDetailListView.setPullLoadEnable(false, mCurrentPage == mMaxPage);
            } else {
                mDetailListView.setPullLoadEnable(true, mCurrentPage == mMaxPage);
            }
        }
        mDetailListView.showFooter();
        mDetailListView.setHeaderLoading(false);
    }

    private class OnItemLongClickCallback implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mDetailListView.setItemChecked(position, true);

            position = position - mDetailListView.getHeaderViewsCount();
            if (position < 0 || position >= mDetailAdapter.getCount()) {
                return false;
            }

            ThreadDetailActionModeCallback cb = new ThreadDetailActionModeCallback(ThreadDetailFragment.this,
                    mFid, mTid, mTitle, mDetailAdapter.getItem(position));
            ((AppCompatActivity) getActivity()).startSupportActionMode(cb);

            return true;
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
            mFam.setVisibility(View.VISIBLE);
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
            mDetailListView.setSelection(mFloorOfPage + mDetailListView.getHeaderViewsCount() - 1);
            mFloorOfPage = -1;
        }
    }

    private class ThreadDetailMsgHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case ThreadListFragment.STAGE_ERROR:
                    Bundle b = msg.getData();
                    UIUtils.errorSnack(getView(),
                            b.getString(ThreadListFragment.STAGE_ERROR_KEY),
                            b.getString(ThreadListFragment.STAGE_DETAIL_KEY));
                    break;
            }
            return false;
        }
    }

    private void showOrLoadPage() {
        showOrLoadPage(false);
    }

    private void showOrLoadPage(boolean refresh) {
        setActionBarTitle((mCurrentPage > 0 && mMaxPage > 0 ? "(" + mCurrentPage + "/" + mMaxPage + ") " : "")
                + mTitle);

        if (mCache.get(mCurrentPage) != null) {
            mDetailBeans.clear();
            mDetailBeans.addAll(mCache.get(mCurrentPage).getAll());
            mDetailAdapter.setBeans(mDetailBeans);

            if (mFloorOfPage == LAST_FLOOR) {
                mDetailListView.setSelection(mDetailAdapter.getCount() - 1 + mDetailListView.getHeaderViewsCount());
            } else if (mFloorOfPage >= 0) {
                mDetailListView.setSelection(mFloorOfPage + mDetailListView.getHeaderViewsCount() - 1);
            } else {
                mDetailListView.setSelection(0);
            }

            mFloorOfPage = -1;
            mGotoPostId = null;

            //if current page loaded from cache, set prefetch flag for next page
            mPrefetching = false;

            setPullLoadStatus();
        } else {
            mLoadingProgressBar.show();
            Bundle b = new Bundle();
            b.putInt(LOADER_PAGE_KEY, mCurrentPage);
            if (refresh || mCurrentPage == mMaxPage || mCurrentPage == LAST_PAGE)
                b.putBoolean(LOADER_REFRESH_KEY, true);
            mInloading = true;
            getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
        }

    }

    private void addAuthorPosts(DetailListBean details) {
        for (DetailBean detail : details.getAll()) {
            if (detail.getAuthor().equals(mCache.get(1).getAll().get(0).getAuthor())) {
                mDetailBeans.add(detail);
            }
        }
        mDetailAdapter.setBeans(mDetailBeans);
    }

    private void showAndLoadAuthorOnly() {
        while (mCache.get(mCurrentPage) != null && mCurrentPage <= mMaxPage) {
            addAuthorPosts(mCache.get(mCurrentPage));
            mCurrentPage++;
        }

        if (mCurrentPage <= mMaxPage) {
            Bundle b = new Bundle();
            b.putInt(LOADER_PAGE_KEY, mCurrentPage);
            mInloading = true;
            getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
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

            int floor = LAST_FLOOR;
            if (!TextUtils.isEmpty(postResult.getFloor()) && TextUtils.isDigitsOnly(postResult.getFloor()))
                floor = Integer.parseInt(postResult.getFloor());
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
        EventBus.getDefault().removeStickyEvent(event);
    }

}
