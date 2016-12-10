package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiEditText;
import com.vdurmont.emoji.EmojiParser;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.async.PrePostAsyncTask;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.db.Content;
import net.jejer.hipda.db.ContentDao;
import net.jejer.hipda.job.ImageUploadEvent;
import net.jejer.hipda.job.ImageUploadJob;
import net.jejer.hipda.job.JobMgr;
import net.jejer.hipda.job.PostJob;
import net.jejer.hipda.job.UploadImage;
import net.jejer.hipda.ui.adapter.GridImageAdapter;
import net.jejer.hipda.ui.adapter.ThreadTypeAdapter;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HtmlCompat;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PostFragment extends BaseFragment {
    private static final int SELECT_PICTURE = 1;

    public static final String ARG_FID_KEY = "fid";
    public static final String ARG_TID_KEY = "tid";
    public static final String ARG_PID_KEY = "pid";
    public static final String ARG_FLOOR_KEY = "floor";
    public static final String ARG_FLOOR_AUTHOR_KEY = "floor_author";
    public static final String ARG_TEXT_KEY = "text";
    public static final String ARG_MODE_KEY = "mode";

    public static final String BUNDLE_POSISTION_KEY = "content_position";

    private String mFid;
    private String mTid;
    private String mPid;
    private int mFloor;
    private String mFloorAuthor;
    private String mText;
    private String mTypeId = "0";
    private int mMode;
    private TextView mTvQuoteText;
    private TextView mTvType;
    private TextView mTvImagesInfo;
    private EditText mEtSubject;
    private EmojiEditText mEtContent;
    private ImageButton mIbEmojiSwitch;
    private int mContentPosition = -1;

    private PrePostAsyncTask.PrePostListener mPrePostListener = new PrePostListener();
    private PrePostInfoBean mPrePostInfo;
    private PrePostAsyncTask mPrePostAsyncTask;
    private Snackbar mSnackbar;
    private int mFetchInfoCount = 0;
    private boolean mFetchingInfo = false;
    private ContentLoadingProgressBar mProgressBar;

    private String mForumName;
    private String mParentSessionId;
    private Map<String, String> mTypeValues;

    private GridImageAdapter mImageAdapter;
    private HiProgressDialog mProgressDialog;
    private boolean mImageUploading = false;
    private Map<Uri, UploadImage> mUploadImages = new LinkedHashMap<>();
    private Collection<Uri> mHoldedImages = new ArrayList<>();
    private long mLastSavedTime = -1;
    private int mDeleteMode = 0; // 0 : edit, 1 : delete

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);

        if (getArguments().containsKey(ARG_FID_KEY)) {
            mFid = getArguments().getString(ARG_FID_KEY);
        }
        if (getArguments().containsKey(ARG_TID_KEY)) {
            mTid = getArguments().getString(ARG_TID_KEY);
        }
        if (getArguments().containsKey(ARG_PID_KEY)) {
            mPid = getArguments().getString(ARG_PID_KEY);
        }
        if (getArguments().containsKey(ARG_FLOOR_KEY)) {
            mFloor = getArguments().getInt(ARG_FLOOR_KEY);
        }
        if (getArguments().containsKey(ARG_FLOOR_AUTHOR_KEY)) {
            mFloorAuthor = getArguments().getString(ARG_FLOOR_AUTHOR_KEY);
        }
        if (getArguments().containsKey(ARG_MODE_KEY)) {
            mMode = getArguments().getInt(ARG_MODE_KEY);
        }
        if (getArguments().containsKey(ARG_TEXT_KEY)) {
            mText = getArguments().getString(ARG_TEXT_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        mEtContent = (EmojiEditText) view.findViewById(R.id.et_reply);
        mTvQuoteText = (TextView) view.findViewById(R.id.tv_quote_text);
        mTvType = (TextView) view.findViewById(R.id.tv_type);
        mTvImagesInfo = (TextView) view.findViewById(R.id.tv_image_info);
        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.preinfo_loading);

        mImageAdapter = new GridImageAdapter(getActivity());

        mTvImagesInfo.setText("图片信息");
        mTvImagesInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentPosition = mEtContent.getSelectionStart();
                showImagesDialog();
            }
        });
        updateImageInfo();

        if (HiUtils.isValidId(mFid)) {
            int fid = Integer.parseInt(mFid);
            int index = HiUtils.getForumIndexByFid(fid);
            if (index >= 0 && index < HiUtils.FORUM_NAMES.length)
                mForumName = HiUtils.FORUM_NAMES[index];
        }

        mEtSubject = (EditText) view.findViewById(R.id.et_subject);
        mEtContent.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        UIUtils.setLineSpacing(mEtContent);

        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                savePostConent(false);
            }
        });

        mEtContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    savePostConent(true);
            }
        });

        mEtSubject.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mEmojiPopup != null && mEmojiPopup.isShowing())
                    mEmojiPopup.dismiss();
            }
        });

        if (mMode == PostHelper.MODE_REPLY_THREAD && !TextUtils.isEmpty(mText)) {
            mEtContent.setText(mText);
        }

        final ImageButton ibReply = (ImageButton) view.findViewById(R.id.ib_reply);
        ibReply.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_send).sizeDp(28).color(Color.GRAY));
        ibReply.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                postReply();
            }
        });

        final TextView tvCountdown = (TextView) view.findViewById(R.id.tv_countdown);

        int timeToWait = PostHelper.getWaitTimeToPost();
        if (timeToWait > 0 && mMode != PostHelper.MODE_EDIT_POST) {
            ibReply.setVisibility(View.INVISIBLE);
            tvCountdown.setText(timeToWait + "");
            tvCountdown.setVisibility(View.VISIBLE);
            new CountDownTimer(timeToWait * 1000, 500) {

                public void onTick(long millisUntilFinished) {
                    tvCountdown.setText((millisUntilFinished / 1000) + "");
                }

                public void onFinish() {
                    tvCountdown.setVisibility(View.GONE);
                    ibReply.setVisibility(View.VISIBLE);
                }
            }.start();
        } else {
            ibReply.setVisibility(View.VISIBLE);
            tvCountdown.setVisibility(View.GONE);
        }

        mIbEmojiSwitch = (ImageButton) view.findViewById(R.id.ib_emoji_switch);
        setUpEmojiPopup(mEtContent, mIbEmojiSwitch);

        setActionBarTitle(R.string.action_reply);
        setActionBarDisplayHomeAsUpEnabled(true);

        switch (mMode) {
            case PostHelper.MODE_REPLY_THREAD:
                setActionBarTitle("回复帖子");
                break;
            case PostHelper.MODE_REPLY_POST:
                setActionBarTitle("回复 " + mFloor + "# " + mFloorAuthor);
                break;
            case PostHelper.MODE_QUOTE_POST:
                setActionBarTitle("引用 " + mFloor + "# " + mFloorAuthor);
                break;
            case PostHelper.MODE_NEW_THREAD:
                setActionBarTitle(mForumName);
                mEtSubject.setVisibility(View.VISIBLE);
                break;
            case PostHelper.MODE_EDIT_POST:
                setActionBarTitle(getActivity().getResources().getString(R.string.action_edit));
                break;
        }

        return view;
    }

    private void savePostConent(boolean force) {
        if (force || SystemClock.uptimeMillis() - mLastSavedTime > 3000) {
            mLastSavedTime = SystemClock.uptimeMillis();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    ContentDao.saveContent(mSessionId, mEtContent.getText().toString());
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (mPrePostInfo == null) {
            fetchPrePostInfo(false);
        } else {
            setupPrePostInfo();
        }

        if (mMode == PostHelper.MODE_NEW_THREAD) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    mEtSubject.requestFocus();
                    long t = SystemClock.uptimeMillis();
                    mEtSubject.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, 0, 0, 0));
                    mEtSubject.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, 0, 0, 0));
                }
            }, 100);
        } else {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    mEtContent.requestFocus();
                    long t = SystemClock.uptimeMillis();
                    mEtContent.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, 0, 0, 0));
                    mEtContent.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, 0, 0, 0));
                    if (mContentPosition < 0)
                        mEtContent.setSelection(mEtContent.getText().length());
                }
            }, 100);
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        savePostConent(true);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mSnackbar != null)
            mSnackbar.dismiss();
        if (mPrePostAsyncTask != null)
            mPrePostAsyncTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_POSISTION_KEY, mContentPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mContentPosition = savedInstanceState.getInt(BUNDLE_POSISTION_KEY, -1);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_reply, menu);

        menu.findItem(R.id.action_upload_img).setIcon(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_add_a_photo).actionBar().color(Color.WHITE));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mMode != PostHelper.MODE_EDIT_POST)
            return;
        MenuItem deleteMenuItem = menu.findItem(R.id.action_delete_post);
        if (deleteMenuItem == null)
            return;
        deleteMenuItem.setVisible(true);
        deleteMenuItem.setEnabled(mPrePostInfo != null && mPrePostInfo.isDeleteable());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_upload_img:
                if (mPrePostInfo == null) {
                    fetchPrePostInfo(false);
                    Toast.makeText(getActivity(), "请等待信息收集结束再选择图片", Toast.LENGTH_LONG).show();
                } else {
                    if (UIUtils.askForPermission(getActivity())) {
                        return true;
                    }
                    mContentPosition = mEtContent.getSelectionStart();
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    if (Build.VERSION.SDK_INT >= 18)
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), SELECT_PICTURE);
                }
                return true;
            case R.id.action_restore_content:
                mEtContent.requestFocus();
                showRestoreContentDialog();
                return true;
            case R.id.action_delete_post:
                showDeletePostDialog();
                return true;
            default:
                return false;
        }
    }

    public void setParentSessionId(String parentSessionId) {
        mParentSessionId = parentSessionId;
    }

    private void postReply() {
        if (mPrePostInfo == null) {
            fetchPrePostInfo(false);
            Toast.makeText(getActivity(), "请等待信息收集结束再发送", Toast.LENGTH_LONG).show();
            return;
        }

        if (mMode == PostHelper.MODE_NEW_THREAD &&
                (HiUtils.FID_BS + "").equals(mFid) && "0".equals(mTypeId)) {
            Toast.makeText(getActivity(), "B&S版发帖必须指定分类", Toast.LENGTH_LONG).show();
            return;
        }

        final String subjectText = mEtSubject.getText().toString();
        if (mEtSubject.getVisibility() == View.VISIBLE) {
            if (Utils.getWordCount(subjectText) < 5) {
                Toast.makeText(getActivity(), "主题字数必须大于 5", Toast.LENGTH_LONG).show();
                return;
            }
            if (Utils.getWordCount(subjectText) > 80) {
                Toast.makeText(getActivity(), "主题字数必须少于 80", Toast.LENGTH_LONG).show();
                return;
            }
        }

        final String replyText = mEtContent.getText().toString();
        if (Utils.getWordCount(replyText) < 5) {
            Toast.makeText(getActivity(), "帖子内容字数必须大于 5", Toast.LENGTH_LONG).show();
            return;
        }

        UIUtils.hideSoftKeyboard(getActivity());

        final List<String> extraImgs = new ArrayList<>();
        if (mPrePostInfo.getAllImages().size() > 0) {
            for (String imgId : mPrePostInfo.getAllImages()) {
                String attachStr = "[attachimg]" + imgId + "[/attachimg]";
                if (!replyText.contains(attachStr)) {
                    extraImgs.add(imgId);
                }
            }
            if (extraImgs.size() > 0) {
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("未使用的图片")
                        .setMessage(HtmlCompat.fromHtml("有 " + extraImgs.size() + " 张图片未以图片标签[attachimg]形式显示在正文中<br>"
                                + "<br>如果您希望其他用户看到这些图片，请选择 <b>保留图片</b>"
                                + "<br>否则请选择 <b>丢弃图片</b>"))
                        .setPositiveButton("保留图片",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        StringBuilder sb = new StringBuilder();
                                        String tail = HiSettingsHelper.getInstance().getTailStr();
                                        String content;
                                        boolean appendTail = false;
                                        if (replyText.trim().endsWith(tail)) {
                                            content = replyText.substring(0, replyText.lastIndexOf(tail));
                                            appendTail = true;
                                        } else {
                                            content = replyText;
                                        }
                                        sb.append(content).append("\n");
                                        for (String imgId : extraImgs) {
                                            sb.append("[attachimg]").append(imgId).append("[/attachimg]").append("\n");
                                            mPrePostInfo.addAttach(imgId);
                                        }
                                        if (appendTail)
                                            sb.append(tail);
                                        startPostJob(subjectText, sb.toString());
                                    }
                                })
                        .setNeutralButton("丢弃图片",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for (String imgId : extraImgs) {
                                            mPrePostInfo.addAttachdel(imgId);
                                        }
                                        startPostJob(subjectText, replyText);
                                    }
                                }).create();
                dialog.show();
                return;
            }
        }
        startPostJob(subjectText, replyText);
    }

    private void startPostJob(String subjectText, String replyText) {
        PostBean postBean = new PostBean();
        postBean.setContent(replyText);
        postBean.setTid(mTid);
        postBean.setPid(mPid);
        postBean.setFid(mFid);
        postBean.setTypeid(mTypeId);
        postBean.setSubject(subjectText);
        postBean.setFloor(mFloor);
        postBean.setDelete(mDeleteMode);

        JobMgr.addJob(new PostJob(mParentSessionId, mMode, mPrePostInfo, postBean));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //avoid double click select button
        if (mImageUploading) {
            return;
        }
        mImageUploading = true;
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                mImageUploading = false;
            }
        }, 2000);

        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_PICTURE) {
            boolean findData = false;
            boolean duplicate = false;
            StringBuilder sb = new StringBuilder();
            sb.append("Device: ").append(Utils.getDeviceName()).append("\n");
            sb.append("Version: ").append(Build.VERSION.SDK_INT).append("\n");
            Collection<Uri> uris = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                ClipData clipData = intent.getClipData();
                sb.append("ClipData: ").append(clipData == null ? "null" : clipData.getItemCount()).append("\n");
                if (clipData != null && clipData.getItemCount() > 0) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        if (!mUploadImages.containsKey(uri)) {
                            uris.add(uri);
                            sb.append("ClipData: item ").append(i).append(" added").append("\n");
                        } else {
                            duplicate = true;
                            sb.append("ClipData: item ").append(i).append(" dup").append("\n");
                        }
                    }
                    findData = true;
                }
            }
            if (!findData && intent.getData() != null) {
                if (!mUploadImages.containsKey(intent.getData())) {
                    uris.add(intent.getData());
                    sb.append("Data: ").append(" added").append("\n");
                } else {
                    duplicate = true;
                    sb.append("Data: ").append(" dup").append("\n");
                }
            }

            if (intent.getData() == null)
                sb.append("Data: null").append("\n");

            if (uris.size() == 0) {
                if (duplicate) {
                    Toast.makeText(getActivity(), "选择的图片重复", Toast.LENGTH_SHORT).show();
                } else {
                    UIUtils.errorSnack(getView(), "无法获取图片信息", sb.toString());
                }
                return;
            }

            mProgressDialog = HiProgressDialog.show(getActivity(), "正在上传...");
            if (mPrePostInfo != null) {
                JobMgr.addJob(new ImageUploadJob(mSessionId, mPrePostInfo.getUid(), mPrePostInfo.getHash(), uris.toArray(new Uri[uris.size()])));
            } else {
                //hold selected images, upload them after fetch pre post info success
                mHoldedImages.addAll(uris);
            }
        }
    }

    private void showRestoreContentDialog() {
        final Content[] contents = ContentDao.getSavedContents(mSessionId);

        if (contents == null || contents.length == 0) {
            Toast.makeText(getActivity(), "没有之前输入的内容", Toast.LENGTH_SHORT).show();
            return;
        }

        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_restore_content, null);
        final ListView listView = (ListView) viewlayout.findViewById(R.id.lv_contents);

        listView.setAdapter(new SavedContentsAdapter(getActivity(), 0, contents));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                if (!TextUtils.isEmpty(mEtContent.getText()) && !mEtContent.getText().toString().endsWith("\n"))
                    mEtContent.append("\n");
                mEtContent.append(contents[position].getContent());
                mEtContent.requestFocus();
                mEtContent.setSelection(mEtContent.getText().length());
                dialog.dismiss();
            }
        });

    }

    private void showDeletePostDialog() {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setTitle("删除本帖？");
        popDialog.setMessage(HtmlCompat.fromHtml("确认删除发表的内容吗？<br><br><font color=red>注意：此操作不可恢复。</font>"));
        popDialog.setPositiveButton("删除",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeleteMode = 1;
                        postReply();
                    }
                });
        popDialog.setIcon(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_exclamation_circle).sizeDp(24).color(Color.RED));
        popDialog.setNegativeButton("取消", null);
        popDialog.create().show();
    }

    private void updateImageInfo() {
        if (mUploadImages.size() > 0) {
            mTvImagesInfo.setVisibility(View.VISIBLE);
            mTvImagesInfo.setText("图片 <" + mUploadImages.size() + ">");
            if (mImageAdapter != null)
                mImageAdapter.setImages(mUploadImages.values());
        } else {
            mTvImagesInfo.setText("没有图片");
            mTvImagesInfo.setVisibility(View.GONE);
        }
    }

    private void appendImage(String imgId) {
        if (isValidImgId(imgId)) {
            String imgTxt = "[attachimg]" + imgId + "[/attachimg]\n";
            int selectionStart = mContentPosition;
            if (mContentPosition < 0 || mContentPosition > mEtContent.length())
                selectionStart = mEtContent.getSelectionStart();
            if (selectionStart > 0 && mEtContent.getText().charAt(selectionStart - 1) != '\n')
                imgTxt = "\n" + imgTxt;
            mEtContent.getText().insert(selectionStart, imgTxt);
            mEtContent.setSelection(selectionStart + imgTxt.length());
            mContentPosition = selectionStart + imgTxt.length();
            mEtContent.requestFocus();
            mPrePostInfo.addAttach(imgId);
            mPrePostInfo.addImage(imgId);
        }
    }

    private class PrePostListener implements PrePostAsyncTask.PrePostListener {
        @Override
        public void PrePostComplete(int mode, boolean result, String message, PrePostInfoBean info) {
            mFetchingInfo = false;
            mProgressBar.hide();
            if (result) {
                mPrePostInfo = info;
                setupPrePostInfo();
                if (mFetchInfoCount > 1)
                    Toast.makeText(getActivity(), "收集信息成功", Toast.LENGTH_SHORT).show();
            } else {
                if (getView() != null) {
                    mSnackbar = Snackbar.make(getView(), "收集信息失败 : " + message, Snackbar.LENGTH_LONG);
                    UIUtils.setSnackbarMessageTextColor(mSnackbar, ContextCompat.getColor(getActivity(), R.color.md_yellow_500));
                    mSnackbar.setAction("重试", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fetchPrePostInfo(true);
                            mSnackbar.dismiss();
                        }
                    });
                    mSnackbar.show();
                }
            }
        }
    }

    private void fetchPrePostInfo(boolean showProgressNow) {
        if (!mFetchingInfo) {
            mFetchingInfo = true;
            mFetchInfoCount++;
            if (showProgressNow)
                mProgressBar.showNow();
            else
                mProgressBar.show();
            mPrePostAsyncTask = new PrePostAsyncTask(getActivity(), mPrePostListener, mMode);
            PostBean postBean = new PostBean();
            postBean.setTid(mTid);
            postBean.setPid(mPid);
            postBean.setFid(mFid);
            mPrePostAsyncTask.execute(postBean);
        }
    }

    private void setupPrePostInfo() {
        if (mPrePostInfo == null)
            return;

        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        mTypeValues = mPrePostInfo.getTypeValues();
        mTypeId = mPrePostInfo.getTypeid();

        if (mTypeValues.size() > 0) {
            mTvType.setText(mTypeValues.get(mTypeId));
            mTvType.setTag(mTypeId);
            mTvType.setVisibility(View.VISIBLE);
            mTvType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showThreadTypesDialog();
                }
            });
        }

        if (!TextUtils.isEmpty(mPrePostInfo.getText())) {
            if (mMode == PostHelper.MODE_EDIT_POST) {
                mEtContent.setText(EmojiParser.parseToUnicode(mPrePostInfo.getText()));
                if (!TextUtils.isEmpty(mPrePostInfo.getSubject())) {
                    mEtSubject.setText(EmojiParser.parseToUnicode(mPrePostInfo.getSubject()));
                    mEtSubject.setVisibility(View.VISIBLE);
                }
            } else {
                mTvQuoteText.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                UIUtils.setLineSpacing(mTvQuoteText);
                mTvQuoteText.setText(mText);
                mTvQuoteText.setVisibility(View.VISIBLE);
                mTvQuoteText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtils.showMessageDialog(getActivity(), mFloor + "# " + mFloorAuthor, mText, true);
                    }
                });
            }
        }

        //try to upload holded images when pre post info is ready
        if (mHoldedImages != null && mHoldedImages.size() > 0) {
            JobMgr.addJob(new ImageUploadJob(mSessionId, mPrePostInfo.getUid(), mPrePostInfo.getHash(), mHoldedImages.toArray(new Uri[mHoldedImages.size()])));
            mHoldedImages.clear();
        }
    }

    private boolean isValidImgId(String imgId) {
        return !TextUtils.isEmpty(imgId)
                && TextUtils.isDigitsOnly(imgId)
                && imgId.length() > 1;
    }

    public boolean isUserInputted() {
        return !TextUtils.isEmpty(mEtContent.getText())
                || !TextUtils.isEmpty(mEtSubject.getText())
                || mUploadImages.size() > 0;
    }

    @Override
    public boolean onBackPressed() {
        if (isUserInputted()) {
            Dialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("放弃发表？")
                    .setMessage("\n确认放弃已输入的内容吗？\n")
                    .setPositiveButton(getResources().getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PostFragment.this.popFragment();
                                }
                            })
                    .setNegativeButton(getResources().getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
            dialog.show();
            return true;
        }
        return false;
    }

    private void showThreadTypesDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_forum_types, null);

        final ListView listView = (ListView) viewlayout.findViewById(R.id.lv_forum_types);

        listView.setAdapter(new ThreadTypeAdapter(getActivity(), mTypeValues));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                mTypeId = mTypeValues.keySet().toArray()[position].toString();
                mTvType.setText(mTypeValues.get(mTypeId));
                dialog.dismiss();
            }
        });

    }

    private void showImagesDialog() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_images, null);

        final GridView gridView = (GridView) viewlayout.findViewById(R.id.gv_images);

        gridView.setAdapter(mImageAdapter);

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setView(viewlayout);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        gridView.setOnItemClickListener(new OnViewItemSingleClickListener() {
            @Override
            public void onItemSingleClick(AdapterView<?> adapterView, View view, int position, long row) {
                if (view.getTag() != null)
                    appendImage(view.getTag().toString());
                dialog.dismiss();
            }
        });

    }

    private void imageProcess(int total, int current, int percentage) {
        mProgressDialog.setMessage("正在上传... (" + (current + 1) + "/" + total + ")");
    }

    private void imageDone(ImageUploadEvent event) {
        UploadImage image = event.mImage;
        mUploadImages.put(image.getUri(), image);
        if (isValidImgId(image.getImgId())) {
            mEmojiPopup.addImage(image.getImgId(), image.getThumb());
            appendImage(image.getImgId());
        } else {
            UIUtils.errorSnack(getView(), "图片上传失败：" + Utils.nullToText(event.message), event.mDetail);
        }
        updateImageInfo();
    }

    private void imageAllDone() {
        mImageUploading = false;
        mProgressDialog.dismiss();
        updateImageInfo();
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ImageUploadEvent event) {
        if (!mSessionId.equals(event.mSessionId))
            return;

        Collection<ImageUploadEvent> events = new ArrayList<>();
        if (event.holdEvents != null && event.holdEvents.size() > 0) {
            events.addAll(event.holdEvents);
        }
        events.add(event);

        for (ImageUploadEvent evt : events) {
            if (evt.type == ImageUploadEvent.UPLOADING) {
                imageProcess(evt.total, evt.current, evt.percentage);
            } else if (evt.type == ImageUploadEvent.ITEM_DONE) {
                imageDone(evt);
            } else if (evt.type == ImageUploadEvent.ALL_DONE) {
                imageAllDone();
            }
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    private class SavedContentsAdapter extends ArrayAdapter {
        Content[] contents;

        public SavedContentsAdapter(Context context, int resource, Content[] objects) {
            super(context, resource, objects);
            contents = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.item_saved_content, parent, false);
            } else {
                row = convertView;
            }
            Content content = contents[position];

            TextView tvContent = (TextView) row.findViewById(R.id.tv_content);
            TextView tvDesc = (TextView) row.findViewById(R.id.tv_desc);

            tvContent.setText(content.getContent().replace("\n", " "));
            tvDesc.setText(content.getDesc());

            return row;
        }
    }

}
