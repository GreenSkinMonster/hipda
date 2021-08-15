package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.jejer.hipda.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by GreenSkinMonster on 2016-11-23.
 */

public class BottomDialog extends BottomSheetDialog {

    public BottomDialog(@NonNull @NotNull Context context) {
        super(context, R.style.BottomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int orientation = getContext().getResources().getConfiguration().orientation;

        Window window = getWindow();
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            window.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = metrics.widthPixels;
            window.setLayout((int) (width * 0.7), ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

}
