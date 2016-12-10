package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostSmsAsyncTask;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.UUID;

/**
 * a base fragment
 * Created by GreenSkinMonster on 2015-05-09.
 */
public abstract class BaseFragment extends Fragment {

    public String mSessionId;
    protected EmojiPopup mEmojiPopup;
    protected IconicsDrawable mKeyboardDrawable;
    protected IconicsDrawable mFaceDrawable;
    protected FloatingActionButton mMainFab;
    protected FloatingActionButton mNotificationFab;

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
                int position = drawer.getPosition(forumId);
                if (drawer.getCurrentSelectedPosition() != position)
                    drawer.setSelectionAtPosition(position, false);
            }
        }
    }

    void setupFab() {
        if (getActivity() != null) {
            if (mMainFab != null) {
                mMainFab.hide();
                mMainFab.setEnabled(false);
            }
            if (mNotificationFab != null) {
                mNotificationFab.hide();
                mNotificationFab.setEnabled(false);
            }
        }
    }

    public boolean isAppBarCollapsible() {
        return this instanceof ThreadListFragment
                || this instanceof ThreadDetailFragment
                || this instanceof SimpleListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionId = UUID.randomUUID().toString();
        setRetainInstance(true);

        MainFrameActivity mainActivity = ((MainFrameActivity) getActivity());
        mMainFab = mainActivity.getMainFab();
        mNotificationFab = mainActivity.getNotificationFab();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupFab();
    }

    @Override
    public void onDestroy() {
        if (mEmojiPopup != null)
            mEmojiPopup.cleanup();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        setupFab();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void scrollToTop() {
    }

    public void stopScroll() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        stopScroll();
        super.onDestroyView();
    }

    /*
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
    */

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
                UIUtils.hideSoftKeyboard(getActivity());
            }
        });

        theButton.setEnabled(false);
    }

    protected void showLoginDialog() {
        if (isAdded()) {
            LoginDialog dialog = LoginDialog.getInstance(getActivity());
            if (dialog != null) {
                dialog.setTitle("用户登录");
                dialog.show();
            }
        }
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean popFragment() {
        return getActivity() != null && ((MainFrameActivity) getActivity()).popFragment();
    }

    protected void setUpEmojiPopup(final EmojiEditText mEtContent, final ImageButton mIbEmojiSwitch) {
        if (mKeyboardDrawable == null)
            mKeyboardDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_keyboard).sizeDp(28).color(Color.GRAY);
        if (mFaceDrawable == null)
            mFaceDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_tag_faces).sizeDp(28).color(Color.GRAY);

        mIbEmojiSwitch.setImageDrawable(mFaceDrawable);
        mIbEmojiSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmojiPopup.toggle();
            }
        });

        mEtContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmojiPopup.isShowing())
                    mEmojiPopup.dismiss();
            }
        });

        mEmojiPopup = ((MainFrameActivity) getActivity()).getEmojiBuilder()
                .setOnEmojiClickedListener(new OnEmojiClickedListener() {
                    @Override
                    public void onEmojiClicked(final Emoji emoji) {
                        mEtContent.requestFocus();
                    }
                }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        mIbEmojiSwitch.setImageDrawable(mKeyboardDrawable);
                    }
                }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        mIbEmojiSwitch.setImageDrawable(mFaceDrawable);
                    }
                }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
                    @Override
                    public void onKeyboardClose() {
                        mEmojiPopup.dismiss();
                    }
                }).build(mEtContent);
    }

}
