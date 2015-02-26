package net.jejer.hipda.async;

import net.jejer.hipda.cache.DoubleImageCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import android.content.Context;


public class VolleyHelper {

	private Context mCtx;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private ImageCache mImageCache;
	private static int DISK_IMAGECACHE_SIZE = 1024*1024*30;
	private ImageCache mAvatarCache;
	private ImageLoader mAvatarLoader;
	private static int DISK_AVATARCACHE_SIZE = 1024*1024*10;

	public void init(Context ctx) {
		mCtx = ctx;
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(mCtx);
		}

		mImageCache = new DoubleImageCache(ctx, "image", 1, 1, DISK_IMAGECACHE_SIZE, 20);
		mImageLoader = new ImageLoader(mRequestQueue, mImageCache);
		mAvatarCache = new DoubleImageCache(ctx, "avatar", 1, 1, DISK_AVATARCACHE_SIZE, 100);
		mAvatarLoader = new ImageLoader(mRequestQueue, mAvatarCache);
	}

	public <T> void add(Request<T> req) {
		mRequestQueue.add(req);
	}

	public ImageLoader getImgLoader() {
		return mImageLoader;
	}

	public ImageLoader getAvatarLoader() {
		return mAvatarLoader;
	}

	public void cancelAll() {
		mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
			@Override
			public boolean apply(Request<?> request) {
				return true;
			}
		});
	}

	private VolleyHelper() {	
	}
	private static class SingletonHolder {
		public static final VolleyHelper INSTANCE = new VolleyHelper();
	}
	public static VolleyHelper getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
