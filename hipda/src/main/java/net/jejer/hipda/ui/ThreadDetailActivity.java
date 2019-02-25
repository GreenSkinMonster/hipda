package net.jejer.hipda.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.widget.FABHideOnScrollBehavior;
import net.jejer.hipda.ui.widget.OnSingleClickListener;
import net.jejer.hipda.utils.UIUtils;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Created by GreenSkinMonster on 2017-06-14.
 */

public class ThreadDetailActivity extends SwipeBaseActivity {

    private View mQuickReply;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_thread_detail);
        mRootView = findViewById(R.id.main_activity_root_view);
        mMainFrameContainer = findViewById(R.id.main_frame_container);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        mQuickReply = findViewById(R.id.quick_reply);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateAppBarScrollFlag();

        mMainFab = (FloatingActionButton) findViewById(R.id.fab_main);

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

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.main_frame_container) == null) {
            Bundle arguments = getIntent().getExtras();
            ThreadDetailFragment fragment = new ThreadDetailFragment();
            fragment.setArguments(arguments);
            fragmentManager.beginTransaction()
                    .add(R.id.main_frame_container, fragment).commit();
        }
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
            mMainFab.show();
        }
    }

    public View getQuickReplyView() {
        return mQuickReply;
    }

}
