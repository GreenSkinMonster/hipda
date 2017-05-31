package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;

/**
 * Created by GreenSkinMonster on 2016-11-14.
 */

public class SimpleDivider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    public SimpleDivider(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String theme = HiSettingsHelper.getInstance().getActiveTheme();
            if ("dark".equals(theme)) {
                mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider_dark);
            } else {
                mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider_light);
            }
        } else {
            mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
