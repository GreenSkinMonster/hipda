package net.jejer.hipda.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.Forum;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by GreenSkinMonster on 2017-06-07.
 */

public class ForumSelectListener extends OnPreferenceClickListener {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<ForumStatus> mForumSelctions;
    private View.OnClickListener mOnClickListener;

    ForumSelectListener(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public boolean onPreferenceSingleClick(final Preference preference) {

        RecyclerView recyclerView = new RecyclerView(mContext);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);

        List<Integer> selectedForums = HiSettingsHelper.getInstance().getForums();
        mForumSelctions = new ArrayList<>(HiUtils.FORUMS.length);
        for (int fid : selectedForums) {
            mForumSelctions.add(new ForumStatus(HiUtils.getForumByFid(fid), true));
        }
        for (Forum forum : HiUtils.FORUMS) {
            if (!selectedForums.contains(forum.getId()))
                mForumSelctions.add(new ForumStatus(forum, false));
        }

        final RvAdapter adapter = new RvAdapter();

        ItemTouchHelper.Callback ithCallback = new ItemTouchHelper.Callback() {
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Collections.swap(mForumSelctions, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        };

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof CompoundButton) {
                    int position = (Integer) v.getTag();
                    mForumSelctions.get(position).mEnabled = ((CompoundButton) v).isChecked();
                }
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(ithCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(recyclerView);
        builder.setTitle(preference.getTitle());

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Integer> forums = new ArrayList<>();
                for (ForumStatus forumStatus : mForumSelctions) {
                    if (forumStatus.mEnabled) {
                        forums.add(forumStatus.mForum.getId());
                    }
                }
                if (forums.size() > 0) {
                    HiSettingsHelper.getInstance().setForums(forums);
                    preference.setSummary(HiUtils.getForumsSummary());
                } else {
                    Toast.makeText(mContext, "至少选择一个版面", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }

    private class RvAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.item_forum_selector, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolder) {
                CheckBox checkBox = ((ViewHolder) holder).cb_forum_enabled;
                checkBox.setText(mForumSelctions.get(position).mForum.getName());
                checkBox.setChecked(mForumSelctions.get(position).mEnabled);
                checkBox.setTag(position);
                checkBox.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            return mForumSelctions != null ? mForumSelctions.size() : 0;
        }
    }

    private class ForumStatus {
        Forum mForum;
        boolean mEnabled;

        ForumStatus(Forum forum, boolean enabled) {
            mForum = forum;
            mEnabled = enabled;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb_forum_enabled;

        ViewHolder(View itemView) {
            super(itemView);
            cb_forum_enabled = (CheckBox) itemView.findViewById(R.id.forum_enabled);
        }
    }
}
