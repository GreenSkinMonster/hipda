package net.jejer.hipda.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.mikepenz.materialdrawer.Drawer;

import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

/**
 * a base fragment
 * Created by GreenSkinMonster on 2015-05-09.
 */
public abstract class BaseFragment extends Fragment {

    void setActionBarTitle(CharSequence title) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            String t = Utils.nullToText(title);
            if (actionBar != null && !t.equals(actionBar.getTitle())) {
                actionBar.setTitle(t);
            }
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
        if (getActivity() != null) {
            Drawer drawerResult = ((MainFrameActivity) getActivity()).drawerResult;
            if (drawerResult != null && !drawerResult.isDrawerOpen()) {
                int position = drawerResult.getFooterPositionFromIdentifier(identifier);
                if (position != -1 && position != drawerResult.getCurrentFooterSelection())
                    drawerResult.setFooterSelection(position, false);
            }
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

    public void scrollToTop() {
    }

    public void stopScroll() {
    }

    @Override
    public void onDestroyView() {
        stopScroll();
        super.onDestroyView();
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        //http://daniel-codes.blogspot.sg/2013/09/smoothing-performance-on-fragment.html
        Animator animator = super.onCreateAnimator(transit, enter, nextAnim);
        if (animator == null && nextAnim != 0) {
            animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
            if (animator != null) {
                if (getView() != null)
                    getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        Glide.with(getActivity()).pauseRequests();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (Glide.with(getActivity()).isPaused())
                            Glide.with(getActivity()).resumeRequests();
                        if (getView() != null)
                            getView().setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if (Glide.with(getActivity()).isPaused())
                            Glide.with(getActivity()).resumeRequests();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
        }
        return animator;
    }
}
