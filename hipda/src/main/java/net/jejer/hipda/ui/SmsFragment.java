package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.okhttp.Request;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SmsFragment extends BaseFragment implements PostSmsAsyncTask.SmsPostListener {

    public static final String ARG_AUTHOR = "AUTHOR";
    public static final String ARG_UID = "UID";

    private String mAuthor;
    private String mUid;
    private SmsAdapter mSmsAdapter;
    private List<SimpleListItemBean> mSmsBeans = new ArrayList<>();
    private SmsListLoaderCallbacks mLoaderCallbacks;
    private ListView mListView;
    private EditText mEtSms;

    private HiProgressDialog postProgressDialog;
    private ContentLoadingProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_AUTHOR)) {
            mAuthor = getArguments().getString(ARG_AUTHOR);
        }
        if (getArguments().containsKey(ARG_UID)) {
            mUid = getArguments().getString(ARG_UID);
        }

        mSmsAdapter = new SmsAdapter(this, new AvatarOnClickListener());
        mLoaderCallbacks = new SmsListLoaderCallbacks();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms, container, false);
        mListView = (ListView) view.findViewById(R.id.lv_sms);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        loadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.sms_loading);

        //to avoid click through this view
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                CharSequence content = Utils.fromHtmlAndStrip(((SimpleListItemBean) adapterView.getItemAtPosition(i)).getInfo());
                if (content.length() > 0) {
                    ClipData clip = ClipData.newPlainText("SMS CONTENT FROM HiPDA", content);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), "短消息内容已经复制至粘贴板", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "短消息内容内容为空", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        ImageButton postIb = (ImageButton) view.findViewById(R.id.ib_send_sms);
        postIb.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_mail_send).sizeDp(28).color(Color.GRAY));

        mEtSms = (EditText) view.findViewById(R.id.et_sms);
        mEtSms.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        postIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyText = mEtSms.getText().toString();
                if (replyText.length() > 0) {
                    new PostSmsAsyncTask(getActivity(), mUid, null, SmsFragment.this, null).execute(replyText);
                    // Close SoftKeyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtSms.getWindowToken(), 0);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.v("onActivityCreated");

        mListView.setAdapter(mSmsAdapter);

        // destroyLoader called here to avoid onLoadFinished called when onResume
        getLoaderManager().destroyLoader(0);
        if (mSmsAdapter.getCount() == 0) {
            loadingProgressBar.show();
            getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.v("onCreateOptionsMenu");
        menu.clear();

        inflater.inflate(R.menu.menu_sms_detail, menu);
        menu.findItem(R.id.action_clear_sms)
                .setIcon(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_trash).actionBar().color(Color.WHITE));

        setActionBarDisplayHomeAsUpEnabled(true);
        setActionBarTitle("短消息 > " + mAuthor);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Implemented in activity
                return false;
            case R.id.action_clear_sms:
                showClearSmsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showClearSmsDialog() {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        popDialog.setTitle("清空短消息？");
        popDialog.setMessage(Html.fromHtml("确认清空所有与用户 <b>" + mAuthor + "</b> 的短消息？<br><br><font color=red>注意：此操作不可恢复。</font>"));
        popDialog.setPositiveButton("清空",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final HiProgressDialog progress = HiProgressDialog.show(getActivity(), "正在处理...");

                        String url = HiUtils.ClearSMS.replace("{uid}", mUid);

                        OkHttpHelper.getInstance().asyncGet(url, new OkHttpHelper.ResultCallback() {
                            @Override
                            public void onError(Request request, Exception e) {
                                progress.dismissError("操作时发生错误 : " + OkHttpHelper.getErrorMessage(e));
                                popFragment();
                                FragmentManager fm = getActivity().getFragmentManager();
                                Fragment fragment = fm.findFragmentByTag(SimpleListFragment.class.getName());
                                if (fragment != null && fragment instanceof SimpleListFragment) {
                                    ((SimpleListFragment) fragment).onRefresh();
                                }
                            }

                            @Override
                            public void onResponse(String response) {
                                progress.dismiss("操作完成");
                                popFragment();
                                FragmentManager fm = getActivity().getFragmentManager();
                                Fragment fragment = fm.findFragmentByTag(SimpleListFragment.class.getName());
                                if (fragment != null && fragment instanceof SimpleListFragment) {
                                    ((SimpleListFragment) fragment).onRefresh();
                                }
                            }
                        });

                    }
                });
        popDialog.setIcon(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_warning).sizeDp(24).color(Color.RED));
        popDialog.setNegativeButton("取消", null);
        popDialog.create().show();
    }

    @Override
    public void onSmsPrePost() {
        postProgressDialog = HiProgressDialog.show(getActivity(), "正在发送...");
    }

    @Override
    public void onSmsPostDone(int status, final String message, AlertDialog dialog) {
        if (status == Constants.STATUS_SUCCESS) {
            mEtSms.setText("");
            //new sms has some delay, so this is a dirty hack
            new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    try {
                        getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
                    } catch (Exception ignored) {

                    }
                    postProgressDialog.dismiss(message);
                }
            }.start();

        } else {
            postProgressDialog.dismissError(message);
        }
    }

    public class SmsListLoaderCallbacks implements LoaderManager.LoaderCallbacks<SimpleListBean> {

        @Override
        public Loader<SimpleListBean> onCreateLoader(int arg0, Bundle arg1) {
            return new SimpleListLoader(SmsFragment.this.getActivity(), SimpleListLoader.TYPE_SMS_DETAIL, 1, mUid);
        }

        @Override
        public void onLoadFinished(Loader<SimpleListBean> loader,
                                   SimpleListBean list) {

            Logger.v("onLoadFinished enter");
            loadingProgressBar.hide();

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

            FragmentUtils.showSpace(getFragmentManager(), false, uid, username);

        }
    }

}
