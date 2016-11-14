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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vanniktech.emoji.EmojiEditText;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.adapter.RecyclerItemClickListener;
import net.jejer.hipda.ui.adapter.SmsAdapter;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HtmlCompat;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class SmsFragment extends BaseFragment implements PostSmsAsyncTask.SmsPostListener {

    public static final String ARG_AUTHOR = "AUTHOR";
    public static final String ARG_UID = "UID";

    private String mAuthor;
    private String mUid;
    private SmsAdapter mSmsAdapter;
    private List<SimpleListItemBean> mSmsBeans = new ArrayList<>();
    private SmsListLoaderCallbacks mLoaderCallbacks;
    private RecyclerView mRecyclerView;

    private EmojiEditText mEtSms;
    private ImageButton mIbEmojiSwitch;
    private ImageButton mIbSendSms;

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

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getActivity(), new OnItemClickListener());
        mSmsAdapter = new SmsAdapter(this, new AvatarOnClickListener(), itemClickListener);
        mLoaderCallbacks = new SmsListLoaderCallbacks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.lv_sms);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        loadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.sms_loading);

        //to avoid click through this view
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mIbSendSms = (ImageButton) view.findViewById(R.id.ib_send_sms);
        mIbSendSms.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_send).sizeDp(28).color(Color.GRAY));

        mEtSms = (EmojiEditText) view.findViewById(R.id.et_sms);
        mEtSms.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
        mIbSendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyText = mEtSms.getText().toString();
                if (replyText.length() > 0) {
                    new PostSmsAsyncTask(getActivity(), mUid, null, SmsFragment.this, null).execute(replyText);
                    UIUtils.hideSoftKeyboard(getActivity());
                }
            }
        });

        mIbEmojiSwitch = (ImageButton) view.findViewById(R.id.ib_emoji_switch);
        setUpEmojiPopup(mEtSms, mIbEmojiSwitch);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setAdapter(mSmsAdapter);

        // destroyLoader called here to avoid onLoadFinished called when onResume
        getLoaderManager().destroyLoader(0);
        if (mSmsAdapter.getItemCount() == 0) {
            loadingProgressBar.show();
            getLoaderManager().restartLoader(0, null, mLoaderCallbacks).forceLoad();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
        popDialog.setMessage(HtmlCompat.fromHtml("确认清空所有与用户 <b>" + mAuthor + "</b> 的短消息？<br><br><font color=red>注意：此操作不可恢复。</font>"));
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
        popDialog.setIcon(new IconicsDrawable(getActivity(), FontAwesome.Icon.faw_exclamation_circle).sizeDp(24).color(Color.RED));
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
        public void onLoadFinished(Loader<SimpleListBean> loader, SimpleListBean list) {
            loadingProgressBar.hide();

            if (list == null || list.getCount() == 0) {
                Toast.makeText(SmsFragment.this.getActivity(),
                        "没有短消息", Toast.LENGTH_LONG).show();
                return;
            }

            mSmsBeans.clear();
            mSmsBeans.addAll(list.getAll());
            mSmsAdapter.setDatas(mSmsBeans);
        }

        @Override
        public void onLoaderReset(Loader<SimpleListBean> arg0) {
        }
    }

    class AvatarOnClickListener extends OnSingleClickListener {
        @Override
        public void onSingleClick(View view) {
            String uid = (String) view.getTag(R.id.avatar_tag_uid);
            String username = (String) view.getTag(R.id.avatar_tag_username);

            FragmentUtils.showSpace(getFragmentManager(), false, uid, username);
        }
    }

    private class OnItemClickListener implements RecyclerItemClickListener.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {
        }

        @Override
        public void onLongItemClick(View view, int position) {
            SimpleListItemBean bean = mSmsAdapter.getItem(position);
            if (bean != null) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                CharSequence content = Utils.fromHtmlAndStrip(bean.getInfo());
                if (content.length() > 0) {
                    ClipData clip = ClipData.newPlainText("SMS CONTENT FROM HiPDA", content);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), "短消息内容已经复制至粘贴板", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "短消息内容内容为空", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onDoubleTap(View view, int position) {
        }
    }

    @Override
    public void scrollToTop() {
        if (mSmsAdapter != null && mSmsAdapter.getItemCount() > 0)
            mRecyclerView.scrollToPosition(0);
    }
}
