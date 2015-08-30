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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.DetailListLoader;
import net.jejer.hipda.async.FavoriteHelper;
import net.jejer.hipda.async.PostAsyncTask;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.cache.ThreadDetailCache;
import net.jejer.hipda.glide.GifTransformation;
import net.jejer.hipda.glide.GlideBitmapTarget;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageView;
import net.jejer.hipda.glide.ImageReadyInfo;
import net.jejer.hipda.glide.ThreadImageDecoder;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.ImageSizeUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class ThreadDetailFragment extends BaseFragment implements PostAsyncTask.PostListener {
    public static final String ARG_TID_KEY = "tid";
    public static final String ARG_PID_KEY = "pid";
    public static final String ARG_TITLE_KEY = "title";
    public static final String ARG_FLOOR_KEY = "floor";
    public static final String ARG_PAGE_KEY = "page";

    public static final int LAST_FLOOR = Integer.MIN_VALUE;
    public static final int LAST_PAGE = Integer.MIN_VALUE;

    private Context mCtx;
    private String mTid;
    private String mGotoPostId;
    private String mTitle;
    private String mFid;
    private XListView mDetailListView;
    private TextView mTipBar;
    private TextView mTitleView;
    private ThreadListLoaderCallbacks mLoaderCallbacks;
    private ThreadDetailAdapter mDetailAdapter;
    private List<DetailBean> mDetailBeans = new ArrayList<>();

    private int mMaxImageDecodeWidth = ImageSizeUtils.NORMAL_IMAGE_DECODE_WIDTH;
    public static int MAX_VIEW_WIDTH = 1080;

    private int mCurrentPage = 1;
    private int mMaxPage = 1;
    private int mGoToPage = 1;
    private int mMaxPostInPage = HiSettingsHelper.getInstance().getMaxPostsInPage();    // user can configure max posts per page in forum setting
    private int mFloorOfPage = -1;    // for every page start form 1
    private boolean mInloading = false;
    private boolean mPrefetching = false;
    private TextView mReplyTextTv;
    private ImageButton mPostReplyIb;
    private View quickReply;
    private Handler mMsgHandler;
    private boolean mAuthorOnly = false;
    private ThreadDetailCache mCache = new ThreadDetailCache();
    public static final String LOADER_PAGE_KEY = "LOADER_PAGE_KEY";

    private HiProgressDialog postProgressDialog;
    private FloatingActionMenu mFam;
    private ContentLoadingProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.v("onCreate");
        super.onCreate(savedInstanceState);

        ((MainFrameActivity) getActivity()).registOnSwipeCallback(this);
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
        }
        if (getArguments().containsKey(ARG_FLOOR_KEY)) {
            mFloorOfPage = getArguments().getInt(ARG_FLOOR_KEY);
        }
        mLoaderCallbacks = new ThreadListLoaderCallbacks();
        mDetailAdapter = new ThreadDetailAdapter(mCtx, getFragmentManager(), this,
                new GoToFloorOnClickListener(), new AvatarOnClickListener());

        EventBus.getDefault().register(mDetailAdapter);

        mMsgHandler = new Handler(new ThreadDetailMsgHandler());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.v("onCreateView");
        View view = inflater.inflate(R.layout.fragment_thread_detail, container, false);

        mDetailListView = (XListView) view.findViewById(R.id.lv_thread_details);
        mDetailListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mTipBar = (TextView) view.findViewById(R.id.thread_detail_tipbar);
        mTipBar.setVisibility(View.INVISIBLE);
        mTipBar.bringToFront();

        mFam = (FloatingActionMenu) view.findViewById(R.id.multiple_actions);
        mFam.setVisibility(View.INVISIBLE);

        loadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.detail_loading);
        loadingProgressBar.show();

        FloatingActionButton fabRefresh = (FloatingActionButton) view.findViewById(R.id.action_fab_refresh);
        fabRefresh.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_refresh).color(Color.WHITE));
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(true);
                loadingProgressBar.showNow();
                mFloorOfPage = LAST_FLOOR;
                refresh();
            }
        });

        FloatingActionButton fabQuickReply = (FloatingActionButton) view.findViewById(R.id.action_fab_quick_reply);
        fabQuickReply.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_reply).color(Color.WHITE));
        fabQuickReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(false);
                mFam.setVisibility(View.INVISIBLE);
                quickReply.setVisibility(View.VISIBLE);
                quickReply.bringToFront();
                (new Handler()).postDelayed(new Runnable() {
                    public void run() {
                        mReplyTextTv.requestFocus();
                        mReplyTextTv.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                        mReplyTextTv.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                    }
                }, 100);
            }
        });

        FloatingActionButton fabGotoPage = (FloatingActionButton) view.findViewById(R.id.action_fab_goto_page);
        fabGotoPage.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_swap_horiz).color(Color.WHITE));
        fabGotoPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFam.close(true);
                showGotoPageDialog();
            }
        });

        if (!HiSettingsHelper.getInstance().getIsLandscape()) {
//            mDetailListView.addHeaderView(inflater.inflate(R.layout.head_thread_detail, null));
//            mTitleView = (TextView) view.findViewById(R.id.thread_detail_title);
//            mTitleView.setTextSize(HiSettingsHelper.getTitleTextSize());
//            mTitleView.setText(mTitle);
        }
        mDetailListView.setPullLoadEnable(false);
        mDetailListView.setPullRefreshEnable(false);
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
                mDetailListView.stopLoadMore();
                showOrLoadPage();
            }
        });


        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showGotoPageDialog();
