package net.jejer.hipda.ui;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.R;
import net.jejer.hipda.async.HiStringRequest;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.async.VolleyHelper;
import net.jejer.hipda.bean.UserInfoBean;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.HiUtils;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class UserinfoFragment extends Fragment {
	private final String LOG_TAG = getClass().getSimpleName();

	public static final String ARG_USERNAME = "USERNAME";
	public static final String ARG_UID = "UID";

	private String mUid;
	private String mUsername;

	private NetworkImageView mAvatarView;
	private TextView mDetailView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(LOG_TAG, "onCreate");
		setHasOptionsMenu(true);

		if (getArguments().containsKey(ARG_USERNAME)) {
			mUsername = getArguments().getString(ARG_USERNAME);
		}

		if (getArguments().containsKey(ARG_UID)) {
			mUid = getArguments().getString(ARG_UID);
		}
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_userinfo, container, false);

		mAvatarView = (NetworkImageView)view.findViewById(R.id.userinfo_avatar);
		mAvatarView.setDefaultImageResId(R.drawable.google_user);
		mAvatarView.setErrorImageResId(R.drawable.google_user);

		TextView usernameTv = (TextView)view.findViewById(R.id.userinfo_username);
		usernameTv.setText(mUsername);

		mDetailView = (TextView)view.findViewById(R.id.userinfo_detail);
		mDetailView.setText("正在获取信息...");

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v(LOG_TAG, "onActivityCreated");

		StringRequest sReq = new HiStringRequest(getActivity(), HiUtils.UserInfoUrl+mUid, 
				new OnDetailLoadComplete(), 
				new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				mDetailView.setText("获取信息失败, 请重试.");
			}
		});
		VolleyHelper.getInstance().add(sReq);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.v(LOG_TAG, "onCreateOptionsMenu");

		menu.clear();
		inflater.inflate(R.menu.menu_userinfo, menu);

		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setTitle(mUsername);

		super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		Log.v(LOG_TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			// Implemented in activity
			return false;
		case R.id.action_send_sms:
			showSendSmsDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	class OnDetailLoadComplete implements Response.Listener<String> {
		@Override
		public void onResponse(String response) {
			UserInfoBean info = HiParser.parseUserInfo(response);
			if (info != null) {
				mAvatarView.setImageUrl(info.getmAvatarUrl(), VolleyHelper.getInstance().getImgLoader());
				mDetailView.setText(info.getmDetail());
			} else {
				mDetailView.setText("解析信息失败, 请重试.");
			}
		}
	}

	private void showSendSmsDialog() {
		final LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View viewlayout = inflater.inflate(R.layout.dialog_userinfo_sms, null);

		final EditText smsTextView = (EditText)viewlayout.findViewById(R.id.et_userinfo_sms);

		final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
		popDialog.setTitle("发送短消息给 "+mUsername);
		popDialog.setView(viewlayout);
		// Add the buttons
		popDialog.setPositiveButton("发送", 
				new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new PostSmsAsyncTask(getActivity(), mUid).execute(smsTextView.getText().toString());
			}});
		popDialog.setNegativeButton("取消", null);
		popDialog.create().show();
	}
}
