package net.jejer.hipda.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2017-06-15.
 */

public class UserInfoActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_left, R.anim.no_anim);

        setContentView(R.layout.activity_user_info);
        rootView = findViewById(R.id.main_activity_root_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle arguments = getIntent().getExtras();
        UserinfoFragment fragment = new UserinfoFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frame_container, fragment).commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

}
