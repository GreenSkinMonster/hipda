package net.jejer.hipda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;

import net.jejer.hipda.R;
import net.jejer.hipda.job.SimpleListJob;
import net.jejer.hipda.ui.widget.OnSingleClickListener;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Created by GreenSkinMonster on 2017-06-16.
 */

public class SimpleListActivity extends SwipeBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simple_list);
        mRootView = findViewById(R.id.main_activity_root_view);
        mMainFrameContainer = findViewById(R.id.main_frame_container);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Fragment fg = getSupportFragmentManager().findFragmentById(R.id.main_frame_container);
                if (fg instanceof BaseFragment) {
                    ((BaseFragment) fg).scrollToTop();
                }
            }
        });

        showFragment();
    }

    @Override
    public void finish() {
        Fragment fg = getSupportFragmentManager().findFragmentById(R.id.main_frame_container);
        if (fg instanceof SearchFragment) {
            finishWithNoSlide();
        } else {
            super.finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            showFragment();
        }
    }

    private void showFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle arguments = getIntent().getExtras();

        BaseFragment fragment;
        if (arguments.getInt(SimpleListFragment.ARG_TYPE) == SimpleListJob.TYPE_SEARCH) {
            fragment = new SearchFragment();
        } else {
            updateAppBarScrollFlag();
            fragment = new SimpleListFragment();
        }

        fragment.setArguments(arguments);
        fragmentManager.beginTransaction()
                .replace(R.id.main_frame_container, fragment).commit();
    }
}
