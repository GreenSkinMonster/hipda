package net.jejer.hipda.glide;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import net.jejer.hipda.utils.HiUtils;

import java.io.InputStream;
import java.net.URL;

import androidx.annotation.NonNull;
import okhttp3.OkHttpClient;

public class OkHttpUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private OkHttpClient client;

        public Factory(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public ModelLoader<GlideUrl, InputStream> build(MultiModelLoaderFactory factories) {
            return new OkHttpUrlLoader(client);
        }

        @Override
        public void teardown() {
            // Do nothing, this instance doesn't own the client.
        }
    }

    private final OkHttpClient client;

    private OkHttpUrlLoader(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull GlideUrl model, int width, int height, @NonNull Options options) {
        boolean forumUrl = false;
        try {
            URL url = model.toURL();
            forumUrl = url.getHost().endsWith(HiUtils.CookieDomain);
        } catch (Exception ignored) {
        }
        if (forumUrl && model.toStringUrl().contains(HiUtils.AvatarPath)) {
            return new LoadData<>(new ObjectKey(model), new AvatarStreamFetcher(client, model));
        } else {
//            if(){
//                return new LoadData<>(new ObjectKey(model), new CacheOnlyFetcher());
//            }
            return new LoadData<>(new ObjectKey(model), new ImageStreamFetcher(client, model, forumUrl));
        }
    }

    @Override
    public boolean handles(GlideUrl model) {
        return true;
    }

}
