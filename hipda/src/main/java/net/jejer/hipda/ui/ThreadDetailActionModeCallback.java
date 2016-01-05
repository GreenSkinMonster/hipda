package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostAsyncTask;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;

public class ThreadDetailActionModeCallback implements ActionMode.Callback {
    private ThreadDetailFragment mFragment;
    private String mTid;
    private String mFid;
    private DetailBean mDetailBean;

    public ThreadDetailActionModeCallback(ThreadDetailFragment fragment, String fid, String tid, DetailBean detailBean) {
        mFragment = fragment;
        mFid = fid;
        mTid = tid;
        mDetailBean = detailBean;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        Bundle arguments = new Bundle();
        PostFragment fragment = new PostFragment();

        switch (item.getItemId()) {
            case R.id.action_edit:
                if (mDetailBean.getAuthor().equals(HiSettingsHelper.getInstance().getUsername())) {
                    mFragment.setHasOptionsMenu(false);

                    arguments.putString(PostFragment.ARG_FID_KEY, mFid);
                    arguments.putString(PostFragment.ARG_TID_KEY, mTid);
                    arguments.putString(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
                    arguments.putString(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
                    arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_EDIT_POST);

                    fragment.setArguments(arguments);
                    fragment.setPostListener(mFragment);

                    mFragment.getFragmentManager().beginTransaction()
                            .add(R.id.main_frame_container, fragment, PostFragment.class.getName())
                            .addToBackStack(PostFragment.class.getName())
                            .commit();
                    mode.finish();
                    return true;
                }
                break;
            case R.id.action_reply:
                mFragment.setHasOptionsMenu(false);

                arguments.putString(PostFragment.ARG_TID_KEY, mTid);
                arguments.putString(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
                arguments.putString(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
                arguments.putString(PostFragment.ARG_FLOOR_AUTHOR_KEY, mDetailBean.getAuthor());
                arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_REPLY_POST);

                fragment.setArguments(arguments);
                fragment.setPostListener(mFragment);

                mFragment.getFragmentManager().beginTransaction()
                        .add(R.id.main_frame_container, fragment, PostFragment.class.getName())
                        .addToBackStack(PostFragment.class.getName())
                        .commit();
                mode.finish();
                return true;
            case R.id.action_quote:
                mFragment.setHasOptionsMenu(false);

                arguments.putString(PostFragment.ARG_TID_KEY, mTid);
                arguments.putString(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
                arguments.putString(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
                arguments.putString(PostFragment.ARG_FLOOR_AUTHOR_KEY, mDetailBean.getAuthor());
                arguments.putInt(PostFragment.ARG_MODE_KEY, PostAsyncTask.MODE_QUOTE_POST);

                fragment.setArguments(arguments);
                fragment.setPostListener(mFragment);

                mFragment.getFragmentManager().beginTransaction()
                        .add(R.id.main_frame_container, fragment, PostFragment.class.getName())
                        .addToBackStack(PostFragment.class.getName())
                        .commit();
                mode.finish();
                return true;
            case R.id.action_copy:
                if (mFragment.getActivity() != null) {
                    ClipboardManager clipboard = (ClipboardManager) mFragment.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("COPY FROM HiPDA", mDetailBean.getContents().getCopyText());
                    clipboard.setPrimaryClip(clip);
                }
                mode.finish();
                return true;
            case R.id.action_select_text:
                if (mFragment.getActivity() != null) {
                    showSelectTextDialog(mFragment.getActivity(), mDetailBean);
                }
                mode.finish();
                return true;
            case R.id.action_share_post:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = HiUtils.DetailListUrl + mTid + "\n"
                        + mDetailBean.getFloor() + "#  作者：" + mDetailBean.getAuthor() + "\n\n"
                        + mDetailBean.getContents().getCopyText();
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                mFragment.startActivity(Intent.createChooser(sharingIntent, "分享文字内容"));
                mode.finish();
                return true;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.clear();

        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu_thread_detail, menu);

        menu.findItem(R.id.action_edit).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_edit).actionBar().color(Color.WHITE));
        menu.findItem(R.id.action_reply).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_mail_reply).actionBar().color(Color.WHITE));
        menu.findItem(R.id.action_quote).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_format_quote).actionBar().color(Color.WHITE));
        menu.findItem(R.id.action_copy).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_copy).actionBar().color(Color.WHITE));
        menu.findItem(R.id.action_share_post).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_share).actionBar().color(Color.WHITE));

        if (!mDetailBean.getAuthor().equalsIgnoreCase(HiSettingsHelper.getInstance().getUsername())) {
            MenuItem item = menu.findItem(R.id.action_edit);
            item.setVisible(false);
        }

        mode.setTitle(mDetailBean.getFloor() + "# " + mDetailBean.getAuthor());
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    private void showSelectTextDialog(Activity activity, DetailBean mDetailBean) {

        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewlayout = inflater.inflate(R.layout.item_select_text, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        final TextView tvTitle = (TextView) viewlayout.findViewById(R.id.tv_select_text_title);
        tvTitle.setText(mDetailBean.getFloor() + "# " + mDetailBean.getAuthor());
        tvTitle.setTextSize(HiSettingsHelper.getInstance().getTitleTextSize());

        final EditText etText = (EditText) viewlayout.findViewById(R.id.et_select_text);
        etText.setText(mDetailBean.getContents().getCopyText().trim());
        etText.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());

        alertDialog.setView(viewlayout);

        alertDialog.setNegativeButton(activity.getResources().getString(android.R.string.cancel), null);

        alertDialog.show();

        etText.requestFocus();
    }

}
