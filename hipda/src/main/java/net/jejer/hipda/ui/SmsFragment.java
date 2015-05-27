package net.jejer.hipda.ui;

import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class SmsFragment extends BaseFragment implements PostSmsAsyncTask.PostListener {

    public static final String ARG_ID = "ID";
    public static final String ARG_UID = "UID";

    private String mId;
    private String mUid;
    private SmsAdapter mSmsAdapter;
    private List<SimpleListItemBean> mSmsBeans = new ArrayList<>();
    private SmsListLoaderCallbacks mLoaderCallbacks;
    private ListView mListView;

    private HiProgressDialog postProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.v("onCreate");
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_ID)) {
            mId = getArguments().getString(ARG_ID);
        }
        if (getArguments().containsKey(ARG_UID)) {
            mUid = getArguments().getString(ARG_UID);
        }

        mSmsAdapter = new SmsAdapter(getActivity(), getFragmentManager(), new AvatarOnClickListener());
        mLoaderCallbacks = new SmsListLoaderCallbacks();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.v("onCreateView");
        View view = inflater.inflate(R.layout.fragment_sms, container, false);
        mListView = (ListView) view.findViewById(R.id.lv_sms);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //to avoid click through this view
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SMS CONTENT FROM HiPDA", ((SimpleListItemBean) adapterView.getItemAtPosition(i)).getInfo());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), "短信息内容已经复制至粘贴板", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        ImageButton postIb = (ImageButton) view.findViewById(R.id.ib_send_sms);
        final EditText etSms = (EditText) view.findViewById(R.id.et_sms);
        etSms.setTextSize(HiSettingsHelper.getPostTextSize());
        postIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyText = etSms.getText().toString();
                if (replyText.length() > 0) {
                    new PostSmsAsyncTask(getActivity(), mUid, SmsFragment.this).execute(replyText);
                    // Close SoftKeyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etSms.getWindowToken(), 0);
                    etSms.setText("");
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.v("onActivityCreated");

        // destroyLoader called here to avoid onLoadFinished called when onResume
        getLoaderManager().destroyLoader(0);
        mListView.setAdapter(mSmsAdapter);
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.v("onCreateOptionsMenu");

        menu.clear();

        setActionBarDisplayHomeAsUpEnabled(true);
        setActionBarTitle("与" + mId + "的短消息");

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrePost() {
        postProgressDialog = HiProgressDialog.show(getActivity(), "正在发送...");
    }

    @Override
    public void onPostDone(int status, String message) {
        if (status == Constants.STATUS_SUCCESS) {
            //new sms has some delay, so this is a dirty hack
            new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    try {
                        getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
                    } catch (Exception ignored) {

                    }
                }
            }.start();
            postProgressDialog.dismiss(message);
        } else {
            postProgressDialog.dismissError(message);
        }

    }

    public class SmsListLoaderCallbacks implements LoaderManager.LoaderCallbacks<SimpleListBean> {

        @Override
        public Loader<SimpleListBean> onCreateLoader(int arg0, Bundle arg1) {
            return new SimpleListLoader(SmsFragment.this.getActivity(), SimpleListLoader.TYPE_SMSDETAIL, 1, mUid);
        }

        @Override
        public void onLoadFinished(Loader<SimpleListBean> loader,
                                   SimpleListBean list) {

            Logger.v("onLoadFinished enter");

            if (list == null || list.getCount() == 0) {
                Toast.makeText(SmsFragment.this.getActivity(),
                        "没有短消息", Toast.LENGTH_LONG).show();
                return;
            }

            mSmsBeans.clear();
            mSmsBeans.addAll(list.getAll());
            mSmsAdapter.setBeans(mSmsBeans);
            mListView.setSelection(mSmsAdapter.getCount());
        }

        @Override
        public void onLoaderReset(Loader<SimpleListBean> arg0) {
            Logger.v("onLoaderReset");
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

}
