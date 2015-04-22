package net.jejer.hipda.ui;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * a simple adapter
 * Created by GreenSkinMonster on 2015-04-22.
 */
public abstract class HiAdapter<T> extends BaseAdapter {

    private List<T> mBeans = new ArrayList<>();

    public void setBeans(List<T> beans) {
        mBeans = beans;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mBeans.size();
    }

    @Override
    public T getItem(int position) {
        return mBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
