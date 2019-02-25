package net.jejer.hipda.glide;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * a simple ViewTarget to follow image's display dimension
 * Created by GreenSkinMonster on 2015-05-08.
 */
public class GlideBitmapTarget extends BitmapImageViewTarget {

    private int mDisplayWidth;
    private int mDisplayHeight;

    public GlideBitmapTarget(ImageView view, int displayWidth, int displayHeight) {
        super(view);
        mDisplayWidth = displayWidth;
        mDisplayHeight = displayHeight;
    }

    @Override
    public void getSize(SizeReadyCallback cb) {
        super.getSize(cb);
        cb.onSizeReady(mDisplayWidth, mDisplayHeight);
    }

    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        super.onResourceReady(resource, transition);
    }

}
