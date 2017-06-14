package net.jejer.hipda.ui;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.widget.FABHideOnScrollBehavior;
import net.jejer.hipda.ui.widget.OnSingleClickListener;
import net.jejer.hipda.utils.UIUtils;

/**
 * Created by GreenSkinMonster on 2017-06-14.
 */

public class ThreadDetailActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_left, R.anim.no_anim);

        setContentView(R.layout.activity_thread_detail);
        rootView = findViewById(R.id.main_activity_root_view);
        mMainFrameContainer = findViewById(R.id.main_frame_container);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateAppBarScrollFlag();

        mMainFab = (FloatingActionButton) findViewById(R.id.fab_main);
        mMainFab.setEnabled(false);

        if (UIUtils.isTablet(this)) {
            mMainFab.setSize(FloatingActionButton.SIZE_NORMAL);
        }

        updateFabGravity();

        mToolbar.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //get top displaying fragment
                Fragment fg = getSupportFragmentManager().findFragmentById(R.id.main_frame_container);
                if (fg instanceof BaseFragment) {
                    ((BaseFragment) fg).scrollToTop();
                }
            }
        });

        Bundle arguments = getIntent().getExtras();
        ThreadDetailFragment fragment = new ThreadDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frame_container, fragment).commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    public void updateFabGravity() {
        CoordinatorLayout.LayoutParams mainFabParams = (CoordinatorLayout.LayoutParams) mMainFab.getLayoutParams();
        if (HiSettingsHelper.getInstance().isFabLeftSide()) {
            mainFabParams.anchorGravity = Gravity.BOTTOM | Gravity.LEFT | Gravity.END;
        } else {
            mainFabParams.anchorGravity = Gravity.BOTTOM | Gravity.RIGHT | Gravity.END;
        }
        if (HiSettingsHelper.getInstance().isFabAutoHide()) {
            mainFabParams.setBehavior(new FABHideOnScrollBehavior());
        } else {
            mainFabParams.setBehavior(null);
            mMainFab.setEnabled(true);
            mMainFab.show();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_frame_container);

        if (fragment instanceof BaseFragment) {
            if (!((BaseFragment) fragment).onBackPressed()) {
                finish();
            }
        }
    }

}