//                if (mDetailListView.isFastScrollEnabled()) {
//                    mDetailListView.setFastScrollEnabled(false);
//                    mDetailListView.setFastScrollAlwaysVisible(false);
//                } else {
//                    mDetailListView.setFastScrollEnabled(true);
//                    mDetailListView.setFastScrollAlwaysVisible(true);
//                }
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
        mReplyTextTv = (TextView) quickReply.findViewById(R.id.tv_reply_text);
        mReplyTextTv.setTextSize(HiSettingsHelper.getPostTextSize());
        mPostReplyIb = (ImageButton) quickReply.findViewById(R.id.ib_reply_post);
        mPostReplyIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyText = mReplyTextTv.getText().toString();
                if (Utils.getWordCount(replyText) < 5) {
                    Toast.makeText(getActivity(), "字数必须大于5", Toast.LENGTH_LONG).show();
                } else {
                    PostBean postBean = new PostBean();
                    postBean.setContent(replyText);
                    postBean.setTid(mTid);
                    new PostAsyncTask(getActivity(), PostAsyncTask.MODE_QUICK_REPLY, null, ThreadDetailFragment.this).execute(postBean);
                    // Close SoftKeyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mReplyTextTv.getWindowToken(), 0);
                    mFam.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Logger.v("onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        mDetailListView.setAdapter(mDetailAdapter);
        mDetailListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDetailListView.setOnItemLongClickListener(new OnItemLongClickCallback());
        mDetailListView.setOnScrollListener(new OnScrollCallback());

        getLoaderManager().initLoader(0, new Bundle(), mLoaderCallbacks);
        showOrLoadPage();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.v("onCreateOptionsMenu");

        menu.clear();
        inflater.inflate(R.menu.menu_thread_detail, menu);

        setActionBarTitle("(" + (mCurrentPage > 0 ? mCurrentPage + "/" + mMaxPage + ") " : "")
                + mTitle);
        setActionBarDisplayHomeAsUpEnabled(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.v("onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_open_url:
                String url = HiUtils.DetailListUrl + mTid;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
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
                Bundle arguments = new Bundle();
                arguments.putString(PostFragment.ARG_TID_KEY, mTid);
                arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_REPLY_THREAD);
                PostFragment fragment = new PostFragment();
                fragment.setArguments(arguments);
                fragment.setPostListener(this);
                if (HiSettingsHelper.getInstance().getIsLandscape()) {
                    getFragmentManager().beginTransaction()
                            .add(R.id.main_frame_container, fragment, PostFragment.class.getName())
                            .addToBackStack(PostFragment.class.getName())
                            .commit();
                } else {
                    getFragmentManager().beginTransaction()
                            .add(R.id.main_frame_container, fragment, PostFragment.class.getName())
                            .addToBackStack(PostFragment.class.getName())
                            .commit();
                }

                return true;
            case R.id.action_refresh_detail:
                refresh();
                return true;
            case R.id.action_image_gallery:
                startImageGallery(0);
                return true;
//            case R.id.action_goto:
//                showGotoPageDialog();
//                return true;
            case R.id.action_only_author:
                mAuthorOnly = !mAuthorOnly;
                mDetailBeans.clear();
                mDetailAdapter.setBeans(mDetailBeans);
                mCurrentPage = 1;
                if (mAuthorOnly) {
                    mDetailListView.setPullLoadEnable(false);
                    mDetailListView.setPullRefreshEnable(false);
                    setActionBarTitle("(只看楼主) " + mTitle);
                    showAndLoadAuthorOnly();
                } else {
                    showOrLoadPage();
                }
                return true;
            case R.id.action_add_favorite:
                FavoriteHelper.getInstance().addFavorite(mCtx, mTid, mTitle);
                return true;
            case R.id.action_remove_favorite:
                FavoriteHelper.getInstance().removeFavorite(mCtx, mTid, mTitle);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        Bundle b = new Bundle();
        b.putInt(LOADER_PAGE_KEY, mCurrentPage);
        getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
    }

    @Override
    public void onPrePost() {
        postProgressDialog = HiProgressDialog.show(mCtx, "正在发表...");
        postProgressDialog.setCancelable(false);
    }

    @Override
    public void onPostDone(int mode, int status, String message, PostBean postBean) {
        if (status == Constants.STATUS_SUCCESS) {
            //pop post fragment on success
            Fragment fg = getFragmentManager().findFragmentById(R.id.main_frame_container);
            if (fg instanceof PostFragment) {
                ((MainFrameActivity) getActivity()).popFragment(false);
            } else if (quickReply.getVisibility() == View.VISIBLE) {
                mReplyTextTv.setText("");
                quickReply.setVisibility(View.INVISIBLE);
            }

            if (postProgressDialog != null) {
                postProgressDialog.dismiss(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_SHORT).show();
            }

            if (!mAuthorOnly) {
                if (mode != PostAsyncTask.MODE_EDIT_POST)
                    mCurrentPage = mMaxPage;

                int floor = LAST_FLOOR;
                if (!TextUtils.isEmpty(postBean.getFloor()) && TextUtils.isDigitsOnly(postBean.getFloor()))
                    floor = Integer.parseInt(postBean.getFloor());
                if (floor == LAST_FLOOR || floor > 0)
                    mFloorOfPage = floor;

                mCache.remove(mCurrentPage);
                showOrLoadPage();
            }

        } else {
            if (postProgressDialog != null) {
                postProgressDialog.dismissError(message);
            } else {
                Toast.makeText(mCtx, message, Toast.LENGTH_LONG).show();
            }
        }
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
    public void onResume() {
        //Logger.v( "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        //Logger.v( "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        //Logger.v( "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        //Logger.v( "onDestory");
        getLoaderManager().destroyLoader(0);
        ((MainFrameActivity) getActivity()).registOnSwipeCallback(null);
        EventBus.getDefault().unregister(mDetailAdapter);
        super.onDestroy();
    }

    public void scrollToTop() {
        stopScroll();
        mDetailListView.setSelection(0);
    }

    public void stopScroll() {
        mDetailListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    public class ThreadListLoaderCallbacks implements LoaderManager.LoaderCallbacks<DetailListBean> {

        @Override
        public Loader<DetailListBean> onCreateLoader(int id, Bundle args) {
            Logger.v("onCreateLoader");

            if (mInloading) {
                return null;
            }

            // Re-enable after load complete if needed.
            mDetailListView.setPullLoadEnable(false);
            mDetailListView.setPullRefreshEnable(false);

            //VolleyHelper.getInstance().cancelAll();
            return new DetailListLoader(mCtx, mMsgHandler, mTid, mGotoPostId, args.getInt(LOADER_PAGE_KEY, 1));
        }

        @Override
        public void onLoadFinished(Loader<DetailListBean> loader,
                                   DetailListBean details) {
            Logger.v("onLoadFinished");

            if (getView() != null)
                MAX_VIEW_WIDTH = getView().getWidth();

            mInloading = false;
            mPrefetching = false;
            loadingProgressBar.hide();

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

            Message msgClean = Message.obtain();
            msgClean.what = ThreadListFragment.STAGE_CLEAN;
            mMsgHandler.sendMessage(msgClean);

            // Set title
            if (details.getTitle() != null && !details.getTitle().isEmpty()) {
                mTitle = details.getTitle();
                if (mTitleView != null) {
                    mTitleView.setText(mTitle);
                }
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
        }


        @Override
        public void onLoaderReset(Loader<DetailListBean> arg0) {
            //Logger.v( "onLoaderReset");

            mInloading = false;
            mPrefetching = false;

            loadingProgressBar.hide();
        }

    }

    private void prefetchNextPage(int pageOffset) {
        if (!mPrefetching && !mAuthorOnly
                && mCache.get(mCurrentPage + pageOffset) == null) {
            int page = mCurrentPage + pageOffset;
            if (page < 1 || page > mMaxPage)
                return;
            mPrefetching = true;
            Logger.v("prefetch page " + page);
            Bundle b = new Bundle();
            b.putInt(LOADER_PAGE_KEY, page);
            getLoaderManager().restartLoader(0, b, mLoaderCallbacks).forceLoad();
        }
    }

    private void setPullLoadStatus() {
        if (mAuthorOnly) {
            mDetailListView.setPullLoadEnable(false);
            mDetailListView.setPullRefreshEnable(false);
        } else {
            if (mCurrentPage == 1) {
                mDetailListView.setPullRefreshEnable(false);
            } else {
                mDetailListView.setPullRefreshEnable(true);
            }
            if (mCurrentPage == mMaxPage) {
                mDetailListView.setPullLoadEnable(false);
            } else {
                mDetailListView.setPullLoadEnable(true);
            }
        }
    }

    private class OnItemLongClickCallback implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mDetailListView.setItemChecked(position, true);

            position = position - mDetailListView.getHeaderViewsCount();
            if (position > mDetailAdapter.getCount()) {
                return false;
            }

            ThreadDetailActionModeCallback cb = new ThreadDetailActionModeCallback(ThreadDetailFragment.this,
                    mFid, mTid, mDetailAdapter.getItem(position));
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
        final Button btnFirstPage = (Button) viewlayout.findViewById(R.id.btn_fisrt_page);
        final Button btnLastPage = (Button) viewlayout.findViewById(R.id.btn_last_page);
        final Button btnNextPage = (Button) viewlayout.findViewById(R.id.btn_next_page);
        final Button btnPreviousPage = (Button) viewlayout.findViewById(R.id.btn_previous_page);
        final SeekBar sbGotoPage = (SeekBar) viewlayout.findViewById(R.id.sb_page);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog dialog;

        //hack for IconicsButton conflict with custom font
        if (!TextUtils.isEmpty(HiSettingsHelper.getInstance().getFont())) {
            btnFirstPage.setText("首");
            btnLastPage.setText("尾");
            btnNextPage.setText("后");
            btnPreviousPage.setText("前");
        }

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
            mReplyTextTv.setText("");
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

            mGoToPage = (floor - 1) / mMaxPostInPage + 1; // page start from 1

            mFloorOfPage = floor % mMaxPostInPage; // floor start from 1
            if (mFloorOfPage == 0) {
                mFloorOfPage = mMaxPostInPage;
            }

            if (mGoToPage != mCurrentPage) {
                mCurrentPage = mGoToPage;
                //getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
                showOrLoadPage();
            } else {
                mDetailListView.setSelection(mFloorOfPage + mDetailListView.getHeaderViewsCount() - 1);
                mFloorOfPage = -1;
            }
        }
    }

    private class ThreadDetailMsgHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            int page = 0;
            Bundle bundle = msg.getData();
            if (bundle != null) {
                page = bundle.getInt(LOADER_PAGE_KEY, 0);
            }
            String pageStr = page > 0 ? "(第" + page + "页)" : "";

            switch (msg.what) {
                case ThreadListFragment.STAGE_ERROR:
                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.red));
                    Bundle b = msg.getData();
                    mTipBar.setText(b.getString(ThreadListFragment.STAGE_ERROR_KEY));
                    Logger.e(b.getString(ThreadListFragment.STAGE_ERROR_KEY));
                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case ThreadListFragment.STAGE_CLEAN:
                    mTipBar.setVisibility(View.INVISIBLE);
                    break;
                case ThreadListFragment.STAGE_DONE:
//                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.green));
//                    mTipBar.setText(pageStr + "加载完成");
//                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case ThreadListFragment.STAGE_RELOGIN:
                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
                    mTipBar.setText("正在登录");
                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case ThreadListFragment.STAGE_GET_WEBPAGE:
                    loadingProgressBar.show();
//                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.purple));
//                    mTipBar.setText(pageStr + "正在获取页面");
//                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case ThreadListFragment.STAGE_PARSE:
//                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.orange));
//                    mTipBar.setText(pageStr + "正在解析页面");
//                    mTipBar.setVisibility(View.VISIBLE);
                    break;
                case ThreadListFragment.STAGE_PREFETCH:
//                    mTipBar.setBackgroundColor(mCtx.getResources().getColor(R.color.green));
//                    mTipBar.setText("正在预读下一页");
//                    mTipBar.setVisibility(View.VISIBLE);
                    break;
            }
            return false;
        }
    }

    private void showOrLoadPage() {

        setActionBarTitle("(" + (mCurrentPage > 0 ? mCurrentPage + "/" + mMaxPage + ") " : "")
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
            Bundle b = new Bundle();
            b.putInt(LOADER_PAGE_KEY, mCurrentPage);
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
            giv.getLayoutParams().width = imageReadyInfo.getWidth();
            giv.getLayoutParams().height = imageReadyInfo.getHeight();
            if (imageReadyInfo.getWidth() > GlideImageView.MIN_SCALE_WIDTH
                    || imageReadyInfo.isGif()) {
                giv.setImageReadyInfo(imageReadyInfo);
                giv.setClickToViewBigImage();
            }

            if (imageReadyInfo.isGif()) {
                Glide.with(mCtx)
                        .load(GlideHelper.getGlideUrl(imageUrl))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new GifTransformation(mCtx))
                        .into(new GlideBitmapTarget(giv, imageReadyInfo.getWidth(), imageReadyInfo.getHeight()));
            } else {
                Glide.with(mCtx)
                        .load(GlideHelper.getGlideUrl(imageUrl))
                        .asBitmap()
                        .cacheDecoder(new FileToStreamDecoder<>(new ThreadImageDecoder(mMaxImageDecodeWidth)))
                        .imageDecoder(new ThreadImageDecoder(mMaxImageDecodeWidth))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new GlideBitmapTarget(giv, imageReadyInfo.getWidth(), imageReadyInfo.getHeight()));
            }
        } else {
            giv.setImageResource(R.drawable.tapatalk_image_broken);
        }
    }

    class AvatarOnClickListener extends OnSingleClickListener {
        @Override
        public void onSingleClick(View arg0) {
            String uid = (String) arg0.getTag(R.id.avatar_tag_uid);
            String username = (String) arg0.getTag(R.id.avatar_tag_username);

            Bundle arguments = new Bundle();
            arguments.putString(UserinfoFragment.ARG_UID, uid);
            arguments.putString(UserinfoFragment.ARG_USERNAME, username);
            UserinfoFragment fragment = new UserinfoFragment();
            fragment.setArguments(arguments);

            setHasOptionsMenu(false);

            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                    .addToBackStack(ThreadDetailFragment.class.getName())
                    .commit();
        }
    }

    public void startImageGallery(int imageIndex) {
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
            popupImageDialog.init(detailListBean, imageIndex);
            popupImageDialog.show(getFragmentManager(), PopupImageDialog.class.getName());
        } else {
            Toast.makeText(mCtx, "本页没有图片", Toast.LENGTH_SHORT).show();
        }
    }

}
