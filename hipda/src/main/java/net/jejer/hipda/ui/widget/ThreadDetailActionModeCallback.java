package net.jejer.hipda.ui.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.PostActivity;
import net.jejer.hipda.ui.PostFragment;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.UIUtils;

public class ThreadDetailActionModeCallback implements ActionMode.Callback {
    private ThreadDetailFragment mFragment;
    private String mTid;
    private int mFid;
    private String mTitle;
    private DetailBean mDetailBean;

    public ThreadDetailActionModeCallback(ThreadDetailFragment fragment, int fid, String tid, String title, DetailBean detailBean) {
        mFragment = fragment;
        mFid = fid;
        mTid = tid;
        mTitle = title;
        mDetailBean = detailBean;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_edit:
                if (HiSettingsHelper.getInstance().getUsername().equalsIgnoreCase(mDetailBean.getAuthor())
                        || HiSettingsHelper.getInstance().getUid().equals(mDetailBean.getUid())) {

                    intent = new Intent(mFragment.getActivity(), PostActivity.class);
                    intent.putExtra(PostFragment.ARG_FID_KEY, mFid);
                    intent.putExtra(PostFragment.ARG_TID_KEY, mTid);
                    intent.putExtra(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
                    intent.putExtra(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
                    intent.putExtra(PostFragment.ARG_MODE_KEY, PostHelper.MODE_EDIT_POST);
                    intent.putExtra(PostFragment.ARG_PARENT_ID, mFragment.mSessionId);
                    ActivityCompat.startActivity(mFragment.getActivity(), intent, null);
                }
                mode.finish();
                return true;
            case R.id.action_reply:
                intent = new Intent(mFragment.getActivity(), PostActivity.class);
                intent.putExtra(PostFragment.ARG_TID_KEY, mTid);
                intent.putExtra(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
                intent.putExtra(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
                intent.putExtra(PostFragment.ARG_FLOOR_AUTHOR_KEY, mDetailBean.getAuthor());
                intent.putExtra(PostFragment.ARG_MODE_KEY, PostHelper.MODE_REPLY_POST);
                intent.putExtra(PostFragment.ARG_TEXT_KEY, mDetailBean.getContents().getCopyText());
                intent.putExtra(PostFragment.ARG_PARENT_ID, mFragment.mSessionId);
                ActivityCompat.startActivity(mFragment.getActivity(), intent, null);

                mode.finish();
                return true;
            case R.id.action_quote:
                intent = new Intent(mFragment.getActivity(), PostActivity.class);
                intent.putExtra(PostFragment.ARG_TID_KEY, mTid);
                intent.putExtra(PostFragment.ARG_PID_KEY, mDetailBean.getPostId());
                intent.putExtra(PostFragment.ARG_FLOOR_KEY, mDetailBean.getFloor());
                intent.putExtra(PostFragment.ARG_FLOOR_AUTHOR_KEY, mDetailBean.getAuthor());
                intent.putExtra(PostFragment.ARG_MODE_KEY, PostHelper.MODE_QUOTE_POST);
                intent.putExtra(PostFragment.ARG_TEXT_KEY, mDetailBean.getContents().getCopyText());
                intent.putExtra(PostFragment.ARG_PARENT_ID, mFragment.mSessionId);
                ActivityCompat.startActivity(mFragment.getActivity(), intent, null);

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
            case R.id.action_only_floor_author:
                if (mFragment.isInAuthorOnlyMode()) {
                    mFragment.cancelAuthorOnlyMode();
                } else {
                    mFragment.enterAuthorOnlyMode(mDetailBean.getUid());
                }
                mode.finish();
                return true;
            case R.id.action_select_text:
                if (mFragment.getActivity() != null) {
                    UIUtils.showMessageDialog(mFragment.getActivity(),
                            mDetailBean.getFloor() + "# " + mDetailBean.getAuthor(),
                            mDetailBean.getContents().getCopyText().trim(),
                            true);
                }
                mode.finish();
                return true;
            case R.id.action_share_post:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "帖子 ：" + mTitle + "\n" +
                        HiUtils.RedirectToPostUrl.replace("{tid}", mTid).replace("{pid}", mDetailBean.getPostId()) + "\n" +
                        mDetailBean.getFloor() + "#  作者 ：" + mDetailBean.getAuthor() + "\n\n" +
                        mDetailBean.getContents().getCopyText();
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
        menu.findItem(R.id.action_reply).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_reply).actionBar().color(Color.WHITE));
        menu.findItem(R.id.action_quote).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_format_quote).actionBar().color(Color.WHITE));
        menu.findItem(R.id.action_copy).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_content_copy).actionBar().color(Color.WHITE));
        menu.findItem(R.id.action_share_post).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_share).actionBar().color(Color.WHITE));
        menu.findItem(R.id.action_select_text).setIcon(new IconicsDrawable(mFragment.getActivity(), GoogleMaterial.Icon.gmd_text_format).actionBar().color(Color.WHITE));

        if (!HiSettingsHelper.getInstance().getUsername().equalsIgnoreCase(mDetailBean.getAuthor())
                && !HiSettingsHelper.getInstance().getUid().equals(mDetailBean.getUid())) {
            MenuItem item = menu.findItem(R.id.action_edit);
            item.setVisible(false);
        }

        MenuItem menuItemAuthor = menu.findItem(R.id.action_only_floor_author);
        if (mFragment.isInAuthorOnlyMode()) {
            menuItemAuthor.setTitle(R.string.action_show_all);
        } else {
            menuItemAuthor.setTitle(R.string.action_only_floor_author);
        }

        mode.setTitle(mDetailBean.getFloor() + "# " + mDetailBean.getAuthor());
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = mFragment.getActivity().getWindow();
            w.setStatusBarColor(Color.BLACK);
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = mFragment.getActivity().getWindow();
            w.setStatusBarColor(ColorHelper.getColorPrimaryDark(mFragment.getActivity()));
        }
    }

}
