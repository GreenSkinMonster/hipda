package net.jejer.hipda.ui;

import android.app.Fragment;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.mikepenz.materialdrawer.Drawer;

/**
 * a base fragment
 * Created by GreenSkinMonster on 2015-05-09.
 */
public abstract class BaseFragment extends Fragment {

    void setActionBarTitle(CharSequence title) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(title);
        }
    }

    void setActionBarTitle(@StringRes int resId) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(resId);
        }
    }

    void setActionBarDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
        }
    }

    void syncActionBarState() {
        ((MainFrameActivity) getActivity()).drawerResult.getActionBarDrawerToggle().syncState();
    }

    void setDrawerSelection(int identifier) {
        //this only set DrawerItem, not StickyDrawerItem
        try {
            Drawer.Result drawerResult = ((MainFrameActivity) getActivity()).drawerResult;
            if (drawerResult.getPositionFromIdentifier(identifier) != drawerResult.getCurrentSelection())
                drawerResult.setSelectionByIdentifier(identifier, false);
        } catch (Exception ignored) {
        }
    }

}
