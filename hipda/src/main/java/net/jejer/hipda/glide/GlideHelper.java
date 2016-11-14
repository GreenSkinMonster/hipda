package net.jejer.hipda.glide;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.LRUCache;
import net.jejer.hipda.ui.BaseFragment;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.HiUtils;

import java.io.File;

import static net.jejer.hipda.glide.MyGlideModule.AVATAR_CACHE_DIR;
import static net.jejer.hipda.glide.MyGlideModule.DEFAULT_AVATAR_FILE;
import static net.jejer.hipda.glide.MyGlideModule.DEFAULT_USER_ICON;

public class GlideHelper {

    private static LRUCache<String, String> NOT_FOUND_AVATARS = new LRUCache<>(1024);

    public final static long AVATAR_CACHE_MILLS = 7 * 24 * 60 * 60 * 1000;
    public final static long AVATAR_404_CACHE_MILLS = 24 * 60 * 60 * 1000;

    public static void loadAvatar(BaseFragment fragment, ImageView view, String avatarUrl) {
        if (isOkToLoad(fragment)) {
            loadAvatar(Glide.with(fragment), view, avatarUrl);
        }
    }

    public static void loadAvatar(RequestManager glide, ImageView view, String avatarUrl) {
        if (NOT_FOUND_AVATARS.containsKey(avatarUrl)) {
            avatarUrl = DEFAULT_AVATAR_FILE.getAbsolutePath();
        }
        if (HiSettingsHelper.getInstance().getBooleanValue(HiSettingsHelper.PERF_CIRCLE_AVATAR, true)) {
            glide.load(avatarUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(DEFAULT_USER_ICON)
                    .crossFade()
                    .bitmapTransform(new CropCircleTransformation(HiApplication.getAppContext()))
                    .into(view);
        } else {
            glide.load(avatarUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .error(DEFAULT_USER_ICON)
                    .crossFade()
                    .into(view);
        }
    }

    public static File getAvatarFile(Context ctx, String avatarUrl) {
        File f = null;
        try {
            FutureTarget<File> future = Glide.with(ctx)
                    .load(avatarUrl)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            f = future.get();
            Glide.clear(future);
        } catch (Exception ignored) {
        }
        return f;
    }

    public static void markAvatarNotFound(String avatarUrl) {
        NOT_FOUND_AVATARS.put(avatarUrl, "");
    }

    public static File getAvatarFile(String url) {
        if (url.contains(HiUtils.AvatarPath)) {
            return new File(AVATAR_CACHE_DIR, url.substring(url.indexOf(HiUtils.AvatarPath) + HiUtils.AvatarPath.length()).replace("/", "_"));
        }
        return null;
    }

    public static boolean isOkToLoad(Context activity) {
        if (activity != null
                && Build.VERSION.SDK_INT >= 17
                && activity instanceof Activity) {
            if (((Activity) activity).isDestroyed())
                return false;
        }
        return true;
    }

    public static boolean isOkToLoad(Fragment fragment) {
        return fragment != null && fragment.getActivity() != null && !fragment.isDetached();
    }

}
