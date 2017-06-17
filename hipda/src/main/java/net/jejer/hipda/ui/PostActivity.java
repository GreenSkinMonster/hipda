package net.jejer.hipda.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.UIUtils;

/**
 * Created by GreenSkinMonster on 2017-06-14.
 */

public class PostActivity extends SwipeBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post);
        mRootView = findViewById(R.id.main_activity_root_view);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        UIUtils.hackStatusBar(this);

        Bundle arguments = getIntent().getExtras();
        PostFragment fragment = new PostFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frame_container, fragment).commit();

        setSwipeBackEnable(false);
    }

    @Override
    public void finish() {
        finishWithNoSlide();
    }
}
