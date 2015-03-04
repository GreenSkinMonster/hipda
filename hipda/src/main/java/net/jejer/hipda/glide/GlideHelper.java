package net.jejer.hipda.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;

public class GlideHelper {

	public static void init(Context context) {
		if (!Glide.isSetup()) {
			GlideBuilder gb = new GlideBuilder(context);
			DiskCache dlw = DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(context), 100 * 1024 * 1024);

			Runtime rt = Runtime.getRuntime();
			long maxMemory = rt.maxMemory();
			gb.setMemoryCache(new LruResourceCache(Math.round(maxMemory * 0.3f)));
			gb.setDiskCache(dlw);

			Glide.setup(gb);
		}
	}

}
