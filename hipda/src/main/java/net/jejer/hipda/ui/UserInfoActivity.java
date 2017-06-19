package net.jejer.hipda.ui;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.UIUtils;

/**
 * Created by GreenSkinMonster on 2017-06-15.
 */

public class UserInfoActivity extends SwipeBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_info);
        mRootView = findViewById(R.id.main_activity_root_view);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        UIUtils.hackStatusBar(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.main_frame_container) == null) {
            Bundle arguments = getIntent().getExtras();
            UserinfoFragment fragment = new UserinfoFragment();
            fragment.setArguments(arguments);
            fragmentManager.beginTransaction()
                    .add(R.id.main_frame_container, fragment).commit();
        }
    }

}
