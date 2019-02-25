package com.vanniktech.emoji;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

final class EmojiPagerAdapter extends PagerAdapter {
    private final List<FrameLayout> views;

    EmojiPagerAdapter(final List<FrameLayout> views) {
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public Object instantiateItem(final ViewGroup pager, final int position) {
        final View view = views.get(position);
        pager.addView(view);
        return view;
    }

    @Override
    public void destroyItem(final ViewGroup pager, final int position, final Object view) {
        pager.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view.equals(object);
    }
}
