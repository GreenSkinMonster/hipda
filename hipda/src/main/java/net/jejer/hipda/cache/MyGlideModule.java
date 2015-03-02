package net.jejer.hipda.cache;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

public class MyGlideModule implements GlideModule {

	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
		builder.setDiskCache(DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(context), 50 * 1024 * 1024));
		builder.setMemoryCache(new LruResourceCache(10 * 1024 * 1024));
	}

	@Override
	public void registerComponents(Context context, Glide glide) {
		// register ModelLoaders here.
	}

}
