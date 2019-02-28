package net.jejer.hipda.glide;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

import androidx.annotation.NonNull;

public class CacheLoader implements ModelLoader<CacheModel, InputStream> {

    public static class Factory implements ModelLoaderFactory<CacheModel, InputStream> {

        Factory() {
        }

        @NonNull
        @Override
        public ModelLoader<CacheModel, InputStream> build(MultiModelLoaderFactory factories) {
            return new CacheLoader();
        }

        @Override
        public void teardown() {
        }
    }

    private CacheLoader() {
    }

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull CacheModel model, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new CacheFetcher());
    }

    @Override
    public boolean handles(@NonNull CacheModel model) {
        return true;
    }

}
