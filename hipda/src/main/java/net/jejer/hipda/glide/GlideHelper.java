package net.jejer.hipda.glide;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import net.jejer.hipda.cache.LRUCache;
import net.jejer.hipda.okhttp.LoggingInterceptor;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.BaseFragment;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class GlideHelper {

    private static String WEEK_KEY;
    private static LRUCache<String, String> NOT_FOUND_AVATARS = new LRUCache<>(512);

    private static Drawable DEFAULT_USER_ICON;

    public static void init(Context context) {
        if (!Glide.isSetup()) {
            GlideBuilder gb = new GlideBuilder(context);

//            long maxMemory = Runtime.getRuntime().maxMemory();
//            gb.setMemoryCache(new LruResourceCache(Math.round(maxMemory * 0.3f)));
//            gb.setBitmapPool(new LruBitmapPool(Math.round(maxMemory * 0.1f)));
            gb.setDiskCache(DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(context), 200 * 1024 * 1024));

            Glide.setup(gb);

            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS);
            client.setReadTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS);
            client.setWriteTimeout(OkHttpHelper.NETWORK_TIMEOUT_SECS, TimeUnit.SECONDS);

            if (Logger.isDebug())
                client.interceptors().add(new LoggingInterceptor());

            final ProgressListener progressListener = new ProgressListener() {
                @Override
                public void update(String url, long bytesRead, long contentLength, boolean done) {
                    int progress = (int) Math.round((100.0 * bytesRead) / contentLength);
                    EventBus.getDefault().post(new GlideImageEvent(url, progress, Constants.STATUS_IN_PROGRESS));
                }
            };

            client.networkInterceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response originalResponse = chain.proceed(chain.request());
                    String url = chain.request().httpUrl().toString();
                    //avatar don't need a progress listener
                    if (url.startsWith(HiUtils.AvatarBaseUrl)) {
                        return originalResponse;
                    }
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(url, originalResponse.body(), progressListener))
                            .build();
                }
            });

            Glide.get(context).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
        }
    }

    public static boolean ready() {
        return Glide.isSetup();
    }

    public static void loadAvatar(BaseFragment fragment, ImageView view, String avatarUrl) {
        if (fragment == null)
            return;

        if (DEFAULT_USER_ICON == null)
            DEFAULT_USER_ICON = new IconicsDrawable(HiApplication.getAppContext(), GoogleMaterial.Icon.gmd_account_box).color(Color.LTGRAY);

        //use year and week number as cache key
        //avatars will be cache for one week at most
        if (WEEK_KEY == null) {
            Calendar calendar = Calendar.getInstance();
            WEEK_KEY = calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.WEEK_OF_YEAR);
        }
        if (NOT_FOUND_AVATARS.containsKey(avatarUrl)) {
            view.setImageDrawable(DEFAULT_USER_ICON);
        } else {
            Glide.with(fragment)
                    .load(avatarUrl)
                    .signature(new StringSignature(avatarUrl + "_" + WEEK_KEY))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .error(DEFAULT_USER_ICON)
                    .crossFade()
                            //.listener(new AvatarRequestListener())
                    .into(view);
        }
    }

    private static class AvatarRequestListener implements RequestListener<String, GlideDrawable> {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            if (e != null) {
                //Volley with OkHttp
                if (e instanceof IOException
                        && !TextUtils.isEmpty(e.getMessage())
                        && e.getMessage().contains("404"))
                    NOT_FOUND_AVATARS.put(model, "");
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
            //User-Agent is set in modified OkHttpStreamFetcher class
            glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                    .build());
        } else {
            glideUrl = new GlideUrl(url);
        }
        return glideUrl;
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

    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;
        private String url;

        public ProgressResponseBody(String url, ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
            this.url = url;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() throws IOException {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(final Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(url, totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

    interface ProgressListener {
        void update(String url, long bytesRead, long contentLength, boolean done);
    }


}
