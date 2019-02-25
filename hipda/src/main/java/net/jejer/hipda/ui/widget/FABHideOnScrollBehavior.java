package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

/**
 * Created by GreenSkinMonster on 2016-11-14.
 */
@SuppressWarnings("unused")
public class FABHideOnScrollBehavior extends FloatingActionButton.Behavior {

    public FABHideOnScrollBehavior() {
        super();
    }

    public FABHideOnScrollBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        boolean isNearBottom = target != null && (target instanceof XRecyclerView) && ((XRecyclerView) target).isNearBottom();
        if (isNearBottom && child.getVisibility() == View.INVISIBLE) {
            child.show();
        } else {
            if (!isNearBottom && dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
                hideFab(child);
            } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
                child.show();
            }
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout,
                child, directTargetChild, target, nestedScrollAxes);
    }

    // http://stackoverflow.com/a/39875070
    @Override
    public boolean getInsetDodgeRect(@NonNull CoordinatorLayout parent, @NonNull FloatingActionButton child, @NonNull Rect rect) {
        super.getInsetDodgeRect(parent, child, rect);
        return false;
    }

    public static void hideFab(FloatingActionButton child) {
        child.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                fab.setVisibility(View.INVISIBLE);
            }
        });
    }
}