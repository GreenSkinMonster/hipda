package net.jejer.hipda.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.AppBarLayout;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2017-06-14.
 */

public class PostActivity extends SwipeBaseActivity {

    public final static int PERMISSIONS_REQUEST_CODE_BOTH = 201;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post);
        mRootView = findViewById(R.id.main_activity_root_view);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.main_frame_container) == null) {
            Bundle arguments = getIntent().getExtras();
            PostFragment fragment = new PostFragment();
            fragment.setArguments(arguments);
            fragmentManager.beginTransaction()
                    .add(R.id.main_frame_container, fragment).commit();
        }

        setSwipeBackEnable(false);
    }

    @Override
    public void finish() {
        finishWithNoSlide();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_BOTH: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    PostFragment fragment = (PostFragment) getSupportFragmentManager().findFragmentById(R.id.main_frame_container);
                    if (fragment != null) {
                        fragment.showImageSelector();
                    }
                }
                break;
            }
        }
    }

}
