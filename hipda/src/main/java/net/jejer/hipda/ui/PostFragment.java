package net.jejer.hipda.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostAsyncTask;
import net.jejer.hipda.async.PrePostAsyncTask;
import net.jejer.hipda.async.UploadImgAsyncTask;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PostBean;
import net.jejer.hipda.utils.HiUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
	private int mMode;
	private TextView mTvAdditional;
	private TextView mTvSubjectMsg;
	private EditText mTvReplyMsg;
	private PrePostAsyncTask.PrePostListener mPrePostListener = new PrePostListener();
	private Map<String, List<String>> mPrePostInfo;
	private PrePostAsyncTask mPrePostAsyncTask;

	private Spinner mSpForum;

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

		mTvReplyMsg = (EditText) view.findViewById(R.id.et_reply);
		mTvAdditional = (TextView) view.findViewById(R.id.et_additional);
		mTvAdditional.setText("正在收集信息");

		mSpForum = (Spinner) view.findViewById(R.id.sp_fid);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.forums, android.R.layout.simple_list_item_1);
		mSpForum.setAdapter(adapter);
		if (mFid != null) {
			mSpForum.setSelection(HiUtils.getForumIndexByFid(getActivity(), mFid));
		}
		mSpForum.setOnItemSelectedListener(new FidSelectListener());

		mTvSubjectMsg = (TextView) view.findViewById(R.id.et_subject);

		mTvReplyMsg.setTextSize(HiSettingsHelper.getPostTextSize());
		mTvAdditional.setTextSize(HiSettingsHelper.getPostTextSize());

		// Prepare emoji tabs
		TabHost th = (TabHost) view.findViewById(R.id.th_emoji);
		th.setup();

		TabSpec tabSpec1 = th.newTabSpec("tab1");
		tabSpec1.setIndicator("默认表情");
		tabSpec1.setContent(R.id.tab1_emoji);
		th.addTab(tabSpec1);

		TabSpec tabSpec2 = th.newTabSpec("tab2");
		tabSpec2.setIndicator("酷酷猴");
		tabSpec2.setContent(R.id.tab2_emoji);
		th.addTab(tabSpec2);

		TabSpec tabSpec3 = th.newTabSpec("tab3");
		tabSpec3.setIndicator("呆呆男");
		tabSpec3.setContent(R.id.tab3_emoji);
		th.addTab(tabSpec3);

		// Prepare emoji icons
		final ExpandableHeightGridView gvTab1 = (ExpandableHeightGridView) th.findViewById(R.id.tab1_emoji);
		gvTab1.setExpanded(true);
		gvTab1.setAdapter(new EmojiAdapter(getActivity(), 1));
		gvTab1.setOnItemClickListener(new GridView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {
				mTvReplyMsg.getText().insert(mTvReplyMsg.getSelectionStart(), (String) gvTab1.getAdapter().getItem(position));
			}
		});
		final ExpandableHeightGridView gvTab2 = (ExpandableHeightGridView) th.findViewById(R.id.tab2_emoji);
		gvTab2.setExpanded(true);
		gvTab2.setAdapter(new EmojiAdapter(getActivity(), 2));
		gvTab2.setOnItemClickListener(new GridView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {
				mTvReplyMsg.getText().insert(mTvReplyMsg.getSelectionStart(), (String) gvTab2.getAdapter().getItem(position));
			}
		});
		final ExpandableHeightGridView gvTab3 = (ExpandableHeightGridView) th.findViewById(R.id.tab3_emoji);
		gvTab3.setExpanded(true);
		gvTab3.setAdapter(new EmojiAdapter(getActivity(), 3));
		gvTab3.setOnItemClickListener(new GridView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {
				mTvReplyMsg.getText().insert(mTvReplyMsg.getSelectionStart(), (String) gvTab3.getAdapter().getItem(position));
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
				mTvSubjectMsg.setVisibility(View.VISIBLE);
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
				String replyText = mTvReplyMsg.getText().toString();
				if (replyText.length() < 5) {
					Toast.makeText(getActivity(), "字数必须大于5", Toast.LENGTH_LONG).show();
					return true;
				}
				String subjectText = mTvSubjectMsg.getText().toString();
				if (mMode == PostAsyncTask.MODE_NEW_THREAD && subjectText.length() < 5) {
					Toast.makeText(getActivity(), "主题字数必须大于5", Toast.LENGTH_LONG).show();
					return true;
				}
				//when edit post, pass floor number
				PostBean postBean = new PostBean();
				postBean.setContent(replyText);
				postBean.setTid(mTid);
				postBean.setPid(mPid);
				postBean.setFid(mFid);
				postBean.setSubject(subjectText);
				postBean.setFloor(mMode == PostAsyncTask.MODE_EDIT_POST ? mFloor : "");
				new PostAsyncTask(getActivity(), mMode, mPrePostInfo, postListener).execute(postBean);

				// Close SoftKeyboard
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mTvReplyMsg.getWindowToken(), 0);

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
				Log.v(LOG_TAG, selectedImageUri.toString());
				Log.v(LOG_TAG, selectedImageUri.getPath());

				Bitmap bitmap = null;
				try {
					bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
					Log.v(LOG_TAG, String.valueOf(bitmap.getByteCount()));
				} catch (Exception e) {
					Log.v(LOG_TAG, "Exception", e);
				}

				final UploadImgButton b = new UploadImgButton(getActivity());
				b.setImgName(getImgName(selectedImageUri));
				b.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						mTvReplyMsg.getText().insert(mTvReplyMsg.getSelectionStart(), "[attachimg]" + b.getImgId() + "[/attachimg]");
						// Add attach id for post
						mPrePostInfo.get("attaches").add(b.getImgId());
					}
				});

				LinearLayout postLayout = (LinearLayout) getActivity().findViewById(R.id.layout_post);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				postLayout.addView(b, 0, params);

				new UploadImgAsyncTask(getActivity(), b, mPrePostInfo.get("uid").get(0), mPrePostInfo.get("hash").get(0)).execute(bitmap);
			}
		}
	}

	private String getImgName(Uri uri) {
		if (uri.getPath().contains(":")) {
			// Split at colon, use second item in the array
			String id = uri.getPath().split(":")[1];

			String[] column = {MediaStore.Images.Media.DATA};

			// where id is equal to
			String sel = MediaStore.Images.Media._ID + "=?";

			CursorLoader cursorloader = new CursorLoader(getActivity(),
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					column,
					sel,
					new String[]{id},
					null);
			Cursor cursor = cursorloader.loadInBackground();

			if (cursor.moveToFirst()) {
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					Log.v(cursor.getColumnName(i), cursor.getString(i));
					String name = cursor.getString(i).toLowerCase(Locale.getDefault());
					if (name.endsWith("jpg")
							|| name.endsWith("jpeg")
							|| name.endsWith("png")
							|| name.endsWith("bmp")
							|| name.endsWith("gif")) {
						if (name.contains("/")) {
							return name.substring(name.lastIndexOf("/") + 1, name.length());
						}
					}
				}
			}

			cursor.close();
		} else {
			CursorLoader cursorloader = new CursorLoader(getActivity(),
					uri,
					null,
					null,
					null,
					null);
			Cursor cursor = cursorloader.loadInBackground();

			if (cursor.moveToFirst()) {
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					Log.v(cursor.getColumnName(i), cursor.getString(i));
					String name = cursor.getString(i).toLowerCase(Locale.getDefault());
					if (name.endsWith("jpg")
							|| name.endsWith("jpeg")
							|| name.endsWith("png")
							|| name.endsWith("bmp")
							|| name.endsWith("gif")) {
						if (name.contains("/")) {
							return name.substring(name.lastIndexOf("/") + 1, name.length());
						}
					}
				}
			}

		}
		return null;
	}

	private class PrePostListener implements PrePostAsyncTask.PrePostListener {
		@Override
		public void PrePostComplete(int mode, boolean result,
									Map<String, List<String>> info) {
			if (result) {
				mPrePostInfo = info;
				mTvAdditional.setVisibility(View.GONE);
				getActivity().invalidateOptionsMenu();
				if (!info.get("text").isEmpty() && !info.get("text").get(0).isEmpty()) {
					if (mode == PostAsyncTask.MODE_EDIT_POST) {
						mTvReplyMsg.setText(info.get("text").get(0));
						if (!info.get("subject").isEmpty() && !info.get("subject").get(0).isEmpty()) {
							mTvSubjectMsg.setText(info.get("subject").get(0));
							mTvSubjectMsg.setVisibility(View.VISIBLE);
						}
					} else {
						mTvAdditional.setText(info.get("text").get(0));
						mTvAdditional.setVisibility(View.VISIBLE);
					}
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
			//parent.getItemAtPosition(pos);
			mFid = String.valueOf(HiUtils.getForumID(getActivity(), pos));
			Log.v("FID", mFid);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}
}
