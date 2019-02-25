package net.jejer.hipda.ui;

import android.os.Bundle;
import android.view.View;

import net.jejer.hipda.ui.widget.swipeback.SwipeBackActivityBase;
import net.jejer.hipda.ui.widget.swipeback.SwipeBackActivityHelper;
import net.jejer.hipda.ui.widget.swipeback.SwipeBackLayout;
import net.jejer.hipda.ui.widget.swipeback.SwipeUtils;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Created by GreenSkinMonster on 2017-06-15.
 */

public class SwipeBackActivity extends AppCompatActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        SwipeUtils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

}
