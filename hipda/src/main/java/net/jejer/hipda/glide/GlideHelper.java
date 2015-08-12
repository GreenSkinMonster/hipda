package net.jejer.hipda.glide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.integration.volley.VolleyUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.okhttp.OkHttpClient;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.LRUCache;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;
import net.jejer.hipda.volley.LoggingInterceptor;
import net.jejer.hipda.volley.VolleyHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class GlideHelper {

    private static String WEEK_KEY;
    private static LRUCache<String, String> NOT_FOUND_AVATARS = new LRUCache<>(512);

    private static Drawable DEFAULT_USER_ICON;

    public static void init(Context context) {
        if (!Glide.isSetup()) {
            GlideBuilder gb = new GlideBuilder(context);

            long maxMemory = Runtime.getRuntime().maxMemory();
            gb.setMemoryCache(new LruResourceCache(Math.round(maxMemory * 0.3f)));
            gb.setDiskCache(DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(context), 100 * 1024 * 1024));

            Glide.setup(gb);

            if (HiSettingsHelper.getInstance().isNewNetLib()) {
                OkHttpClient client = new OkHttpClient();
                if (Logger.isDebug())
                    client.interceptors().add(new LoggingInterceptor());
                Glide.get(context).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
            } else {
                Glide.get(context).register(GlideUrl.class, InputStream.class, new VolleyUrlLoader.Factory(context));
            }

        }
    }

    public static void loadAvatar(Context ctx, ImageView view, String avatarUrl) {
        if (ctx == null)
            return;
        if (Build.VERSION.SDK_INT >= 17
                && (ctx instanceof Activity)
                && ((Activity) ctx).isDestroyed())
            return;

        if (DEFAULT_USER_ICON == null)
            DEFAULT_USER_ICON = new IconicsDrawable(ctx, GoogleMaterial.Icon.gmd_account_box).color(Color.LTGRAY);

        //use year and week number as cache key
        //avatars will be cache for one week at most
        if (WEEK_KEY == null) {
            Calendar calendar = Calendar.getInstance();
            WEEK_KEY = calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.WEEK_OF_YEAR);
        }
        if (NOT_FOUND_AVATARS.containsKey(avatarUrl)) {
            view.setImageDrawable(DEFAULT_USER_ICON);
        } else {
            Glide.with(ctx)
                    .load(avatarUrl)
                    .signature(new StringSignature(avatarUrl + "_" + WEEK_KEY))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .error(DEFAULT_USER_ICON)
                    .crossFade()
                    .listener(new AvatarRequestListener())
                    .into(view);
        }
    }

    private static class AvatarRequestListener implements RequestListener<String, GlideDrawable> {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            if (e != null) {
                if (HiSettingsHelper.getInstance().isNewNetLib()) {
                    //Volley with OkHttp
                    if (e instanceof IOException
                            && !TextUtils.isEmpty(e.getMessage())
                            && e.getMessage().contains("404"))
                        NOT_FOUND_AVATARS.put(model, "");
                } else {
                    //stock Volley
                    Throwable t = e.getCause();
                    if (t instanceof VolleyError) {
                        VolleyError volleyError = (VolleyError) t;
                        if (volleyError.networkResponse != null
                                && volleyError.networkResponse.statusCode == 404)
                            NOT_FOUND_AVATARS.put(model, "");
                    }
                }
            }
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    }

    public static GlideUrl getGlideUrl(String url) {
        GlideUrl glideUrl;
        if (Utils.nullToText(url).startsWith(HiUtils.BaseUrl)) {
            glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                    .setHeader("User-Agent", HiUtils.UserAgent)
                    .addHeader("Cookie", "cdb_auth=" + VolleyHelper.getInstance().getAuthCookie())
                    .build());
        } else {
            glideUrl = new GlideUrl(url);
        }
        return glideUrl;
    }

}
