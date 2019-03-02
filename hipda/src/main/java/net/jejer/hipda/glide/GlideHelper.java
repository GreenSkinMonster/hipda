package net.jejer.hipda.glide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.BaseFragment;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class GlideHelper {

    private static File AVATAR_CACHE_DIR;

    private static Drawable DEFAULT_USER_ICON;
    public static File SYSTEM_AVATAR_FILE;
    public static File DEFAULT_AVATAR_FILE;

    final static long AVATAR_CACHE_MILLS = 3 * 24 * 60 * 60 * 1000;
    final static long AVATAR_404_CACHE_MILLS = 24 * 60 * 60 * 1000;

    private static Map<String, String> AVATAR_CACHE_KEYS = new HashMap<>();

    public static void loadAvatar(BaseFragment fragment, ImageView view, String avatarUrl) {
        if (isOkToLoad(fragment)) {
            loadAvatar(Glide.with(fragment), view, avatarUrl);
        }
    }

    public static void loadAvatar(RequestManager glide, ImageView view, @NonNull String avatarUrl) {
        String cacheKey = AVATAR_CACHE_KEYS.get(avatarUrl);
        if (cacheKey == null) {
            cacheKey = avatarUrl;
        }
        if (HiSettingsHelper.getInstance().isCircleAvatar()) {
            glide.load(new AvatarModel(avatarUrl))
                    .signature(new ObjectKey(cacheKey))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .error(DEFAULT_USER_ICON)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(view);
        } else {
            glide.load(new AvatarModel(avatarUrl))
                    .signature(new ObjectKey(cacheKey))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new CenterCrop(), new RoundedCorners(Utils.dpToPx(HiApplication.getAppContext(), 4)))
                    .error(DEFAULT_USER_ICON)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(view);
        }
    }

    public static File getAvatarFile(Context ctx, String avatarUrl) {
        File f = null;
        try {
            f = Glide.with(ctx)
                    .download(avatarUrl)
                    .submit()
                    .get();
        } catch (Exception e) {
            Logger.e(e);
        }
        return f;
    }

    public static File getAvatarFile(String url) {
        if (url.contains(HiUtils.AvatarPath)) {
            return new File(AVATAR_CACHE_DIR, url.substring(url.indexOf(HiUtils.AvatarPath) + HiUtils.AvatarPath.length()).replace("/", "_"));
        }
        return null;
    }

    public static void clearAvatarCache(String url) {
        File f = getAvatarFile(url);
        if (f != null && f.exists()) {
            f.delete();
        }
        AVATAR_CACHE_KEYS.put(url, System.currentTimeMillis() + "");
    }

    public static void clearAvatarFiles() throws Exception {
        AVATAR_CACHE_DIR.delete();
    }

    public static boolean isOkToLoad(Context activity) {
        if (activity instanceof Activity) {
            return !Utils.isDestroyed((Activity) activity);
        }
        return true;
    }

    public static boolean isOkToLoad(Fragment fragment) {
        return fragment != null && fragment.getActivity() != null && !fragment.isDetached();
    }

    public static void initDefaultFiles() {
        AVATAR_CACHE_DIR = Glide.getPhotoCacheDir(HiApplication.getAppContext(), "avatar");

        HashMap<String, Drawable> avatars = new HashMap<>();
        avatars.put("circle", new IconicsDrawable(HiApplication.getAppContext(), GoogleMaterial.Icon.gmd_account_circle).color(Color.LTGRAY).sizeDp(64));
        avatars.put("default", new IconicsDrawable(HiApplication.getAppContext(), GoogleMaterial.Icon.gmd_account_box).color(Color.LTGRAY).sizeDp(64));
        avatars.put("system", new IconicsDrawable(HiApplication.getAppContext(), GoogleMaterial.Icon.gmd_info_outline).color(Color.LTGRAY).sizeDp(64));

        for (String key : avatars.keySet()) {
            Drawable drawable = avatars.get(key);
            File file = new File(AVATAR_CACHE_DIR, key + ".png");
            if (!file.exists()) {
                try {
                    Bitmap b = drawableToBitmap(drawable);
                    b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                    b.recycle();
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        }

        if (HiSettingsHelper.getInstance().isCircleAvatar()) {
            DEFAULT_USER_ICON = avatars.get("circle");
            DEFAULT_AVATAR_FILE = new File(AVATAR_CACHE_DIR, "circle.png");
        } else {
            DEFAULT_USER_ICON = avatars.get("default");
            DEFAULT_AVATAR_FILE = new File(AVATAR_CACHE_DIR, "default.png");
        }
        SYSTEM_AVATAR_FILE = new File(AVATAR_CACHE_DIR, "system.png");
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
