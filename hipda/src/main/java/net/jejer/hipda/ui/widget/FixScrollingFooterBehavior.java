package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * http://stackoverflow.com/a/35405095
 * hack, avoid footer scrolled out of screen
 */
@SuppressWarnings("unused")
public class FixScrollingFooterBehavior extends AppBarLayout.ScrollingViewBehavior {

    private AppBarLayout appBarLayout;

    public FixScrollingFooterBehavior() {
        super();
    }

    public FixScrollingFooterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        if (appBarLayout == null) {
            appBarLayout = (AppBarLayout) dependency;
        }

        final boolean result = super.onDependentViewChanged(parent, child, dependency);
        final int bottomPadding = calculateBottomPadding(appBarLayout);
        final boolean paddingChanged = bottomPadding != child.getPaddingBottom();
        if (paddingChanged) {
            child.setPadding(
                    child.getPaddingLeft(),
                    child.getPaddingTop(),
                    child.getPaddingRight(),
                    bottomPadding);
            child.requestLayout();
        }
        return paddingChanged || result;
    }


    private int calculateBottomPadding(AppBarLayout dependency) {
        if ("0".equals(dependency.getTag()))
            return 0;
        final int totalScrollRange = dependency.getTotalScrollRange();
        return totalScrollRange + dependency.getTop();
    }
}