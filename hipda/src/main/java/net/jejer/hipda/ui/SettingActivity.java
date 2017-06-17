package net.jejer.hipda.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import net.jejer.hipda.R;
import net.jejer.hipda.ui.setting.SettingMainFragment;
import net.jejer.hipda.ui.setting.SettingNestedFragment;
import net.jejer.hipda.utils.UIUtils;

/**
 * Created by GreenSkinMonster on 2017-06-16.
 */

public class SettingActivity extends SwipeBaseActivity {

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
        if (arguments == null) {
            SettingMainFragment fragment = new SettingMainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_frame_container, fragment).commit();
        } else if (arguments.containsKey(AboutFragment.TAG_KEY)) {
            AboutFragment fragment = new AboutFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_frame_container, fragment).commit();
        } else {
            SettingNestedFragment fragment = new SettingNestedFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_frame_container, fragment).commit();
        }
    }

}
