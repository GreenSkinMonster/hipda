package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.okhttp.LoggingInterceptor;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.okhttp.ProgressListener;
import net.jejer.hipda.okhttp.ProgressResponseBody;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by GreenSkinMonster on 2016-11-03.
 */

public class MyGlideModule implements GlideModule {

    static File AVATAR_CACHE_DIR;
    static File DEFAULT_AVATAR_FILE;
    static Drawable DEFAULT_USER_ICON;

    private final static int DEFAULT_CACHE_SIZE = 500;
    private final static int MIN_CACHE_SIZE = 300;
    public final static String AVATAR_CACHE_DIR_NAME = "avatar";

    @Override
    public void applyOptions(Context context, GlideBuilder gb) {
        String cacheSizeStr = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_CACHE_SIZE_IN_MB, DEFAULT_CACHE_SIZE + "");
        int cacheSize = DEFAULT_CACHE_SIZE;
        if (TextUtils.isDigitsOnly(cacheSizeStr)) {
            cacheSize = Integer.parseInt(cacheSizeStr);
            if (cacheSize < MIN_CACHE_SIZE) {
                cacheSize = DEFAULT_CACHE_SIZE;
            }
        }
        gb.setDiskCache(new ExternalCacheDiskCacheFactory(context, cacheSize * 1024 * 1024));

        AVATAR_CACHE_DIR = Glide.getPhotoCacheDir(context, AVATAR_CACHE_DIR_NAME);

        DEFAULT_USER_ICON = new IconicsDrawable(HiApplication.getAppContext(), GoogleMaterial.Icon.gmd_account_box).color(Color.LTGRAY).sizeDp(64);
        DEFAULT_AVATAR_FILE = new File(AVATAR_CACHE_DIR, "default.png");
        if (!DEFAULT_AVATAR_FILE.exists()) {
            try {
                Bitmap b = drawableToBitmap(DEFAULT_USER_ICON);
                b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(DEFAULT_AVATAR_FILE));
                b.recycle();
            } catch (Exception e) {
                Logger.e(e);
            }
        }
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS)
                .readTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS)
                .writeTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS);

        if (HiSettingsHelper.getInstance().isTrustAllCerts()) {
            OkHttpHelper.setupTrustAllCerts(builder);
        }

        if (Logger.isDebug())
            builder.addInterceptor(new LoggingInterceptor());

        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(String url, long bytesRead, long contentLength, boolean done) {
                if (done) {
                    EventBus.getDefault().post(new GlideImageEvent(url, 100, ImageInfo.IN_PROGRESS));
                } else {
                    int progress = (int) Math.round((100.0 * bytesRead) / contentLength);
                    EventBus.getDefault().post(new GlideImageEvent(url, progress, ImageInfo.IN_PROGRESS));
                }
            }
        };

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                String url = chain.request().url().toString();
                //avatar don't need a progress listener
                if (url.startsWith(HiUtils.AvatarBaseUrl)) {
                    return originalResponse;
                }
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(url, originalResponse.body(), progressListener))
                        .build();
            }
        });

        OkHttpClient client = builder.build();

        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
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
