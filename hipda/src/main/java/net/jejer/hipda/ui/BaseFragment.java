package net.jejer.hipda.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

/**
 * a base fragment
 * Created by GreenSkinMonster on 2015-05-09.
 */
public abstract class BaseFragment extends Fragment {

    protected static final int FAB_ICON_SIZE_DP = 20;

    protected void setActionBarTitle(CharSequence title) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            String t = Utils.nullToText(title);
            if (actionBar != null && !t.equals(actionBar.getTitle())) {
                actionBar.setTitle(t);
            }
        }
    }

    void setActionBarTitle(@StringRes int resId) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(resId);
        }
    }

    void setActionBarDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        if (getActivity() != null && getActivity() instanceof MainFrameActivity) {
            ((MainFrameActivity) getActivity()).setDrawerHomeIdicator(showHomeAsUp);
        }
    }

    void syncActionBarState() {
        if (getActivity() != null) {
            Drawer drawerResult = ((MainFrameActivity) getActivity()).drawer;
            if (drawerResult != null)
                drawerResult.getActionBarDrawerToggle().syncState();
        }
    }

    void setDrawerSelection(int forumId) {
        //re-select forum on back
        if (getActivity() != null) {
            Drawer drawer = ((MainFrameActivity) getActivity()).drawer;
            if (drawer != null && !drawer.isDrawerOpen()) {
                int position = drawer.getStickyFooterPosition(forumId);
                if (drawer.getCurrentStickyFooterSelectedPosition() != position)
                    drawer.setStickyFooterSelectionAtPosition(position, false);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.v("onAttach : " + getClass().getName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.v("onDetach : " + getClass().getName());
    }

    public void scrollToTop() {
    }

    public void stopScroll() {
    }

    @Override
    public void onDestroyView() {
        stopScroll();
        super.onDestroyView();
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        //http://daniel-codes.blogspot.sg/2013/09/smoothing-performance-on-fragment.html
        Animator animator = super.onCreateAnimator(transit, enter, nextAnim);
        if (animator == null && nextAnim != 0) {
            animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
            if (animator != null) {
                if (getView() != null)
                    getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (getView() != null)
                            getView().setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
        }
        return animator;
    }

    void showSendSmsDialog(final String uid, final String username, final PostSmsAsyncTask.SmsPostListener listener) {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.dialog_send_sms, null);

        final EditText etSmsContent = (EditText) viewlayout.findViewById(R.id.et_sms_content);
        final EditText etRecipient = (EditText) viewlayout.findViewById(R.id.et_sms_receipient);
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());

        if (!TextUtils.isEmpty(uid)) {
            etRecipient.setVisibility(View.GONE);
        }

        popDialog.setTitle("发送短消息" + (!TextUtils.isEmpty(uid) ? "给 " + Utils.nullToText(username) : ""));
        popDialog.setView(viewlayout);
        popDialog.setPositiveButton("发送", null);
        popDialog.setNegativeButton("取消", null);
        final AlertDialog dialog = popDialog.create();

        if (etRecipient.getVisibility() == View.VISIBLE) {
            etRecipient.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean textNeeded = (etRecipient.getVisibility() == View.VISIBLE && TextUtils.isEmpty(etRecipient.getText()))
                            || TextUtils.isEmpty(etSmsContent.getText());
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!textNeeded);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        etSmsContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean textNeeded = (etRecipient.getVisibility() == View.VISIBLE && TextUtils.isEmpty(etRecipient.getText()))
                        || TextUtils.isEmpty(etSmsContent.getText());
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!textNeeded);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialog.show();

        Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String recipient = username;
                String content = Utils.nullToText(etSmsContent.getText().toString()).trim();
                if (TextUtils.isEmpty(uid))
                    recipient = etRecipient.getText().toString().trim();

                if (TextUtils.isEmpty(uid) && TextUtils.isEmpty(recipient)) {
                    Toast.makeText(getActivity(), "请填写收件人", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(getActivity(), "请填写内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                new PostSmsAsyncTask(getActivity(), uid, recipient, listener, dialog).execute(content);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSmsContent.getWindowToken(), 0);
            }
        });

        theButton.setEnabled(false);
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean popFragment() {
        return getActivity() != null && ((MainFrameActivity) getActivity()).popFragment();
    }

}
