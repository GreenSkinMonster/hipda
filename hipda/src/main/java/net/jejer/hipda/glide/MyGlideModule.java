package net.jejer.hipda.glide;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageInfo;
import net.jejer.hipda.okhttp.LoggingInterceptor;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.okhttp.ProgressListener;
import net.jejer.hipda.okhttp.ProgressResponseBody;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by GreenSkinMonster on 2016-11-03.
 */

@GlideModule
public class MyGlideModule extends AppGlideModule {

    private final static int DEFAULT_CACHE_SIZE = 500;
    private final static int MIN_CACHE_SIZE = 300;


    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder gb) {
        String cacheSizeStr = HiSettingsHelper.getInstance().getStringValue(HiSettingsHelper.PERF_CACHE_SIZE_IN_MB, DEFAULT_CACHE_SIZE + "");
        int cacheSize = DEFAULT_CACHE_SIZE;
        if (TextUtils.isDigitsOnly(cacheSizeStr)) {
            cacheSize = Integer.parseInt(cacheSizeStr);
            if (cacheSize < MIN_CACHE_SIZE) {
                cacheSize = DEFAULT_CACHE_SIZE;
            }
        }
        gb.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, cacheSize * 1024 * 1024));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            gb.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565));

        GlideHelper.initDefaultFiles();
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
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

        registry.prepend(AvatarModel.class, InputStream.class, new AvatarLoader.Factory(client));
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

}
