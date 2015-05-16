package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.mikepenz.materialdrawer.Drawer;

import net.jejer.hipda.utils.Logger;

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
//        if (getActivity() != null) {
//            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//            if (actionBar != null)
//                actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
//        }
    }

    void syncActionBarState() {
        if (getActivity() != null) {
            Drawer.Result drawerResult = ((MainFrameActivity) getActivity()).drawerResult;
            if (drawerResult != null)
                drawerResult.getActionBarDrawerToggle().syncState();
        }
    }

    void setDrawerSelection(int identifier) {
        //this only set DrawerItem, not StickyDrawerItem
        try {
            if (getActivity() != null) {
                Drawer.Result drawerResult = ((MainFrameActivity) getActivity()).drawerResult;
                if (drawerResult != null
                        && !drawerResult.isDrawerOpen()
                        && drawerResult.getPositionFromIdentifier(identifier) != drawerResult.getCurrentSelection())
                    drawerResult.setSelectionByIdentifier(identifier, false);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Logger.v("onAttach : " + getClass().getName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.v("onDetach : " + getClass().getName());
    }
}
