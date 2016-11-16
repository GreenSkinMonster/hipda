package net.jejer.hipda.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GreenSkinMonster on 2016-11-08.
 */

public abstract class BaseRvAdapter<V> extends RecyclerView.Adapter {

    private static final int TYPE_HEADER = -1;
    private static final int TYPE_FOOTER = -2;

    private View mHeaderView;
    private View mFooterView;
    protected RecyclerItemClickListener mListener;

    private List<V> mDatas = new ArrayList<>();

    public abstract RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType);

    public abstract void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, int position);

    @Override
    public int getItemCount() {
        return mDatas.size() + (hasHeader() ? 1 : 0) + (hasFooter() ? 1 : 0);
    }

    public void setDatas(List<V> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    public List<V> getDatas() {
        return mDatas;
    }

    public V getItem(int position) {
        int pos = hasHeader() ? position - 1 : position;
        if (pos < 0 || pos >= mDatas.size())
            return null;
        return mDatas.get(pos);
    }

    public int getHeaderCount() {
        return hasHeader() ? 1 : 0;
    }

    private boolean hasHeader() {
        return mHeaderView != null;
    }

    private boolean hasFooter() {
        return mFooterView != null;
    }

    public void setHeaderView(View view) {
        if (view == null)
            removeHeaderView();
        if (mHeaderView != null) {
            mHeaderView = view;
            notifyItemChanged(0);
        } else {
            mHeaderView = view;
            notifyItemInserted(0);
        }
    }

    public void removeHeaderView() {
        if (mHeaderView != null) {
            mHeaderView = null;
            notifyItemRemoved(0);
        }
    }

    public void setFooterView(View view) {
        if (view == null)
            removeFooterView();
        if (mFooterView != null) {
            mFooterView = view;
            notifyItemChanged(getItemCount() - 1);
        } else {
            mFooterView = view;
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public void removeFooterView() {
        if (mFooterView != null) {
            mFooterView = null;
            notifyItemRemoved(getItemCount() - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (hasHeader() && position == 0) {
            return TYPE_HEADER;
        }
        if (hasFooter() && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return super.getItemViewType(hasHeader() ? position - 1 : position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == TYPE_HEADER) {
            itemView = mHeaderView;
        } else if (viewType == TYPE_FOOTER) {
            itemView = mFooterView;
        }
        if (itemView != null)
            return new RecyclerView.ViewHolder(itemView) {
            };
        return onCreateViewHolderImpl(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER || getItemViewType(position) == TYPE_FOOTER) {
            return;
        }
        onBindViewHolderImpl(holder, position);
        holder.itemView.setTag(position);
        holder.itemView.setOnTouchListener(mListener);
    }

}
