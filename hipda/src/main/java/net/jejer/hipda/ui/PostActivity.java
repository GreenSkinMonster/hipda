package net.jejer.hipda.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2017-06-14.
 */

public class PostActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post);
        rootView = findViewById(R.id.main_activity_root_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle arguments = getIntent().getExtras();
        PostFragment fragment = new PostFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frame_container, fragment).commit();
    }

}
