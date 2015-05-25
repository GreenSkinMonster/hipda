package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.mikepenz.materialdrawer.Drawer;

import net.jejer.hipda.utils.HiUtils;
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
            Drawer drawerResult = ((MainFrameActivity) getActivity()).drawerResult;
            if (drawerResult != null)
                drawerResult.getActionBarDrawerToggle().syncState();
        }
    }

    void setDrawerSelection(int identifier) {
        //re-select forum on back
        try {
            if (getActivity() != null) {
                Drawer drawerResult = ((MainFrameActivity) getActivity()).drawerResult;

                //seems bug in getFooterPositionFromIdentifier
                //get postion in my way
                int position = -1;
                for (int i = 0; i < HiUtils.FORUM_IDS.length; i++) {
                    if (HiUtils.isForumEnabled(HiUtils.FORUM_IDS[i])
                            && identifier == HiUtils.FORUM_IDS[i]) {
                        position = i;
                        break;
                    }
                }

                if (drawerResult != null && !drawerResult.isDrawerOpen()) {
//                int postion = drawerResult.getFooterPositionFromIdentifier(identifier);
//                if (postion != -1 && postion != drawerResult.getCurrentFooterSelection())
                    if (position != -1
                            && position != drawerResult.getCurrentFooterSelection())
                        drawerResult.setFooterSelection(position, false);
                }
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
