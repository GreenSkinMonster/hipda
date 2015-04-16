package net.jejer.hipda.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostAsyncTask;
import net.jejer.hipda.async.PrePostAsyncTask;
import net.jejer.hipda.async.UploadImgAsyncTask;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.bean.PrePostInfoBean;
import net.jejer.hipda.utils.CursorUtils;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.ImageFileInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostFragment extends Fragment {
    private static final int SELECT_PICTURE = 1;

    private final String LOG_TAG = getClass().getSimpleName();
    public static final String ARG_FID_KEY = "fid";
    public static final String ARG_TID_KEY = "tid";
    public static final String ARG_PID_KEY = "pid";
    public static final String ARG_FLOOR_KEY = "floor";
    public static final String ARG_MODE_KEY = "mode";

    private String mFid;
    private String mTid;
    private String mPid;
    private String mFloor;
    private String mTypeid = "0";
    private int mMode;
    private TextView mTvAdditional;
    private EditText mEtSubjectMsg;
    private EditText mEtReplyMsg;
    private PrePostAsyncTask.PrePostListener mPrePostListener = new PrePostListener();
    private PrePostInfoBean mPrePostInfo;
    private PrePostAsyncTask mPrePostAsyncTask;

    private Spinner mSpForum;
    private Spinner mSpTypeIds;
    private Collection<UploadImgButton> uploadImgButtons = new ArrayList<>();


    private PostAsyncTask.PostListener postListener;

    public void setPostListener(PostAsyncTask.PostListener postListener) {
        this.postListener = postListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

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
            mFloor = getArguments().getString(ARG_FLOOR_KEY);
        }
        if (getArguments().containsKey(ARG_MODE_KEY)) {
            mMode = getArguments().getInt(ARG_MODE_KEY);
        }

        // Start fetch info
        mPrePostAsyncTask = new PrePostAsyncTask(getActivity(), mPrePostListener, mMode);
        PostBean postBean = new PostBean();
        postBean.setTid(mTid);
        postBean.setPid(mPid);
        postBean.setFid(mFid);
        mPrePostAsyncTask.execute(postBean);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        mEtReplyMsg = (EditText) view.findViewById(R.id.et_reply);
        mTvAdditional = (TextView) view.findViewById(R.id.et_additional);
        mTvAdditional.setText("正在收集信息");
        if (HiSettingsHelper.getInstance().isEinkModeUIEnabled()) {
            mTvAdditional.setBackgroundColor(getActivity().getResources().getColor(R.color.background_grey));
        }

        mSpForum = (Spinner) view.findViewById(R.id.sp_fid);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.forums, android.R.layout.simple_list_item_1);
        mSpForum.setAdapter(adapter);
        mSpForum.setOnItemSelectedListener(new FidSelectListener());
        mSpForum.setEnabled(false);
        if (mFid != null) {
            mSpForum.setSelection(HiUtils.getForumIndexByFid(getActivity(), mFid));
        }

        mSpTypeIds = (Spinner) view.findViewById(R.id.sp_typeid);
        mSpTypeIds.setOnItemSelectedListener(new TypeidSelectListener());

        mEtSubjectMsg = (EditText) view.findViewById(R.id.et_subject);

        mEtReplyMsg.setTextSize(HiSettingsHelper.getPostTextSize());
        mTvAdditional.setTextSize(HiSettingsHelper.getPostTextSize());

        final ExpandableHeightGridView gvTab1 = (ExpandableHeightGridView) view.findViewById(R.id.tab1_emoji);
        gvTab1.setExpanded(true);
        gvTab1.setAdapter(new EmojiAdapter(getActivity(), 1));
        gvTab1.setOnItemClickListener(new GridView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                mEtReplyMsg.getText().insert(mEtReplyMsg.getSelectionStart(), (String) gvTab1.getAdapter().getItem(position));
            }
        });
        final ExpandableHeightGridView gvTab2 = (ExpandableHeightGridView) view.findViewById(R.id.tab2_emoji);
        gvTab2.setExpanded(true);
        gvTab2.setAdapter(new EmojiAdapter(getActivity(), 2));
        gvTab2.setOnItemClickListener(new GridView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                mEtReplyMsg.getText().insert(mEtReplyMsg.getSelectionStart(), (String) gvTab2.getAdapter().getItem(position));
            }
        });
        final ExpandableHeightGridView gvTab3 = (ExpandableHeightGridView) view.findViewById(R.id.tab3_emoji);
        gvTab3.setExpanded(true);
        gvTab3.setAdapter(new EmojiAdapter(getActivity(), 3));
        gvTab3.setOnItemClickListener(new GridView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                mEtReplyMsg.getText().insert(mEtReplyMsg.getSelectionStart(), (String) gvTab3.getAdapter().getItem(position));
            }
        });

        WindowManager w = getActivity().getWindowManager();
        Point size = new Point();
        w.getDefaultDisplay().getSize(size);
        int measuredWidth = size.x;

        final Button emojiBtn1 = (Button) view.findViewById(R.id.btn1_emoji);
        final Button emojiBtn2 = (Button) view.findViewById(R.id.btn2_emoji);
        final Button emojiBtn3 = (Button) view.findViewById(R.id.btn3_emoji);
        emojiBtn1.setWidth(measuredWidth / 3);
        emojiBtn2.setWidth(measuredWidth / 3);
        emojiBtn3.setWidth(measuredWidth / 3);
        emojiBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiBtn1.setBackgroundColor(getActivity().getResources().getColor(R.color.background_grey));
                emojiBtn2.setBackgroundColor(getActivity().getResources().getColor(R.color.background_silver));
                emojiBtn3.setBackgroundColor(getActivity().getResources().getColor(R.color.background_silver));
                gvTab1.setVisibility(View.VISIBLE);
                gvTab2.setVisibility(View.GONE);
                gvTab3.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEtReplyMsg.getWindowToken(), 0);
            }
        });
        emojiBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiBtn1.setBackgroundColor(getActivity().getResources().getColor(R.color.background_silver));
                emojiBtn2.setBackgroundColor(getActivity().getResources().getColor(R.color.background_grey));
                emojiBtn3.setBackgroundColor(getActivity().getResources().getColor(R.color.background_silver));
                gvTab1.setVisibility(View.GONE);
                gvTab2.setVisibility(View.VISIBLE);
                gvTab3.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEtReplyMsg.getWindowToken(), 0);
            }
        });
        emojiBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiBtn1.setBackgroundColor(getActivity().getResources().getColor(R.color.background_silver));
                emojiBtn2.setBackgroundColor(getActivity().getResources().getColor(R.color.background_silver));
                emojiBtn3.setBackgroundColor(getActivity().getResources().getColor(R.color.background_grey));
                gvTab1.setVisibility(View.GONE);
                gvTab2.setVisibility(View.GONE);
                gvTab3.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEtReplyMsg.getWindowToken(), 0);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
        mPrePostAsyncTask.cancel(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(LOG_TAG, "onCreateOptionsMenu");

        menu.clear();
        inflater.inflate(R.menu.menu_reply, menu);

        if (menu.getItem(1).getTitle().equals("发送")) {
            // Disable and Enable send button
            if (mPrePostInfo == null) {
                menu.getItem(1).setEnabled(false);
            } else {
                menu.getItem(1).setEnabled(true);
            }
        }

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setTitle(R.string.action_reply);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        switch (mMode) {
            case PostAsyncTask.MODE_REPLY_THREAD:
                getActivity().getActionBar().setTitle("回复帖子");
                break;
            case PostAsyncTask.MODE_REPLY_POST:
                getActivity().getActionBar().setTitle("回复 " + mFloor + "#");
                break;
            case PostAsyncTask.MODE_QUOTE_POST:
                getActivity().getActionBar().setTitle("引用 " + mFloor + "#");
                break;
            case PostAsyncTask.MODE_NEW_THREAD:
                getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.action_new_thread));
                mSpForum.setVisibility(View.VISIBLE);
                mSpTypeIds.setVisibility(View.VISIBLE);
                mEtSubjectMsg.setVisibility(View.VISIBLE);
                break;
            case PostAsyncTask.MODE_EDIT_POST:
                getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.action_edit));
                break;
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(LOG_TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_upload_img:
                if (mPrePostInfo == null) {
                    Toast.makeText(getActivity(), "请等待信息收集结束再选择图片", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), SELECT_PICTURE);
                }
                return true;
            case R.id.action_post:
                //new thread or edit fist post
                if (mSpTypeIds.getVisibility() == View.VISIBLE && "6".equals(mFid) && "0".equals(mTypeid)) {
                    Toast.makeText(getActivity(), "B&S版发帖必须指定分类", Toast.LENGTH_LONG).show();
                    return true;
                }

                String subjectText = mEtSubjectMsg.getText().toString();
                if (mMode == PostAsyncTask.MODE_NEW_THREAD && subjectText.length() < 5) {
                    Toast.makeText(getActivity(), "主题字数必须大于5", Toast.LENGTH_LONG).show();
                    return true;
                }

                String replyText = mEtReplyMsg.getText().toString();
                if (replyText.length() < 5) {
                    Toast.makeText(getActivity(), "帖子内容字数必须大于5", Toast.LENGTH_LONG).show();
                    return true;
                }

                if (uploadImgButtons.size() > 0) {
                    boolean needWarn = false;
                    for (UploadImgButton uploadBtn : uploadImgButtons) {
                        if (isValidImgId(uploadBtn.getImgId())) {
                            String attachStr = "[attachimg]" + uploadBtn.getImgId() + "[/attachimg]";
                            if (!replyText.contains(attachStr)) {
                                needWarn = true;
                                uploadBtn.setTypeface(null, Typeface.BOLD);
                            }
                        }
                    }
                    if (needWarn) {
                        Toast.makeText(getActivity(), "有图片未添加到帖子中，长按可删除（保存后生效）", Toast.LENGTH_LONG).show();
                        return true;
                    }
                }

                //when edit post, pass floor number
                PostBean postBean = new PostBean();
                postBean.setContent(replyText);
                postBean.setTid(mTid);
                postBean.setPid(mPid);
                postBean.setFid(mFid);
                postBean.setTypeid(mTypeid);
                postBean.setSubject(subjectText);
                postBean.setFloor(mMode == PostAsyncTask.MODE_EDIT_POST ? mFloor : "");
                new PostAsyncTask(getActivity(), mMode, mPrePostInfo, postListener).execute(postBean);

                // Close SoftKeyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEtReplyMsg.getWindowToken(), 0);

                // Close reply fragment
                ((MainFrameActivity) getActivity()).popFragment(false);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(LOG_TAG, "onActivityResult");
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                } catch (Exception e) {
                    Log.v(LOG_TAG, "Exception", e);
                }

                ImageFileInfo imageFileInfo = CursorUtils.getImageFileInfo(getActivity(), selectedImageUri);
                String fileName = imageFileInfo.getFileName();

                final UploadImgButton uploadBtn = new UploadImgButton(getActivity(), this);
                uploadBtn.setImgName(fileName);
                uploadBtn.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (isValidImgId(uploadBtn.getImgId())) {
                            appendImage(uploadBtn.getImgId());
                            uploadBtn.setTypeface(null, Typeface.NORMAL);
                        } else {
                            Toast.makeText(getActivity(), "图片未成功上传", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                uploadBtn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (isValidImgId(uploadBtn.getImgId())) {
                            mPrePostInfo.removeAttach(uploadBtn.getImgId());
                            mPrePostInfo.addAttachdel(uploadBtn.getImgId());
                        }
                        uploadImgButtons.remove(uploadBtn);
                        uploadBtn.setVisibility(View.GONE);
                        if (!TextUtils.isEmpty(mEtReplyMsg.getText()) && isValidImgId(uploadBtn.getImgId()))
                            mEtReplyMsg.setText(mEtReplyMsg.getText().toString().replace("[attachimg]" + uploadBtn.getImgId() + "[/attachimg]", ""));
                        return true;
                    }
                });
                uploadImgButtons.add(uploadBtn);

                LinearLayout postLayout = (LinearLayout) getActivity().findViewById(R.id.layout_post);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                postLayout.addView(uploadBtn, 0, params);

                new UploadImgAsyncTask(getActivity(), uploadBtn, imageFileInfo, mPrePostInfo.getUid(), mPrePostInfo.getHash()).execute(bitmap);
            }
        }
    }

    public void appendImage(String imgId) {
        if (isValidImgId(imgId)) {
            mEtReplyMsg.getText().insert(mEtReplyMsg.getSelectionStart(), "[attachimg]" + imgId + "[/attachimg]");
            mPrePostInfo.addAttach(imgId);
        }
    }

    private class PrePostListener implements PrePostAsyncTask.PrePostListener {
        @Override
        public void PrePostComplete(int mode, boolean result,
                                    PrePostInfoBean info) {
            if (result) {
                mPrePostInfo = info;
                mTvAdditional.setVisibility(View.GONE);
                getActivity().invalidateOptionsMenu();

                if (mode == PostAsyncTask.MODE_NEW_THREAD) {
                    KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
                    List<String> typeids = info.getTypeidValues();
                    if (typeids != null && typeids.size() > 0) {
                        adapter.setEntryValues(info.getTypeidValues().toArray(new String[typeids.size()]));
                        adapter.setEntries(info.getTypeidNames().toArray(new String[typeids.size()]));
                        mSpTypeIds.setAdapter(adapter);
                        mSpTypeIds.setVisibility(View.VISIBLE);
                    } else {
                        String[] noNames = {"无分类"};
                        String[] noValues = {"0"};
                        adapter.setEntries(noNames);
                        adapter.setEntryValues(noValues);
                        mSpTypeIds.setAdapter(adapter);
                        mSpTypeIds.setVisibility(View.VISIBLE);
                        mSpTypeIds.setEnabled(false);
                    }
                }

                if (!TextUtils.isEmpty(info.getText())) {
                    if (mode == PostAsyncTask.MODE_EDIT_POST) {
                        mEtReplyMsg.setText(info.getText());
                        if (!TextUtils.isEmpty(info.getSubject())) {
                            mEtSubjectMsg.setText(info.getSubject());
                            mEtSubjectMsg.setVisibility(View.VISIBLE);
                        }
                        List<String> typeids = info.getTypeidValues();
                        if (typeids != null && typeids.size() > 0
                                && !TextUtils.isEmpty(info.getTypeid())) {
                            String typeid = info.getTypeid();
                            KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
                            adapter.setEntryValues(info.getTypeidValues().toArray(new String[typeids.size()]));
                            adapter.setEntries(info.getTypeidNames().toArray(new String[typeids.size()]));
                            mSpTypeIds.setAdapter(adapter);
                            mSpTypeIds.setVisibility(View.VISIBLE);
                            mSpForum.setVisibility(View.VISIBLE);
                            for (int i = 0; i < typeids.size(); i++) {
                                String tmpid = typeids.get(i);
                                if (tmpid.equals(typeid)) {
                                    mSpTypeIds.setSelection(i);
                                    break;
                                }
                            }
                        }
                    } else {
                        mTvAdditional.setText(info.getText());
                        mTvAdditional.setVisibility(View.VISIBLE);
                    }
                }

                if (mMode == PostAsyncTask.MODE_NEW_THREAD) {
                    (new Handler()).postDelayed(new Runnable() {
                        public void run() {
                            mEtSubjectMsg.requestFocus();
                            long t = SystemClock.uptimeMillis();
                            mEtSubjectMsg.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, 0, 0, 0));
                            mEtSubjectMsg.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, 0, 0, 0));
                        }
                    }, 200);
                } else {
                    (new Handler()).postDelayed(new Runnable() {
                        public void run() {
                            mEtReplyMsg.requestFocus();
                            long t = SystemClock.uptimeMillis();
                            mEtReplyMsg.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, 0, 0, 0));
                            mEtReplyMsg.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_UP, 0, 0, 0));
                        }
                    }, 200);
                }

            } else {
                mTvAdditional.setText("收集信息失败，请返回重试");
                mTvAdditional.setTextColor(getActivity().getResources().getColor(R.color.red));
            }
        }
    }

    private class FidSelectListener implements Spinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mFid = String.valueOf(HiUtils.getForumID(getActivity(), pos));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class TypeidSelectListener implements Spinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
            mTypeid = adapter.getEntryValue(pos);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private boolean isValidImgId(String imgId) {
        return !TextUtils.isEmpty(imgId)
                && TextUtils.isDigitsOnly(imgId)
                && imgId.length() > 5;
    }

}
