package net.jejer.hipda.ui.setting;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.R;
import net.jejer.hipda.async.BlacklistHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.BaseFragment;
import net.jejer.hipda.ui.widget.ContentLoadingView;
import net.jejer.hipda.ui.widget.OnSingleClickListener;
import net.jejer.hipda.ui.widget.SimpleDivider;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.HiParser;
import net.jejer.hipda.utils.UIUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by GreenSkinMonster on 2017-07-15.
 */

public class BlacklistFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG_KEY = "BLACKLIST_KEY";

    private List<String> mBlacklists = new ArrayList<>();

    private String mFormhash;
    private LayoutInflater mInflater;
    private Drawable mDrawable;

    private View.OnClickListener mOnClickListener;
    private SwipeRefreshLayout mSwipeLayout;
    private ContentLoadingView mLoadingView;

    private RvAdapter mAdapter = new RvAdapter();
    private List<String> mRemoving = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);
        mInflater = inflater;

        mDrawable = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_close)
                .color(Color.GRAY)
                .sizeDp(12);

        mOnClickListener = new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                removeFromBlacklist((String) v.getTag());
                v.setVisibility(View.INVISIBLE);
            }
        };

        mSwipeLayout = view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(ColorHelper.getSwipeColor(getActivity()));
        mSwipeLayout.setProgressBackgroundColorSchemeColor(ColorHelper.getSwipeBackgroundColor(getActivity()));

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new SimpleDivider(getActivity()));

        mLoadingView = view.findViewById(R.id.content_loading);

        recyclerView.setAdapter(mAdapter);

        refresh();

        setActionBarTitle("黑名单");
        return view;
    }

    protected void refresh() {
        if (!mSwipeLayout.isRefreshing())
            mSwipeLayout.setRefreshing(true);

        mRemoving.clear();
        BlacklistHelper.getBlacklists(new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
                UIUtils.toast("获取黑名单发生错误 : " + OkHttpHelper.getErrorMessage(e).getMessage());
                mSwipeLayout.setRefreshing(false);
                mLoadingView.setState(mBlacklists.size() > 0 ? ContentLoadingView.CONTENT : ContentLoadingView.NO_DATA);
            }

            @Override
            public void onResponse(String response) {
                mSwipeLayout.setRefreshing(false);
                try {
                    Document doc = Jsoup.parse(response);
                    mFormhash = HiParser.parseFormhash(doc);
                    String errorMsg = HiParser.parseErrorMessage(doc);
                    if (TextUtils.isEmpty(errorMsg)) {
                        mBlacklists = HiParser.parseBlacklist(doc);
                        mAdapter.notifyDataSetChanged();
                        HiSettingsHelper.getInstance().setBlacklists(mBlacklists);
                        HiSettingsHelper.getInstance().setBlacklistSyncTime();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.toast("黑名单数据已同步");
                            }
                        }, 200);
                    } else {
                        UIUtils.toast(errorMsg);
                    }
                } catch (Exception e) {
                    UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                }
                mLoadingView.setState(mBlacklists.size() > 0 ? ContentLoadingView.CONTENT : ContentLoadingView.NO_DATA);
            }
        });
    }

    private void removeFromBlacklist(final String username) {
        mRemoving.add(username);
        BlacklistHelper.delBlacklist(mFormhash, username, new OkHttpHelper.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {
                UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
            }

            @Override
            public void onResponse(String response) {
                try {
                    Document doc = Jsoup.parse(response);
                    String errorMsg = HiParser.parseErrorMessage(doc);
                    if (!TextUtils.isEmpty(errorMsg)) {
                        UIUtils.toast(errorMsg);
                    } else {
                        int pos = -1;
                        for (int i = 0; i < mBlacklists.size(); i++) {
                            String u = mBlacklists.get(i);
                            if (username.equals(u)) {
                                pos = i;
                                break;
                            }
                        }
                        if (pos != -1) {
                            mBlacklists.remove(pos);
                            mAdapter.notifyItemRemoved(pos);
                            if (mAdapter.getItemCount() - pos - 1 > 0)
                                mAdapter.notifyItemRangeChanged(pos, mAdapter.getItemCount() - pos - 1);

                            mLoadingView.setState(mBlacklists.size() > 0 ? ContentLoadingView.CONTENT : ContentLoadingView.NO_DATA);
                        } else {
                            refresh();
                        }
                        HiSettingsHelper.getInstance().removeFromBlacklist(username);
                    }
                } catch (Exception e) {
                    UIUtils.toast(OkHttpHelper.getErrorMessage(e).getMessage());
                }

            }
        });
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private class RvAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.item_blacklist, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            String username = mBlacklists.get(position);

            viewHolder.tv_username.setText(username);
            viewHolder.ib_remove.setImageDrawable(mDrawable);
            viewHolder.ib_remove.setTag(username);
            viewHolder.ib_remove.setOnClickListener(mOnClickListener);
            viewHolder.ib_remove.setVisibility(mRemoving.contains(username) ? View.INVISIBLE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return mBlacklists != null ? mBlacklists.size() : 0;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_username;
        ImageButton ib_remove;

        ViewHolder(View itemView) {
            super(itemView);
            tv_username = itemView.findViewById(R.id.tv_username);
            ib_remove = itemView.findViewById(R.id.ib_remove);
        }
    }

}
