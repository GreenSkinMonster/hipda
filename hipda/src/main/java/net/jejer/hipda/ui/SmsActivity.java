package net.jejer.hipda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2017-06-15.
 */

public class SmsActivity extends SwipeBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sms);
        mRootView = findViewById(R.id.main_activity_root_view);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showFragment();
    }

    private void showFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle arguments = getIntent().getExtras();
        SmsFragment fragment = new SmsFragment();
        fragment.setArguments(arguments);
        fragmentManager.beginTransaction()
                .replace(R.id.main_frame_container, fragment).commit();
    }

}
